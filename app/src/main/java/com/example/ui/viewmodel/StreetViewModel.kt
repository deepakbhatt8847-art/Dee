package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.StreetEntity
import com.example.data.StreetRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class StreetViewModel(application: Application) : AndroidViewModel(application) {

    val repository: StreetRepository

    init {
        val database = AppDatabase.getDatabase(application)
        repository = StreetRepository(database.streetDao())
    }

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val streetList: StateFlow<List<StreetEntity>> = _searchQuery
        .flatMapLatest { query -> repository.searchStreets(query) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun addOrUpdateStreet(streetName: String, roundNumber: String) {
        viewModelScope.launch {
            repository.addOrUpdateStreet(streetName, roundNumber)
        }
    }

    fun deleteStreet(streetName: String) {
        viewModelScope.launch {
            repository.deleteStreet(streetName)
        }
    }

    fun importBulkText(rawText: String, onComplete: (Int) -> Unit) {
        viewModelScope.launch {
            val lines = rawText.lines()
            val parsed = mutableListOf<Pair<String, String>>()
            for (line in lines) {
                val trimmed = line.trim()
                if (trimmed.isEmpty()) continue
                // Support formats:
                // 1. "HIGH ST, ROUND 01" or "HIGH ST, ROUND 1" or "HIGH ST, 01"
                // 2. "HIGH ST - ROUND 01" or "HIGH ST : ROUND 01"
                // 3. "HIGH ST ROUND 01"
                when {
                    trimmed.contains(",") -> {
                        val parts = trimmed.split(",", limit = 2)
                        if (parts.size == 2) parsed.add(Pair(parts[0], parts[1]))
                    }
                    trimmed.contains("-") -> {
                        val parts = trimmed.split("-", limit = 2)
                        if (parts.size == 2) parsed.add(Pair(parts[0], parts[1]))
                    }
                    trimmed.contains(":") -> {
                        val parts = trimmed.split(":", limit = 2)
                        if (parts.size == 2) parsed.add(Pair(parts[0], parts[1]))
                    }
                    else -> {
                        // Try regex split on "ROUND" keyword
                        val roundIndex = trimmed.indexOf("ROUND", ignoreCase = true)
                        if (roundIndex > 0) {
                            val street = trimmed.substring(0, roundIndex).trim()
                            val round = trimmed.substring(roundIndex).trim()
                            parsed.add(Pair(street, round))
                        }
                    }
                }
            }
            if (parsed.isNotEmpty()) {
                repository.insertBulk(parsed)
            }
            onComplete(parsed.size)
        }
    }

    suspend fun getExportFormattedText(): String {
        val list = repository.getAllList().sortedBy { it.streetName }
        if (list.isEmpty()) return "No streets saved."
        return list.joinToString("\n") { "${it.streetName} - ${it.roundNumber}" }
    }
}
