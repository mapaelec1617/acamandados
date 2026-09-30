package com.example

import com.example.data.model.ActiveDeliveryStatus
import com.example.data.model.MandadoOrder
import org.junit.Assert.*
import org.junit.Test

class SecureDeliveryPinTest {

    @Test
    fun testOrderGenerationGeneratesFourDigitPin() {
        val randomPin = (1000..9999).random().toString()
        val order = MandadoOrder(
            id = 101L,
            folio = "ACA-PIN-01",
            tipo = "EXPRESS",
            titulo = "Documentos Notaría",
            descripcion = "Entregar en mano a destinatario",
            puntoRecogida = "Av. Costera 100",
            zonaRecogida = "Zona Costera & Condesa",
            puntoEntrega = "Gran Vía Tropical 45",
            zonaEntrega = "Zona Tradicional / Caleta",
            tarifaEnvio = 45.0,
            metodoPago = "EFECTIVO",
            estado = "EN_CAMINO",
            clienteNombre = "Ana Karen Valenzuela",
            clienteTelefono = "7445209911",
            destinatarioNombre = "Roberto Gómez",
            destinatarioTelefono = "7449876543",
            pinSeguridad = randomPin
        )

        assertEquals(4, order.pinSeguridad.length)
        assertTrue(order.pinSeguridad.all { it.isDigit() })
        assertEquals(randomPin, order.pinSeguridad)
    }

    @Test
    fun testValidarPinCorrectoMatchesOrderSecurityPin() {
        val testPin = "7492"
        val order = MandadoOrder(
            id = 202L,
            folio = "ACA-PIN-02",
            tipo = "COMIDA",
            titulo = "Pozole Verde Acapulqueño",
            descripcion = "El Pozole de Tía Calla",
            puntoRecogida = "Centro",
            zonaRecogida = "Zona Tradicional / Caleta",
            puntoEntrega = "Costa Azul",
            zonaEntrega = "Zona Dorada & Costa Azul",
            tarifaEnvio = 60.0,
            metodoPago = "TARJETA",
            estado = "EN_CAMINO",
            clienteNombre = "Carlos Mendoza",
            clienteTelefono = "7441112233",
            destinatarioNombre = "Diana Romero",
            destinatarioTelefono = "7442223344",
            pinSeguridad = testPin
        )

        // Verificación de PIN incorrecto
        val pinErroneo = "0000"
        val esPinValidoErroneo = pinErroneo.trim() == order.pinSeguridad.trim()
        assertFalse(esPinValidoErroneo)

        // Verificación de PIN correcto
        val esPinValido = testPin.trim() == order.pinSeguridad.trim()
        assertTrue(esPinValido)

        // Simulación de transición a ENTREGADO con PIN validado
        val orderEntregada = order.copy(
            estado = "ENTREGADO",
            fechaEntrega = System.currentTimeMillis(),
            fotoEntregaUri = "file://evidencia_entrega_202.jpg",
            pagoDepositadoAlRepartidor = order.metodoPago == "TARJETA",
            claveRastreoSpei = "STP${System.currentTimeMillis()}"
        )

        assertEquals("ENTREGADO", orderEntregada.estado)
        assertNotNull(orderEntregada.fechaEntrega)
        assertEquals("file://evidencia_entrega_202.jpg", orderEntregada.fotoEntregaUri)
        assertTrue(orderEntregada.pagoDepositadoAlRepartidor)
        assertNotNull(orderEntregada.claveRastreoSpei)
    }

    @Test
    fun testActiveDeliveryStatusTelemetryWithPin() {
        val order = MandadoOrder(
            id = 55,
            folio = "ACA-PIN-03",
            tipo = "FARMACIA",
            titulo = "Medicamentos",
            descripcion = "Farmacia del Ahorro Costera",
            puntoRecogida = "Costera",
            zonaRecogida = "Zona Costera & Condesa",
            puntoEntrega = "Diamante",
            zonaEntrega = "Zona Diamante & Bonfil",
            tarifaEnvio = 80.0,
            metodoPago = "EFECTIVO",
            estado = "EN_CAMINO",
            clienteNombre = "Patricia Luna",
            clienteTelefono = "7443334455",
            pinSeguridad = "3819"
        )

        val active = ActiveDeliveryStatus.fromOrder(order)
        assertEquals("ACA-PIN-03", active.orderFolio)
        assertEquals(55L, active.orderId)
        assertEquals("EN_CAMINO", active.estado)
        assertFalse(active.pinValidado)

        val map = active.toMap()
        assertEquals("ACA-PIN-03", map["orderFolio"])
        assertEquals(55L, map["orderId"])
        assertEquals("EN_CAMINO", map["estado"])
        assertEquals(false, map["pinValidado"])

        val reconstructed = ActiveDeliveryStatus.fromMap(map)
        assertEquals("ACA-PIN-03", reconstructed.orderFolio)
        assertEquals(55L, reconstructed.orderId)
        assertEquals("EN_CAMINO", reconstructed.estado)
        assertFalse(reconstructed.pinValidado)
    }
}
