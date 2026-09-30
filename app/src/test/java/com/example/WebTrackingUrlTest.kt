package com.example

import com.example.data.db.AppDatabase
import com.example.data.model.MandadoOrder
import com.example.data.repository.FirestoreRepository
import com.example.data.repository.MandadosRepository
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import androidx.room.Room

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class WebTrackingUrlTest {

    private lateinit var database: AppDatabase
    private lateinit var repository: MandadosRepository
    private lateinit var firestoreRepository: FirestoreRepository

    @Before
    fun setup() {
        val context = RuntimeEnvironment.getApplication()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        firestoreRepository = FirestoreRepository(context = context)
        repository = MandadosRepository(
            mandadoDao = database.mandadoDao(),
            repartidorDao = database.repartidorDao(),
            calificacionDao = database.calificacionDao(),
            notificationDao = database.notificationDao(),
            firestoreRepository = firestoreRepository
        )
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun testUniqueUrlGenerationStructureAndToken() {
        val order = MandadoOrder(
            id = 101L,
            folio = "ACA-9081",
            tipo = "EXPRESS",
            titulo = "Documentos Urgentes",
            descripcion = "Contrato notarial",
            puntoRecogida = "Costera Miguel Alemán #100",
            zonaRecogida = "Zona Costera & Condesa",
            puntoEntrega = "Fracc. Las Brisas #12",
            zonaEntrega = "Zona Tradicional / Caleta",
            tarifaEnvio = 70.0,
            metodoPago = "EFECTIVO",
            estado = "EN_CAMINO",
            clienteNombre = "Carlos Mendoza",
            clienteTelefono = "7441112233",
            destinatarioNombre = "Roberto Gómez",
            destinatarioTelefono = "7449876543",
            pinSeguridad = "8492",
            trackingToken = "trk_abc123xyz"
        )

        val webUrl = order.getWebTrackingUrl()
        assertTrue("La URL debe iniciar con el dominio de rastreo web", webUrl.startsWith("https://track.acamandados.com/order/ACA-9081"))
        assertTrue("La URL debe contener el token único de seguridad", webUrl.contains("token=trk_abc123xyz"))

        val shortUrl = order.getShortWebTrackingUrl()
        assertEquals("https://acamandados.com/t/ACA-9081", shortUrl)
    }

    @Test
    fun testDistinctOrdersGenerateUniqueUrlsAndTokens() = runBlocking {
        val orderId1 = repository.crearMandado(
            tipo = "EXPRESS",
            titulo = "Pedido 1",
            descripcion = "Detalle 1",
            puntoRecogida = "A",
            zonaRecogida = "Costera",
            puntoEntrega = "B",
            zonaEntrega = "Condesa",
            costoEstimadoProductos = 0.0,
            tarifaEnvio = 50.0,
            metodoPago = "EFECTIVO",
            clienteNombre = "Ana",
            clienteTelefono = "7441111111",
            remitenteNombre = "Ana",
            remitenteTelefono = "7441111111",
            destinatarioNombre = "Beto",
            destinatarioTelefono = "7442222222",
            referenciasDomicilio = "Portón verde",
            tamanoPaquete = "CHICO",
            confirmacionDimensionesMoto = true
        )

        val orderId2 = repository.crearMandado(
            tipo = "FARMACIA",
            titulo = "Pedido 2",
            descripcion = "Detalle 2",
            puntoRecogida = "C",
            zonaRecogida = "Diamante",
            puntoEntrega = "D",
            zonaEntrega = "Costa Azul",
            costoEstimadoProductos = 120.0,
            tarifaEnvio = 65.0,
            metodoPago = "TARJETA",
            clienteNombre = "Carlos",
            clienteTelefono = "7443333333",
            remitenteNombre = "Carlos",
            remitenteTelefono = "7443333333",
            destinatarioNombre = "Diana",
            destinatarioTelefono = "7444444444",
            referenciasDomicilio = "Edificio azul",
            tamanoPaquete = "MEDIANO",
            confirmacionDimensionesMoto = true
        )

        val order1 = repository.getOrderById(orderId1)
        val order2 = repository.getOrderById(orderId2)

        assertNotNull(order1)
        assertNotNull(order2)

        assertNotEquals(order1?.folio, order2?.folio)
        assertNotEquals(order1?.trackingToken, order2?.trackingToken)
        assertNotEquals(order1?.getWebTrackingUrl(), order2?.getWebTrackingUrl())

        assertTrue(order1!!.trackingToken.startsWith("trk_"))
        assertTrue(order2!!.trackingToken.startsWith("trk_"))
    }

    @Test
    fun testShareableTrackingMessageFormat() {
        val order = MandadoOrder(
            id = 505L,
            folio = "ACA-5544",
            tipo = "COMIDA",
            titulo = "Ceviche Acapulqueño",
            descripcion = "Mariscos El Morro",
            puntoRecogida = "Costera #40",
            zonaRecogida = "Costera",
            puntoEntrega = "Costa Azul #8",
            zonaEntrega = "Costa Azul",
            tarifaEnvio = 55.0,
            metodoPago = "TARJETA",
            estado = "EN_CAMINO",
            clienteNombre = "Patricia Luna",
            clienteTelefono = "7445556677",
            destinatarioNombre = "Fernando Díaz",
            destinatarioTelefono = "7448889900",
            pinSeguridad = "5678",
            trackingToken = "trk_ceviche44"
        )

        val shareMessage = order.getShareableTrackingMessage()

        assertTrue(shareMessage.contains("Fernando Díaz"))
        assertTrue(shareMessage.contains("https://track.acamandados.com/order/ACA-5544?token=trk_ceviche44"))
        assertTrue(shareMessage.contains("5678"))
        assertTrue(shareMessage.contains("sin descargar app"))
    }

    @Test
    fun testFirestoreRepositoryPersistsTrackingTokenAndUrl() {
        val order = MandadoOrder(
            id = 707L,
            folio = "ACA-7070",
            tipo = "DOCUMENTACION",
            titulo = "Póliza de Seguro",
            descripcion = "Entregar en oficinas",
            puntoRecogida = "Centro",
            zonaRecogida = "Centro",
            puntoEntrega = "Diamante",
            zonaEntrega = "Diamante",
            tarifaEnvio = 85.0,
            metodoPago = "EFECTIVO",
            estado = "SOLICITADO",
            clienteNombre = "Jorge Morales",
            clienteTelefono = "7447778899",
            destinatarioNombre = "Lic. Salgado",
            destinatarioTelefono = "7441113355",
            pinSeguridad = "9012",
            trackingToken = "trk_poliza_7070"
        )

        val map = FirestoreRepository.orderToMap(order)
        assertEquals("trk_poliza_7070", map["trackingToken"])
        assertEquals("https://track.acamandados.com/order/ACA-7070?token=trk_poliza_7070", map["webTrackingUrl"])
    }
}
