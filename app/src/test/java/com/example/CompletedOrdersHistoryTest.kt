package com.example

import com.example.data.model.MandadoOrder
import com.example.ui.screens.HistorialViewMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CompletedOrdersHistoryTest {

    private fun createSampleCompletedOrder(
        id: Long = 1L,
        folio: String = "ACA-1040",
        tipo: String = "COMIDA",
        titulo: String = "Pozole y Tacos",
        tarifaEnvio: Double = 50.0,
        costoProductos: Double = 180.0,
        metodoPago: String = "TARJETA",
        fechaEntrega: Long = 1758650000000L,
        repartidorId: Long? = 1L
    ): MandadoOrder {
        return MandadoOrder(
            id = id,
            folio = folio,
            tipo = tipo,
            titulo = titulo,
            descripcion = "Entregado con éxito en mano.",
            puntoRecogida = "Av Costera 240",
            zonaRecogida = "Costera",
            puntoEntrega = "Torre Mayan",
            zonaEntrega = "Diamante",
            costoProductosEstimado = costoProductos,
            tarifaEnvio = tarifaEnvio,
            metodoPago = metodoPago,
            estado = "ENTREGADO",
            clienteNombre = "Ana Karen Valenzuela",
            clienteTelefono = "7441234567",
            repartidorId = repartidorId,
            repartidorNombre = "Carlos Mendoza",
            repartidorTelefono = "7449876543",
            fechaEntrega = fechaEntrega,
            pinSeguridad = "4321",
            calificadoPorCliente = true,
            calificacionClienteEstrellas = 5
        )
    }

    @Test
    fun testCompletedOrderHasRequiredFields() {
        val order = createSampleCompletedOrder()

        // 1. Details: Status
        assertEquals("ENTREGADO", order.estado)

        // 2. Details: Price (tarifaEnvio + costoProductosEstimado)
        val totalCustomerPrice = order.tarifaEnvio + order.costoProductosEstimado
        assertEquals(50.0, order.tarifaEnvio, 0.001)
        assertEquals(180.0, order.costoProductosEstimado, 0.001)
        assertEquals(230.0, totalCustomerPrice, 0.001)

        // 3. Details: Date
        assertNotNull(order.fechaEntrega)
        assertTrue(order.fechaEntrega!! > 0)
    }

    @Test
    fun testCompletedOrdersFilterByRolePerspective() {
        val completedOrder1 = createSampleCompletedOrder(id = 1L, repartidorId = 1L)
        val completedOrder2 = createSampleCompletedOrder(id = 2L, folio = "ACA-1041", repartidorId = 2L)
        val activeOrder = createSampleCompletedOrder(id = 3L).copy(estado = "EN_CAMINO")

        val allOrders = listOf(completedOrder1, completedOrder2, activeOrder)

        // Client completed orders: all with estado == ENTREGADO
        val clientCompleted = allOrders.filter { it.estado == "ENTREGADO" }
        assertEquals(2, clientCompleted.size)

        // Driver completed orders: only where driverId matches driver
        val driverId = 1L
        val driverCompleted = allOrders.filter { it.estado == "ENTREGADO" && it.repartidorId == driverId }
        assertEquals(1, driverCompleted.size)
        assertEquals("ACA-1040", driverCompleted.first().folio)
    }

    @Test
    fun testCompletedOrdersFinancialTotals() {
        val orderCash = createSampleCompletedOrder(
            id = 1,
            tarifaEnvio = 60.0,
            costoProductos = 120.0,
            metodoPago = "EFECTIVO"
        )
        val orderCard = createSampleCompletedOrder(
            id = 2,
            tarifaEnvio = 45.0,
            costoProductos = 200.0,
            metodoPago = "TARJETA"
        )

        val orders = listOf(orderCash, orderCard)

        // Total paid by customer
        val totalCliente = orders.sumOf { it.tarifaEnvio + it.costoProductosEstimado }
        assertEquals(425.0, totalCliente, 0.001)

        // 100% earnings for driver
        val totalGananciasRepartidor = orders.sumOf { it.tarifaEnvio }
        assertEquals(105.0, totalGananciasRepartidor, 0.001)

        // Breakdown: Cash in hand vs Card SPEI
        val efectivo = orders.filter { it.metodoPago == "EFECTIVO" }.sumOf { it.tarifaEnvio }
        val tarjetaSpei = orders.filter { it.metodoPago == "TARJETA" }.sumOf { it.tarifaEnvio }

        assertEquals(60.0, efectivo, 0.001)
        assertEquals(45.0, tarjetaSpei, 0.001)
    }

    @Test
    fun testHistorialViewModeValues() {
        assertEquals("CLIENTE", HistorialViewMode.CLIENTE.name)
        assertEquals("REPARTIDOR", HistorialViewMode.REPARTIDOR.name)
    }
}
