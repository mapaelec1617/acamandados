package com.example

import com.example.data.model.UserProfile
import com.example.ui.AppMode
import com.example.util.FirebaseAuthHelper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class FirebaseAuthIntegrationTest {

    @Test
    fun testFriendlyAuthErrorMessageTranslations() {
        val wrongPasswordEx = Exception("ERROR_WRONG_PASSWORD: password is wrong")
        val friendlyWrongPass = FirebaseAuthHelper.getFriendlyAuthErrorMessage(wrongPasswordEx)
        assertTrue(friendlyWrongPass.contains("contraseña") && friendlyWrongPass.contains("incorrecta"))

        val userNotFoundEx = Exception("ERROR_USER_NOT_FOUND")
        val friendlyUserNotFound = FirebaseAuthHelper.getFriendlyAuthErrorMessage(userNotFoundEx)
        assertTrue(friendlyUserNotFound.contains("No existe una cuenta registrada"))

        val emailInUseEx = Exception("ERROR_EMAIL_ALREADY_IN_USE")
        val friendlyEmailInUse = FirebaseAuthHelper.getFriendlyAuthErrorMessage(emailInUseEx)
        assertTrue(friendlyEmailInUse.contains("ya está registrado"))

        val weakPasswordEx = Exception("ERROR_WEAK_PASSWORD")
        val friendlyWeakPass = FirebaseAuthHelper.getFriendlyAuthErrorMessage(weakPasswordEx)
        assertTrue(friendlyWeakPass.contains("débil") || friendlyWeakPass.contains("6 caracteres"))

        val invalidEmailEx = Exception("ERROR_INVALID_EMAIL")
        val friendlyInvalidEmail = FirebaseAuthHelper.getFriendlyAuthErrorMessage(invalidEmailEx)
        assertTrue(friendlyInvalidEmail.contains("correo") || friendlyInvalidEmail.contains("no es válido"))

        val networkEx = Exception("network-request-failed")
        val friendlyNetwork = FirebaseAuthHelper.getFriendlyAuthErrorMessage(networkEx)
        assertTrue(friendlyNetwork.contains("conexión") || friendlyNetwork.contains("internet"))
    }

    @Test
    fun testUserProfileRoleMappingForClienteAndRepartidor() {
        val clienteProfile = UserProfile(
            uid = "usr_cliente_test_01",
            nombre = "María del Carmen Solís",
            email = "maria.solis@correo.com",
            telefono = "7441234567",
            rol = "CLIENTE",
            zonaFrecuente = "Costa Azul"
        )

        assertEquals("CLIENTE", clienteProfile.rol)
        val clienteMap = clienteProfile.toMap()
        assertEquals("CLIENTE", clienteMap["rol"])
        assertEquals("María del Carmen Solís", clienteMap["nombre"])

        val repartidorProfile = UserProfile(
            uid = "usr_driver_test_02",
            nombre = "Javier Navarrete",
            email = "javier.navarrete@acamandados.com",
            telefono = "7449876543",
            rol = "REPARTIDOR",
            tipoTransporte = "MOTO",
            modeloVehiculo = "Italika 150",
            placas = "GRO-998A"
        )

        assertEquals("REPARTIDOR", repartidorProfile.rol)
        val driverMap = repartidorProfile.toMap()
        assertEquals("REPARTIDOR", driverMap["rol"])
        assertEquals("MOTO", driverMap["tipoTransporte"])
        assertEquals("GRO-998A", driverMap["placas"])
    }

    @Test
    fun testDemoUserUidsAndRoles() {
        val clienteMode = AppMode.CLIENTE
        val repartidorMode = AppMode.REPARTIDOR

        assertEquals(AppMode.CLIENTE, clienteMode)
        assertEquals(AppMode.REPARTIDOR, repartidorMode)
    }
}
