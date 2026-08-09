package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "streets")
data class StreetEntity(
    @PrimaryKey val streetName: String, // Uppercase street name e.g. "HIGH STREET" or "HIGH ST"
    val roundNumber: String,            // Round identifier e.g. "ROUND 01"
    val updatedTimestamp: Long = System.currentTimeMillis()
)
