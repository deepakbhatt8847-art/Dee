package com.example.data

import kotlinx.coroutines.flow.Flow
import java.util.Locale

class StreetRepository(private val streetDao: StreetDao) {

    val allStreets: Flow<List<StreetEntity>> = streetDao.getAllStreets()

    fun searchStreets(query: String): Flow<List<StreetEntity>> {
        return if (query.isBlank()) {
            streetDao.getAllStreets()
        } else {
            streetDao.searchStreets(query.trim().uppercase(Locale.ROOT))
        }
    }

    suspend fun addOrUpdateStreet(streetName: String, roundNumber: String) {
        val cleanStreet = streetName.trim().uppercase(Locale.ROOT)
        val cleanRound = roundNumber.trim().uppercase(Locale.ROOT)
        if (cleanStreet.isNotEmpty() && cleanRound.isNotEmpty()) {
            streetDao.insertOrUpdate(
                StreetEntity(
                    streetName = cleanStreet,
                    roundNumber = cleanRound
                )
            )
        }
    }

    suspend fun insertBulk(streets: List<Pair<String, String>>) {
        val entities = streets.mapNotNull { (name, round) ->
            val cleanName = name.trim().uppercase(Locale.ROOT)
            val cleanRound = round.trim().uppercase(Locale.ROOT)
            if (cleanName.isNotEmpty() && cleanRound.isNotEmpty()) {
                StreetEntity(cleanName, cleanRound)
            } else null
        }
        if (entities.isNotEmpty()) {
            streetDao.insertAll(entities)
        }
    }

    suspend fun deleteStreet(streetName: String) {
        streetDao.deleteByName(streetName)
    }

    suspend fun getAllList(): List<StreetEntity> {
        return streetDao.getAllList()
    }

    /**
     * Matches raw scanned text against database streets.
     * Searches for street names inside the scanned text.
     */
    suspend fun matchStreetInText(scannedText: String): StreetEntity? {
        val streets = streetDao.getAllList()
        if (streets.isEmpty()) return null

        val cleanScannedText = scannedText.uppercase(Locale.ROOT)

        // Try exact line/word sequence matches first, longest street names prioritized
        val sortedStreets = streets.sortedByDescending { it.streetName.length }

        for (street in sortedStreets) {
            val name = street.streetName
            if (cleanScannedText.contains(name)) {
                return street
            }
        }

        // Try word variant matching (e.g., ST vs STREET, RD vs ROAD, PDE vs PARADE, AVE vs AVENUE, HWY vs HIGHWAY)
        for (street in sortedStreets) {
            val normalizedTarget = normalizeStreetVariants(street.streetName)
            val normalizedScanned = normalizeStreetVariants(cleanScannedText)
            if (normalizedScanned.contains(normalizedTarget)) {
                return street
            }
        }

        return null
    }

    private fun normalizeStreetVariants(text: String): String {
        return text
            .replace("\\bSTREET\\b".toRegex(), "ST")
            .replace("\\bROAD\\b".toRegex(), "RD")
            .replace("\\bPARADE\\b".toRegex(), "PDE")
            .replace("\\bAVENUE\\b".toRegex(), "AVE")
            .replace("\\bHIGHWAY\\b".toRegex(), "HWY")
            .replace("\\bDRIVE\\b".toRegex(), "DR")
            .replace("\\bCOURT\\b".toRegex(), "CT")
            .replace("\\bPLACE\\b".toRegex(), "PL")
            .replace("\\bCRESCENT\\b".toRegex(), "CRES")
            .replace("\\bLANE\\b".toRegex(), "LN")
            .replace("\\bBOULEVARD\\b".toRegex(), "BLVD")
    }
}
