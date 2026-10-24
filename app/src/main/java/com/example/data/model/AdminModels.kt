package com.example.data.model

/**
 * Modelos de datos para el Panel de Administración de Operaciones en Acapulco.
 * Permite supervisar mandados activos, ubicación de repartidores en tiempo real y
 * el estado general de las entregas del día (KPIs).
 */
data class DriverTelemetryInfo(
    val id: Long,
    val nombre: String,
    val telefono: String,
    val vehiculo: String,
    val placas: String,
    val calificacion: Double,
    val estado: String, // "DISPONIBLE", "EN_RECOLECCION", "EN_RUTA", "DESCONECTADO"
    val zonaNombre: String,
    val lat: Double,
    val lng: Double,
    val velocidadKmh: Int,
    val bateriaPorcentaje: Int,
    val mandadoFolioAsignado: String? = null,
    val pedidosHoy: Int = 0,
    val pedidosCompletadosTotal: Int = 0,
    val ultimaActualizacion: Long = System.currentTimeMillis()
)

data class AdminDailyStats(
    val totalPedidosHoy: Int,
    val pedidosEntregadosHoy: Int,
    val pedidosActivosHoy: Int,
    val pedidosPendientesAsignacion: Int,
    val pedidosCanceladosHoy: Int,
    val tasaExitoPorcentaje: Double,
    val tiempoPromedioMinutos: Int,
    val totalIngresosEnvioMxn: Double,
    val totalProductosMxn: Double,
    val totalTransaccionadoMxn: Double,
    val repartidoresConectados: Int,
    val repartidoresEnRuta: Int,
    val repartidoresDisponibles: Int,
    val distribucionPorZona: Map<String, Int> = emptyMap()
)

enum class AdminTab {
    RESUMEN_METRICAS,
    MANDADOS_ACTIVOS,
    UBICACION_REPARTIDORES
}
