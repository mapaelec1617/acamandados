package com.example

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import com.example.data.db.AppDatabase
import com.example.data.repository.MandadosRepository
import com.example.ui.RegistroRepartidorViewModel
import kotlinx.coroutines.flow.first
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
class RegistroRepartidorYSeguridadTest {

    private lateinit var application: Application
    private lateinit var database: AppDatabase
    private lateinit var repository: MandadosRepository
    private lateinit var registroViewModel: RegistroRepartidorViewModel

    @Before
    fun setup() {
        application = ApplicationProvider.getApplicationContext()
        database = AppDatabase.getDatabase(application, kotlinx.coroutines.GlobalScope)
        repository = MandadosRepository(
            mandadoDao = database.mandadoDao(),
            repartidorDao = database.repartidorDao(),
            calificacionDao = database.calificacionDao(),
            notificationDao = database.notificationDao()
        )
        registroViewModel = RegistroRepartidorViewModel(application)
    }

    @Test
    fun testRegistroRepartidorValidationRules() {
        // Inicialmente el formulario está vacío y no debe validar
        val errorInicial = registroViewModel.validarFormulario()
        assertNotNull(errorInicial)
        assertTrue(errorInicial!!.contains("nombre", ignoreCase = true))

        // Nombre válido
        registroViewModel.updateNombre("José Manuel Pineda")
        val errorTel = registroViewModel.validarFormulario()
        assertNotNull(errorTel)
        assertTrue(errorTel!!.contains("teléfono", ignoreCase = true))

        // Teléfono con caracteres especiales debe filtrarse solo a dígitos y validar 10 dígitos
        registroViewModel.updateTelefono("(744) 987-6543")
        assertEquals("7449876543", registroViewModel.formState.value.telefono)
        assertEquals(10, registroViewModel.formState.value.telefono.length)

        // Modelo de vehículo
        val errorModelo = registroViewModel.validarFormulario()
        assertNotNull(errorModelo)
        assertTrue(errorModelo!!.contains("vehículo", ignoreCase = true))

        registroViewModel.updateTipoTransporte("MOTO")
        registroViewModel.updateModeloVehiculo("Italika 150Z")

        // Placas obligatorias si es moto
        val errorPlacas = registroViewModel.validarFormulario()
        assertNotNull(errorPlacas)
        assertTrue(errorPlacas!!.contains("placas", ignoreCase = true))

        registroViewModel.updatePlacas("gro-772b")
        assertEquals("GRO-772B", registroViewModel.formState.value.placas)

        // Términos obligatorios
        val errorTerminos = registroViewModel.validarFormulario()
        assertNotNull(errorTerminos)
        assertTrue(errorTerminos!!.contains("términos", ignoreCase = true) || errorTerminos.contains("seguridad", ignoreCase = true))

        registroViewModel.updateTerminosAceptados(true)
        val validado = registroViewModel.validarFormulario()
        assertNull(validado)
    }

    @Test
    fun testBicicletaDoesNotRequirePlacas() {
        registroViewModel.updateNombre("Luis García")
        registroViewModel.updateTelefono("7441122334")
        registroViewModel.updateTipoTransporte("BICI")
        registroViewModel.updateModeloVehiculo("Bicicleta Benotto Montaña")
        registroViewModel.updateTerminosAceptados(true)

        val validado = registroViewModel.validarFormulario()
        assertNull("Bicicleta no debe requerir placas de circulación", validado)
    }

    @Test
    fun testRegistroRepartidorSuccessfulSubmission() = runBlocking {
        registroViewModel.updateNombre("Antonio Solís")
        registroViewModel.updateTelefono("7445566778")
        registroViewModel.updateTipoTransporte("MOTONETA")
        registroViewModel.updateModeloVehiculo("Honda Dio 110")
        registroViewModel.updatePlacas("GRO-123K")
        registroViewModel.updateZonaPreferida("Zona Diamante")
        registroViewModel.updateTerminosAceptados(true)

        var successCalled = false
        registroViewModel.registrarRepartidor { profile ->
            successCalled = true
            assertEquals("Antonio Solís", profile.nombre)
            assertEquals("7445566778", profile.telefono)
            assertEquals("MOTONETA", profile.tipoTransporte)
            assertEquals("GRO-123K", profile.placas)
        }

        var attempts = 0
        while (!successCalled && attempts < 20) {
            ShadowLooper.idleMainLooper()
            kotlinx.coroutines.delay(50)
            attempts++
        }

        assertTrue(successCalled)
        assertTrue(registroViewModel.formState.value.isSuccess)

        // Verificar persistencia en Room DAO
        val savedProfile = database.repartidorDao().getProfileFlow().first()
        assertNotNull(savedProfile)
        assertEquals("Antonio Solís", savedProfile!!.nombre)
        assertEquals("MOTONETA", savedProfile.tipoTransporte)
    }

    @Test
    fun testPinSecurityBruteForceProtectionAndConstantTime() = runBlocking {
        val orderId = repository.crearMandado(
            tipo = "DOCUMENTACION",
            titulo = "Envío de Documentos",
            descripcion = "Documentos notariales Costera",
            puntoRecogida = "Costera 125",
            zonaRecogida = "Costera Miguel Alemán",
            puntoEntrega = "Farallón 88",
            zonaEntrega = "Costera Miguel Alemán",
            costoEstimadoProductos = 0.0,
            tarifaEnvio = 45.0,
            metodoPago = "EFECTIVO",
            clienteNombre = "Carlos Vega",
            clienteTelefono = "7449988776"
        )

        val order = database.mandadoDao().getOrderById(orderId)
        assertNotNull(order)
        val realPin = order!!.pinSeguridad

        // Verificar que el PIN generado tiene exactamente 4 dígitos numéricos
        assertEquals(4, realPin.length)
        assertTrue(realPin.all { it.isDigit() })

        // Intentos incorrectos sucesivos para verificar protección contra fuerza bruta
        assertFalse(repository.validarPinYFinalizarEntrega(orderId, "0000", null))
        assertFalse(repository.validarPinYFinalizarEntrega(orderId, "1111", null))
        assertFalse(repository.validarPinYFinalizarEntrega(orderId, "2222", null))
        assertFalse(repository.validarPinYFinalizarEntrega(orderId, "3333", null))
        assertFalse(repository.validarPinYFinalizarEntrega(orderId, "4444", null))

        // Al sexto intento, incluso si envía el PIN correcto, debe estar bloqueado por fuerza bruta
        val blockedResult = repository.validarPinYFinalizarEntrega(orderId, realPin, null)
        assertFalse("El PIN debe bloquearse tras 5 intentos fallidos para mitigar ataques de fuerza bruta", blockedResult)
    }
}
