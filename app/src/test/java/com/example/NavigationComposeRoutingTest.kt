package com.example

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import com.example.ui.AppMode
import com.example.ui.ClienteTab
import com.example.ui.MandadosViewModel
import com.example.ui.RepartidorTab
import com.example.data.model.TipoMandado
import com.example.ui.navigation.AcaDestinations
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class NavigationComposeRoutingTest {

    private lateinit var app: Application
    private lateinit var viewModel: MandadosViewModel

    @Before
    fun setup() {
        app = ApplicationProvider.getApplicationContext()
        viewModel = MandadosViewModel(app)
    }

    @Test
    fun testNavigationDestinationsAreUniqueAndValid() {
        val routes = listOf(
            AcaDestinations.INICIO,
            AcaDestinations.LOGIN,
            AcaDestinations.REGISTRO_REPARTIDOR,
            AcaDestinations.PRIVACY_POLICY,
            AcaDestinations.DASHBOARD,
            AcaDestinations.HISTORIAL_ORDENES,
            AcaDestinations.EDITAR_PERFIL
        )

        // Verificar que no haya rutas duplicadas
        assertEquals(routes.size, routes.distinct().size)

        // Verificar que todas tengan nombres semánticos válidos
        routes.forEach { route ->
            assertTrue(route.isNotBlank())
            assertFalse(route.contains(" "))
        }
    }

    @Test
    fun testScreenStatePreservationDuringNavigationTransitions() = runBlocking {
        // Simular que el cliente ingresa datos en un formulario de mandado
        viewModel.puntoRecogidaInput.value = "Costera Miguel Alemán 125, Acapulco"
        viewModel.puntoEntregaInput.value = "Fracc. Las Brisas, Casa 4"
        viewModel.costoProductosInput.value = "250.00"
        viewModel.descripcionInput.value = "Medicamentos de farmacia urgente"

        assertEquals("Costera Miguel Alemán 125, Acapulco", viewModel.puntoRecogidaInput.value)

        // Abrir pantalla de políticas de privacidad
        viewModel.openPrivacyPolicyScreen()
        assertTrue(viewModel.showPrivacyPolicyScreen.value)

        // Volver atrás desde políticas de privacidad
        viewModel.closePrivacyPolicyScreen()
        assertFalse(viewModel.showPrivacyPolicyScreen.value)

        // Comprobar que los datos del formulario NO se perdieron al navegar y regresar
        assertEquals("Costera Miguel Alemán 125, Acapulco", viewModel.puntoRecogidaInput.value)
        assertEquals("Fracc. Las Brisas, Casa 4", viewModel.puntoEntregaInput.value)
        assertEquals("250.00", viewModel.costoProductosInput.value)
        assertEquals("Medicamentos de farmacia urgente", viewModel.descripcionInput.value)
    }

    @Test
    fun testDashboardTabBackStackHierarchy() {
        // En modo cliente: navegar a pestaña secundaria SOLICITAR
        viewModel.switchMode(AppMode.CLIENTE)
        viewModel.setClienteTab(ClienteTab.SOLICITAR)
        assertEquals(ClienteTab.SOLICITAR, viewModel.clienteTab.value)

        // El BackHandler simula regresar a la pestaña raíz (INICIO)
        viewModel.setClienteTab(ClienteTab.INICIO)
        assertEquals(ClienteTab.INICIO, viewModel.clienteTab.value)

        // En modo repartidor: navegar a BILLETERA
        viewModel.switchMode(AppMode.REPARTIDOR)
        viewModel.setRepartidorTab(RepartidorTab.BILLETERA_SUSCRIPCION)
        assertEquals(RepartidorTab.BILLETERA_SUSCRIPCION, viewModel.repartidorTab.value)

        // El BackHandler simula regresar a la pestaña raíz (DISPONIBLES)
        viewModel.setRepartidorTab(RepartidorTab.DISPONIBLES)
        assertEquals(RepartidorTab.DISPONIBLES, viewModel.repartidorTab.value)
    }

    @Test
    fun testSubscreensOpenAndCloseTransitions() {
        // Registro de repartidor
        viewModel.openRegistroRepartidorScreen()
        assertTrue(viewModel.showRegistroRepartidorScreen.value)
        viewModel.closeRegistroRepartidorScreen()
        assertFalse(viewModel.showRegistroRepartidorScreen.value)

        // Historial de mandados
        viewModel.openCompletedOrdersScreen()
        assertTrue(viewModel.showCompletedOrdersScreen.value)
        viewModel.closeCompletedOrdersScreen()
        assertFalse(viewModel.showCompletedOrdersScreen.value)

        // Editar perfil
        viewModel.openEditProfile()
        assertTrue(viewModel.showEditProfileScreen.value)
        viewModel.closeEditProfile()
        assertFalse(viewModel.showEditProfileScreen.value)
    }
}
