package com.example.data.repository

import android.content.Context
import android.util.Log
import com.example.data.model.ActiveDeliveryStatus
import com.example.data.model.MandadoOrder
import com.example.data.model.RepartidorProfile
import com.example.data.model.UserProfile
import com.example.util.FirebaseAuthHelper
import java.security.MessageDigest
import java.util.concurrent.ConcurrentHashMap
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
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
 * Repositorio de persistencia en la nube utilizando Firebase Firestore.
 * Gestiona de manera centralizada y reactiva:
 *  1. Perfiles de usuario (Clientes y Repartidores).
 *  2. Solicitudes de mandados / pedidos (Creación, consulta y actualización).
 *  3. Estados de entrega activa y telemetría de ruta en tiempo real.
 */
class FirestoreRepository(
    private val firestoreInstance: FirebaseFirestore? = null,
    private val context: Context? = null
) {
    companion object {
        private const val TAG = "FirestoreRepository"

        // Colecciones principales en Firestore
        const val COLLECTION_USER_PROFILES = "user_profiles"
        const val COLLECTION_ORDER_REQUESTS = "order_requests"
        const val COLLECTION_ACTIVE_DELIVERIES = "active_deliveries"

        fun create(context: Context? = null): FirestoreRepository {
            return FirestoreRepository(context = context)
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

        fun orderToMap(order: MandadoOrder): Map<String, Any?> = mapOf(
            "id" to order.id,
            "folio" to order.folio,
            "tipo" to order.tipo,
            "titulo" to order.titulo,
            "descripcion" to order.descripcion,
            "puntoRecogida" to order.puntoRecogida,
            "zonaRecogida" to order.zonaRecogida,
            "puntoEntrega" to order.puntoEntrega,
            "zonaEntrega" to order.zonaEntrega,
            "costoProductosEstimado" to order.costoProductosEstimado,
            "tarifaEnvio" to order.tarifaEnvio,
            "metodoPago" to order.metodoPago,
            "estado" to order.estado,
            "clienteNombre" to order.clienteNombre,
            "clienteTelefono" to order.clienteTelefono,
            "remitenteNombre" to order.remitenteNombre,
            "remitenteTelefono" to order.remitenteTelefono,
            "destinatarioNombre" to order.destinatarioNombre,
            "destinatarioTelefono" to order.destinatarioTelefono,
            "referenciasDomicilio" to order.referenciasDomicilio,
            "pinSeguridad" to order.pinSeguridad,
            "tamanoPaquete" to order.tamanoPaquete,
            "confirmacionDimensionesMoto" to order.confirmacionDimensionesMoto,
            "fotoRecoleccionUri" to order.fotoRecoleccionUri,
            "fotoEntregaUri" to order.fotoEntregaUri,
            "esCobroContraEntrega" to order.esCobroContraEntrega,
            "montoProductoCobrar" to order.montoProductoCobrar,
            "recargoZonaLejana" to order.recargoZonaLejana,
            "tarjetaUltimos4" to order.tarjetaUltimos4,
            "tarjetaTitular" to order.tarjetaTitular,
            "tarjetaAprobada" to order.tarjetaAprobada,
            "repartidorId" to order.repartidorId,
            "repartidorNombre" to order.repartidorNombre,
            "repartidorTelefono" to order.repartidorTelefono,
            "repartidorVehiculo" to order.repartidorVehiculo,
            "repartidorPlacas" to order.repartidorPlacas,
            "fechaCreacion" to order.fechaCreacion,
            "fechaEntrega" to order.fechaEntrega,
            "pagoDepositadoAlRepartidor" to order.pagoDepositadoAlRepartidor,
            "claveRastreoSpei" to order.claveRastreoSpei,
            "notasAdicionales" to order.notasAdicionales,
            "latRecogida" to order.latRecogida,
            "lngRecogida" to order.lngRecogida,
            "latEntrega" to order.latEntrega,
            "lngEntrega" to order.lngEntrega,
            "calificadoPorCliente" to order.calificadoPorCliente,
            "calificadoPorRepartidor" to order.calificadoPorRepartidor,
            "calificacionClienteEstrellas" to order.calificacionClienteEstrellas,
            "calificacionClienteComentario" to order.calificacionClienteComentario,
            "calificacionRepartidorEstrellas" to order.calificacionRepartidorEstrellas,
            "calificacionRepartidorComentario" to order.calificacionRepartidorComentario,
            "trackingToken" to order.trackingToken,
            "webTrackingUrl" to order.getWebTrackingUrl()
        )

        fun orderFromMap(map: Map<String, Any?>): MandadoOrder {
            return MandadoOrder(
                id = (map["id"] as? Number)?.toLong() ?: 0L,
                folio = map["folio"] as? String ?: "",
                tipo = map["tipo"] as? String ?: "EXPRESS",
                titulo = map["titulo"] as? String ?: "",
                descripcion = map["descripcion"] as? String ?: "",
                puntoRecogida = map["puntoRecogida"] as? String ?: "",
                zonaRecogida = map["zonaRecogida"] as? String ?: "",
                puntoEntrega = map["puntoEntrega"] as? String ?: "",
                zonaEntrega = map["zonaEntrega"] as? String ?: "",
                costoProductosEstimado = (map["costoProductosEstimado"] as? Number)?.toDouble() ?: 0.0,
                tarifaEnvio = (map["tarifaEnvio"] as? Number)?.toDouble() ?: 0.0,
                metodoPago = map["metodoPago"] as? String ?: "EFECTIVO",
                estado = map["estado"] as? String ?: "SOLICITADO",
                clienteNombre = map["clienteNombre"] as? String ?: "",
                clienteTelefono = map["clienteTelefono"] as? String ?: "",
                remitenteNombre = map["remitenteNombre"] as? String ?: "",
                remitenteTelefono = map["remitenteTelefono"] as? String ?: "",
                destinatarioNombre = map["destinatarioNombre"] as? String ?: "",
                destinatarioTelefono = map["destinatarioTelefono"] as? String ?: "",
                referenciasDomicilio = map["referenciasDomicilio"] as? String ?: "",
                pinSeguridad = map["pinSeguridad"] as? String ?: "1234",
                tamanoPaquete = map["tamanoPaquete"] as? String ?: "MEDIANO",
                confirmacionDimensionesMoto = map["confirmacionDimensionesMoto"] as? Boolean ?: true,
                fotoRecoleccionUri = map["fotoRecoleccionUri"] as? String,
                fotoEntregaUri = map["fotoEntregaUri"] as? String,
                esCobroContraEntrega = map["esCobroContraEntrega"] as? Boolean ?: false,
                montoProductoCobrar = (map["montoProductoCobrar"] as? Number)?.toDouble() ?: 0.0,
                recargoZonaLejana = (map["recargoZonaLejana"] as? Number)?.toDouble() ?: 0.0,
                tarjetaUltimos4 = map["tarjetaUltimos4"] as? String ?: "",
                tarjetaTitular = map["tarjetaTitular"] as? String ?: "",
                tarjetaAprobada = map["tarjetaAprobada"] as? Boolean ?: false,
                repartidorId = (map["repartidorId"] as? Number)?.toLong(),
                repartidorNombre = map["repartidorNombre"] as? String,
                repartidorTelefono = map["repartidorTelefono"] as? String,
                repartidorVehiculo = map["repartidorVehiculo"] as? String,
                repartidorPlacas = map["repartidorPlacas"] as? String,
                fechaCreacion = (map["fechaCreacion"] as? Number)?.toLong() ?: System.currentTimeMillis(),
                fechaEntrega = (map["fechaEntrega"] as? Number)?.toLong(),
                pagoDepositadoAlRepartidor = map["pagoDepositadoAlRepartidor"] as? Boolean ?: false,
                claveRastreoSpei = map["claveRastreoSpei"] as? String,
                notasAdicionales = map["notasAdicionales"] as? String ?: "",
                latRecogida = (map["latRecogida"] as? Number)?.toDouble() ?: 16.8580,
                lngRecogida = (map["lngRecogida"] as? Number)?.toDouble() ?: -99.8760,
                latEntrega = (map["latEntrega"] as? Number)?.toDouble() ?: 16.8531,
                lngEntrega = (map["lngEntrega"] as? Number)?.toDouble() ?: -99.8705,
                calificadoPorCliente = map["calificadoPorCliente"] as? Boolean ?: false,
                calificadoPorRepartidor = map["calificadoPorRepartidor"] as? Boolean ?: false,
                calificacionClienteEstrellas = (map["calificacionClienteEstrellas"] as? Number)?.toInt(),
                calificacionClienteComentario = map["calificacionClienteComentario"] as? String,
                calificacionRepartidorEstrellas = (map["calificacionRepartidorEstrellas"] as? Number)?.toInt(),
                calificacionRepartidorComentario = map["calificacionRepartidorComentario"] as? String,
                trackingToken = map["trackingToken"] as? String ?: ""
            )
        }
    }

    private val db: FirebaseFirestore? by lazy {
        firestoreInstance ?: run {
            context?.let { ctx ->
                FirebaseAuthHelper.ensureFirebaseApp(ctx)
                try {
                    val dbId = ctx.getString(com.example.R.string.firestore_database_id)
                    FirebaseFirestore.getInstance(dbId)
                } catch (e: Exception) {
                    logW(TAG, "No se pudo obtener la instancia de FirebaseFirestore con custom DB ID: ${e.message}")
                    null
                }
            }
        }
    }

    // Mitigación de fuerza bruta para validación de PIN en la nube
    private val pinFailedAttempts = ConcurrentHashMap<String, Int>()

    // =========================================================================
    // 1. GESTIÓN DE PERFILES DE USUARIO (User Profiles)
    // =========================================================================

    /**
     * Guarda o actualiza un perfil de usuario (Cliente o Repartidor) en Firestore.
     */
    suspend fun saveUserProfile(profile: UserProfile): Result<Unit> = withContext(NonCancellable) {
        val firestore = db ?: return@withContext Result.failure(IllegalStateException("Firestore no está disponible"))
        try {
            val docId = if (profile.uid.isNotBlank()) profile.uid else "user_${System.currentTimeMillis()}"
            val docRef = firestore.collection(COLLECTION_USER_PROFILES).document(docId)
            val data = profile.toMap() + ("ultimaActividad" to System.currentTimeMillis())
            docRef.set(data, SetOptions.merge()).awaitTask()
            logD(TAG, "Perfil de usuario guardado con éxito: $docId")
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            logE(TAG, "Error al guardar perfil de usuario: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Guarda el perfil de un Repartidor directamente en Firestore.
     */
    suspend fun saveRepartidorProfile(repartidor: RepartidorProfile, uid: String = "rep_${repartidor.id}"): Result<Unit> {
        val userProfile = UserProfile.fromRepartidorProfile(repartidor, uid)
        return saveUserProfile(userProfile)
    }

    /**
     * Obtiene el perfil de un usuario dado su UID.
     */
    suspend fun getUserProfile(uid: String): Result<UserProfile?> {
        val firestore = db ?: return Result.failure(IllegalStateException("Firestore no está disponible"))
        return try {
            val docSnapshot = firestore.collection(COLLECTION_USER_PROFILES).document(uid).get().awaitTask()
            if (docSnapshot.exists()) {
                val data = docSnapshot.data
                val profile = data?.let { UserProfile.fromMap(it) }
                Result.success(profile)
            } else {
                Result.success(null)
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            logE(TAG, "Error al obtener perfil del usuario $uid: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Observa en tiempo real las actualizaciones del perfil de un usuario.
     */
    fun observeUserProfile(uid: String): Flow<UserProfile?> {
        val firestore = db ?: return flowOf(null)
        return callbackFlow {
            val docRef = firestore.collection(COLLECTION_USER_PROFILES).document(uid)
            val listener: ListenerRegistration = docRef.addSnapshotListener { snapshot, error ->
                if (error != null) {
                    logW(TAG, "Error en listener de perfil de usuario ($uid): ${error.message}")
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
            logD(TAG, "Rol actualizado a $safeRol para usuario $uid en Firestore")
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            logE(TAG, "Error al actualizar rol de usuario $uid: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Actualiza la disponibilidad de un repartidor en Firestore.
     */
    suspend fun updateUserAvailability(uid: String, disponible: Boolean): Result<Unit> = withContext(NonCancellable) {
        val firestore = db ?: return@withContext Result.failure(IllegalStateException("Firestore no está disponible"))
        try {
            firestore.collection(COLLECTION_USER_PROFILES).document(uid)
                .update("disponibleParaMandados", disponible, "ultimaActividad", System.currentTimeMillis())
                .awaitTask()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            logE(TAG, "Error al actualizar disponibilidad: ${e.message}", e)
            Result.failure(e)
        }
    }

    // =========================================================================
    // 2. GESTIÓN DE SOLICITUDES DE MANDADOS (Order Requests)
    // =========================================================================

    /**
     * Guarda o publica una nueva solicitud de mandado en Firestore.
     */
    suspend fun saveOrderRequest(order: MandadoOrder): Result<String> = withContext(NonCancellable) {
        val firestore = db ?: return@withContext Result.failure(IllegalStateException("Firestore no está disponible"))
        try {
            val docId = if (order.folio.isNotBlank()) order.folio else "ACA-${order.id}"
            val docRef = firestore.collection(COLLECTION_ORDER_REQUESTS).document(docId)
            val data = orderToMap(order)
            docRef.set(data, SetOptions.merge()).awaitTask()

            // Inicializar también el estado de entrega activa
            val activeRef = firestore.collection(COLLECTION_ACTIVE_DELIVERIES).document(docId)
            val activeData = ActiveDeliveryStatus.fromOrder(order).toMap()
            activeRef.set(activeData, SetOptions.merge()).awaitTask()

            logD(TAG, "Solicitud de mandado persistida con éxito en Firestore: $docId")
            Result.success(docId)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            logE(TAG, "Error al persistir solicitud de mandado: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Obtiene una solicitud de mandado por su folio o identificador.
     */
    suspend fun getOrderRequest(orderFolio: String): Result<MandadoOrder?> {
        val firestore = db ?: return Result.failure(IllegalStateException("Firestore no está disponible"))
        return try {
            val snapshot = firestore.collection(COLLECTION_ORDER_REQUESTS).document(orderFolio).get().awaitTask()
            if (snapshot.exists()) {
                val data = snapshot.data
                Result.success(data?.let { orderFromMap(it) })
            } else {
                Result.success(null)
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            logE(TAG, "Error al obtener mandado $orderFolio: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Observa todas las solicitudes de mandado ordenadas cronológicamente.
     */
    fun observeAllOrderRequests(): Flow<List<MandadoOrder>> {
        val firestore = db ?: return flowOf(emptyList())
        return callbackFlow {
            val query = firestore.collection(COLLECTION_ORDER_REQUESTS)
                .orderBy("fechaCreacion", Query.Direction.DESCENDING)

            val listener = query.addSnapshotListener { snapshot, error ->
                if (error != null) {
                    logW(TAG, "Error al escuchar solicitudes de mandado: ${error.message}")
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val list = snapshot?.documents?.mapNotNull { doc ->
                    doc.data?.let { orderFromMap(it) }
                } ?: emptyList()
                trySend(list)
            }
            awaitClose { listener.remove() }
        }
    }

    /**
     * Observa las solicitudes de mandados que están activas y disponibles para repartidores ("SOLICITADO").
     */
    fun observeAvailableOrderRequests(): Flow<List<MandadoOrder>> {
        val firestore = db ?: return flowOf(emptyList())
        return callbackFlow {
            val query = firestore.collection(COLLECTION_ORDER_REQUESTS)
                .whereEqualTo("estado", "SOLICITADO")
                .orderBy("fechaCreacion", Query.Direction.DESCENDING)

            val listener = query.addSnapshotListener { snapshot, error ->
                if (error != null) {
                    logW(TAG, "Error al escuchar mandados disponibles: ${error.message}")
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val list = snapshot?.documents?.mapNotNull { doc ->
                    doc.data?.let { orderFromMap(it) }
                } ?: emptyList()
                trySend(list)
            }
            awaitClose { listener.remove() }
        }
    }

    /**
     * Observa los mandados solicitados por un cliente específico.
     */
    fun observeOrdersForClient(clienteTelefono: String): Flow<List<MandadoOrder>> {
        val firestore = db ?: return flowOf(emptyList())
        return callbackFlow {
            val query = firestore.collection(COLLECTION_ORDER_REQUESTS)
                .whereEqualTo("clienteTelefono", clienteTelefono)
                .orderBy("fechaCreacion", Query.Direction.DESCENDING)

            val listener = query.addSnapshotListener { snapshot, error ->
                if (error != null) {
                    logW(TAG, "Error al escuchar mandados del cliente: ${error.message}")
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val list = snapshot?.documents?.mapNotNull { doc ->
                    doc.data?.let { orderFromMap(it) }
                } ?: emptyList()
                trySend(list)
            }
            awaitClose { listener.remove() }
        }
    }

    /**
     * Cancela una solicitud de mandado.
     */
    suspend fun cancelOrderRequest(orderFolio: String): Result<Unit> = withContext(NonCancellable) {
        val firestore = db ?: return@withContext Result.failure(IllegalStateException("Firestore no está disponible"))
        try {
            val updates = mapOf(
                "estado" to "CANCELADO",
                "ultimaActualizacion" to System.currentTimeMillis()
            )
            firestore.collection(COLLECTION_ORDER_REQUESTS).document(orderFolio)
                .update(updates).awaitTask()
            firestore.collection(COLLECTION_ACTIVE_DELIVERIES).document(orderFolio)
                .update(updates).awaitTask()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            logE(TAG, "Error al cancelar mandado $orderFolio: ${e.message}", e)
            Result.failure(e)
        }
    }

    // =========================================================================
    // 3. GESTIÓN DE ESTADOS DE ENTREGA ACTIVA (Active Delivery Statuses)
    // =========================================================================

    /**
     * Asigna un repartidor a una solicitud de mandado y actualiza su estado a "ACEPTADO".
     */
    suspend fun assignDeliveryPartner(orderFolio: String, driver: RepartidorProfile): Result<Unit> = withContext(NonCancellable) {
        val firestore = db ?: return@withContext Result.failure(IllegalStateException("Firestore no está disponible"))
        try {
            val now = System.currentTimeMillis()
            val updates = mapOf(
                "estado" to "ACEPTADO",
                "repartidorId" to driver.id,
                "repartidorNombre" to driver.nombre,
                "repartidorTelefono" to driver.telefono,
                "repartidorVehiculo" to driver.modeloVehiculo,
                "repartidorPlacas" to driver.placas,
                "ultimaActualizacion" to now
            )

            firestore.collection(COLLECTION_ORDER_REQUESTS).document(orderFolio)
                .update(updates).awaitTask()

            firestore.collection(COLLECTION_ACTIVE_DELIVERIES).document(orderFolio)
                .set(updates, SetOptions.merge()).awaitTask()

            logD(TAG, "Repartidor ${driver.nombre} asignado al mandado $orderFolio en Firestore")
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            logE(TAG, "Error al asignar repartidor al mandado $orderFolio: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Actualiza el estado de la entrega ("EN_CAMINO", "ENTREGADO", etc.).
     */
    suspend fun updateDeliveryStatus(
        orderFolio: String,
        nuevoEstado: String,
        extraData: Map<String, Any?> = emptyMap()
    ): Result<Unit> = withContext(NonCancellable) {
        val firestore = db ?: return@withContext Result.failure(IllegalStateException("Firestore no está disponible"))
        try {
            val now = System.currentTimeMillis()
            val updates = mutableMapOf<String, Any?>(
                "estado" to nuevoEstado,
                "ultimaActualizacion" to now
            )
            updates.putAll(extraData)

            firestore.collection(COLLECTION_ORDER_REQUESTS).document(orderFolio)
                .update(updates).awaitTask()

            firestore.collection(COLLECTION_ACTIVE_DELIVERIES).document(orderFolio)
                .set(updates, SetOptions.merge()).awaitTask()

            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            logE(TAG, "Error al actualizar estado del mandado $orderFolio a $nuevoEstado: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Registra la foto de comprobante en origen (Proof of Condition) y avanza a "EN_CAMINO".
     */
    suspend fun recordProofOfCondition(orderFolio: String, fotoUri: String): Result<Unit> {
        return updateDeliveryStatus(
            orderFolio = orderFolio,
            nuevoEstado = "EN_CAMINO",
            extraData = mapOf("fotoRecoleccionUri" to fotoUri)
        )
    }

    /**
     * Actualiza la telemetría y ubicación en tiempo real del repartidor para el seguimiento en vivo.
     */
    suspend fun updateActiveDeliveryLocation(
        orderFolio: String,
        lat: Double,
        lng: Double,
        speedKmh: Int,
        bearing: Float,
        etaMinutes: Int,
        distanceRemainingKm: Double
    ): Result<Unit> {
        val firestore = db ?: return Result.failure(IllegalStateException("Firestore no está disponible"))
        return try {
            val now = System.currentTimeMillis()
            val telemetry = mapOf(
                "repartidorLat" to lat,
                "repartidorLng" to lng,
                "speedKmh" to speedKmh,
                "bearing" to bearing,
                "etaMinutos" to etaMinutes,
                "distanciaRestanteKm" to distanceRemainingKm,
                "ultimaActualizacion" to now
            )

            firestore.collection(COLLECTION_ACTIVE_DELIVERIES).document(orderFolio)
                .set(telemetry, SetOptions.merge()).awaitTask()

            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            logW(TAG, "Error al actualizar telemetría de entrega activa $orderFolio: ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * Valida el PIN de seguridad de 4 dígitos y finaliza la entrega en Firestore.
     */
    suspend fun validatePinAndCompleteDelivery(
        orderFolio: String,
        pinIngresado: String,
        expectedPin: String,
        fotoEntregaUri: String? = null,
        claveSpei: String? = null
    ): Result<Boolean> = withContext(NonCancellable) {
        val currentAttempts = pinFailedAttempts.getOrDefault(orderFolio, 0)
        if (currentAttempts >= 5) {
            logW(TAG, "PIN bloqueado temporalmente por exceso de intentos fallidos (fuerza bruta) para $orderFolio")
            return@withContext Result.success(false)
        }

        val expectedBytes = expectedPin.trim().toByteArray(Charsets.UTF_8)
        val inputBytes = pinIngresado.trim().toByteArray(Charsets.UTF_8)
        val isPinCorrect = MessageDigest.isEqual(expectedBytes, inputBytes)

        if (!isPinCorrect) {
            pinFailedAttempts[orderFolio] = currentAttempts + 1
            return@withContext Result.success(false)
        }

        pinFailedAttempts.remove(orderFolio)

        val firestore = db ?: return@withContext Result.failure(IllegalStateException("Firestore no está disponible"))
        try {
            val now = System.currentTimeMillis()
            val updates = mutableMapOf<String, Any?>(
                "estado" to "ENTREGADO",
                "pinValidado" to true,
                "fechaEntrega" to now,
                "ultimaActualizacion" to now
            )
            fotoEntregaUri?.let { updates["fotoEntregaUri"] = it }
            claveSpei?.let {
                updates["claveRastreoSpei"] = it
                updates["pagoDepositadoAlRepartidor"] = true
            }

            firestore.collection(COLLECTION_ORDER_REQUESTS).document(orderFolio)
                .update(updates).awaitTask()

            firestore.collection(COLLECTION_ACTIVE_DELIVERIES).document(orderFolio)
                .set(updates, SetOptions.merge()).awaitTask()

            logD(TAG, "Entrega completada exitosamente con PIN para el mandado $orderFolio")
            Result.success(true)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            logE(TAG, "Error al completar entrega con PIN para $orderFolio: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Observa en tiempo real el estado y telemetría de un mandado activo en particular.
     */
    fun observeActiveDeliveryStatus(orderFolio: String): Flow<ActiveDeliveryStatus?> {
        val firestore = db ?: return flowOf(null)
        return callbackFlow {
            val docRef = firestore.collection(COLLECTION_ACTIVE_DELIVERIES).document(orderFolio)
            val listener = docRef.addSnapshotListener { snapshot, error ->
                if (error != null) {
                    logW(TAG, "Error al observar entrega activa $orderFolio: ${error.message}")
                    trySend(null)
                    return@addSnapshotListener
                }
                if (snapshot != null && snapshot.exists()) {
                    val data = snapshot.data
                    trySend(data?.let { ActiveDeliveryStatus.fromMap(it) })
                } else {
                    trySend(null)
                }
            }
            awaitClose { listener.remove() }
        }
    }

    /**
     * Observa las entregas activas asignadas a un repartidor en específico.
     */
    fun observeActiveDeliveriesForDriver(driverId: Long): Flow<List<ActiveDeliveryStatus>> {
        val firestore = db ?: return flowOf(emptyList())
        return callbackFlow {
            val query = firestore.collection(COLLECTION_ACTIVE_DELIVERIES)
                .whereEqualTo("repartidorId", driverId)
                .whereIn("estado", listOf("ACEPTADO", "EN_CAMINO"))

            val listener = query.addSnapshotListener { snapshot, error ->
                if (error != null) {
                    logW(TAG, "Error al observar entregas activas del repartidor $driverId: ${error.message}")
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val list = snapshot?.documents?.mapNotNull { doc ->
                    doc.data?.let { ActiveDeliveryStatus.fromMap(it) }
                } ?: emptyList()
                trySend(list)
            }
            awaitClose { listener.remove() }
        }
    }

    // =========================================================================
    // UTILIDADES DE TAREAS ASINCRONAS
    // =========================================================================

    /**
     * Espera a que un Task de Google Play Services / Firebase se complete de manera segura.
     */
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
