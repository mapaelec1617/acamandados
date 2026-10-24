package com.example

import android.app.Application
import android.os.Build
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.test.core.app.ApplicationProvider
import com.example.ui.DarkThemeConfig
import com.example.ui.MandadosViewModel
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class DynamicColorsAndThemeTest {

    private lateinit var app: Application
    private lateinit var viewModel: MandadosViewModel

    @Before
    fun setup() {
        app = ApplicationProvider.getApplicationContext()
        viewModel = MandadosViewModel(app)
    }

    @Test
    fun testDynamicColorsConfigurationDefaultsAndToggles() {
        // Por defecto, Dynamic Colors debe estar activo para cumplir con Material Design 3
        assertTrue(viewModel.dynamicColorConfig.value)

        // Desactivar manualmente
        viewModel.setDynamicColorConfig(false)
        assertFalse(viewModel.dynamicColorConfig.value)

        // Alternar (toggle)
        viewModel.toggleDynamicColor()
        assertTrue(viewModel.dynamicColorConfig.value)

        viewModel.toggleDynamicColor()
        assertFalse(viewModel.dynamicColorConfig.value)
    }

    @Test
    fun testDarkThemeConfigCycleAndLabels() {
        // Inicialmente el modo de tema es SYSTEM
        assertEquals(DarkThemeConfig.SYSTEM, viewModel.darkThemeConfig.value)
        assertEquals("Automático (Sistema)", DarkThemeConfig.SYSTEM.label)
        assertEquals("Modo Claro", DarkThemeConfig.LIGHT.label)
        assertEquals("Modo Oscuro", DarkThemeConfig.DARK.label)

        // Ciclo del conmutador rápido: SYSTEM -> LIGHT -> DARK -> SYSTEM
        viewModel.toggleDarkTheme()
        assertEquals(DarkThemeConfig.LIGHT, viewModel.darkThemeConfig.value)

        viewModel.toggleDarkTheme()
        assertEquals(DarkThemeConfig.DARK, viewModel.darkThemeConfig.value)

        viewModel.toggleDarkTheme()
        assertEquals(DarkThemeConfig.SYSTEM, viewModel.darkThemeConfig.value)

        // Asignación directa
        viewModel.setDarkThemeConfig(DarkThemeConfig.DARK)
        assertEquals(DarkThemeConfig.DARK, viewModel.darkThemeConfig.value)
    }

    @Test
    fun testDynamicColorSchemeApiAvailabilityOnModernAndroid() {
        // En SDK 36 (>= Android S / API 31), dynamicLightColorScheme y dynamicDarkColorScheme deben estar disponibles
        assertTrue(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)

        val dynLight = dynamicLightColorScheme(app)
        assertNotNull(dynLight)
        assertNotNull(dynLight.primary)
        assertNotNull(dynLight.surface)
        assertNotNull(dynLight.background)

        val dynDark = dynamicDarkColorScheme(app)
        assertNotNull(dynDark)
        assertNotNull(dynDark.primary)
        assertNotNull(dynDark.surface)
        assertNotNull(dynDark.background)
    }
}
