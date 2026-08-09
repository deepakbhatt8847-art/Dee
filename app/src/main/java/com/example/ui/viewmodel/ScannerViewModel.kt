package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.StreetEntity
import com.example.data.StreetRepository
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class ScanResultState(
    val detectedStreet: String = "Point camera at address",
    val detectedRound: String = "--",
    val rawScannedText: String = "",
    val isMatchFound: Boolean = false,
    val isScanning: Boolean = false,
    val lastScanTime: Long = 0L
)

class ScannerViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: StreetRepository
    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    init {
        val database = AppDatabase.getDatabase(application)
        repository = StreetRepository(database.streetDao())
    }

    private val _scanResult = MutableStateFlow(ScanResultState())
    val scanResult: StateFlow<ScanResultState> = _scanResult.asStateFlow()

    private val _documentScanText = MutableStateFlow<String?>(null)
    val documentScanText: StateFlow<String?> = _documentScanText.asStateFlow()

    fun clearDocumentScanText() {
        _documentScanText.value = null
    }

    /**
     * Process an InputImage from CameraX analyzer or photo picker
     */
    fun processImage(inputImage: InputImage, isLiveStream: Boolean = true) {
        if (_scanResult.value.isScanning && isLiveStream) return

        _scanResult.value = _scanResult.value.copy(isScanning = true)

        recognizer.process(inputImage)
            .addOnSuccessListener { visionText ->
                val fullText = visionText.text
                viewModelScope.launch(Dispatchers.IO) {
                    val match = repository.matchStreetInText(fullText)
                    withContext(Dispatchers.Main) {
                        if (match != null) {
                            _scanResult.value = ScanResultState(
                                detectedStreet = match.streetName,
                                detectedRound = match.roundNumber,
                                rawScannedText = fullText,
                                isMatchFound = true,
                                isScanning = false,
                                lastScanTime = System.currentTimeMillis()
                            )
                        } else {
                            _scanResult.value = ScanResultState(
                                detectedStreet = if (fullText.isNotBlank()) "No street match found" else "Point camera at address",
                                detectedRound = "--",
                                rawScannedText = fullText,
                                isMatchFound = false,
                                isScanning = false,
                                lastScanTime = System.currentTimeMillis()
                            )
                        }
                    }
                }
            }
            .addOnFailureListener { e ->
                _scanResult.value = _scanResult.value.copy(
                    detectedStreet = "Error scanning text",
                    detectedRound = "--",
                    isScanning = false
                )
            }
    }

    /**
     * Process a photo selected from camera/gallery for mail scanning
     */
    fun scanImageUri(context: Context, uri: Uri, onResult: (Boolean, String, String) -> Unit) {
        try {
            _scanResult.value = ScanResultState(
                detectedStreet = "Scanning photo...",
                detectedRound = "--",
                isScanning = true
            )
            val image = InputImage.fromFilePath(context, uri)
            recognizer.process(image)
                .addOnSuccessListener { visionText ->
                    val fullText = visionText.text
                    viewModelScope.launch(Dispatchers.IO) {
                        val match = repository.matchStreetInText(fullText)
                        withContext(Dispatchers.Main) {
                            if (match != null) {
                                _scanResult.value = ScanResultState(
                                    detectedStreet = match.streetName,
                                    detectedRound = match.roundNumber,
                                    rawScannedText = fullText,
                                    isMatchFound = true,
                                    isScanning = false
                                )
                                onResult(true, match.streetName, match.roundNumber)
                            } else {
                                _scanResult.value = ScanResultState(
                                    detectedStreet = "No matching street found",
                                    detectedRound = "--",
                                    rawScannedText = fullText,
                                    isMatchFound = false,
                                    isScanning = false
                                )
                                onResult(false, "No match", "--")
                            }
                        }
                    }
                }
                .addOnFailureListener {
                    _scanResult.value = ScanResultState(
                        detectedStreet = "Failed to process photo",
                        detectedRound = "--",
                        isScanning = false
                    )
                    onResult(false, "Error", "--")
                }
        } catch (e: Exception) {
            _scanResult.value = ScanResultState(
                detectedStreet = "Error loading image",
                detectedRound = "--",
                isScanning = false
            )
            onResult(false, "Error", "--")
        }
    }

    /**
     * OCR Scan for paper document sheets (extract text to display in dialog for manual/bulk import)
     */
    fun scanDocumentSheet(context: Context, uri: Uri) {
        try {
            val image = InputImage.fromFilePath(context, uri)
            recognizer.process(image)
                .addOnSuccessListener { visionText ->
                    _documentScanText.value = visionText.text.ifBlank { "No readable text detected on document." }
                }
                .addOnFailureListener {
                    _documentScanText.value = "Failed to scan document sheet."
                }
        } catch (e: Exception) {
            _documentScanText.value = "Error reading document file."
        }
    }

    override fun onCleared() {
        super.onCleared()
        recognizer.close()
    }
}
