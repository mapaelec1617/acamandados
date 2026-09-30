package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "mandado_orders")
data class MandadoOrder(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val folio: String,
    val tipo: String, // "DOCUMENTACION", "FARMACIA", "COMIDA", "COMPRAS", "EXPRESS"
    val titulo: String,
    val descripcion: String,
    val puntoRecogida: String,
    val zonaRecogida: String,
    val puntoEntrega: String,
    val zonaEntrega: String,
    val costoProductosEstimado: Double = 0.0,
    val tarifaEnvio: Double, // 100% para el repartidor
    val metodoPago: String, // "EFECTIVO", "TARJETA"
    val estado: String = "SOLICITADO", // "SOLICITADO", "ACEPTADO", "EN_CAMINO", "ENTREGADO", "CANCELADO"
    val clienteNombre: String,
    val clienteTelefono: String,
    val remitenteNombre: String = "",
    val remitenteTelefono: String = "",
    val destinatarioNombre: String = "",
    val destinatarioTelefono: String = "",
    val referenciasDomicilio: String = "",
    // Validación de Seguridad estilo Uber Connect
    val pinSeguridad: String = "1234", // Código de 4 dígitos generado aleatoriamente
    // Categorización de carga estilo Roadie
    val tamanoPaquete: String = "MEDIANO", // "CHICO", "MEDIANO", "GRANDE"
    val confirmacionDimensionesMoto: Boolean = true,
    // Fotos de comprobante (Proof of Condition en origen y entrega en destino)
    val fotoRecoleccionUri: String? = null,
    val fotoEntregaUri: String? = null,
    // Cobro Contra Entrega (COD) para comercios locales
    val esCobroContraEntrega: Boolean = false,
    val montoProductoCobrar: Double = 0.0,
    // Geofencing y recargo por zona lejana
    val recargoZonaLejana: Double = 0.0,
    // Detalles de pago con tarjeta en la app
    val tarjetaUltimos4: String = "",
    val tarjetaTitular: String = "",
    val tarjetaAprobada: Boolean = false,
    // Asignación de repartidor
    val repartidorId: Long? = null,
    val repartidorNombre: String? = null,
    val repartidorTelefono: String? = null,
    val repartidorVehiculo: String? = null,
    val repartidorPlacas: String? = null,
    val fechaCreacion: Long = System.currentTimeMillis(),
    val fechaEntrega: Long? = null,
    val pagoDepositadoAlRepartidor: Boolean = false,
    val claveRastreoSpei: String? = null,
    val notasAdicionales: String = "",
    val latRecogida: Double = 16.8580,
    val lngRecogida: Double = -99.8760,
    val latEntrega: Double = 16.8531,
    val lngEntrega: Double = -99.8705,
    val calificadoPorCliente: Boolean = false,
    val calificadoPorRepartidor: Boolean = false,
    val calificacionClienteEstrellas: Int? = null,
    val calificacionClienteComentario: String? = null,
    val calificacionRepartidorEstrellas: Int? = null,
    val calificacionRepartidorComentario: String? = null,
    // Token criptográfico único para rastreo web sin app
    val trackingToken: String = ""
) {
    fun getNombreRemitente(): String = if (remitenteNombre.isNotBlank()) remitenteNombre else clienteNombre
    fun getTelefonoRemitente(): String = if (remitenteTelefono.isNotBlank()) remitenteTelefono else clienteTelefono
    fun getNombreDestinatario(): String = if (destinatarioNombre.isNotBlank()) destinatarioNombre else clienteNombre
    fun getTelefonoDestinatario(): String = if (destinatarioTelefono.isNotBlank()) destinatarioTelefono else clienteTelefono
    fun getReferencias(): String = if (referenciasDomicilio.isNotBlank()) referenciasDomicilio else notasAdicionales

    fun getTrackingKey(): String {
        return if (trackingToken.isNotBlank()) {
            trackingToken
        } else {
            // Fallback determinista para órdenes previas
            val cleanFolio = folio.replace("[^A-Za-z0-9]".toRegex(), "").lowercase()
            "tk_${cleanFolio}_${pinSeguridad}"
        }
    }

    /**
     * Genera la URL única para que usuarios sin la app puedan rastrear el pedido en vivo.
     * Estructura: https://track.acamandados.com/order/{folio}?token={uniqueToken}
     */
    fun getWebTrackingUrl(): String {
        val key = getTrackingKey()
        return "https://track.acamandados.com/order/$folio?token=$key"
    }

    fun getShortWebTrackingUrl(): String = "https://acamandados.com/t/$folio"

    /**
     * Mensaje listo para compartir por WhatsApp, SMS o Sharesheet
     */
    fun getShareableTrackingMessage(): String {
        val dest = getNombreDestinatario()
        val url = getWebTrackingUrl()
        return "¡Hola $dest! 🛵 Sigue la entrega de tu mandado en tiempo real sin descargar app aquí:\n$url\n\n🔐 Tu PIN de seguridad para recibir es: $pinSeguridad\n(Díctaselo al repartidor al llegar)."
    }

    fun getMontoMercanciaCobrar(): Double = if (esCobroContraEntrega) montoProductoCobrar else costoProductosEstimado

    fun getTotalCobroEfectivo(): Double {
        return if (esCobroContraEntrega) {
            montoProductoCobrar + tarifaEnvio
        } else if (metodoPago == "EFECTIVO") {
            costoProductosEstimado + tarifaEnvio
        } else {
            0.0
        }
    }
}

enum class TamanoPaquete(val id: String, val label: String, val descripcion: String, val capacidadMoto: String, val limite: String) {
    CHICO("CHICO", "Chico", "Sobres, documentos o llaves", "Cabe en bolsillo o mochila pequeña", "Hasta 1 kg"),
    MEDIANO("MEDIANO", "Mediano", "Caja de zapatos, comida o paquete estándar", "Cabe en mochila tradicional de repartidor", "Hasta 5 kg"),
    GRANDE("GRANDE", "Grande", "Hasta 12 kg (Máx 40x40x40 cm)", "Ocupa parrilla o caja trasera de moto", "Hasta 12 kg (Límite moto)")
}

enum class TipoMandado(val id: String, val label: String, val iconoDesc: String) {
    DOCUMENTACION("DOCUMENTACION", "Envíos & Documentos", "Sobres, llaves, paquetes y trámites"),
    FARMACIA("FARMACIA", "Farmacia & Medicinas", "Medicamentos de receta o mostrador"),
    COMIDA("COMIDA", "Comida & Antojos", "Restaurantes locales, pozolerías, mariscos"),
    COMPRAS("COMPRAS", "Súper & Tiendita", "OXXO, Chedraui, mercado o abarrotes"),
    EXPRESS("EXPRESS", "Mandado Express", "Cualquier vuelta o encargo personal")
}

enum class MetodoPago(val id: String, val label: String, val detalle: String) {
    EFECTIVO("EFECTIVO", "Efectivo", "Pagas directo al repartidor al recibir"),
    TARJETA("TARJETA", "Tarjeta en la App", "Pago digital seguro con tarjeta de crédito/débito en la app")
}

enum class EstadoMandado(val id: String, val label: String) {
    SOLICITADO("SOLICITADO", "Buscando repartidor"),
    ACEPTADO("ACEPTADO", "Repartidor asignado"),
    EN_CAMINO("EN_CAMINO", "En camino a entrega"),
    ENTREGADO("ENTREGADO", "Entregado con éxito"),
    CANCELADO("CANCELADO", "Cancelado")
}
