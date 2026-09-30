package com.example

import com.example.data.model.MandadoOrder
import com.example.ui.screens.EstadoEnvioFiltro
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ClienteHistorialMandadosTest {

    private val sampleOrders = listOf(
        MandadoOrder(
            id = 1L,
            folio = "ACA-1001",
            tipo = "DOCUMENTACION",
            titulo = "Envío de llaves a departamento",
            descripcion = "Sobre sellado con llaves de acceso",
            puntoRecogida = "Av. Costera Miguel Alemán #180",
            zonaRecogida = "Costera Miguel Alemán",
            puntoEntrega = "Condominio Costa Azul Torre B #402",
            zonaEntrega = "Costa Azul & Club Deportivo",
            tarifaEnvio = 45.0,
            metodoPago = "EFECTIVO",
            estado = "SOLICITADO",
            clienteNombre = "Ana Karen Valenzuela",
            clienteTelefono = "7445209911"
        ),
        MandadoOrder(
            id = 2L,
            folio = "ACA-1002",
            tipo = "FARMACIA",
            titulo = "Medicamentos urgentes",
            descripcion = "Comprar suero oral y paracetamol",
            puntoRecogida = "Farmacia del Ahorro Magallanes",
            zonaRecogida = "Magallanes & Galerías Acapulco",
            puntoEntrega = "Hotel Fiesta Americana, Costera",
            zonaEntrega = "Costera Miguel Alemán",
            tarifaEnvio = 50.0,
            metodoPago = "TARJETA",
            estado = "EN_CAMINO",
            clienteNombre = "Ana Karen Valenzuela",
            clienteTelefono = "7445209911",
            repartidorNombre = "Carlos Mendoza (Moto)",
            pinSeguridad = "7384"
        ),
        MandadoOrder(
            id = 3L,
            folio = "ACA-1003",
            tipo = "COMPRAS",
            titulo = "Compras en OXXO Condesa",
            descripcion = "2 garrafones de agua y botana",
            puntoRecogida = "OXXO Condesa Costera",
            zonaRecogida = "Costera Miguel Alemán",
            puntoEntrega = "Fracc. Condesa Privada 3",
            zonaEntrega = "Costera Miguel Alemán",
            tarifaEnvio = 40.0,
            metodoPago = "EFECTIVO",
            estado = "ENTREGADO",
            clienteNombre = "Ana Karen Valenzuela",
            clienteTelefono = "7445209911",
            repartidorNombre = "Luis Morales",
            fechaEntrega = System.currentTimeMillis()
        )
    )

    @Test
    fun testClasificacionPorEstados() {
        val pendientes = sampleOrders.filter { it.estado == "SOLICITADO" }
        val enProgreso = sampleOrders.filter {
            it.estado == "ACEPTADO" || it.estado == "ASIGNADO" || it.estado == "RECOGIDO" || it.estado == "EN_CAMINO" || it.estado == "EN_RUTA"
        }
        val entregados = sampleOrders.filter { it.estado == "ENTREGADO" || it.estado == "COMPLETADO" }

        assertEquals(1, pendientes.size)
        assertEquals("ACA-1001", pendientes.first().folio)
        assertEquals("SOLICITADO", pendientes.first().estado)

        assertEquals(1, enProgreso.size)
        assertEquals("ACA-1002", enProgreso.first().folio)
        assertEquals("EN_CAMINO", enProgreso.first().estado)
        assertEquals("7384", enProgreso.first().pinSeguridad)

        assertEquals(1, entregados.size)
        assertEquals("ACA-1003", entregados.first().folio)
        assertEquals("ENTREGADO", entregados.first().estado)
    }

    @Test
    fun testFiltrosEstadoEnvioEnum() {
        val filtroPendientes = EstadoEnvioFiltro.PENDIENTES
        assertEquals("Pendientes", filtroPendientes.label)

        val filtroEnProgreso = EstadoEnvioFiltro.EN_PROGRESO
        assertEquals("En Progreso", filtroEnProgreso.label)

        val filtroEntregados = EstadoEnvioFiltro.ENTREGADOS
        assertEquals("Entregados", filtroEntregados.label)
    }

    @Test
    fun testBusquedaPorFolioYDestino() {
        val queryFolio = "1002"
        val resultadoFolio = sampleOrders.filter { it.folio.contains(queryFolio) }
        assertEquals(1, resultadoFolio.size)
        assertEquals("ACA-1002", resultadoFolio.first().folio)

        val queryLugar = "Fiesta Americana"
        val resultadoLugar = sampleOrders.filter { it.puntoEntrega.contains(queryLugar, ignoreCase = true) }
        assertEquals(1, resultadoLugar.size)
        assertEquals("ACA-1002", resultadoLugar.first().folio)
    }
}
