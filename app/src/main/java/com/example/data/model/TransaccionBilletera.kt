package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entidad que registra los movimientos de la billetera del repartidor:
 * - Ingresos por cada mandado completado (tarifa de envío + reembolsos)
 * - Retiros a cuenta bancaria CLABE (dispersiones SPEI)
 * - Pagos o renovaciones de suscripción semanal
 */
@Entity(tableName = "transacciones_billetera")
data class TransaccionBilletera(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val tipo: String, // "INGRESO", "RETIRO", "SUSCRIPCION"
    val concepto: String,
    val detalle: String,
    val monto: Double,
    val fecha: Long = System.currentTimeMillis(),
    val orderFolio: String? = null,
    val orderId: Long? = null,
    val metodoPago: String = "SPEI", // "TARJETA", "EFECTIVO", "SPEI", "SALDO"
    val claveRastreoSpei: String? = null,
    val bancoDestino: String? = null,
    val clabeTerminacion: String? = null,
    val estado: String = "LIQUIDADO" // "LIQUIDADO", "EN_PROCESO"
) {
    val isIngreso: Boolean
        get() = tipo == "INGRESO"

    val isRetiro: Boolean
        get() = tipo == "RETIRO" || tipo == "SUSCRIPCION"
}
