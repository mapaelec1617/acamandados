package com.example

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.DarkThemeConfig
import com.example.ui.MainAppScreen
import com.example.ui.MandadosViewModel
import com.example.ui.theme.AcaMandadosTheme

class MainActivity : ComponentActivity() {
    private val viewModel: MandadosViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        handleIncomingIntent(intent)

        setContent {
            val darkThemeConfig by viewModel.darkThemeConfig.collectAsStateWithLifecycle()
            val dynamicColorConfig by viewModel.dynamicColorConfig.collectAsStateWithLifecycle()
            val isDark = when (darkThemeConfig) {
                DarkThemeConfig.SYSTEM -> isSystemInDarkTheme()
                DarkThemeConfig.LIGHT -> false
                DarkThemeConfig.DARK -> true
            }

            AcaMandadosTheme(darkTheme = isDark, dynamicColor = dynamicColorConfig) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainAppScreen(viewModel = viewModel)
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIncomingIntent(intent)
    }

    private fun handleIncomingIntent(intent: Intent?) {
        if (intent == null) return
        val uri = intent.data
        if (uri != null) {
            viewModel.handleIncomingWebTrackingUrl(uri.toString())
        }
        val target = intent.getStringExtra("EXTRA_NAVIGATE_TO")
        if (target == "REPARTIDOR_DISPONIBLES") {
            viewModel.switchMode(com.example.ui.AppMode.REPARTIDOR)
            viewModel.setRepartidorTab(com.example.ui.RepartidorTab.DISPONIBLES)
        } else if (target == "CLIENTE_MIS_MANDADOS") {
            viewModel.switchMode(com.example.ui.AppMode.CLIENTE)
            viewModel.setClienteTab(com.example.ui.ClienteTab.MIS_MANDADOS)
        }
    }
}
