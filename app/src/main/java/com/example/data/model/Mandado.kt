package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room database entity for 'Mandado' that includes fields for description, location,
 * status, and price to track deliveries.
 */
@Entity(tableName = "mandados")
data class Mandado(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val description: String,
    val location: String,
    val status: String = "PENDING",
    val price: Double = 0.0,
    val createdAt: Long = System.currentTimeMillis()
)
