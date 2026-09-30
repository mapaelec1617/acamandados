package com.example.data.repository

import android.content.Context
import android.util.Log
import com.example.data.model.UserProfile
import com.example.util.FirebaseAuthHelper
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Repositorio dedicado a la persistencia y sincronización de perfiles de usuario en Firebase Firestore.
 * Gestiona de manera granular la información de contacto, preferencias de entrega y datos operativos.
 */
class UserProfileRepository(
    private val firestoreInstance: FirebaseFirestore? = null,
    private val context: Context? = null
) {
    companion object {
        private const val TAG = "UserProfileRepository"
        const val COLLECTION_USER_PROFILES = "user_profiles"

        fun create(context: Context? = null): UserProfileRepository {
            return UserProfileRepository(context = context)
        }

        private fun logD(tag: String, msg: String) {
            try {
                Log.d(tag, msg)
            } catch (_: Throwable) {
                println("[$tag] DEBUG: $msg")
            }
        }

        private fun logW(tag: String, msg: String) {
            try {
                Log.w(tag, msg)
            } catch (_: Throwable) {
                println("[$tag] WARN: $msg")
            }
        }

        private fun logE(tag: String, msg: String, tr: Throwable? = null) {
            if (tr is CancellationException || tr is java.util.concurrent.CancellationException) {
                return
            }
            try {
                if (tr != null) Log.e(tag, msg, tr) else Log.e(tag, msg)
            } catch (_: Throwable) {
                println("[$tag] ERROR: $msg ${tr?.message ?: ""}")
            }
        }
    }

    private val db: FirebaseFirestore? by lazy {
        firestoreInstance ?: run {
            context?.let { FirebaseAuthHelper.ensureFirebaseApp(it) }
            try {
                FirebaseFirestore.getInstance()
            } catch (e: Exception) {
                logW(TAG, "No se pudo obtener la instancia de FirebaseFirestore: ${e.message}")
                null
            }
        }
    }

    /**
     * Guarda o sobrescribe por completo el perfil de usuario en Firestore.
     */
    suspend fun saveUserProfile(profile: UserProfile): Result<Unit> = withContext(NonCancellable) {
        val firestore = db ?: return@withContext Result.failure(IllegalStateException("Firestore no está disponible"))
        try {
            val docId = profile.uid.ifBlank { "user_${System.currentTimeMillis()}" }
            val docRef = firestore.collection(COLLECTION_USER_PROFILES).document(docId)
            val data = profile.toMap().toMutableMap()
            data["ultimaActividad"] = System.currentTimeMillis()

            docRef.set(data, SetOptions.merge()).awaitTask()
            logD(TAG, "Perfil de usuario $docId guardado exitosamente en Firestore")
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            logE(TAG, "Error al guardar perfil de usuario: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Consulta el perfil de usuario por su identificador único (UID).
     */
    suspend fun getUserProfile(uid: String): Result<UserProfile?> {
        val firestore = db ?: return Result.failure(IllegalStateException("Firestore no está disponible"))
        return try {
            val snapshot = firestore.collection(COLLECTION_USER_PROFILES).document(uid).get().awaitTask()
            if (snapshot.exists()) {
                val data = snapshot.data
                val profile = data?.let { UserProfile.fromMap(it) }
                Result.success(profile)
            } else {
                Result.success(null)
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            logE(TAG, "Error al consultar perfil del usuario $uid: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Observa en tiempo real mediante snapshots cualquier cambio en el perfil del usuario.
     */
    fun observeUserProfile(uid: String): Flow<UserProfile?> {
        val firestore = db ?: return flowOf(null)
        return callbackFlow {
            val docRef = firestore.collection(COLLECTION_USER_PROFILES).document(uid)
            val listener: ListenerRegistration = docRef.addSnapshotListener { snapshot, error ->
                if (error != null) {
                    logW(TAG, "Error al escuchar perfil del usuario ($uid): ${error.message}")
                    trySend(null)
                    return@addSnapshotListener
                }
                if (snapshot != null && snapshot.exists()) {
                    val data = snapshot.data
                    trySend(data?.let { UserProfile.fromMap(it) })
                } else {
                    trySend(null)
                }
            }
            awaitClose { listener.remove() }
        }
    }

    /**
     * Actualiza la información de contacto y domicilio habitual del usuario en Acapulco.
     */
    suspend fun updateContactInfo(
        uid: String,
        nombre: String,
        telefono: String,
        email: String,
        direccionFrecuente: String,
        zonaFrecuente: String,
        referenciasDomicilio: String,
        notificacionesWhatsApp: Boolean
    ): Result<Unit> = withContext(NonCancellable) {
        val firestore = db ?: return@withContext Result.failure(IllegalStateException("Firestore no está disponible"))
        try {
            val updates = mapOf(
                "nombre" to nombre,
                "telefono" to telefono,
                "email" to email,
                "direccionFrecuente" to direccionFrecuente,
                "zonaFrecuente" to zonaFrecuente,
                "referenciasDomicilio" to referenciasDomicilio,
                "notificacionesWhatsApp" to notificacionesWhatsApp,
                "ultimaActividad" to System.currentTimeMillis()
            )
            firestore.collection(COLLECTION_USER_PROFILES).document(uid)
                .set(updates, SetOptions.merge()).awaitTask()
            logD(TAG, "Información de contacto actualizada para $uid")
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            logE(TAG, "Error al actualizar contacto para $uid: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Actualiza las preferencias de entrega (método de pago, PIN de seguridad, tamaño de paquete, propina, etc.).
     */
    suspend fun updateDeliveryPreferences(
        uid: String,
        metodoPagoPreferido: String,
        exigirPinSeguridad: Boolean,
        tamanoPaquetePreferido: String,
        dejarEnPuertaSiNoContesta: Boolean,
        notificacionesPush: Boolean,
        propinaDefaultPorcentaje: Int
    ): Result<Unit> = withContext(NonCancellable) {
        val firestore = db ?: return@withContext Result.failure(IllegalStateException("Firestore no está disponible"))
        try {
            val updates = mapOf(
                "metodoPagoPreferido" to metodoPagoPreferido,
                "exigirPinSeguridad" to exigirPinSeguridad,
                "tamanoPaquetePreferido" to tamanoPaquetePreferido,
                "dejarEnPuertaSiNoContesta" to dejarEnPuertaSiNoContesta,
                "notificacionesPush" to notificacionesPush,
                "propinaDefaultPorcentaje" to propinaDefaultPorcentaje,
                "ultimaActividad" to System.currentTimeMillis()
            )
            firestore.collection(COLLECTION_USER_PROFILES).document(uid)
                .set(updates, SetOptions.merge()).awaitTask()
            logD(TAG, "Preferencias de entrega actualizadas para $uid")
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            logE(TAG, "Error al actualizar preferencias de entrega para $uid: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Actualiza los datos operativos de vehículo y bancarios para repartidores.
     */
    suspend fun updateDriverVehicleAndBanking(
        uid: String,
        tipoTransporte: String,
        modeloVehiculo: String,
        placas: String,
        clabe: String,
        banco: String
    ): Result<Unit> = withContext(NonCancellable) {
        val firestore = db ?: return@withContext Result.failure(IllegalStateException("Firestore no está disponible"))
        try {
            val updates = mapOf(
                "tipoTransporte" to tipoTransporte,
                "modeloVehiculo" to modeloVehiculo,
                "placas" to placas,
                "clabeInterbancaria" to clabe,
                "banco" to banco,
                "ultimaActividad" to System.currentTimeMillis()
            )
            firestore.collection(COLLECTION_USER_PROFILES).document(uid)
                .set(updates, SetOptions.merge()).awaitTask()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            logE(TAG, "Error al actualizar datos de vehículo/banco para $uid: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Actualiza el estado de disponibilidad del usuario/repartidor.
     */
    suspend fun updateAvailability(uid: String, disponible: Boolean): Result<Unit> = withContext(NonCancellable) {
        val firestore = db ?: return@withContext Result.failure(IllegalStateException("Firestore no está disponible"))
        try {
            val updates = mapOf(
                "disponibleParaMandados" to disponible,
                "ultimaActividad" to System.currentTimeMillis()
            )
            firestore.collection(COLLECTION_USER_PROFILES).document(uid)
                .set(updates, SetOptions.merge()).awaitTask()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            logE(TAG, "Error al actualizar disponibilidad para $uid: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Actualiza la preferencia de rol del usuario ('CLIENTE' o 'REPARTIDOR') en Firestore.
     */
    suspend fun updateUserRole(uid: String, nuevoRol: String): Result<Unit> = withContext(NonCancellable) {
        val firestore = db ?: return@withContext Result.failure(IllegalStateException("Firestore no está disponible"))
        try {
            val safeRol = if (nuevoRol.equals("REPARTIDOR", ignoreCase = true)) "REPARTIDOR" else "CLIENTE"
            val updates = mapOf(
                "rol" to safeRol,
                "ultimaActividad" to System.currentTimeMillis()
            )
            firestore.collection(COLLECTION_USER_PROFILES).document(uid)
                .set(updates, SetOptions.merge()).awaitTask()
            logD(TAG, "Preferencia de rol actualizada a $safeRol para $uid en Firestore")
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            logE(TAG, "Error al actualizar rol de usuario para $uid: ${e.message}", e)
            Result.failure(e)
        }
    }

    private suspend fun <T> Task<T>.awaitTask(): T = suspendCancellableCoroutine { continuation ->
        addOnSuccessListener { result ->
            if (continuation.isActive) continuation.resume(result)
        }
        addOnFailureListener { exception ->
            if (continuation.isActive) continuation.resumeWithException(exception)
        }
        addOnCanceledListener {
            continuation.cancel()
        }
    }
}
