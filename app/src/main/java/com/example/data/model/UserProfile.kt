package com.example.data.model

/**
 * Modelo de perfil de usuario persistido en Firebase Firestore ("user_profiles").
 * Admite tanto clientes particulares como repartidores asociados en Acapulco,
 * incluyendo información de contacto y preferencias de entrega.
 */
data class UserProfile(
    val uid: String = "",
    val nombre: String = "",
    val email: String = "",
    val telefono: String = "",
    val rol: String = "CLIENTE", // "CLIENTE" o "REPARTIDOR"
    val fotoUrl: String? = null,

    // Información de Contacto & Domicilio Habitual (Acapulco)
    val direccionFrecuente: String = "",
    val zonaFrecuente: String = "Costera Miguel Alemán",
    val referenciasDomicilio: String = "",
    val notificacionesWhatsApp: Boolean = true,

    // Preferencias de Entrega
    val metodoPagoPreferido: String = "EFECTIVO", // "EFECTIVO" o "TARJETA"
    val exigirPinSeguridad: Boolean = true,
    val tamanoPaquetePreferido: String = "MEDIANO", // "CHICO", "MEDIANO", "GRANDE"
    val dejarEnPuertaSiNoContesta: Boolean = false,
    val notificacionesPush: Boolean = true,
    val propinaDefaultPorcentaje: Int = 10, // 0, 10, 15, 20

    // Atributos específicos para Repartidor
    val tipoTransporte: String = "MOTO", // MOTO, BICI, AUTO
    val modeloVehiculo: String = "",
    val placas: String = "",
    val clabeInterbancaria: String = "",
    val banco: String = "BBVA México",
    val disponibleParaMandados: Boolean = true,
    val suscripcionActiva: Boolean = true,
    val pedidosCompletados: Int = 0,
    val calificacion: Double = 5.0,
    val fechaCreacion: Long = System.currentTimeMillis(),
    val ultimaActividad: Long = System.currentTimeMillis()
) {
    fun toMap(): Map<String, Any?> = mapOf(
        "uid" to uid,
        "nombre" to nombre,
        "email" to email,
        "telefono" to telefono,
        "rol" to rol,
        "fotoUrl" to fotoUrl,
        "direccionFrecuente" to direccionFrecuente,
        "zonaFrecuente" to zonaFrecuente,
        "referenciasDomicilio" to referenciasDomicilio,
        "notificacionesWhatsApp" to notificacionesWhatsApp,
        "metodoPagoPreferido" to metodoPagoPreferido,
        "exigirPinSeguridad" to exigirPinSeguridad,
        "tamanoPaquetePreferido" to tamanoPaquetePreferido,
        "dejarEnPuertaSiNoContesta" to dejarEnPuertaSiNoContesta,
        "notificacionesPush" to notificacionesPush,
        "propinaDefaultPorcentaje" to propinaDefaultPorcentaje,
        "tipoTransporte" to tipoTransporte,
        "modeloVehiculo" to modeloVehiculo,
        "placas" to placas,
        "clabeInterbancaria" to clabeInterbancaria,
        "banco" to banco,
        "disponibleParaMandados" to disponibleParaMandados,
        "suscripcionActiva" to suscripcionActiva,
        "pedidosCompletados" to pedidosCompletados,
        "calificacion" to calificacion,
        "fechaCreacion" to fechaCreacion,
        "ultimaActividad" to ultimaActividad
    )

    companion object {
        fun fromMap(map: Map<String, Any?>): UserProfile {
            return UserProfile(
                uid = map["uid"] as? String ?: "",
                nombre = map["nombre"] as? String ?: "",
                email = map["email"] as? String ?: "",
                telefono = map["telefono"] as? String ?: "",
                rol = map["rol"] as? String ?: "CLIENTE",
                fotoUrl = map["fotoUrl"] as? String,
                direccionFrecuente = map["direccionFrecuente"] as? String ?: "",
                zonaFrecuente = map["zonaFrecuente"] as? String ?: "Costera Miguel Alemán",
                referenciasDomicilio = map["referenciasDomicilio"] as? String ?: "",
                notificacionesWhatsApp = map["notificacionesWhatsApp"] as? Boolean ?: true,
                metodoPagoPreferido = map["metodoPagoPreferido"] as? String ?: "EFECTIVO",
                exigirPinSeguridad = map["exigirPinSeguridad"] as? Boolean ?: true,
                tamanoPaquetePreferido = map["tamanoPaquetePreferido"] as? String ?: "MEDIANO",
                dejarEnPuertaSiNoContesta = map["dejarEnPuertaSiNoContesta"] as? Boolean ?: false,
                notificacionesPush = map["notificacionesPush"] as? Boolean ?: true,
                propinaDefaultPorcentaje = (map["propinaDefaultPorcentaje"] as? Number)?.toInt() ?: 10,
                tipoTransporte = map["tipoTransporte"] as? String ?: "MOTO",
                modeloVehiculo = map["modeloVehiculo"] as? String ?: "",
                placas = map["placas"] as? String ?: "",
                clabeInterbancaria = map["clabeInterbancaria"] as? String ?: "",
                banco = map["banco"] as? String ?: "BBVA México",
                disponibleParaMandados = map["disponibleParaMandados"] as? Boolean ?: true,
                suscripcionActiva = map["suscripcionActiva"] as? Boolean ?: true,
                pedidosCompletados = (map["pedidosCompletados"] as? Number)?.toInt() ?: 0,
                calificacion = (map["calificacion"] as? Number)?.toDouble() ?: 5.0,
                fechaCreacion = (map["fechaCreacion"] as? Number)?.toLong() ?: System.currentTimeMillis(),
                ultimaActividad = (map["ultimaActividad"] as? Number)?.toLong() ?: System.currentTimeMillis()
            )
        }

        fun fromRepartidorProfile(repartidor: RepartidorProfile, uid: String = "rep_${repartidor.id}"): UserProfile {
            return UserProfile(
                uid = uid,
                nombre = repartidor.nombre,
                telefono = repartidor.telefono,
                rol = "REPARTIDOR",
                tipoTransporte = repartidor.tipoTransporte,
                modeloVehiculo = repartidor.modeloVehiculo,
                placas = repartidor.placas,
                clabeInterbancaria = repartidor.clabeInterbancaria,
                banco = repartidor.banco,
                disponibleParaMandados = repartidor.disponibleParaMandados,
                suscripcionActiva = repartidor.suscripcionActiva,
                pedidosCompletados = repartidor.pedidosCompletados,
                calificacion = repartidor.calificacion
            )
        }
    }
}
