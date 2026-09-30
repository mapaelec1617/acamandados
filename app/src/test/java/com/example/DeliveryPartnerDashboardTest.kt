package com.example

import com.example.data.model.AcapulcoLocations
import com.example.data.model.MandadoOrder
import com.example.data.model.RepartidorProfile
import com.example.ui.screens.DashboardFilter
import com.example.ui.screens.DashboardSort
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DeliveryPartnerDashboardTest {

    private fun createIncomingOrder(
        id: Long,
        folio: String,
        tarifaEnvio: Double,
        metodoPago: String,
        latRecogida: Double,
        lngRecogida: Double,
        latEntrega: Double,
        lngEntrega: Double,
        fechaCreacion: Long = System.currentTimeMillis()
    ): MandadoOrder {
        return MandadoOrder(
            id = id,
            folio = folio,
            tipo = "EXPRESS",
            titulo = "Envío Express $folio",
            descripcion = "Recoger y entregar paquete",
            puntoRecogida = "Punto Origen $folio",
            zonaRecogida = "Costera Miguel Alemán",
            puntoEntrega = "Punto Destino $folio",
            zonaEntrega = "Costa Azul",
            tarifaEnvio = tarifaEnvio,
            metodoPago = metodoPago,
            estado = "SOLICITADO",
            clienteNombre = "Cliente Test",
            clienteTelefono = "7441112233",
            latRecogida = latRecogida,
            lngRecogida = lngRecogida,
            latEntrega = latEntrega,
            lngEntrega = lngEntrega,
            fechaCreacion = fechaCreacion
        )
    }

    @Test
    fun testDistanceCalculationForIncomingOrders() {
        // Driver position in Costera / Magallanes
        val driverLat = 16.8580
        val driverLng = -99.8760

        // Pickup position at Costera Miguel Alemán (16.8531, -99.8705)
        val pickupLat = 16.8531
        val pickupLng = -99.8705

        // Delivery position at Costa Azul (16.8480, -99.8510)
        val deliveryLat = 16.8480
        val deliveryLng = -99.8510

        val distToPickup = AcapulcoLocations.calcularDistanciaKm(driverLat, driverLng, pickupLat, pickupLng)
        val distRoute = AcapulcoLocations.calcularDistanciaKm(pickupLat, pickupLng, deliveryLat, deliveryLng)
        val totalDistance = distToPickup + distRoute

        // Verify distance calculations are positive and accurate for Acapulco
        assertTrue("Distance to pickup should be between 0.3 and 1.5 km", distToPickup in 0.3..1.5)
        assertTrue("Delivery route distance should be between 1.5 and 3.0 km", distRoute in 1.5..3.0)
        assertTrue("Total distance should be greater than zero", totalDistance > 0)
    }

    @Test
    fun testPayoutAndNetEarningsBreakdown() {
        // Order with $75 MXN payout and cash payment
        val cashOrder = createIncomingOrder(
            id = 1L,
            folio = "ACA-101",
            tarifaEnvio = 75.0,
            metodoPago = "EFECTIVO",
            latRecogida = 16.8580,
            lngRecogida = -99.8760,
            latEntrega = 16.8480,
            lngEntrega = -99.8510
        ).copy(costoProductosEstimado = 150.0)

        // Driver keeps 100% of the delivery payout without commission
        assertEquals(75.0, cashOrder.tarifaEnvio, 0.001)

        // Total cash to collect includes delivery fee and goods advance
        val totalCashToCollect = cashOrder.getTotalCobroEfectivo()
        assertEquals(225.0, totalCashToCollect, 0.001)

        // Card order
        val cardOrder = createIncomingOrder(
            id = 2L,
            folio = "ACA-102",
            tarifaEnvio = 90.0,
            metodoPago = "TARJETA",
            latRecogida = 16.8580,
            lngRecogida = -99.8760,
            latEntrega = 16.7820,
            lngEntrega = -99.7890
        )

        // Cash to collect on delivery for card order is 0.0 (paid via app)
        assertEquals(0.0, cardOrder.getTotalCobroEfectivo(), 0.001)
        assertEquals(90.0, cardOrder.tarifaEnvio, 0.001)
    }

    @Test
    fun testIncomingOrdersFilteringByDistanceAndPayout() {
        val driverLat = 16.8580
        val driverLng = -99.8760

        // Nearby order (< 4 km) with lower payout
        val nearbyOrder = createIncomingOrder(
            id = 1L,
            folio = "ACA-NEARBY",
            tarifaEnvio = 50.0,
            metodoPago = "EFECTIVO",
            latRecogida = 16.8531,
            lngRecogida = -99.8705,
            latEntrega = 16.8480,
            lngEntrega = -99.8510
        )

        // Far order (> 8 km, Diamante) with high payout ($95.0)
        val farOrder = createIncomingOrder(
            id = 2L,
            folio = "ACA-FAR",
            tarifaEnvio = 95.0,
            metodoPago = "TARJETA",
            latRecogida = 16.7820,
            lngRecogida = -99.7890,
            latEntrega = 16.7580,
            lngEntrega = -99.7540
        )

        val orders = listOf(nearbyOrder, farOrder)

        // Filter: Cercanos (< 4 km)
        val nearbyFiltered = orders.filter { order ->
            AcapulcoLocations.calcularDistanciaKm(driverLat, driverLng, order.latRecogida, order.lngRecogida) <= 4.0
        }
        assertEquals(1, nearbyFiltered.size)
        assertEquals("ACA-NEARBY", nearbyFiltered.first().folio)

        // Filter: Mayor Pago (> $65 MXN)
        val highPayoutFiltered = orders.filter { it.tarifaEnvio >= 65.0 }
        assertEquals(1, highPayoutFiltered.size)
        assertEquals("ACA-FAR", highPayoutFiltered.first().folio)

        // Filter: Solo Efectivo
        val cashFiltered = orders.filter { it.metodoPago == "EFECTIVO" }
        assertEquals(1, cashFiltered.size)
        assertEquals("ACA-NEARBY", cashFiltered.first().folio)
    }

    @Test
    fun testIncomingOrdersSorting() {
        val driverLat = 16.8580
        val driverLng = -99.8760

        val order1 = createIncomingOrder(
            id = 1L,
            folio = "ACA-1",
            tarifaEnvio = 60.0,
            metodoPago = "EFECTIVO",
            latRecogida = 16.8531,
            lngRecogida = -99.8705, // ~0.8 km
            latEntrega = 16.8480,
            lngEntrega = -99.8510
        )

        val order2 = createIncomingOrder(
            id = 2L,
            folio = "ACA-2",
            tarifaEnvio = 110.0,
            metodoPago = "TARJETA",
            latRecogida = 16.7820, // ~12 km
            lngRecogida = -99.7890,
            latEntrega = 16.7580,
            lngEntrega = -99.7540
        )

        val orders = listOf(order1, order2)

        // Sort by Payout descending
        val sortedByPayout = orders.sortedByDescending { it.tarifaEnvio }
        assertEquals("ACA-2", sortedByPayout.first().folio)

        // Sort by Distance ascending
        val sortedByDistance = orders.sortedBy { order ->
            AcapulcoLocations.calcularDistanciaKm(driverLat, driverLng, order.latRecogida, order.lngRecogida)
        }
        assertEquals("ACA-1", sortedByDistance.first().folio)
    }

    @Test
    fun testOrderAcceptanceStateTransition() {
        val incomingOrder = createIncomingOrder(
            id = 100L,
            folio = "ACA-999",
            tarifaEnvio = 80.0,
            metodoPago = "EFECTIVO",
            latRecogida = 16.8580,
            lngRecogida = -99.8760,
            latEntrega = 16.8480,
            lngEntrega = -99.8510
        )

        assertEquals("SOLICITADO", incomingOrder.estado)

        // Driver profile accepts the order
        val driverProfile = RepartidorProfile(
            id = 1L,
            nombre = "Carlos Mendoza",
            telefono = "7449876543",
            modeloVehiculo = "Italika FT150 Blanca",
            placas = "GRO-489A",
            suscripcionActiva = true
        )

        // Simulated accept
        val acceptedOrder = incomingOrder.copy(
            estado = "ACEPTADO",
            repartidorId = driverProfile.id,
            repartidorNombre = driverProfile.nombre,
            repartidorTelefono = driverProfile.telefono,
            repartidorVehiculo = driverProfile.modeloVehiculo,
            repartidorPlacas = driverProfile.placas
        )

        assertEquals("ACEPTADO", acceptedOrder.estado)
        assertEquals(driverProfile.id, acceptedOrder.repartidorId)
        assertEquals(driverProfile.nombre, acceptedOrder.repartidorNombre)
        assertEquals(driverProfile.modeloVehiculo, acceptedOrder.repartidorVehiculo)
        assertEquals(driverProfile.placas, acceptedOrder.repartidorPlacas)
    }

    @Test
    fun testProximityMultirangeFilteringAndSlider() {
        val driverLat = 16.8580 // Magallanes
        val driverLng = -99.8760

        // Orden 1: Muy cerca (~0.8 km)
        val veryCloseOrder = createIncomingOrder(
            id = 1L,
            folio = "ACA-CLOSE-1",
            tarifaEnvio = 45.0,
            metodoPago = "EFECTIVO",
            latRecogida = 16.8531,
            lngRecogida = -99.8705, // Costera
            latEntrega = 16.8480,
            lngEntrega = -99.8510
        )

        // Orden 2: Cerca (~3.0 km)
        val mediumOrder = createIncomingOrder(
            id = 2L,
            folio = "ACA-MED-2",
            tarifaEnvio = 55.0,
            metodoPago = "TARJETA",
            latRecogida = 16.8480,
            lngRecogida = -99.8510, // Costa Azul
            latEntrega = 16.8531,
            lngEntrega = -99.8705
        )

        // Orden 3: Lejana (~12.5 km)
        val farOrder = createIncomingOrder(
            id = 3L,
            folio = "ACA-FAR-3",
            tarifaEnvio = 90.0,
            metodoPago = "EFECTIVO",
            latRecogida = 16.7820,
            lngRecogida = -99.7890, // Diamante
            latEntrega = 16.7580,
            lngEntrega = -99.7540
        )

        val orders = listOf(veryCloseOrder, mediumOrder, farOrder)

        // 1. Filtro < 2 km
        val within2km = orders.filter { order ->
            AcapulcoLocations.calcularDistanciaKm(driverLat, driverLng, order.latRecogida, order.lngRecogida) <= 2.0
        }
        assertEquals(1, within2km.size)
        assertEquals("ACA-CLOSE-1", within2km.first().folio)

        // 2. Filtro < 4 km
        val within4km = orders.filter { order ->
            AcapulcoLocations.calcularDistanciaKm(driverLat, driverLng, order.latRecogida, order.lngRecogida) <= 4.0
        }
        assertEquals(2, within4km.size)

        // 3. Filtro Slider dinámico en 5 km
        val sliderRadius5km = 5.0
        val withinSlider = orders.filter { order ->
            AcapulcoLocations.calcularDistanciaKm(driverLat, driverLng, order.latRecogida, order.lngRecogida) <= sliderRadius5km
        }
        assertEquals(2, withinSlider.size)
        assertFalse(withinSlider.any { it.folio == "ACA-FAR-3" })

        // 4. Filtro Slider dinámico en 15 km (incluye toda la bahía)
        val sliderRadius15km = 15.0
        val withinAll = orders.filter { order ->
            AcapulcoLocations.calcularDistanciaKm(driverLat, driverLng, order.latRecogida, order.lngRecogida) <= sliderRadius15km
        }
        assertEquals(3, withinAll.size)
    }
}
