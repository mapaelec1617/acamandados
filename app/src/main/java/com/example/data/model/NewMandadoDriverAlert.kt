package com.example.data.model

/**
 * Modelo para alertas inmediatas en primer plano enviadas a los repartidores
 * cuando un cliente solicita un nuevo mandado en Acapulco.
 */
data class NewMandadoDriverAlert(
    val orderId: Long,
    val folio: String,
    val titulo: String,
    val zonaRecogida: String,
    val zonaEntrega: String,
    val tarifaEnvio: Double,
    val metodoPago: String,
    val tamanoPaquete: String,
    val codAviso: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
