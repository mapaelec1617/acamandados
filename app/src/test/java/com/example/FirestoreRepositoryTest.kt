package com.example

import com.example.data.model.ActiveDeliveryStatus
import com.example.data.model.MandadoOrder
import com.example.data.model.RepartidorProfile
import com.example.data.model.UserProfile
import com.example.data.repository.FirestoreRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class FirestoreRepositoryTest {

    @Test
    fun testUserProfileSerializationAndMapping() {
        val originalProfile = UserProfile(
            uid = "user_acapulco_101",
            nombre = "Carlos Mendoza",
            email = "carlos.mendoza@acamandados.com",
            telefono = "7441234567",
            rol = "REPARTIDOR",
            fotoUrl = "https://example.com/avatar.jpg",
            tipoTransporte = "MOTO",
            modeloVehiculo = "Italika FT150 Blanca",
            placas = "GRO-489A",
            clabeInterbancaria = "012180015678901234",
            banco = "BBVA México",
            disponibleParaMandados = true,
            suscripcionActiva = true,
            pedidosCompletados = 24,
            calificacion = 4.98
        )

        val map = originalProfile.toMap()
        assertEquals("user_acapulco_101", map["uid"])
        assertEquals("Carlos Mendoza", map["nombre"])
        assertEquals("REPARTIDOR", map["rol"])
        assertEquals("GRO-489A", map["placas"])
        assertEquals(24, map["pedidosCompletados"])
        assertEquals(4.98, map["calificacion"])

        val reconstructed = UserProfile.fromMap(map)
        assertEquals(originalProfile.uid, reconstructed.uid)
        assertEquals(originalProfile.nombre, reconstructed.nombre)
        assertEquals(originalProfile.email, reconstructed.email)
        assertEquals(originalProfile.telefono, reconstructed.telefono)
        assertEquals(originalProfile.rol, reconstructed.rol)
        assertEquals(originalProfile.tipoTransporte, reconstructed.tipoTransporte)
        assertEquals(originalProfile.modeloVehiculo, reconstructed.modeloVehiculo)
        assertEquals(originalProfile.placas, reconstructed.placas)
        assertEquals(originalProfile.clabeInterbancaria, reconstructed.clabeInterbancaria)
        assertEquals(originalProfile.banco, reconstructed.banco)
        assertTrue(reconstructed.disponibleParaMandados)
        assertTrue(reconstructed.suscripcionActiva)
        assertEquals(24, reconstructed.pedidosCompletados)
        assertEquals(4.98, reconstructed.calificacion, 0.001)
    }

    @Test
    fun testUserProfileFromRepartidorProfile() {
        val repProfile = RepartidorProfile(
            id = 5,
            nombre = "Mario Guerrero",
            telefono = "7449988776",
            tipoTransporte = "MOTO",
            modeloVehiculo = "Yamaha YBR 125",
            placas = "GRO-772B",
            suscripcionActiva = true,
            clabeInterbancaria = "123456789012345678",
            banco = "Santander",
            disponibleParaMandados = true,
            pedidosCompletados = 40,
            calificacion = 5.0
        )

        val userProfile = UserProfile.fromRepartidorProfile(repProfile, "rep_5")
        assertEquals("rep_5", userProfile.uid)
        assertEquals("Mario Guerrero", userProfile.nombre)
        assertEquals("REPARTIDOR", userProfile.rol)
        assertEquals("Yamaha YBR 125", userProfile.modeloVehiculo)
        assertEquals("GRO-772B", userProfile.placas)
        assertEquals(40, userProfile.pedidosCompletados)
        assertEquals(5.0, userProfile.calificacion, 0.001)
    }

    @Test
    fun testActiveDeliveryStatusSerialization() {
        val activeDelivery = ActiveDeliveryStatus(
            orderFolio = "ACA-5021",
            orderId = 105L,
            estado = "EN_CAMINO",
            repartidorId = 2L,
            repartidorNombre = "Carlos Mendoza",
            repartidorTelefono = "7441234567",
            repartidorVehiculo = "Italika FT150",
            repartidorPlacas = "GRO-489A",
            repartidorLat = 16.8580,
            repartidorLng = -99.8760,
            speedKmh = 42,
            bearing = 120.5f,
            etaMinutos = 6,
            distanciaRestanteKm = 2.4,
            fotoRecoleccionUri = "content://media/images/pickup_105.jpg",
            pinValidado = false,
            claveRastreoSpei = null
        )

        val map = activeDelivery.toMap()
        assertEquals("ACA-5021", map["orderFolio"])
        assertEquals(105L, map["orderId"])
        assertEquals("EN_CAMINO", map["estado"])
        assertEquals(42, map["speedKmh"])
        assertEquals(6, map["etaMinutos"])
        assertEquals(2.4, map["distanciaRestanteKm"])

        val reconstructed = ActiveDeliveryStatus.fromMap(map)
        assertEquals("ACA-5021", reconstructed.orderFolio)
        assertEquals(105L, reconstructed.orderId)
        assertEquals("EN_CAMINO", reconstructed.estado)
        assertEquals("Carlos Mendoza", reconstructed.repartidorNombre)
        assertEquals(16.8580, reconstructed.repartidorLat!!, 0.0001)
        assertEquals(-99.8760, reconstructed.repartidorLng!!, 0.0001)
        assertEquals(42, reconstructed.speedKmh)
        assertEquals(120.5f, reconstructed.bearing!!, 0.1f)
        assertEquals(6, reconstructed.etaMinutos)
        assertEquals(2.4, reconstructed.distanciaRestanteKm!!, 0.01)
        assertEquals("content://media/images/pickup_105.jpg", reconstructed.fotoRecoleccionUri)
        assertFalse(reconstructed.pinValidado)
    }

    @Test
    fun testActiveDeliveryStatusFromOrder() {
        val order = MandadoOrder(
            id = 201L,
            folio = "ACA-9921",
            tipo = "FARMACIA",
            titulo = "Medicamentos urgentes",
            descripcion = "Antibióticos y suero",
            puntoRecogida = "Farmacias del Ahorro Costera",
            zonaRecogida = "Costera",
            puntoEntrega = "Costa Azul #12",
            zonaEntrega = "Costa Azul",
            tarifaEnvio = 65.0,
            metodoPago = "TARJETA",
            estado = "ACEPTADO",
            clienteNombre = "Ana Karen Valenzuela",
            clienteTelefono = "7445209911",
            repartidorId = 1L,
            repartidorNombre = "Carlos Mendoza",
            repartidorTelefono = "7441234567",
            repartidorVehiculo = "Italika FT150",
            repartidorPlacas = "GRO-489A"
        )

        val active = ActiveDeliveryStatus.fromOrder(order)
        assertEquals("ACA-9921", active.orderFolio)
        assertEquals(201L, active.orderId)
        assertEquals("ACEPTADO", active.estado)
        assertEquals("Carlos Mendoza", active.repartidorNombre)
        assertEquals("GRO-489A", active.repartidorPlacas)
        assertFalse(active.pinValidado)
    }

    @Test
    fun testFirestoreRepositoryConstantsAndOfflineGracefulHandling() = runBlocking {
        assertEquals("user_profiles", FirestoreRepository.COLLECTION_USER_PROFILES)
        assertEquals("order_requests", FirestoreRepository.COLLECTION_ORDER_REQUESTS)
        assertEquals("active_deliveries", FirestoreRepository.COLLECTION_ACTIVE_DELIVERIES)

        // Verificamos que si se inicializa sin instancia Firestore o sin contexto, maneja los errores con Result.failure
        val repository = FirestoreRepository(firestoreInstance = null, context = null)

        val saveUserResult = repository.saveUserProfile(UserProfile(uid = "test_user_offline"))
        assertTrue(saveUserResult.isFailure)

        val getUserResult = repository.getUserProfile("test_user_offline")
        assertTrue(getUserResult.isFailure)

        val saveOrderResult = repository.saveOrderRequest(
            MandadoOrder(
                id = 1L,
                folio = "ACA-0001",
                tipo = "EXPRESS",
                titulo = "Test",
                descripcion = "Test",
                puntoRecogida = "A",
                zonaRecogida = "A",
                puntoEntrega = "B",
                zonaEntrega = "B",
                tarifaEnvio = 50.0,
                metodoPago = "EFECTIVO",
                estado = "SOLICITADO",
                clienteNombre = "Test",
                clienteTelefono = "7440000000"
            )
        )
        assertTrue(saveOrderResult.isFailure)

        val observeUserFlow = repository.observeUserProfile("test_user_offline")
        assertNull(observeUserFlow.first())

        val observeOrdersFlow = repository.observeAllOrderRequests()
        assertTrue(observeOrdersFlow.first().isEmpty())

        val observeDeliveriesFlow = repository.observeActiveDeliveriesForDriver(1L)
        assertTrue(observeDeliveriesFlow.first().isEmpty())
    }
}
