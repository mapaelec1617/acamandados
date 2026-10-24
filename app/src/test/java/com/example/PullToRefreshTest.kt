package com.example

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import com.example.ui.MandadosViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowLooper

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class PullToRefreshTest {

    private lateinit var app: Application
    private lateinit var viewModel: MandadosViewModel

    @Before
    fun setup() {
        app = ApplicationProvider.getApplicationContext()
        viewModel = MandadosViewModel(app)
    }

    @Test
    fun testPullToRefreshInitialStateAndTrigger() {
        // Inicialmente el estado de refresco debe ser falso
        assertFalse(viewModel.isRefreshing.value)

        // Al ejecutar refreshData se activa el estado de actualización
        viewModel.refreshData()
        ShadowLooper.idleMainLooper()
        assertTrue(viewModel.isRefreshing.value)

        // Tras completar el retraso de simulación, vuelve a false y notifica al usuario
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks()
        assertFalse(viewModel.isRefreshing.value)
        assertNotNull(viewModel.userMessage.value)
    }

    @Test
    fun testPullToRefreshPreservesOrderListIntegrity() {
        val initialOrderCount = viewModel.allOrders.value.size

        viewModel.refreshData()
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks()

        // La lista de órdenes permanece accesible y completa tras el refresco
        val postRefreshCount = viewModel.allOrders.value.size
        assertEquals(initialOrderCount, postRefreshCount)
    }
}
