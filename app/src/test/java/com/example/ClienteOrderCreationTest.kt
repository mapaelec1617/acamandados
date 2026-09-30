package com.example

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.TipoMandado
import com.example.ui.ClienteTab
import com.example.ui.MandadosViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ClienteOrderCreationTest {

    private lateinit var app: Application
    private lateinit var viewModel: MandadosViewModel

    @Before
    fun setup() {
        app = ApplicationProvider.getApplicationContext()
        viewModel = MandadosViewModel(app)
    }

    @Test
    fun testValidationPreventsOrderCreationWithEmptyFields() = runBlocking {
        // Resetear campos
        viewModel.puntoRecogidaInput.value = ""
        viewModel.puntoEntregaInput.value = ""
        viewModel.descripcionInput.value = ""

        // Intento 1: Sin origen
        viewModel.crearNuevoMandado {}
        val msg1 = viewModel.userMessage.value
        assertNotNull(msg1)
        assertTrue(msg1!!.contains("origen") || msg1.contains("recogida"))

        // Intento 2: Con origen pero sin destino
        viewModel.puntoRecogidaInput.value = "Av. Costera Miguel Alemán #180"
        viewModel.crearNuevoMandado {}
        val msg2 = viewModel.userMessage.value
        assertNotNull(msg2)
        assertTrue(msg2!!.contains("destino") || msg2.contains("entrega"))

        // Intento 3: Con origen y destino pero sin descripción
        viewModel.puntoEntregaInput.value = "Condominio Costa Azul Torre B #402"
        viewModel.crearNuevoMandado {}
        val msg3 = viewModel.userMessage.value
        assertNotNull(msg3)
        assertTrue(msg3!!.contains("describe") || msg3.contains("descripción"))
    }

    @Test
    fun testSuccessfulOrderCreationWithOriginDestinationAndDescription() = runBlocking {
        // Configurar campos válidos
        viewModel.selectedTipo.value = TipoMandado.DOCUMENTACION
        viewModel.puntoRecogidaInput.value = "Av. Costera Miguel Alemán #180, Condesa"
        viewModel.puntoEntregaInput.value = "Condominio Torre de Mar #402, Costa Azul"
        viewModel.descripcionInput.value = "Entrega de sobre sellado con llaves de departamento"
        viewModel.clienteNombreInput.value = "Ana Karen Valenzuela"
        viewModel.clienteTelefonoInput.value = "7445209911"
        viewModel.confirmacionDimensionesMoto.value = true

        var createdId: Long = -1
        viewModel.crearNuevoMandado { id ->
            createdId = id
        }

        // Permitir procesamiento asíncrono en Room y ViewModelScope
        org.robolectric.shadows.ShadowLooper.idleMainLooper()
        for (i in 1..25) {
            if (viewModel.recentlyCreatedOrder.value != null) break
            kotlinx.coroutines.delay(50)
            org.robolectric.shadows.ShadowLooper.idleMainLooper()
        }

        val recentOrder = viewModel.recentlyCreatedOrder.value
        assertNotNull(recentOrder)
        assertEquals("Av. Costera Miguel Alemán #180, Condesa", recentOrder!!.puntoRecogida)
        assertEquals("Condominio Torre de Mar #402, Costa Azul", recentOrder.puntoEntrega)
        assertEquals("Entrega de sobre sellado con llaves de departamento", recentOrder.descripcion)
        assertTrue(recentOrder.pinSeguridad.isNotBlank())
        assertTrue(recentOrder.folio.isNotBlank())
    }

    @Test
    fun testQuickCategorySelectionPreFillsService() {
        viewModel.iniciarMandadoPorTipo(TipoMandado.FARMACIA)
        assertEquals(TipoMandado.FARMACIA, viewModel.selectedTipo.value)
        assertEquals(TipoMandado.FARMACIA.label, viewModel.tituloInput.value)
        assertEquals(ClienteTab.SOLICITAR, viewModel.clienteTab.value)

        viewModel.iniciarMandadoPorTipo(TipoMandado.COMPRAS)
        assertEquals(TipoMandado.COMPRAS, viewModel.selectedTipo.value)
        assertEquals(TipoMandado.COMPRAS.label, viewModel.tituloInput.value)
    }
}
