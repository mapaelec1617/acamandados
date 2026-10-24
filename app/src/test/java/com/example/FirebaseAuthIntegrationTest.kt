package com.example

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import com.example.ui.AppMode
import com.example.ui.MandadosViewModel
import com.example.util.FirebaseAuthHelper
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class FirebaseAuthIntegrationTest {

    private lateinit var app: Application
    private lateinit var viewModel: MandadosViewModel

    @Before
    fun setup() {
        app = ApplicationProvider.getApplicationContext()
        viewModel = MandadosViewModel(app)
    }

    @Test
    fun testDefaultWebClientIdConfiguredForGoogleSignIn() {
        val webClientId = app.getString(R.string.default_web_client_id)
        assertNotNull(webClientId)
        assertTrue(webClientId.endsWith(".apps.googleusercontent.com"))
        assertTrue(webClientId.isNotBlank())
    }

    @Test
    fun testFriendlyAuthErrorMessageTranslations() {
        val userNotFoundError = Exception("com.google.firebase.auth.FirebaseAuthInvalidUserException: user-not-found")
        assertEquals(
            "No existe una cuenta registrada con este correo electrónico.",
            FirebaseAuthHelper.getFriendlyAuthErrorMessage(userNotFoundError)
        )

        val wrongPasswordError = Exception("com.google.firebase.auth.FirebaseAuthInvalidCredentialsException: wrong-password")
        assertEquals(
            "La contraseña ingresada es incorrecta. Por favor verifica tus datos.",
            FirebaseAuthHelper.getFriendlyAuthErrorMessage(wrongPasswordError)
        )

        val emailInUseError = Exception("com.google.firebase.auth.FirebaseAuthUserCollisionException: email-already-in-use")
        assertEquals(
            "Este correo ya está registrado. Intenta iniciar sesión en su lugar.",
            FirebaseAuthHelper.getFriendlyAuthErrorMessage(emailInUseError)
        )
    }

    @Test
    fun testClientGoogleAuthRegistrationAndStateSync() = runBlocking {
        val testEmail = "cliente.test@acamandados.com"
        val testName = "Mariana Leyva"

        viewModel.loginWithGoogleProfile(
            email = testEmail,
            displayName = testName,
            role = AppMode.CLIENTE
        )

        var retries = 0
        while (viewModel.currentUser.value == null && retries < 60) {
            org.robolectric.shadows.ShadowLooper.runUiThreadTasksIncludingDelayedTasks()
            Thread.sleep(100)
            retries++
        }
        org.robolectric.shadows.ShadowLooper.runUiThreadTasksIncludingDelayedTasks()

        val currentUser = viewModel.currentUser.value
        assertNotNull(currentUser)
        assertEquals(testEmail, currentUser?.email)
        assertEquals(testName, currentUser?.displayName)
        assertEquals(AppMode.CLIENTE, currentUser?.role)
        assertEquals(AppMode.CLIENTE, viewModel.currentMode.value)
        assertTrue(currentUser?.isGoogleAuth == true)

        // Logout
        viewModel.logout()
        org.robolectric.shadows.ShadowLooper.runUiThreadTasksIncludingDelayedTasks()
        assertNull(viewModel.currentUser.value)
    }

    @Test
    fun testDriverGoogleAuthRegistrationAndStateSync() = runBlocking {
        val testEmail = "repartidor.test@acamandados.com"
        val testName = "Jorge Salgado"

        viewModel.loginWithGoogleProfile(
            email = testEmail,
            displayName = testName,
            role = AppMode.REPARTIDOR
        )

        var retries = 0
        while (viewModel.currentUser.value == null && retries < 20) {
            org.robolectric.shadows.ShadowLooper.runUiThreadTasksIncludingDelayedTasks()
            Thread.sleep(50)
            retries++
        }
        org.robolectric.shadows.ShadowLooper.runUiThreadTasksIncludingDelayedTasks()

        val currentUser = viewModel.currentUser.value
        assertNotNull(currentUser)
        assertEquals(testEmail, currentUser?.email)
        assertEquals(testName, currentUser?.displayName)
        assertEquals(AppMode.REPARTIDOR, currentUser?.role)
        assertEquals(AppMode.REPARTIDOR, viewModel.currentMode.value)

        // Logout
        viewModel.logout()
        org.robolectric.shadows.ShadowLooper.runUiThreadTasksIncludingDelayedTasks()
        assertNull(viewModel.currentUser.value)
    }
}
