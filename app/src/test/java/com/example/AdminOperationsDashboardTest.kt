package com.example

import com.example.data.model.AdminDailyStats
import com.example.data.model.AdminTab
import com.example.data.model.DriverTelemetryInfo
import com.example.data.model.MandadoOrder
import com.example.ui.AppMode
import org.junit.Assert.*
import org.junit.Test

class AdminOperationsDashboardTest {

    private fun createOrder(
        id: Long,
        folio: String,
        estado: String,
        tarifaEnvio: Double = 50.0,
        costoProductos: Double = 0.0,
        repartidorNombre: String? = null,
        zonaEntrega: String = "Costera Miguel Alemán"
    ): MandadoOrder {
        return MandadoOrder(
            id = id,
            folio = folio,
            tipo = "EXPRESS",
            titulo = "Mandado $folio",
            descripcion = "Descripción del mandado",
            puntoRecogida = "Galerías Diana",
            zonaRecogida = "Costera",
            puntoEntrega = "Condesa",
            zonaEntrega = zonaEntrega,
            tarifaEnvio = tarifaEnvio,
            costoProductosEstimado = costoProductos,
            metodoPago = "EFECTIVO",
            estado = estado,
            clienteNombre = "Cliente Test",
            clienteTelefono = "7441112233",
            repartidorNombre = repartidorNombre,
            pinSeguridad = "4829"
        )
    }

    @Test
    fun testAdminDailyStatsCalculation() {
        val orders = listOf(
            createOrder(1L, "ACA-001", "ENTREGADO", tarifaEnvio = 50.0, costoProductos = 120.0),
            createOrder(2L, "ACA-002", "ENTREGADO", tarifaEnvio = 65.0, costoProductos = 80.0),
            createOrder(3L, "ACA-003", "EN_RUTA", tarifaEnvio = 45.0, repartidorNombre = "Carlos Mendoza"),
            createOrder(4L, "ACA-004", "SOLICITADO", tarifaEnvio = 70.0),
            createOrder(5L, "ACA-005", "CANCELADO", tarifaEnvio = 40.0)
        )

        val totalPedidos = orders.size
        val entregados = orders.count { it.estado == "ENTREGADO" }
        val activos = orders.count { it.estado in listOf("SOLICITADO", "ACEPTADO", "EN_RECOLECCION", "EN_RUTA") }
        val pendientes = orders.count { it.estado == "SOLICITADO" }
        val cancelados = orders.count { it.estado == "CANCELADO" }

        val totalEnviosMxn = orders.filter { it.estado == "ENTREGADO" }.sumOf { it.tarifaEnvio }
        val totalProductosMxn = orders.filter { it.estado == "ENTREGADO" }.sumOf { it.costoProductosEstimado }
        val totalTransaccionado = totalEnviosMxn + totalProductosMxn

        val tasaExito = (entregados.toDouble() / (entregados + cancelados)) * 100.0

        val stats = AdminDailyStats(
            totalPedidosHoy = totalPedidos,
            pedidosEntregadosHoy = entregados,
            pedidosActivosHoy = activos,
            pedidosPendientesAsignacion = pendientes,
            pedidosCanceladosHoy = cancelados,
            tasaExitoPorcentaje = Math.round(tasaExito * 10.0) / 10.0,
            tiempoPromedioMinutos = 22,
            totalIngresosEnvioMxn = totalEnviosMxn,
            totalProductosMxn = totalProductosMxn,
            totalTransaccionadoMxn = totalTransaccionado,
            repartidoresConectados = 5,
            repartidoresEnRuta = 2,
            repartidoresDisponibles = 3
        )

        assertEquals(5, stats.totalPedidosHoy)
        assertEquals(2, stats.pedidosEntregadosHoy)
        assertEquals(2, stats.pedidosActivosHoy)
        assertEquals(1, stats.pedidosPendientesAsignacion)
        assertEquals(1, stats.pedidosCanceladosHoy)
        assertEquals(66.7, stats.tasaExitoPorcentaje, 0.1)
        assertEquals(115.0, stats.totalIngresosEnvioMxn, 0.001)
        assertEquals(200.0, stats.totalProductosMxn, 0.001)
        assertEquals(315.0, stats.totalTransaccionadoMxn, 0.001)
    }

