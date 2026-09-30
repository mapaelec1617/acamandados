package com.example.data.repository

import com.example.data.db.CalificacionDao
import com.example.data.db.MandadoDao
import com.example.data.db.NotificationDao
import com.example.data.db.RepartidorDao
import com.example.data.model.AcapulcoLocations
import com.example.data.model.CalificacionReview
import com.example.data.model.DepositoTarjetaLog
import com.example.data.model.MandadoOrder
import com.example.data.model.NotificationItem
import com.example.data.model.RepartidorProfile
import kotlinx.coroutines.flow.Flow
import java.util.UUID

class MandadosRepository(
    private val mandadoDao: MandadoDao,
    private val repartidorDao: RepartidorDao,
    private val calificacionDao: CalificacionDao,
    private val notificationDao: NotificationDao,
    val firestoreRepository: FirestoreRepository? = null
) {
    val allOrders: Flow<List<MandadoOrder>> = mandadoDao.getAllOrders()
    val availableOrders: Flow<List<MandadoOrder>> = mandadoDao.getAvailableOrders()
    val activeOrders: Flow<List<MandadoOrder>> = mandadoDao.getActiveOrders()
    val repartidorProfile: Flow<RepartidorProfile?> = repartidorDao.getProfileFlow()
    val depositosLog: Flow<List<DepositoTarjetaLog>> = repartidorDao.getDepositosFlow()
    val allReviews: Flow<List<CalificacionReview>> = calificacionDao.getAllCalificaciones()
    val allNotifications: Flow<List<NotificationItem>> = notificationDao.getAllNotifications()
    val unreadNotificationsCount: Flow<Int> = notificationDao.getUnreadCountFlow()

    fun getReviewsForUser(nombre: String): Flow<List<CalificacionReview>> =
        calificacionDao.getCalificacionesPara(nombre)

    fun getNotificationsForRole(role: String): Flow<List<NotificationItem>> =
        notificationDao.getNotificationsForRole(role)

    fun getCurrentDriverActiveOrder(driverId: Long): Flow<MandadoOrder?> =
        mandadoDao.getCurrentActiveDriverOrder(driverId)

    fun getDriverCompletedOrders(driverId: Long): Flow<List<MandadoOrder>> =
        mandadoDao.getDriverCompletedOrders(driverId)

    suspend fun getOrderById(orderId: Long): MandadoOrder? = mandadoDao.getOrderById(orderId)

    suspend fun crearMandado(
        tipo: String,
        titulo: String,
        descripcion: String,
        puntoRecogida: String,
        zonaRecogida: String,
        puntoEntrega: String,
        zonaEntrega: String,
        costoEstimadoProductos: Double,
        tarifaEnvio: Double,
        metodoPago: String,
        clienteNombre: String,
        clienteTelefono: String,
        remitenteNombre: String = "",
        remitenteTelefono: String = "",
        destinatarioNombre: String = "",
        destinatarioTelefono: String = "",
        referenciasDomicilio: String = "",
        tamanoPaquete: String = "MEDIANO",
        confirmacionDimensionesMoto: Boolean = true,
        esCobroContraEntrega: Boolean = false,
        montoProductoCobrar: Double = 0.0,
        recargoZonaLejana: Double = 0.0,
        tarjetaUltimos4: String = "",
        tarjetaTitular: String = "",
        tarjetaAprobada: Boolean = false,
        notas: String = ""
    ): Long {
        val randomFolioNum = (1050..9999).random()
        val randomPin = (1000..9999).random().toString()
        val uniqueTrackingToken = "trk_" + java.util.UUID.randomUUID().toString().replace("-", "").take(10)
        val (latRec, lngRec) = AcapulcoLocations.getCoordenadasParaZona(zonaRecogida)
        val (latEnt, lngEnt) = AcapulcoLocations.getCoordenadasParaZona(zonaEntrega)

        val order = MandadoOrder(
            folio = "ACA-$randomFolioNum",
            tipo = tipo,
            titulo = titulo,
            descripcion = descripcion,
            puntoRecogida = puntoRecogida,
            zonaRecogida = zonaRecogida,
            puntoEntrega = puntoEntrega,
            zonaEntrega = zonaEntrega,
            costoProductosEstimado = costoEstimadoProductos,
            tarifaEnvio = tarifaEnvio,
            metodoPago = metodoPago,
            estado = "SOLICITADO",
            clienteNombre = clienteNombre,
            clienteTelefono = clienteTelefono,
            remitenteNombre = remitenteNombre.ifBlank { clienteNombre },
            remitenteTelefono = remitenteTelefono.ifBlank { clienteTelefono },
            destinatarioNombre = destinatarioNombre.ifBlank { clienteNombre },
            destinatarioTelefono = destinatarioTelefono.ifBlank { clienteTelefono },
            referenciasDomicilio = referenciasDomicilio,
            pinSeguridad = randomPin,
            trackingToken = uniqueTrackingToken,
            tamanoPaquete = tamanoPaquete,
            confirmacionDimensionesMoto = confirmacionDimensionesMoto,
            esCobroContraEntrega = esCobroContraEntrega,
            montoProductoCobrar = montoProductoCobrar,
            recargoZonaLejana = recargoZonaLejana,
            tarjetaUltimos4 = tarjetaUltimos4,
            tarjetaTitular = tarjetaTitular,
            tarjetaAprobada = tarjetaAprobada,
            notasAdicionales = notas,
            fechaCreacion = System.currentTimeMillis(),
            latRecogida = latRec,
            lngRecogida = lngRec,
            latEntrega = latEnt,
            lngEntrega = lngEnt
        )
        val orderId = mandadoDao.insertOrder(order)

        // Alerta de nueva solicitud para los repartidores (indicando distancia aproximada y método de pago)
        val driverCoord = AcapulcoLocations.getCoordenadasParaZona("Magallanes & Galerías Acapulco")
        val distKm = AcapulcoLocations.calcularDistanciaKm(driverCoord.first, driverCoord.second, latRec, lngRec)
        val distText = String.format("%.1f km", distKm)
        val pagoAviso = if (metodoPago == "TARJETA") "💳 Pagado en app" else "💵 Efectivo"
        val codAviso = if (esCobroContraEntrega) " • 🛍️ COD $${montoProductoCobrar.toInt()}" else ""

        notificationDao.insertNotification(
            NotificationItem(
                titulo = "🚨 Nueva solicitud en $zonaRecogida ($pagoAviso)",
                mensaje = "$titulo [$tamanoPaquete] hasta $zonaEntrega ($distText). Ganancia 100% tuya: $${String.format("%.2f", tarifaEnvio)} MXN$codAviso",
                tipo = "NUEVA_SOLICITUD",
                orderFolio = order.folio,
                orderId = orderId,
                distanciaAprox = distText,
                paraRol = "REPARTIDOR"
            )
        )

        return orderId
    }

    suspend fun registrarFotoRecoleccion(orderId: Long, fotoUri: String) {
        val order = mandadoDao.getOrderById(orderId) ?: return
        val updated = order.copy(
            fotoRecoleccionUri = fotoUri,
            estado = "EN_CAMINO"
        )
        mandadoDao.updateOrder(updated)

        // Notificación para el cliente: pedido recogido y foto de condición registrada
        notificationDao.insertNotification(
            NotificationItem(
                titulo = "📸 ¡Mandado recogido y verificado!",
                mensaje = "El repartidor tomó la foto de condición en origen y va en camino a tu domicilio en ${order.zonaEntrega}.",
                tipo = "RECOGIDO",
                orderFolio = order.folio,
                orderId = orderId,
                paraRol = "CLIENTE"
            )
        )
    }

    suspend fun validarPinYFinalizarEntrega(orderId: Long, pinIngresado: String, fotoEntregaUri: String?): Boolean {
        val order = mandadoDao.getOrderById(orderId) ?: return false
        if (pinIngresado.trim() != order.pinSeguridad.trim()) {
            return false
        }

        val speiKey = if (order.metodoPago == "TARJETA") "STP${System.currentTimeMillis()}" else null
        val updated = order.copy(
            estado = "ENTREGADO",
            fechaEntrega = System.currentTimeMillis(),
            fotoEntregaUri = fotoEntregaUri ?: order.fotoEntregaUri,
            pagoDepositadoAlRepartidor = order.metodoPago == "TARJETA",
            claveRastreoSpei = speiKey
        )
        mandadoDao.updateOrder(updated)

        // Notificación cliente
        notificationDao.insertNotification(
            NotificationItem(
                titulo = "✅ ¡Mandado entregado con PIN correcto!",
                mensaje = "Tu mandado ${order.folio} fue entregado con éxito tras validar el PIN de seguridad de 4 dígitos.",
                tipo = "ENTREGADO",
                orderFolio = order.folio,
                orderId = orderId,
                paraRol = "CLIENTE"
            )
        )

        // Notificación repartidor
        notificationDao.insertNotification(
            NotificationItem(
                titulo = "🎉 ¡PIN validado y entrega completada!",
                mensaje = "Has completado la entrega de ${order.folio}. Ganancia $${String.format("%.2f", order.tarifaEnvio)} MXN acreditada 100% para ti.",
                tipo = "ENTREGADO",
                orderFolio = order.folio,
                orderId = orderId,
                paraRol = "REPARTIDOR"
            )
        )

        // Si fue pagado por tarjeta y se entrega, se registra el depósito bancario del mismo día
        if (order.metodoPago == "TARJETA") {
            val driver = repartidorDao.getProfileSync()
            val clabeTerm = driver?.clabeInterbancaria?.takeLast(4) ?: "0123"
            val banco = driver?.banco ?: "BBVA México"
            val key = speiKey ?: "STP${System.currentTimeMillis()}"
            repartidorDao.insertDeposito(
                DepositoTarjetaLog(
                    orderFolio = order.folio,
                    monto = order.tarifaEnvio + order.costoProductosEstimado,
                    fecha = System.currentTimeMillis(),
                    bancoDestino = banco,
                    clabeTerminacion = clabeTerm,
                    claveRastreoSpei = key,
                    estado = "DEPOSITADO_HOY"
                )
            )

            notificationDao.insertNotification(
                NotificationItem(
                    titulo = "💳 ¡Depósito SPEI el mismo día!",
                    mensaje = "Se depositaron $${String.format("%.2f", order.tarifaEnvio + order.costoProductosEstimado)} MXN a tu cuenta $banco (terminación $clabeTerm). Clave de rastreo: $key",
                    tipo = "SPEI",
                    orderFolio = order.folio,
                    orderId = orderId,
                    paraRol = "REPARTIDOR"
                )
            )
        }

        val driver = repartidorDao.getProfileSync()
        if (driver != null) {
            repartidorDao.insertOrUpdateProfile(
                driver.copy(pedidosCompletados = driver.pedidosCompletados + 1)
            )
        }

        try {
            firestoreRepository?.validatePinAndCompleteDelivery(
                orderFolio = order.folio,
                pinIngresado = pinIngresado,
                expectedPin = order.pinSeguridad,
                fotoEntregaUri = fotoEntregaUri,
                claveSpei = speiKey
            )
        } catch (_: Exception) {
            // Manejo tolerante offline
        }

        return true
    }

    suspend fun tomarMandado(orderId: Long, driver: RepartidorProfile) {
        val order = mandadoDao.getOrderById(orderId) ?: return
        val updated = order.copy(
            estado = "ACEPTADO",
            repartidorId = driver.id,
            repartidorNombre = driver.nombre,
            repartidorTelefono = driver.telefono,
            repartidorVehiculo = driver.modeloVehiculo,
            repartidorPlacas = driver.placas
        )
        mandadoDao.updateOrder(updated)

        // Notificación para el cliente: Repartidor asignado
        notificationDao.insertNotification(
            NotificationItem(
                titulo = "🛵 ¡Repartidor asignado!",
                mensaje = "${driver.nombre} ha tomado tu mandado (${order.folio}) en ${order.zonaRecogida}.",
                tipo = "ASIGNADO",
                orderFolio = order.folio,
                orderId = orderId,
                paraRol = "CLIENTE"
            )
        )
    }

    suspend fun avanzarEstadoMandado(orderId: Long, nuevoEstado: String) {
        val order = mandadoDao.getOrderById(orderId) ?: return
        val updated = if (nuevoEstado == "ENTREGADO") {
            val speiKey = if (order.metodoPago == "TARJETA") "STP${System.currentTimeMillis()}" else null
            order.copy(
                estado = nuevoEstado,
                fechaEntrega = System.currentTimeMillis(),
                pagoDepositadoAlRepartidor = order.metodoPago == "TARJETA",
                claveRastreoSpei = speiKey
            )
        } else {
            order.copy(estado = nuevoEstado)
        }
        mandadoDao.updateOrder(updated)

        when (nuevoEstado) {
            "EN_CAMINO" -> {
                // Notificación para el cliente: pedido recogido y en camino
                notificationDao.insertNotification(
                    NotificationItem(
                        titulo = "📦 ¡Tu pedido ha sido recogido!",
                        mensaje = "El repartidor ya recogió tus artículos y va en camino a tu domicilio en ${order.zonaEntrega}.",
                        tipo = "RECOGIDO",
                        orderFolio = order.folio,
                        orderId = orderId,
                        paraRol = "CLIENTE"
                    )
                )
            }
            "ENTREGADO" -> {
                // Notificación para el cliente: entregado + invitar a calificar
                notificationDao.insertNotification(
                    NotificationItem(
                        titulo = "✅ ¡Mandado entregado!",
                        mensaje = "Tu mandado ${order.folio} fue entregado con éxito. ¡Califica el servicio de ${order.repartidorNombre ?: "tu repartidor"}!",
                        tipo = "ENTREGADO",
                        orderFolio = order.folio,
                        orderId = orderId,
                        paraRol = "CLIENTE"
                    )
                )

                // Notificación para el repartidor: entrega completada
                notificationDao.insertNotification(
                    NotificationItem(
                        titulo = "🎉 ¡Mandado entregado!",
                        mensaje = "Has completado la entrega de ${order.folio}. Ganancia $${String.format("%.2f", order.tarifaEnvio)} MXN acreditada 100% para ti.",
                        tipo = "ENTREGADO",
                        orderFolio = order.folio,
                        orderId = orderId,
                        paraRol = "REPARTIDOR"
                    )
                )

                // Si fue pagado por tarjeta y se entrega, se registra el depósito bancario del mismo día
                if (order.metodoPago == "TARJETA") {
                    val driver = repartidorDao.getProfileSync()
                    val clabeTerm = driver?.clabeInterbancaria?.takeLast(4) ?: "0123"
                    val banco = driver?.banco ?: "BBVA México"
                    val speiKey = updated.claveRastreoSpei ?: "STP${System.currentTimeMillis()}"
                    repartidorDao.insertDeposito(
                        DepositoTarjetaLog(
                            orderFolio = order.folio,
                            monto = order.tarifaEnvio + order.costoProductosEstimado,
                            fecha = System.currentTimeMillis(),
                            bancoDestino = banco,
                            clabeTerminacion = clabeTerm,
                            claveRastreoSpei = speiKey,
                            estado = "DEPOSITADO_HOY"
                        )
                    )

                    // Notificación push de depósito SPEI
                    notificationDao.insertNotification(
                        NotificationItem(
                            titulo = "💳 ¡Depósito SPEI el mismo día!",
                            mensaje = "Se depositaron $${String.format("%.2f", order.tarifaEnvio + order.costoProductosEstimado)} MXN a tu cuenta $banco (terminación $clabeTerm). Clave de rastreo: $speiKey",
                            tipo = "SPEI",
                            orderFolio = order.folio,
                            orderId = orderId,
                            paraRol = "REPARTIDOR"
                        )
                    )
                }

                // Actualizar contador del repartidor
                val driver = repartidorDao.getProfileSync()
                if (driver != null) {
                    repartidorDao.insertOrUpdateProfile(
                        driver.copy(pedidosCompletados = driver.pedidosCompletados + 1)
                    )
                }
            }
        }
    }

    suspend fun agregarNotificacionCerca(orderId: Long, folio: String, driverName: String, distText: String) {
        notificationDao.insertNotification(
            NotificationItem(
                titulo = "📍 ¡Tu repartidor está cerca!",
                mensaje = "$driverName está a $distText de tu ubicación. ¡Prepárate para recibir tu mandado!",
                tipo = "CERCA",
                orderFolio = folio,
                orderId = orderId,
                distanciaAprox = distText,
                paraRol = "CLIENTE"
            )
        )
    }

    suspend fun calificarServicio(
        orderId: Long,
        tipo: String, // "CLIENTE_A_REPARTIDOR" o "REPARTIDOR_A_CLIENTE"
        estrellas: Int,
        comentario: String,
        tags: String,
        autorNombre: String,
        destinatarioNombre: String
    ) {
        val order = mandadoDao.getOrderById(orderId) ?: return
        val review = CalificacionReview(
            orderId = orderId,
            orderFolio = order.folio,
            tipo = tipo,
            estrellas = estrellas,
            comentario = comentario,
            tags = tags,
            autorNombre = autorNombre,
            destinatarioNombre = destinatarioNombre,
            fecha = System.currentTimeMillis()
        )
        calificacionDao.insertCalificacion(review)

        // Actualizar el pedido
        if (tipo == "CLIENTE_A_REPARTIDOR") {
            mandadoDao.updateOrder(
                order.copy(
                    calificadoPorCliente = true,
                    calificacionClienteEstrellas = estrellas,
                    calificacionClienteComentario = comentario
                )
            )

            // Actualizar promedio de calificación del repartidor
            val driver = repartidorDao.getProfileSync()
            if (driver != null) {
                val newRating = ((driver.calificacion * driver.pedidosCompletados) + estrellas) / (driver.pedidosCompletados + 1)
                val roundedRating = Math.round(newRating * 100.0) / 100.0
                repartidorDao.insertOrUpdateProfile(driver.copy(calificacion = roundedRating))
            }

            // Notificación al repartidor sobre la nueva calificación
            notificationDao.insertNotification(
                NotificationItem(
                    titulo = "⭐ ¡Nueva calificación recibida! ($estrellas ★)",
                    mensaje = "$autorNombre te calificó con $estrellas estrellas: \"${if (comentario.isNotBlank()) comentario else "Excelente servicio"}\"",
                    tipo = "ENTREGADO",
                    orderFolio = order.folio,
                    orderId = orderId,
                    paraRol = "REPARTIDOR"
                )
            )
        } else {
            mandadoDao.updateOrder(
                order.copy(
                    calificadoPorRepartidor = true,
                    calificacionRepartidorEstrellas = estrellas,
                    calificacionRepartidorComentario = comentario
                )
            )

            // Notificación al cliente
            notificationDao.insertNotification(
                NotificationItem(
                    titulo = "⭐ ¡Calificación recibida del repartidor! ($estrellas ★)",
                    mensaje = "$autorNombre te calificó con $estrellas estrellas por tu pedido ${order.folio}.",
                    tipo = "ENTREGADO",
                    orderFolio = order.folio,
                    orderId = orderId,
                    paraRol = "CLIENTE"
                )
            )
        }
    }

    suspend fun markNotificationAsRead(id: Long) {
        notificationDao.markAsRead(id)
    }

    suspend fun markAllNotificationsAsRead() {
        notificationDao.markAllAsRead()
    }

    suspend fun insertCompletedOrder(order: MandadoOrder): Long {
        return mandadoDao.insertOrder(order)
    }

    suspend fun cancelarMandado(orderId: Long) {
        val order = mandadoDao.getOrderById(orderId) ?: return
        mandadoDao.updateOrder(order.copy(estado = "CANCELADO"))
    }

    suspend fun renovarSuscripcionSemanal() {
        val profile = repartidorDao.getProfileSync() ?: RepartidorProfile()
        val extension = 7 * 24 * 60 * 60 * 1000L
        val baseTime = if (profile.suscripcionFechaFin > System.currentTimeMillis()) {
            profile.suscripcionFechaFin
        } else {
            System.currentTimeMillis()
        }
        val updated = profile.copy(
            suscripcionActiva = true,
            suscripcionFechaFin = baseTime + extension
        )
        repartidorDao.insertOrUpdateProfile(updated)
    }

    suspend fun actualizarPerfilRepartidor(
        nombre: String,
        telefono: String,
        tipoTransporte: String,
        modelo: String,
        placas: String,
        clabe: String,
        banco: String
    ) {
        val current = repartidorDao.getProfileSync() ?: RepartidorProfile()
        val updated = current.copy(
            nombre = nombre,
            telefono = telefono,
            tipoTransporte = tipoTransporte,
            modeloVehiculo = modelo,
            placas = placas,
            clabeInterbancaria = clabe,
            banco = banco
        )
        repartidorDao.insertOrUpdateProfile(updated)
    }

    suspend fun alternarDisponibilidad(disponible: Boolean) {
        val current = repartidorDao.getProfileSync() ?: return
        repartidorDao.insertOrUpdateProfile(current.copy(disponibleParaMandados = disponible))
    }
}
