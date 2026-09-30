package com.example

import com.example.data.model.UserProfile
import com.example.data.repository.UserProfileRepository
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
class UserProfileRepositoryTest {

    @Test
    fun testUserProfileContactAndDeliveryPreferencesSerialization() {
        val profile = UserProfile(
            uid = "usr_acapulco_99",
            nombre = "Ana Karen Valenzuela",
            email = "karen.valenzuela@acamandados.com",
            telefono = "7445209911",
            rol = "CLIENTE",
            direccionFrecuente = "Av. Costera Miguel Alemán #240, Fracc. Condesa",
            zonaFrecuente = "Zona Costera & Condesa",
            referenciasDomicilio = "Edificio blanco con portón café",
            notificacionesWhatsApp = true,
            metodoPagoPreferido = "TARJETA",
            exigirPinSeguridad = true,
            tamanoPaquetePreferido = "CHICO",
            dejarEnPuertaSiNoContesta = true,
            notificacionesPush = true,
            propinaDefaultPorcentaje = 15
        )

        val map = profile.toMap()
        assertEquals("usr_acapulco_99", map["uid"])
        assertEquals("Ana Karen Valenzuela", map["nombre"])
        assertEquals("Av. Costera Miguel Alemán #240, Fracc. Condesa", map["direccionFrecuente"])
        assertEquals("Zona Costera & Condesa", map["zonaFrecuente"])
        assertEquals("TARJETA", map["metodoPagoPreferido"])
        assertEquals(true, map["exigirPinSeguridad"])
        assertEquals("CHICO", map["tamanoPaquetePreferido"])
        assertEquals(true, map["dejarEnPuertaSiNoContesta"])
        assertEquals(15, map["propinaDefaultPorcentaje"])

        val reconstructed = UserProfile.fromMap(map)
        assertEquals(profile.uid, reconstructed.uid)
        assertEquals(profile.nombre, reconstructed.nombre)
        assertEquals(profile.direccionFrecuente, reconstructed.direccionFrecuente)
        assertEquals(profile.zonaFrecuente, reconstructed.zonaFrecuente)
        assertEquals(profile.referenciasDomicilio, reconstructed.referenciasDomicilio)
        assertTrue(reconstructed.notificacionesWhatsApp)
        assertEquals("TARJETA", reconstructed.metodoPagoPreferido)
        assertTrue(reconstructed.exigirPinSeguridad)
        assertEquals("CHICO", reconstructed.tamanoPaquetePreferido)
        assertTrue(reconstructed.dejarEnPuertaSiNoContesta)
        assertTrue(reconstructed.notificacionesPush)
        assertEquals(15, reconstructed.propinaDefaultPorcentaje)
    }

    @Test
    fun testUserProfileRepositoryConstantsAndOfflineHandling() = runBlocking {
        assertEquals("user_profiles", UserProfileRepository.COLLECTION_USER_PROFILES)

        val repo = UserProfileRepository(firestoreInstance = null, context = null)

        val saveResult = repo.saveUserProfile(UserProfile(uid = "offline_user"))
        assertTrue(saveResult.isFailure)

        val getResult = repo.getUserProfile("offline_user")
        assertTrue(getResult.isFailure)

        val updateContactResult = repo.updateContactInfo(
            uid = "offline_user",
            nombre = "Carlos M",
            telefono = "7441112233",
            email = "test@acamandados.com",
            direccionFrecuente = "Costera #100",
            zonaFrecuente = "Costera",
            referenciasDomicilio = "Frente a playa",
            notificacionesWhatsApp = true
        )
        assertTrue(updateContactResult.isFailure)

        val updatePrefResult = repo.updateDeliveryPreferences(
            uid = "offline_user",
            metodoPagoPreferido = "EFECTIVO",
            exigirPinSeguridad = true,
            tamanoPaquetePreferido = "MEDIANO",
            dejarEnPuertaSiNoContesta = false,
            notificacionesPush = true,
            propinaDefaultPorcentaje = 10
        )
        assertTrue(updatePrefResult.isFailure)

        val updateRoleResult = repo.updateUserRole("offline_user", "REPARTIDOR")
        assertTrue(updateRoleResult.isFailure)

        val flow = repo.observeUserProfile("offline_user")
        assertNull(flow.first())
    }

    @Test
    fun testRoleSelectionClienteAndRepartidorMapping() {
        val userClient = UserProfile(
            uid = "usr_client_1",
            nombre = "Cliente Acapulco",
            rol = "CLIENTE"
        )
        assertEquals("CLIENTE", userClient.rol)
        val clientMap = userClient.toMap()
        assertEquals("CLIENTE", clientMap["rol"])

        val userDriver = userClient.copy(rol = "REPARTIDOR")
        assertEquals("REPARTIDOR", userDriver.rol)
        val driverMap = userDriver.toMap()
        assertEquals("REPARTIDOR", driverMap["rol"])
    }
}