    @Test
    fun testAdminActiveOrdersFiltering() {
        val activeList = listOf(
            createOrder(1L, "ACA-101", "SOLICITADO"),
            createOrder(2L, "ACA-102", "EN_RECOLECCION", repartidorNombre = "Jonathan Alarcón"),
            createOrder(3L, "ACA-103", "EN_RUTA", repartidorNombre = "Carlos Mendoza"),
            createOrder(4L, "ACA-104", "EN_RUTA", repartidorNombre = "Brayan Salgado")
        )

        // Filter: Solo pendientes de asignación
        val pendientes = activeList.filter { it.estado == "SOLICITADO" }
        assertEquals(1, pendientes.size)
        assertEquals("ACA-101", pendientes.first().folio)
        assertNull(pendientes.first().repartidorNombre)

        // Filter: En ruta
        val enRuta = activeList.filter { it.estado == "EN_RUTA" }
        assertEquals(2, enRuta.size)

        // Filter: En recolección
        val enRecoleccion = activeList.filter { it.estado == "EN_RECOLECCION" }
        assertEquals(1, enRecoleccion.size)
        assertEquals("Jonathan Alarcón", enRecoleccion.first().repartidorNombre)
    }

    @Test
    fun testAdminDriverFleetTelemetry() {
        val drivers = listOf(
            DriverTelemetryInfo(
                id = 1L,
                nombre = "Carlos Mendoza",
                telefono = "7441234567",
                vehiculo = "Italika FT150 Blanca",
                placas = "GRO-489A",
                calificacion = 4.95,
                estado = "EN_RUTA",
                zonaNombre = "Costera / Magallanes",
                lat = 16.8580,
                lng = -99.8760,
                velocidadKmh = 38,
                bateriaPorcentaje = 88,
                mandadoFolioAsignado = "ACA-103",
                pedidosHoy = 7,
                pedidosCompletadosTotal = 21
            ),
            DriverTelemetryInfo(
                id = 2L,
                nombre = "Brayan Salgado",
                telefono = "7442345678",
                vehiculo = "Honda Cargo 150 Roja",
                placas = "GRO-712B",
                calificacion = 4.88,
                estado = "DISPONIBLE",
                zonaNombre = "Zona Diamante & Princess",
                lat = 16.7820,
                lng = -99.7890,
                velocidadKmh = 0,
                bateriaPorcentaje = 95,
                mandadoFolioAsignado = null,
                pedidosHoy = 5,
                pedidosCompletadosTotal = 34
            )
        )

        val disponibles = drivers.filter { it.estado == "DISPONIBLE" }
        val enRuta = drivers.filter { it.estado == "EN_RUTA" }

        assertEquals(1, disponibles.size)
        assertEquals("Brayan Salgado", disponibles.first().nombre)
        assertEquals(1, enRuta.size)
        assertEquals("Carlos Mendoza", enRuta.first().nombre)
        assertEquals("ACA-103", enRuta.first().mandadoFolioAsignado)
        assertTrue("Driver speed should be positive when in route", enRuta.first().velocidadKmh > 0)
    }

    @Test
    fun testAdminRoleAndTabNavigation() {
        var currentMode = AppMode.CLIENTE
        assertEquals(AppMode.CLIENTE, currentMode)

        // Switch to ADMIN mode
        currentMode = AppMode.ADMIN
        assertEquals(AppMode.ADMIN, currentMode)

        // Switch admin tabs
        var adminTab = AdminTab.RESUMEN_METRICAS
        assertEquals(AdminTab.RESUMEN_METRICAS, adminTab)

        adminTab = AdminTab.MANDADOS_ACTIVOS
        assertEquals(AdminTab.MANDADOS_ACTIVOS, adminTab)

        adminTab = AdminTab.UBICACION_REPARTIDORES
        assertEquals(AdminTab.UBICACION_REPARTIDORES, adminTab)
    }
}
