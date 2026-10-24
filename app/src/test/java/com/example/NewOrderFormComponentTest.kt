package com.example

import android.app.Application
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.core.app.ApplicationProvider
import com.example.ui.MandadosViewModel
import com.example.ui.components.NewOrderFormComponent
import com.example.ui.theme.AcaMandadosTheme
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w600dp-h1600dp")
class NewOrderFormComponentTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var app: Application
    private lateinit var viewModel: MandadosViewModel

    @Before
    fun setup() {
        app = ApplicationProvider.getApplicationContext()
        viewModel = MandadosViewModel(app)
    }

    @Test
    fun testFormComponentAllowsInputOfOriginDestinationAndDescription() {
        val originState = mutableStateOf("")
        val destinationState = mutableStateOf("")
        val descriptionState = mutableStateOf("")
        var submitted = false

        composeTestRule.setContent {
            AcaMandadosTheme {
                NewOrderFormComponent(
                    origen = originState.value,
                    onOrigenChange = { originState.value = it },
                    destino = destinationState.value,
                    onDestinoChange = { destinationState.value = it },
                    descripcion = descriptionState.value,
                    onDescripcionChange = { descriptionState.value = it },
                    onSubmit = { submitted = true }
                )
            }
        }

        // Verificar existencia y visibilidad de campos
        composeTestRule.onNodeWithTag("input_order_origin").assertIsDisplayed()
        composeTestRule.onNodeWithTag("input_order_destination").assertIsDisplayed()
        composeTestRule.onNodeWithTag("input_order_description").assertIsDisplayed()
        composeTestRule.onNodeWithTag("btn_submit_new_order").assertIsDisplayed()

        // Ingresar datos en origen
        composeTestRule.onNodeWithTag("input_order_origin")
            .performTextInput("Costera Miguel Alemán #180, Condesa")
        assertEquals("Costera Miguel Alemán #180, Condesa", originState.value)

        // Ingresar datos en destino
        composeTestRule.onNodeWithTag("input_order_destination")
            .performTextInput("Condominio Torre de Mar #402, Costa Azul")
        assertEquals("Condominio Torre de Mar #402, Costa Azul", destinationState.value)

        // Ingresar datos en descripción
        composeTestRule.onNodeWithTag("input_order_description")
            .performTextInput("Entrega de documentos sellados y llaves")
        assertEquals("Entrega de documentos sellados y llaves", descriptionState.value)

        // Pulsar botón de enviar
        composeTestRule.onNodeWithTag("btn_submit_new_order").performClick()
        assertTrue("Submit callback must be called", submitted)
    }

    @Test
    fun testSuggestionChipsPreFillOriginDestinationAndDescription() {
        val originState = mutableStateOf("")
        val destinationState = mutableStateOf("")
        val descriptionState = mutableStateOf("")

        composeTestRule.setContent {
            AcaMandadosTheme {
                NewOrderFormComponent(
                    origen = originState.value,
                    onOrigenChange = { originState.value = it },
                    destino = destinationState.value,
                    onDestinoChange = { destinationState.value = it },
                    descripcion = descriptionState.value,
                    onDescripcionChange = { descriptionState.value = it }
                )
            }
        }

        // Tocar chip de origen rápido
        composeTestRule.onNodeWithText("🏖️ Costera #180").performClick()
        assertTrue("Origin should be pre-filled", originState.value.contains("Costera"))

        // Tocar chip de destino rápido
        composeTestRule.onNodeWithText("🏢 Costa Azul").performClick()
        assertTrue("Destination should be pre-filled", destinationState.value.contains("Costa Azul"))

        // Tocar chip de descripción rápida
        composeTestRule.onNodeWithText("📄 Documentos / Llaves").performClick()
        assertTrue("Description should be pre-filled with preset", descriptionState.value.contains("documentos"))
    }

    @Test
    fun testValidationErrorsDisplayProperlyWhenFieldsAreEmpty() {
        composeTestRule.setContent {
            AcaMandadosTheme {
                NewOrderFormComponent(
                    origen = "",
                    onOrigenChange = {},
                    destino = "",
                    onDestinoChange = {},
                    descripcion = "",
                    onDescripcionChange = {},
                    origenError = "⚠️ La dirección de origen es obligatoria",
                    destinoError = "⚠️ La dirección de destino es obligatoria",
                    descripcionError = "⚠️ La descripción del mandado es obligatoria"
                )
            }
        }

        composeTestRule.onNodeWithText("⚠️ La dirección de origen es obligatoria").assertIsDisplayed()
        composeTestRule.onNodeWithText("⚠️ La dirección de destino es obligatoria").assertIsDisplayed()
        composeTestRule.onNodeWithText("⚠️ La descripción del mandado es obligatoria").assertIsDisplayed()
    }

    @Test
    fun testViewModelBackedFormBindsStateCorrectly() {
        viewModel.puntoRecogidaInput.value = "OXXO Condesa Costera"
        viewModel.puntoEntregaInput.value = "Fracc. Magallanes #20"
        viewModel.descripcionInput.value = "Comprar café y víveres"

        composeTestRule.setContent {
            AcaMandadosTheme {
                NewOrderFormComponent(viewModel = viewModel)
            }
        }

        composeTestRule.onNodeWithText("OXXO Condesa Costera").assertIsDisplayed()
        composeTestRule.onNodeWithText("Fracc. Magallanes #20").assertIsDisplayed()
        composeTestRule.onNodeWithText("Comprar café y víveres").assertIsDisplayed()
    }
}
