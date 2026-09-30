package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "repartidor_profile")
data class RepartidorProfile(
    @PrimaryKey
    val id: Long = 1,
    val nombre: String = "Carlos Mendoza",
    val telefono: String = "7441234567",
    val tipoTransporte: String = "MOTO", // MOTO, BICI, AUTO
    val modeloVehiculo: String = "Italika FT150 Blanca",
    val placas: String = "GRO-489A",
    val suscripcionActiva: Boolean = true,
    val suscripcionMontoSemanal: Double = 150.0,
    val suscripcionFechaFin: Long = System.currentTimeMillis() + (7 * 24 * 60 * 60 * 1000L), // 7 días activos
    val clabeInterbancaria: String = "012180015678901234",
    val banco: String = "BBVA México",
    val disponibleParaMandados: Boolean = true,
    val pedidosCompletados: Int = 18,
    val calificacion: Double = 4.95
)

@Entity(tableName = "deposito_tarjeta_log")
data class DepositoTarjetaLog(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val orderFolio: String,
    val monto: Double,
    val fecha: Long = System.currentTimeMillis(),
    val bancoDestino: String,
    val clabeTerminacion: String,
    val claveRastreoSpei: String,
    val estado: String = "DEPOSITADO_HOY" // DEPOSITADO_HOY, EN_PROCESO
)
