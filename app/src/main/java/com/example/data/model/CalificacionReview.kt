package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "calificaciones")
data class CalificacionReview(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val orderId: Long,
    val orderFolio: String,
    val tipo: String, // "CLIENTE_A_REPARTIDOR", "REPARTIDOR_A_CLIENTE"
    val estrellas: Int, // 1 a 5
    val comentario: String,
    val tags: String = "", // e.g. "Puntual, Amable, Cuidó el pedido"
    val autorNombre: String,
    val destinatarioNombre: String,
    val fecha: Long = System.currentTimeMillis()
)
