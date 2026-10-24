package com.example

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import com.example.ui.MandadosViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowLooper

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class PoliticasSeguridadYAutorizacionTest {

    private lateinit var app: Application
    private lateinit var viewModel: MandadosViewModel

    @Before
    fun setup() {
        app = ApplicationProvider.getApplicationContext()
        viewModel = MandadosViewModel(app)
    }

    @Test
    fun testPoliticasSeguridadDialogStateChangesCorrectly() = runBlocking {
        // Inicialmente cerrado
        assertFalse(viewModel.showPoliticasSeguridadDialog.value)

        // Abrir diálogo de políticas de seguridad
        viewModel.openPoliticasSeguridadDialog()
        assertTrue(viewModel.showPoliticasSeguridadDialog.value)

        // Cerrar diálogo
        viewModel.closePoliticasSeguridadDialog()
        assertFalse(viewModel.showPoliticasSeguridadDialog.value)
    }

    @Test
    fun testAutorizacionDatosDialogStateChangesCorrectly() = runBlocking {
        // Inicialmente cerrado
        assertFalse(viewModel.showAutorizacionDatosDialog.value)

        // Abrir diálogo de autorización de datos
        viewModel.openAutorizacionDatosDialog()
        assertTrue(viewModel.showAutorizacionDatosDialog.value)

        // Cerrar diálogo
        viewModel.closeAutorizacionDatosDialog()
        assertFalse(viewModel.showAutorizacionDatosDialog.value)
    }

    @Test
    fun testUserConsentDataPreferencesCanBeToggled() = runBlocking {
        // Por defecto consentimientos activos
        assertTrue(viewModel.userConsentDataAuth.value)
        assertTrue(viewModel.userConsentGpsTracking.value)
        assertTrue(viewModel.userConsentPushNotifications.value)

        // Desactivar temporalmente notificaciones o GPS
        viewModel.userConsentPushNotifications.value = false
        assertFalse(viewModel.userConsentPushNotifications.value)

        viewModel.userConsentGpsTracking.value = false
        assertFalse(viewModel.userConsentGpsTracking.value)

        // Reactivar autorización de datos
        viewModel.userConsentDataAuth.value = true
        assertTrue(viewModel.userConsentDataAuth.value)
    }

    @Test
    fun testDeleteAccountDialogAndDataPurgeWorkflow() = runBlocking {
        // Abrir diálogo de eliminación de cuenta
        viewModel.openDeleteAccountDialog()
        assertTrue(viewModel.showDeleteAccountDialog.value)

        var completed = false
        viewModel.deleteUserAccount {
            completed = true
        }

        // El diálogo se cierra inmediatamente al confirmar
        assertFalse(viewModel.showDeleteAccountDialog.value)

        var attempts = 0
        while (!completed && attempts < 20) {
            ShadowLooper.idleMainLooper()
            kotlinx.coroutines.delay(50)
            attempts++
        }

        // Verificar que el mensaje informativo cumple con políticas de privacidad
        val msg = viewModel.userMessage.value
        assertNotNull(msg)
        assertTrue(msg!!.contains("políticas de privacidad") || msg.contains("eliminados"))

        // Verificar que la sesión fue limpiada
        assertNull(viewModel.currentUser.value)
        assertEquals("deleted", viewModel.userProfile.value.uid)
        assertTrue(completed)
    }
}
