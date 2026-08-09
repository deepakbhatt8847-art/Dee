package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.capitalize
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.StreetEntity
import com.example.ui.theme.AusPostRed
import com.example.ui.theme.DarkCardSurface
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.RoundGreen
import com.example.ui.viewmodel.ScannerViewModel
import com.example.ui.viewmodel.StreetViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StreetListScreen(
    streetViewModel: StreetViewModel,
    scannerViewModel: ScannerViewModel,
    snackbarHostState: SnackbarHostState
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val searchQuery by streetViewModel.searchQuery.collectAsState()
    val streetList by streetViewModel.streetList.collectAsState()
    val documentScanText by scannerViewModel.documentScanText.collectAsState()

    var showAddEditDialog by remember { mutableStateOf(false) }
    var selectedStreetToEdit by remember { mutableStateOf<StreetEntity?>(null) }
    var streetToDelete by remember { mutableStateOf<StreetEntity?>(null) }

    // Document Picker / Camera photo launcher for Paper Sheet OCR Scanner
    val documentPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            scannerViewModel.scanDocumentSheet(context, it)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Street & Round Database",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                },
                actions = {
                    IconButton(
                        onClick = { documentPickerLauncher.launch("image/*") },
                        modifier = Modifier.testTag("scan_paper_document_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.DocumentScanner,
                            contentDescription = "Scan Paper List Sheet",
                            tint = RoundGreen
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkSurface,
                    titleContentColor = Color.White
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    selectedStreetToEdit = null
                    showAddEditDialog = true
                },
                containerColor = AusPostRed,
                contentColor = Color.White,
                modifier = Modifier.testTag("add_street_fab")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Add Street", fontWeight = FontWeight.Bold)
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Search Input Box
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { streetViewModel.onSearchQueryChanged(it) },
                placeholder = { Text("Search street name or round number...", color = Color.Gray) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = Color.Gray
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { streetViewModel.onSearchQueryChanged("") }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear search",
                                tint = Color.Gray
                            )
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = DarkCardSurface,
                    unfocusedContainerColor = DarkCardSurface,
                    focusedBorderColor = AusPostRed,
                    unfocusedBorderColor = Color.Transparent,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
                    .testTag("search_street_input")
            )

            // Street List
            if (streetList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (searchQuery.isNotBlank()) "No streets matched \"$searchQuery\"" else "No streets saved yet.\nTap + Add Street or Scan Paper Sheet to populate.",
                        color = Color.Gray,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        fontSize = 15.sp
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 12.dp)
                ) {
                    items(streetList, key = { it.streetName }) { item ->
                        StreetCardItem(
                            street = item,
                            onEdit = {
                                selectedStreetToEdit = item
                                showAddEditDialog = true
                            },
                            onDelete = {
                                streetToDelete = item
                            }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }

    // Add / Edit Dialog
    if (showAddEditDialog) {
        AddEditStreetDialog(
            initialStreet = selectedStreetToEdit?.streetName ?: "",
            initialRound = selectedStreetToEdit?.roundNumber ?: "",
            isEditing = selectedStreetToEdit != null,
            onDismiss = { showAddEditDialog = false },
            onSave = { street, round ->
                streetViewModel.addOrUpdateStreet(street, round)
                showAddEditDialog = false
                scope.launch {
                    snackbarHostState.showSnackbar("Saved: $street -> $round")
                }
            }
        )
    }

    // Delete Confirmation Dialog
    streetToDelete?.let { street ->
        AlertDialog(
            onDismissRequest = { streetToDelete = null },
            title = { Text("Delete Street", color = Color.White) },
            text = { Text("Are you sure you want to delete ${street.streetName} (${street.roundNumber})?", color = Color.LightGray) },
            confirmButton = {
                TextButton(
                    onClick = {
                        streetViewModel.deleteStreet(street.streetName)
                        streetToDelete = null
                        scope.launch {
                            snackbarHostState.showSnackbar("Deleted ${street.streetName}")
                        }
                    }
                ) {
                    Text("Delete", color = AusPostRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { streetToDelete = null }) {
                    Text("Cancel", color = Color.Gray)
                }
            },
            containerColor = DarkSurface
        )
    }

    // Paper Sheet OCR Document Scan Result Dialog
    documentScanText?.let { rawText ->
        AlertDialog(
            onDismissRequest = { scannerViewModel.clearDocumentScanText() },
            title = { Text("Scanned Sheet OCR Text", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp)
                        .verticalScroll(rememberScrollState())
                        .background(Color.Black, RoundedCornerShape(8.dp))
                        .padding(12.dp)
                ) {
                    Text(
                        text = rawText,
                        color = RoundGreen,
                        fontSize = 13.sp,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        scannerViewModel.clearDocumentScanText()
                        selectedStreetToEdit = null
                        showAddEditDialog = true
                    }
                ) {
                    Text("Add Street Manually", color = AusPostRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { scannerViewModel.clearDocumentScanText() }) {
                    Text("Close", color = Color.Gray)
                }
            },
            containerColor = DarkSurface
        )
    }
}

@Composable
private fun StreetCardItem(
    street: StreetEntity,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = DarkCardSurface),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = street.streetName,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(4.dp))
                Surface(
                    color = RoundGreen.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = street.roundNumber,
                        color = RoundGreen,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }

            IconButton(
                onClick = onEdit,
                modifier = Modifier.testTag("edit_street_${street.streetName}")
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit Round",
                    tint = Color(0xFF64B5F6)
                )
            }

            IconButton(
                onClick = onDelete,
                modifier = Modifier.testTag("delete_street_${street.streetName}")
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete Street",
                    tint = AusPostRed
                )
            }
        }
    }
}

@Composable
private fun AddEditStreetDialog(
    initialStreet: String,
    initialRound: String,
    isEditing: Boolean,
    onDismiss: () -> Unit,
    onSave: (String, String) -> Unit
) {
    var streetName by remember { mutableStateOf(initialStreet) }
    var roundNumber by remember { mutableStateOf(initialRound) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (isEditing) "Edit Street Round" else "Add New Street",
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = streetName,
                    onValueChange = { streetName = it },
                    label = { Text("Street Name (e.g. HIGH ST)") },
                    enabled = !isEditing, // Lock street name if editing
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = AusPostRed,
                        unfocusedBorderColor = Color.Gray
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("street_name_input")
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = roundNumber,
                    onValueChange = { roundNumber = it },
                    label = { Text("Round Number (e.g. ROUND 04)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = AusPostRed,
                        unfocusedBorderColor = Color.Gray
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("round_number_input")
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (streetName.isNotBlank() && roundNumber.isNotBlank()) {
                        onSave(streetName.trim().uppercase(), roundNumber.trim().uppercase())
                    }
                },
                modifier = Modifier.testTag("save_street_button")
            ) {
                Text("Save", color = AusPostRed, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color.Gray)
            }
        },
        containerColor = DarkSurface
    )
}
