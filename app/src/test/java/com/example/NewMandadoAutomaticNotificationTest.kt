package com.example

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.NotificationItem
import com.example.ui.AppMode
import com.example.ui.MandadosViewModel
import com.example.ui.RepartidorTab
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowLooper

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class NewMandadoAutomaticNotificationTest {

    private lateinit var app: Application
    private lateinit var viewModel: MandadosViewModel

    @Before
    fun setup() {
        app = ApplicationProvider.getApplicationContext()
        viewModel = MandadosViewModel(app)
    }

    @Test
    fun testOrderCreationAutomaticallyCreatesDriverNotificationInDb() = runBlocking {
        // Iniciar colección activa para que WhileSubscribed active la consulta de Room
        val collectJob = launch { viewModel.allNotifications.collect {} }

        viewModel.tituloInput.value = "Documentos Notaría 3"
        viewModel.descripcionInput.value = "Entrega de escrituras en sobre sellado"
        viewModel.puntoRecogidaInput.value = "Notaría 3, Costera Miguel Alemán #50"
        viewModel.zonaRecogidaInput.value = "Costera Miguel Alemán"
        viewModel.puntoEntregaInput.value = "Banco Banorte, Zócalo Acapulco"
        viewModel.zonaEntregaInput.value = "Zócalo & Centro Histórico"
        viewModel.clienteNombreInput.value = "Lic. Eduardo Garza"
        viewModel.clienteTelefonoInput.value = "7449876543"
        viewModel.confirmacionDimensionesMoto.value = true

        var createdOrderId: Long? = null
        viewModel.crearNuevoMandado { orderId ->
            createdOrderId = orderId
        }

        // Permitir ejecución del coroutine en viewModelScope
        ShadowLooper.idleMainLooper()
        for (i in 1..25) {
            if (createdOrderId != null) break
            delay(50)
            ShadowLooper.idleMainLooper()
        }

        assertNotNull("Order ID should not be null", createdOrderId)

        // Verificar notificación automática para repartidores esperando la emisión del Flow
        var driverNotification: NotificationItem? = null
        for (i in 1..30) {
            ShadowLooper.idleMainLooper()
            val list = viewModel.allNotifications.value
            driverNotification = list.find {
                it.paraRol == "REPARTIDOR" && it.tipo == "NUEVA_SOLICITUD" && it.orderId == createdOrderId
            }
            if (driverNotification != null) break
            delay(50)
        }

        collectJob.cancel()

        assertNotNull("System must automatically create a notification for drivers", driverNotification)
        assertTrue("Notification title must mention pickup zone", driverNotification!!.titulo.contains("Costera"))
        assertTrue("Notification message must contain destination", driverNotification.mensaje.contains("Zócalo") || driverNotification.mensaje.contains("Centro"))
        assertNotNull("Notification must contain approximate distance", driverNotification.distanciaAprox)
    }

    @Test
    fun testOrderCreationSetsHeadsUpBannerAlertForDrivers() = runBlocking {
        viewModel.tituloInput.value = "Almuerzo Pescado a la Talla"
        viewModel.descripcionInput.value = "2 órdenes de pescado con tortillas y frijoles"
        viewModel.puntoRecogidaInput.value = "Restaurante Beto Godoy, Barra Vieja"
        viewModel.zonaRecogidaInput.value = "Barra Vieja"
        viewModel.puntoEntregaInput.value = "Hotel Princess Mundo Imperial"
        viewModel.zonaEntregaInput.value = "Diamante & Boulevard de las Naciones"
        viewModel.confirmacionDimensionesMoto.value = true

        var createdOrderId: Long? = null
        viewModel.crearNuevoMandado { orderId ->
            createdOrderId = orderId
        }

        ShadowLooper.idleMainLooper()
        for (i in 1..25) {
            if (createdOrderId != null) break
            delay(50)
            ShadowLooper.idleMainLooper()
        }

        assertNotNull("Order ID should not be null", createdOrderId)

        val alert = viewModel.newMandadoAlert.value
        assertNotNull("Heads-up alert banner should be populated for drivers", alert)
        assertEquals(createdOrderId, alert?.orderId)
        assertEquals("Barra Vieja", alert?.zonaRecogida)
        assertEquals("Diamante & Boulevard de las Naciones", alert?.zonaEntrega)
        assertTrue("Fee must be greater than zero", (alert?.tarifaEnvio ?: 0.0) > 0.0)

        // Descartar alerta
        viewModel.dismissNewMandadoAlert()
        assertNull("Alert must be dismissed", viewModel.newMandadoAlert.value)
    }

    @Test
    fun testAcceptingFromAlertSwitchesToRepartidorMode() = runBlocking {
        viewModel.newMandadoAlert.value = com.example.data.model.NewMandadoDriverAlert(
            orderId = 999L,
            folio = "ACA-9999",
            titulo = "Entrega Express Test",
            zonaRecogida = "Costera",
            zonaEntrega = "Diamante",
            tarifaEnvio = 80.0,
            metodoPago = "EFECTIVO",
            tamanoPaquete = "CHICO"
        )

        viewModel.switchMode(AppMode.CLIENTE)
        assertEquals(AppMode.CLIENTE, viewModel.currentMode.value)

        viewModel.acceptOrViewNewMandadoFromAlert(999L)

        // Debe cambiar automáticamente al modo repartidor en la pestaña de disponibles
        assertEquals(AppMode.REPARTIDOR, viewModel.currentMode.value)
        assertEquals(RepartidorTab.DISPONIBLES, viewModel.repartidorTab.value)
        assertNull("Banner alert should be cleared after acceptance/view", viewModel.newMandadoAlert.value)
    }

    @Test
    fun testSimulateOrderCreationTriggersDriverNotificationAndAlert() = runBlocking {
        val collectJob = launch { viewModel.allNotifications.collect {} }

        viewModel.simularNuevoMandadoCliente()

        ShadowLooper.idleMainLooper()
        for (i in 1..25) {
            if (viewModel.newMandadoAlert.value != null) break
            delay(50)
            ShadowLooper.idleMainLooper()
        }

        val alert = viewModel.newMandadoAlert.value
        assertNotNull("Simulating a new order should generate a heads-up alert", alert)
        assertTrue("Alert title should mention urgent express shipment", alert!!.titulo.contains("Express"))

        var latestNotif: NotificationItem? = null
        for (i in 1..30) {
            ShadowLooper.idleMainLooper()
            val list = viewModel.allNotifications.value
            latestNotif = list.find { it.paraRol == "REPARTIDOR" && it.tipo == "NUEVA_SOLICITUD" && it.orderId == alert.orderId }
            if (latestNotif != null) break
            delay(50)
        }

        collectJob.cancel()

        assertNotNull("Driver notification must exist in database", latestNotif)
    }
}
