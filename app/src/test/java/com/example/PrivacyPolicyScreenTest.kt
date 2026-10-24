package com.example

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import com.example.ui.MandadosViewModel
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowLooper

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class PrivacyPolicyScreenTest {

    private lateinit var app: Application
    private lateinit var viewModel: MandadosViewModel

    @Before
    fun setup() {
        app = ApplicationProvider.getApplicationContext()
        viewModel = MandadosViewModel(app)
    }

    @Test
    fun testPrivacyPolicyScreenNavigationStateTransitions() = runBlocking {
        // Inicialmente la pantalla está cerrada
        assertFalse(viewModel.showPrivacyPolicyScreen.value)

        // Abrir PrivacyPolicyScreen
        viewModel.openPrivacyPolicyScreen()
        assertTrue(viewModel.showPrivacyPolicyScreen.value)

        // Cerrar PrivacyPolicyScreen
        viewModel.closePrivacyPolicyScreen()
        assertFalse(viewModel.showPrivacyPolicyScreen.value)
    }

    @Test
    fun testDataAuthorizationsUpdateAndPersistence() = runBlocking {
        // Verificar estados iniciales por defecto (consentimientos concedidos)
        assertTrue(viewModel.userConsentDataAuth.value)
        assertTrue(viewModel.userConsentGpsTracking.value)
        assertTrue(viewModel.userConsentPushNotifications.value)
        assertTrue(viewModel.userConsentPhoneMasking.value)
        assertTrue(viewModel.userConsentPhotoEvidence.value)

        // Actualizar autorizaciones con revocación parcial
        viewModel.updateDataAuthorizations(
            generalAuth = true,
            gpsTracking = false,
            pushNotifications = false,
            phoneMasking = true,
            photoEvidence = false
        )

        // Verificar que el estado refleja los cambios
        assertTrue(viewModel.userConsentDataAuth.value)
        assertFalse(viewModel.userConsentGpsTracking.value)
        assertFalse(viewModel.userConsentPushNotifications.value)
        assertTrue(viewModel.userConsentPhoneMasking.value)
        assertFalse(viewModel.userConsentPhotoEvidence.value)

        // Mensaje de usuario debe confirmarse
        val userMsg = viewModel.userMessage.value
        assertNotNull(userMsg)
        assertTrue(userMsg!!.contains("autorización de datos") || userMsg.contains("actualizadas"))

        // Restablecer todas a verdadero
        viewModel.updateDataAuthorizations(
            generalAuth = true,
            gpsTracking = true,
            pushNotifications = true,
            phoneMasking = true,
            photoEvidence = true
        )
        assertTrue(viewModel.userConsentGpsTracking.value)
        assertTrue(viewModel.userConsentPushNotifications.value)
        assertTrue(viewModel.userConsentPhotoEvidence.value)
    }

    @Test
    fun testAccountDeletionFromPrivacyPolicyWorkflow() = runBlocking {
        var callbackInvoked = false
        viewModel.deleteUserAccount {
            callbackInvoked = true
        }

        var attempts = 0
        while (!callbackInvoked && attempts < 20) {
            ShadowLooper.idleMainLooper()
            kotlinx.coroutines.delay(50)
            attempts++
        }

        assertTrue(callbackInvoked)
        assertEquals("deleted", viewModel.userProfile.value.uid)
        val msg = viewModel.userMessage.value
        assertNotNull(msg)
        assertTrue(msg!!.contains("políticas de privacidad") || msg.contains("eliminados"))
    }
}
