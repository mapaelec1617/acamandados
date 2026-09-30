package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "app_notifications")
data class NotificationItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val titulo: String,
    val mensaje: String,
    val tipo: String, // "NUEVA_SOLICITUD", "ASIGNADO", "RECOGIDO", "CERCA", "ENTREGADO", "SPEI"
    val orderFolio: String? = null,
    val orderId: Long? = null,
    val distanciaAprox: String? = null,
    val fecha: Long = System.currentTimeMillis(),
    val leida: Boolean = false,
    val paraRol: String = "TODOS" // "CLIENTE", "REPARTIDOR", "TODOS"
)
