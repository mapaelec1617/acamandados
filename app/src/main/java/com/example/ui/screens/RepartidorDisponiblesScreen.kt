package com.example.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.ui.MandadosViewModel

/**
 * Pantalla central de Mandados Disponibles y Dashboard para Repartidores en Acapulco.
 * Integra visualización en tiempo real de mandados entrantes con cálculo de distancia,
 * desglose de payout 100% íntegro para el repartidor y botón de aceptación directa.
 */
@Composable
fun RepartidorDisponiblesScreen(
    viewModel: MandadosViewModel,
    modifier: Modifier = Modifier
) {
    RepartidorDashboardScreen(
        viewModel = viewModel,
        modifier = modifier
    )
}
