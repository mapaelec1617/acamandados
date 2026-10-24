package com.example.util

import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.NoCredentialException
import com.example.R
import com.google.android.gms.tasks.Task
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

object FirebaseAuthHelper {
    private const val TAG = "FirebaseAuthHelper"

    /**
     * Asegura que FirebaseApp esté inicializado incluso si falta google-services.json
     * en el entorno de desarrollo.
     */
    fun ensureFirebaseApp(context: Context): FirebaseApp? {
        return try {
            if (FirebaseApp.getApps(context).isEmpty()) {
                FirebaseApp.initializeApp(context.applicationContext)
            } else {
                FirebaseApp.getInstance()
            }
        } catch (e: Exception) {
            Log.w(TAG, "FirebaseApp initialization: ${e.message}")
            null
        }
    }

    fun getAuth(context: Context): FirebaseAuth? {
        return try {
            ensureFirebaseApp(context)
            FirebaseAuth.getInstance()
        } catch (e: Exception) {
            Log.w(TAG, "FirebaseAuth instance error: ${e.message}")
            null
        }
    }

    fun getCurrentFirebaseUser(context: Context): FirebaseUser? {
        return getAuth(context)?.currentUser
    }

    /**
     * Autenticación con Google Sign-In mediante la moderna API Android Credential Manager
     * y vinculación con Firebase Authentication.
     */
    suspend fun signInWithGoogleCredentialManager(
        context: Context,
        serverClientId: String
    ): Result<FirebaseUser> {
        return try {
            val credentialManager = CredentialManager.create(context)

            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(serverClientId)
                .setAutoSelectEnabled(false)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val result = credentialManager.getCredential(
                request = request,
                context = context
            )

            val credential = result.credential
            if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                val idToken = googleIdTokenCredential.idToken
                Log.d(TAG, "Google ID Token recibido exitosamente.")

                val auth = getAuth(context)
                    ?: return Result.failure(IllegalStateException("No se pudo inicializar Firebase Auth"))

                val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
                val authResult = auth.signInWithCredential(firebaseCredential).awaitTask()
                val user = authResult.user
                    ?: return Result.failure(IllegalStateException("Usuario Firebase es nulo tras inicio de sesión"))

                Result.success(user)
            } else {
                Result.failure(IllegalStateException("Tipo de credencial no compatible: ${credential.type}"))
            }
        } catch (e: NoCredentialException) {
            Log.i(TAG, "No se encontraron credenciales de Google en el dispositivo/emulador. Procediendo a fallback.")
            Result.failure(GoogleNoCredentialsException(e.message ?: "No credentials available"))
        } catch (e: Exception) {
            if (e.message?.contains("No credentials", ignoreCase = true) == true) {
                Log.i(TAG, "Credenciales no disponibles en este entorno: ${e.message}")
                Result.failure(GoogleNoCredentialsException(e.message ?: "No credentials available"))
            } else {
                Log.w(TAG, "Resultado de Google Sign-In con Credential Manager: ${e.message}")
                Result.failure(e)
            }
        }
    }

    /**
     * Autenticación con Correo Electrónico y Contraseña usando Firebase Auth.
     */
    suspend fun signInWithEmail(
        context: Context,
        email: String,
        password: String
    ): Result<FirebaseUser> {
        val auth = getAuth(context)
            ?: return Result.failure(IllegalStateException("No se pudo inicializar Firebase Auth"))

        return try {
            val authResult = auth.signInWithEmailAndPassword(email.trim(), password).awaitTask()
            val user = authResult.user
                ?: return Result.failure(IllegalStateException("Usuario nulo tras iniciar sesión"))
            Log.d(TAG, "Inicio de sesión exitoso con email: ${user.email}")
            Result.success(user)
        } catch (e: Exception) {
            Log.w(TAG, "Error en signInWithEmail: ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * Creación de cuenta (Registro) con Correo Electrónico y Contraseña usando Firebase Auth.
     */
    suspend fun createUserWithEmail(
        context: Context,
        email: String,
        password: String,
        displayName: String
    ): Result<FirebaseUser> {
        val auth = getAuth(context)
            ?: return Result.failure(IllegalStateException("No se pudo inicializar Firebase Auth"))

        return try {
            val authResult = auth.createUserWithEmailAndPassword(email.trim(), password).awaitTask()
            val user = authResult.user
                ?: return Result.failure(IllegalStateException("Usuario nulo tras crear cuenta"))

            if (displayName.isNotBlank()) {
                try {
                    val updateRequest = UserProfileChangeRequest.Builder()
                        .setDisplayName(displayName.trim())
                        .build()
                    user.updateProfile(updateRequest).awaitTask()
                } catch (e: Exception) {
                    Log.w(TAG, "No se pudo actualizar el nombre en Firebase Auth: ${e.message}")
                }
            }

            Log.d(TAG, "Cuenta creada exitosamente en Firebase Auth: ${user.email}")
            Result.success(user)
        } catch (e: Exception) {
            Log.w(TAG, "Error en createUserWithEmail: ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * Envía un correo para restablecer la contraseña a través de Firebase Auth.
     */
    suspend fun sendPasswordReset(
        context: Context,
        email: String
    ): Result<Unit> {
        val auth = getAuth(context)
            ?: return Result.failure(IllegalStateException("No se pudo inicializar Firebase Auth"))

        return try {
            auth.sendPasswordResetEmail(email.trim()).awaitTask()
            Log.d(TAG, "Correo de restablecimiento enviado a: $email")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.w(TAG, "Error en sendPasswordReset: ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * Traduce los códigos y excepciones de Firebase Auth a mensajes amigables en español.
     */
    fun getFriendlyAuthErrorMessage(exception: Throwable): String {
        val message = exception.message ?: ""
        return when {
            message.contains("ERROR_INVALID_CUSTOM_TOKEN") || message.contains("invalid-credential") ->
                "Las credenciales ingresadas son incorrectas o ya no son válidas."
            message.contains("ERROR_WRONG_PASSWORD") || message.contains("wrong-password") ->
                "La contraseña ingresada es incorrecta. Por favor verifica tus datos."
            message.contains("ERROR_USER_NOT_FOUND") || message.contains("user-not-found") ->
                "No existe una cuenta registrada con este correo electrónico."
            message.contains("ERROR_EMAIL_ALREADY_IN_USE") || message.contains("email-already-in-use") ->
                "Este correo ya está registrado. Intenta iniciar sesión en su lugar."
            message.contains("ERROR_WEAK_PASSWORD") || message.contains("weak-password") ->
                "La contraseña es muy débil. Debe tener al menos 6 caracteres."
            message.contains("ERROR_INVALID_EMAIL") || message.contains("invalid-email") ->
                "El formato del correo electrónico no es válido."
            message.contains("ERROR_USER_DISABLED") || message.contains("user-disabled") ->
                "Esta cuenta ha sido inhabilitada temporalmente."
            message.contains("ERROR_TOO_MANY_REQUESTS") || message.contains("too-many-requests") ->
                "Demasiados intentos fallidos. Por seguridad, espera un momento antes de reintentar."
            message.contains("network-request-failed") || message.contains("network") ->
                "Error de conexión. Verifica tu acceso a internet."
            else -> exception.localizedMessage ?: "Ocurrió un error al procesar la autenticación."
        }
    }

    /**
     * Cierra la sesión en Firebase Authentication.
     */
    fun signOut(context: Context) {
        try {
            getAuth(context)?.signOut()
        } catch (e: Exception) {
            Log.w(TAG, "Error en signOut: ${e.message}")
        }
    }
}

class GoogleNoCredentialsException(message: String) : Exception(message)

/**
 * Extensión segura para esperar Tasks de Google Play Services / Firebase sin bibliotecas externas adicionales.
 */
suspend fun <T> Task<T>.awaitTask(): T = suspendCancellableCoroutine { continuation ->
    addOnSuccessListener { result ->
        continuation.resume(result)
    }
    addOnFailureListener { exception ->
        continuation.resumeWithException(exception)
    }
    addOnCanceledListener {
        continuation.cancel()
    }
}
