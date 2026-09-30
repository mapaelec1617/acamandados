package com.example.data.model

/**
 * Modelo de estado y telemetría de entrega activa persistido en Firebase Firestore ("active_deliveries").
 * Permite monitoreo en tiempo real entre cliente y repartidor en Acapulco.
 */
data class ActiveDeliveryStatus(
    val orderFolio: String = "",
    val orderId: Long = 0L,
    val estado: String = "SOLICITADO", // SOLICITADO, ACEPTADO, EN_CAMINO, ENTREGADO, CANCELADO
    val repartidorId: Long? = null,
    val repartidorNombre: String? = null,
    val repartidorTelefono: String? = null,
    val repartidorVehiculo: String? = null,
    val repartidorPlacas: String? = null,
    val repartidorLat: Double? = null,
    val repartidorLng: Double? = null,
    val speedKmh: Int? = null,
    val bearing: Float? = null,
    val etaMinutos: Int? = null,
    val distanciaRestanteKm: Double? = null,
    val fotoRecoleccionUri: String? = null,
    val fotoEntregaUri: String? = null,
    val pinValidado: Boolean = false,
    val claveRastreoSpei: String? = null,
    val ultimaActualizacion: Long = System.currentTimeMillis()
) {
    fun toMap(): Map<String, Any?> = mapOf(
        "orderFolio" to orderFolio,
        "orderId" to orderId,
        "estado" to estado,
        "repartidorId" to repartidorId,
        "repartidorNombre" to repartidorNombre,
        "repartidorTelefono" to repartidorTelefono,
        "repartidorVehiculo" to repartidorVehiculo,
        "repartidorPlacas" to repartidorPlacas,
        "repartidorLat" to repartidorLat,
        "repartidorLng" to repartidorLng,
        "speedKmh" to speedKmh,
        "bearing" to bearing,
        "etaMinutos" to etaMinutos,
        "distanciaRestanteKm" to distanciaRestanteKm,
        "fotoRecoleccionUri" to fotoRecoleccionUri,
        "fotoEntregaUri" to fotoEntregaUri,
        "pinValidado" to pinValidado,
        "claveRastreoSpei" to claveRastreoSpei,
        "ultimaActualizacion" to ultimaActualizacion
    )

    companion object {
        fun fromMap(map: Map<String, Any?>): ActiveDeliveryStatus {
            return ActiveDeliveryStatus(
                orderFolio = map["orderFolio"] as? String ?: "",
                orderId = (map["orderId"] as? Number)?.toLong() ?: 0L,
                estado = map["estado"] as? String ?: "SOLICITADO",
                repartidorId = (map["repartidorId"] as? Number)?.toLong(),
                repartidorNombre = map["repartidorNombre"] as? String,
                repartidorTelefono = map["repartidorTelefono"] as? String,
                repartidorVehiculo = map["repartidorVehiculo"] as? String,
                repartidorPlacas = map["repartidorPlacas"] as? String,
                repartidorLat = (map["repartidorLat"] as? Number)?.toDouble(),
                repartidorLng = (map["repartidorLng"] as? Number)?.toDouble(),
                speedKmh = (map["speedKmh"] as? Number)?.toInt(),
                bearing = (map["bearing"] as? Number)?.toFloat(),
                etaMinutos = (map["etaMinutos"] as? Number)?.toInt(),
                distanciaRestanteKm = (map["distanciaRestanteKm"] as? Number)?.toDouble(),
                fotoRecoleccionUri = map["fotoRecoleccionUri"] as? String,
                fotoEntregaUri = map["fotoEntregaUri"] as? String,
                pinValidado = map["pinValidado"] as? Boolean ?: false,
                claveRastreoSpei = map["claveRastreoSpei"] as? String,
                ultimaActualizacion = (map["ultimaActualizacion"] as? Number)?.toLong() ?: System.currentTimeMillis()
            )
        }

        fun fromOrder(order: MandadoOrder): ActiveDeliveryStatus {
            return ActiveDeliveryStatus(
                orderFolio = order.folio,
                orderId = order.id,
                estado = order.estado,
                repartidorId = order.repartidorId,
                repartidorNombre = order.repartidorNombre,
                repartidorTelefono = order.repartidorTelefono,
                repartidorVehiculo = order.repartidorVehiculo,
                repartidorPlacas = order.repartidorPlacas,
                fotoRecoleccionUri = order.fotoRecoleccionUri,
                fotoEntregaUri = order.fotoEntregaUri,
                pinValidado = order.estado == "ENTREGADO",
                claveRastreoSpei = order.claveRastreoSpei,
                ultimaActualizacion = System.currentTimeMillis()
            )
        }
    }
}
