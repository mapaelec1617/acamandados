package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.MandadoOrder
import com.example.data.model.TipoMandado
import com.example.data.model.TransaccionBilletera
import com.example.ui.MandadosViewModel
import com.example.ui.components.ComprobantePagoDialog
import com.example.ui.components.DetalleTransaccionDialog
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Pantalla de Billetera y Liquidación de Pagos para el Repartidor.
 * Permite:
 *  1. Procesar los pagos de mandados completados (dispersión SPEI bancaria inmediata o cobro en efectivo).
 *  2. Dispersar en lote todas las ganancias de tarjeta pendientes a la cuenta CLABE (SPEI mismo día).
 *  3. Ver un historial detallado de transacciones (ingresos por mandados completados y retiros realizados).
 *  4. Solicitar retiros SPEI inmediatos hacia la cuenta bancaria CLABE registrada.
 *  5. Ver y compartir comprobantes digitales de pago con clave de rastreo bancaria STP.
 *  6. Monitorear suscripción semanal activa ($150 MXN) y balance general.
 */
@Composable
fun RepartidorBilleteraScreen(
    viewModel: MandadosViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val profile by viewModel.repartidorProfile.collectAsStateWithLifecycle()
    val depositos by viewModel.depositosLog.collectAsStateWithLifecycle()
    val completedOrders by viewModel.driverCompletedOrders.collectAsStateWithLifecycle()
    val activeReceiptOrder by viewModel.activeReceiptOrder.collectAsStateWithLifecycle()
    val transacciones by viewModel.transaccionesBilletera.collectAsStateWithLifecycle()
    val activeSelectedTransaccion by viewModel.activeSelectedTransaccion.collectAsStateWithLifecycle()

    // Clasificación de pagos de mandados completados
    val pendingPayoutOrders = remember(completedOrders) {
        completedOrders.filter { !it.pagoDepositadoAlRepartidor }
    }
    val processedOrders = remember(completedOrders) {
        completedOrders.filter { it.pagoDepositadoAlRepartidor }
    }

    val totalPendienteDispersar = remember(pendingPayoutOrders) {
        pendingPayoutOrders.filter { it.metodoPago == "TARJETA" }.sumOf { it.tarifaEnvio + it.costoProductosEstimado }
    }
    val totalEfectivo = remember(completedOrders) {
        completedOrders.filter { it.metodoPago == "EFECTIVO" }.sumOf { it.tarifaEnvio + it.costoProductosEstimado }
    }
    val totalDispersadoSpei = remember(depositos) {
        depositos.sumOf { it.monto }
    }

    // Totales de movimientos en el historial de la billetera
    val totalIngresosMandados = remember(transacciones) {
        transacciones.filter { it.isIngreso }.sumOf { it.monto }
    }
    val totalRetirosRealizados = remember(transacciones) {
        transacciones.filter { it.tipo == "RETIRO" }.sumOf { it.monto }
    }
    val totalSuscripciones = remember(transacciones) {
        transacciones.filter { it.tipo == "SUSCRIPCION" }.sumOf { it.monto }
    }
    val saldoDisponibleEnBilletera = remember(totalIngresosMandados, totalRetirosRealizados, totalSuscripciones) {
        maxOf(0.0, totalIngresosMandados - totalRetirosRealizados - totalSuscripciones)
    }

    val dateFormatter = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("es", "MX")) }
    val dateOnlyFormatter = remember { SimpleDateFormat("EEEE dd MMMM", Locale("es", "MX")) }

    var showRenovarDialog by remember { mutableStateOf(false) }
    var orderToProcess by remember { mutableStateOf<MandadoOrder?>(null) }
    var showBatchProcessDialog by remember { mutableStateOf(false) }
    var showSolicitarRetiroDialog by remember { mutableStateOf(false) }
    var paymentFilter by remember { mutableStateOf("TODOS") } // "TODOS", "PENDIENTES", "DISPERSADOS", "EFECTIVO"
    var transaccionFilter by remember { mutableStateOf("TODAS") } // "TODAS", "INGRESOS", "RETIROS", "SUSCRIPCION"

    val filteredOrders = remember(completedOrders, paymentFilter) {
        when (paymentFilter) {
            "PENDIENTES" -> completedOrders.filter { !it.pagoDepositadoAlRepartidor }
            "DISPERSADOS" -> completedOrders.filter { it.metodoPago == "TARJETA" && it.pagoDepositadoAlRepartidor }
            "EFECTIVO" -> completedOrders.filter { it.metodoPago == "EFECTIVO" }
            else -> completedOrders
        }
    }

    val filteredTransacciones = remember(transacciones, transaccionFilter) {
        when (transaccionFilter) {
            "INGRESOS" -> transacciones.filter { it.isIngreso }
            "RETIROS" -> transacciones.filter { it.tipo == "RETIRO" }
            "SUSCRIPCION" -> transacciones.filter { it.tipo == "SUSCRIPCION" }
            else -> transacciones
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(AcaBackground)
            .testTag("screen_repartidor_billetera"),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Encabezado
        item {
            Column {
                Text(
                    text = "Mi Billetera & Liquidaciones",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = AcaAzulDark
                )
                Text(
                    text = "Procesa pagos de tus mandados, consulta tu historial y solicita retiros a tu cuenta bancaria",
                    fontSize = 11.5.sp,
                    color = AcaGrisSubtle
                )
            }
        }

        // Tarjeta Principal de Balance y Retiro
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("card_wallet_balance_hero")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(AcaTurquesa)
                                )
                                Text(
                                    text = "Saldo Disponible para Retirar",
                                    color = Color.White.copy(alpha = 0.85f),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "$${String.format("%.2f", saldoDisponibleEnBilletera)} MXN",
                                color = AcaTurquesaLight,
                                fontSize = 26.sp,
                                fontWeight = FontWeight.Black
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF1E293B)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(Icons.Default.AccountBalance, contentDescription = null, tint = AcaTurquesa, modifier = Modifier.size(14.dp))
                                Text(
                                    text = "${profile?.banco ?: "BBVA"} •• ${profile?.clabeInterbancaria?.takeLast(4) ?: "1234"}",
                                    color = Color.White.copy(alpha = 0.9f),
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }

                    // Acciones Rápidas de la Billetera (Retirar SPEI y Dispersar)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { showSolicitarRetiroDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = AcaTurquesa),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("btn_solicitar_retiro")
                        ) {
                            Icon(Icons.Default.Payments, contentDescription = null, tint = Color(0xFF00382E), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Solicitar Retiro",
                                color = Color(0xFF00382E),
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }

                        if (totalPendienteDispersar > 0) {
                            Button(
                                onClick = { showBatchProcessDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF334155)),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("btn_dispersar_todo_spei")
                            ) {
                                Icon(Icons.Default.Bolt, contentDescription = null, tint = AcaTurquesaLight, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Dispersar Tarjeta",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.5.sp
                                )
                            }
                        }
                    }

                    // Métricas Rápidas
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Ganancias Mandados: +$${String.format("%.2f", totalIngresosMandados)}",
                            color = Color(0xFF86EFAC),
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Retirado a Banco: -$${String.format("%.2f", totalRetirosRealizados)}",
                            color = Color(0xFFFCA5A5),
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        // Banner informativo de Dispersión Inmediata si hay cobros con tarjeta pendientes
        item {
            AnimatedVisibility(
                visible = totalPendienteDispersar > 0,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFFEF3C7),
                    border = BorderStroke(1.dp, Color(0xFFFCD34D)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Bolt, contentDescription = null, tint = Color(0xFFB45309), modifier = Modifier.size(22.dp))
                            Column {
                                Text(
                                    text = "Tienes $${String.format("%.2f", totalPendienteDispersar)} MXN de tarjeta pendientes",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.5.sp,
                                    color = Color(0xFF92400E)
                                )
                                Text(
                                    text = "${pendingPayoutOrders.count { it.metodoPago == "TARJETA" }} viajes listos para acreditar hoy mismo",
                                    fontSize = 10.5.sp,
                                    color = Color(0xFFB45309)
                                )
                            }
                        }
                        Button(
                            onClick = { showBatchProcessDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF92400E)),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Dispersar", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Resumen de Ganancias en Cuadrícula (Efectivo, Tarjeta Dispersada)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Ganancia Efectivo
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier
                        .weight(1f)
                        .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(14.dp))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Payments,
                                contentDescription = null,
                                tint = AcaTurquesaDark,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Efectivo Directo", fontSize = 10.5.sp, color = AcaGrisSubtle)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "$${String.format("%.2f", totalEfectivo)}",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = AcaGrisEquilibrio
                        )
                        Text(
                            text = "Cobrado en mano",
                            fontSize = 9.sp,
                            color = AcaGreenSuccess,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Ganancia Tarjeta Dispersada (SPEI)
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier
                        .weight(1f)
                        .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(14.dp))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CreditCard,
                                contentDescription = null,
                                tint = AcaAzulConfianza,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("SPEI Dispersado", fontSize = 10.5.sp, color = AcaGrisSubtle)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "$${String.format("%.2f", totalDispersadoSpei)}",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = AcaAzulDark
                        )
                        Text(
                            text = "En tu banco",
                            fontSize = 9.sp,
                            color = AcaAzulConfianza,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Tarjeta de Suscripción Semanal ($150 MXN)
        item {
            val isActiva = profile?.suscripcionActiva == true
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = if (isActiva) AcaAzulDark else Color(0xFF7F1D1D)),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Suscripción Semanal Repartidor",
                                color = Color.White.copy(alpha = 0.85f),
                                fontSize = 11.5.sp
                            )
                            Text(
                                text = "$150.00 MXN / semana",
                                color = AcaTurquesaLight,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = if (isActiva) AcaGreenSuccess else Color.White
                        ) {
                            Text(
                                text = if (isActiva) "✓ Activa" else "Inactiva",
                                color = if (isActiva) Color.White else Color(0xFF7F1D1D),
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isActiva && profile != null) "Vence: ${dateOnlyFormatter.format(Date(profile!!.suscripcionFechaFin))}" else "Suscripción vencida",
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 11.sp
                        )

                        TextButton(
                            onClick = { showRenovarDialog = true },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text("Renovar (+7 días)", color = AcaTurquesaLight, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Sección Central 1: Gestión de Pagos de Mandados Entregados (Liquidación)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Mandados y Liquidación de Pagos",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = AcaAzulDark
                    )

                    if (pendingPayoutOrders.isNotEmpty()) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFFFEF3C7)
                        ) {
                            Text(
                                text = "${pendingPayoutOrders.size} por liquidar",
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFB45309),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                // Filtros de mandados en la billetera
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    contentPadding = PaddingValues(horizontal = 2.dp)
                ) {
                    val filterOptions = listOf(
                        "TODOS" to "Todos (${completedOrders.size})",
                        "PENDIENTES" to "⏳ Por Liquidar (${pendingPayoutOrders.size})",
                        "DISPERSADOS" to "💳 Tarjeta SPEI",
                        "EFECTIVO" to "💵 Efectivo"
                    )
                    items(filterOptions) { (key, label) ->
                        FilterChip(
                            selected = paymentFilter == key,
                            onClick = { paymentFilter = key },
                            label = { Text(label, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = AcaAzulDark,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
            }
        }

        // Listado de Mandados y sus Estados de Pago
        if (filteredOrders.isEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ReceiptLong, contentDescription = null, tint = AcaGrisSubtle, modifier = Modifier.size(36.dp))
                        Text(
                            text = "No hay mandados en esta categoría",
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = AcaAzulDark
                        )
                    }
                }
            }
        } else {
            items(filteredOrders, key = { it.id }) { order ->
                RepartidorOrderPaymentCard(
                    order = order,
                    onProcessPayment = {
                        orderToProcess = order
                    },
                    onViewReceipt = {
                        viewModel.openPaymentReceipt(order)
                    }
                )
            }
        }

        // =========================================================================
        // SECCIÓN CENTRAL 2: HISTORIAL DETALLADO DE TRANSACCIONES DE LA BILLETERA
        // =========================================================================
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Historial Detallado de Transacciones",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = AcaAzulDark
                        )
                        Text(
                            text = "Ingresos por mandados completados y retiros bancarios",
                            fontSize = 11.sp,
                            color = AcaGrisSubtle
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFFF1F5F9)
                    ) {
                        Text(
                            text = "${filteredTransacciones.size} movs",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = AcaAzulDark,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }

                // Filtros del Historial de Transacciones
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    contentPadding = PaddingValues(horizontal = 2.dp)
                ) {
                    val transFilterOptions = listOf(
                        "TODAS" to "Todas (${transacciones.size})",
                        "INGRESOS" to "📥 Ingresos (${transacciones.count { it.isIngreso }})",
                        "RETIROS" to "📤 Retiros SPEI (${transacciones.count { it.tipo == "RETIRO" }})",
                        "SUSCRIPCION" to "🔄 Suscripción (${transacciones.count { it.tipo == "SUSCRIPCION" }})"
                    )
                    items(transFilterOptions) { (key, label) ->
                        FilterChip(
                            selected = transaccionFilter == key,
                            onClick = { transaccionFilter = key },
                            label = { Text(label, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = AcaTurquesaDark,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
            }
        }

        // Listado del Historial de Transacciones
        if (filteredTransacciones.isEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("empty_transactions_card")
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Default.History, contentDescription = null, tint = AcaGrisSubtle, modifier = Modifier.size(36.dp))
                        Text(
                            text = "No hay transacciones registradas en este filtro",
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = AcaAzulDark
                        )
                        Text(
                            text = "Los ingresos por mandados entregados y retiros SPEI aparecerán aquí",
                            fontSize = 11.sp,
                            color = AcaGrisSubtle
                        )
                    }
                }
            }
        } else {
            items(filteredTransacciones, key = { it.id }) { trans ->
                TransaccionItemCard(
                    transaccion = trans,
                    onClick = {
                        viewModel.openTransaccionDetail(trans)
                    },
                    modifier = Modifier.testTag("transaccion_item_${trans.id}")
                )
            }
        }
    }

    // Modal de Comprobante Digital SPEI
    activeReceiptOrder?.let { receiptOrder ->
        ComprobantePagoDialog(
            order = receiptOrder,
            profile = profile,
            onDismiss = { viewModel.closePaymentReceipt() }
        )
    }

    // Modal de Detalle Completo de Transacción
    activeSelectedTransaccion?.let { selectedTrans ->
        DetalleTransaccionDialog(
            transaccion = selectedTrans,
            onDismiss = { viewModel.closeTransaccionDetail() },
            onViewOrderReceipt = { folio ->
                val order = completedOrders.find { it.folio == folio }
                if (order != null) {
                    viewModel.openPaymentReceipt(order)
                }
            }
        )
    }

    // Diálogo para Solicitar Retiro SPEI a Cuenta Bancaria
    if (showSolicitarRetiroDialog) {
        SolicitarRetiroDialog(
            saldoDisponible = saldoDisponibleEnBilletera,
            banco = profile?.banco ?: "BBVA México",
            clabeTerm = profile?.clabeInterbancaria?.takeLast(4) ?: "1234",
            onDismiss = { showSolicitarRetiroDialog = false },
            onConfirmRetiro = { monto ->
                viewModel.solicitarRetiro(monto)
                showSolicitarRetiroDialog = false
            }
        )
    }

    // Diálogo de Confirmación para Procesar Pago Individual
    orderToProcess?.let { order ->
        val totalMonto = order.tarifaEnvio + order.costoProductosEstimado
        val isTarjeta = order.metodoPago == "TARJETA"

        AlertDialog(
            onDismissRequest = { orderToProcess = null },
            icon = {
                Icon(
                    imageVector = if (isTarjeta) Icons.Default.CreditCard else Icons.Default.Payments,
                    contentDescription = null,
                    tint = if (isTarjeta) AcaAzulConfianza else Color(0xFF047857),
                    modifier = Modifier.size(32.dp)
                )
            },
            title = {
                Text(
                    text = if (isTarjeta) "Procesar Depósito SPEI" else "Confirmar Cobro en Efectivo",
                    fontWeight = FontWeight.Bold,
                    color = AcaAzulDark
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Mandado: ${order.folio} (${order.titulo})",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        color = AcaAzulDark
                    )
                    Text(
                        text = "Monto a liquidar: $${String.format("%.2f", totalMonto)} MXN",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        color = if (isTarjeta) AcaAzulDark else Color(0xFF047857)
                    )
                    Text(
                        text = if (isTarjeta) {
                            "Se realizará la transferencia SPEI inmediata a tu cuenta ${profile?.banco ?: "BBVA"} terminación ••${profile?.clabeInterbancaria?.takeLast(4) ?: "1234"}."
                        } else {
                            "Confirma que recibiste el pago en mano de parte del cliente (${order.clienteNombre})."
                        },
                        fontSize = 12.sp,
                        color = AcaGrisEquilibrio
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.procesarPagoMandado(order.id)
                        orderToProcess = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isTarjeta) AcaAzulDark else Color(0xFF047857)
                    ),
                    modifier = Modifier.testTag("btn_confirmar_procesar_pago")
                ) {
                    Text(if (isTarjeta) "Dispersar SPEI Ahora" else "Confirmar Cobro", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { orderToProcess = null }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Diálogo de Confirmación para Dispersar Todos los Pagos en Lote
    if (showBatchProcessDialog) {
        AlertDialog(
            onDismissRequest = { showBatchProcessDialog = false },
            icon = {
                Icon(Icons.Default.Bolt, contentDescription = null, tint = Color(0xFFD97706), modifier = Modifier.size(36.dp))
            },
            title = {
                Text("Dispersión Inmediata SPEI", fontWeight = FontWeight.Bold, color = AcaAzulDark)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Monto Total Acumulado: $${String.format("%.2f", totalPendienteDispersar)} MXN",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF047857)
                    )
                    Text(
                        text = "Se procesarán todos los pagos de tarjeta pendientes (${pendingPayoutOrders.count { it.metodoPago == "TARJETA" }} mandados) y se depositarán hoy mismo vía SPEI a tu cuenta:",
                        fontSize = 12.sp,
                        color = AcaGrisEquilibrio
                    )
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFF1F5F9),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("Banco: ${profile?.banco ?: "BBVA México"}", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = AcaAzulDark)
                            Text("CLABE: •••• •••• •••• ${profile?.clabeInterbancaria?.takeLast(4) ?: "1234"}", fontSize = 11.sp, color = AcaGrisSubtle)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.procesarTodosLosPagosPendientes()
                        showBatchProcessDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AcaTurquesaDark),
                    modifier = Modifier.testTag("btn_confirm_batch_spei")
                ) {
                    Text("Confirmar y Dispersar", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showBatchProcessDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Diálogo de Renovación de Suscripción Semanal
    if (showRenovarDialog) {
        AlertDialog(
            onDismissRequest = { showRenovarDialog = false },
            title = {
                Text("Suscripción Semanal AcaMandados", fontWeight = FontWeight.Bold, color = AcaAzulDark)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Monto semanal: $150.00 MXN",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = AcaTurquesaDark
                    )
                    Text(
                        text = "Beneficios:\n• 100% de ganancia en cada viaje para ti.\n• Sin comisiones por mandado.\n• Depósitos bancarios de tarjeta el mismo día.\n• Soporte y coordinación local en Acapulco.",
                        fontSize = 12.sp,
                        color = AcaGrisEquilibrio
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.renovarSuscripcion()
                        showRenovarDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AcaTurquesa)
                ) {
                    Text("Confirmar Pago de $150 MXN", color = Color(0xFF00382E), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showRenovarDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

/**
 * Componente individual para visualizar un movimiento del historial de transacciones.
 */
@Composable
private fun TransaccionItemCard(
    transaccion: TransaccionBilletera,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dateFormatter = remember { SimpleDateFormat("dd MMM, HH:mm 'hrs'", Locale("es", "MX")) }
    val fechaStr = remember(transaccion.fecha) { dateFormatter.format(Date(transaccion.fecha)) }

    val isIngreso = transaccion.isIngreso
    val isRetiro = transaccion.tipo == "RETIRO"

    val (badgeBg, iconVector, iconColor) = when (transaccion.tipo) {
        "INGRESO" -> Triple(Color(0xFFDCFCE7), Icons.Default.ArrowDownward, Color(0xFF16A34A))
        "RETIRO" -> Triple(Color(0xFFFEE2E2), Icons.Default.ArrowUpward, Color(0xFFDC2626))
        else -> Triple(Color(0xFFEFF6FF), Icons.Default.Autorenew, Color(0xFF2563EB))
    }

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                // Ícono circular distintivo
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(badgeBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = iconVector,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = transaccion.concepto,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.5.sp,
                            color = AcaAzulDark,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (transaccion.orderFolio != null) {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = Color(0xFFF1F5F9)
                            ) {
                                Text(
                                    text = transaccion.orderFolio,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AcaAzulDark,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                        }
                    }

                    Text(
                        text = transaccion.detalle,
                        fontSize = 11.sp,
                        color = AcaGrisSubtle,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = fechaStr,
                            fontSize = 10.sp,
                            color = AcaGrisSubtle
                        )
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = if (transaccion.metodoPago == "TARJETA") Color(0xFFEFF6FF) else Color(0xFFF8FAFC)
                        ) {
                            Text(
                                text = transaccion.metodoPago,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (transaccion.metodoPago == "TARJETA") Color(0xFF1D4ED8) else AcaGrisEquilibrio,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    }
                }
            }

            // Columna de Monto y Estado
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.padding(start = 8.dp)
            ) {
                Text(
                    text = "${if (isIngreso) "+" else "-"}$${String.format("%.2f", transaccion.monto)}",
                    fontWeight = FontWeight.Black,
                    fontSize = 14.5.sp,
                    color = if (isIngreso) Color(0xFF047857) else Color(0xFFB91C1C)
                )

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFFDCFCE7)
                ) {
                    Text(
                        text = transaccion.estado,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF15803D),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }
}

/**
 * Diálogo para solicitar retiro SPEI a la cuenta bancaria CLABE.
 */
@Composable
private fun SolicitarRetiroDialog(
    saldoDisponible: Double,
    banco: String,
    clabeTerm: String,
    onDismiss: () -> Unit,
    onConfirmRetiro: (Double) -> Unit
) {
    var montoText by remember { mutableStateOf(if (saldoDisponible > 0) String.format(Locale.US, "%.2f", saldoDisponible) else "100.00") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(Icons.Default.Payments, contentDescription = null, tint = AcaTurquesaDark, modifier = Modifier.size(36.dp))
        },
        title = {
            Text("Solicitar Retiro SPEI", fontWeight = FontWeight.Bold, color = AcaAzulDark)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Transfiere tus ganancias acumuladas de mandados a tu cuenta bancaria registrada sin comisiones.",
                    fontSize = 12.sp,
                    color = AcaGrisEquilibrio
                )

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFFF1F5F9),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Cuenta de destino", fontSize = 10.5.sp, color = AcaGrisSubtle)
                            Text(
                                text = "$banco •••• $clabeTerm",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = AcaAzulDark
                            )
                        }
                        Surface(shape = RoundedCornerShape(6.dp), color = Color(0xFFDCFCE7)) {
                            Text("SPEI Mismo Día", fontSize = 9.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFF15803D), modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                        }
                    }
                }

                // Campo de Monto a Retirar
                OutlinedTextField(
                    value = montoText,
                    onValueChange = {
                        montoText = it
                        errorMessage = null
                    },
                    label = { Text("Monto a Retirar (MXN)") },
                    prefix = { Text("$ ", fontWeight = FontWeight.Bold) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    isError = errorMessage != null,
                    supportingText = {
                        if (errorMessage != null) {
                            Text(errorMessage!!, color = MaterialTheme.colorScheme.error)
                        } else {
                            Text("Saldo disponible: $${String.format("%.2f", saldoDisponible)} MXN", color = AcaGrisSubtle)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_monto_retiro")
                )

                // Chips de selección rápida
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val presets = listOf(100.0, 200.0, 500.0)
                    presets.forEach { preset ->
                        OutlinedButton(
                            onClick = {
                                montoText = String.format(Locale.US, "%.2f", preset)
                                errorMessage = null
                            },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("$$preset", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                    if (saldoDisponible > 0) {
                        OutlinedButton(
                            onClick = {
                                montoText = String.format(Locale.US, "%.2f", saldoDisponible)
                                errorMessage = null
                            },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Todo", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = AcaTurquesaDark)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = montoText.toDoubleOrNull()
                    if (amount == null || amount <= 0) {
                        errorMessage = "Ingresa un monto válido mayor a 0"
                    } else {
                        onConfirmRetiro(amount)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = AcaTurquesaDark),
                modifier = Modifier.testTag("btn_confirmar_retiro_spei")
            ) {
                Text("Confirmar Retiro", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

/**
 * Tarjeta individual de Mandado Completado con su estado de pago y acciones para procesar o ver comprobante.
 */
@Composable
private fun RepartidorOrderPaymentCard(
    order: MandadoOrder,
    onProcessPayment: () -> Unit,
    onViewReceipt: () -> Unit,
    modifier: Modifier = Modifier
) {
    val totalMonto = order.tarifaEnvio + order.costoProductosEstimado
    val isTarjeta = order.metodoPago == "TARJETA"
    val isDepositado = order.pagoDepositadoAlRepartidor
    val tipoEnum = TipoMandado.values().find { it.id == order.tipo } ?: TipoMandado.EXPRESS

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = BorderStroke(1.dp, if (!isDepositado) Color(0xFFFCD34D) else Color(0xFFE2E8F0)),
        modifier = modifier
            .fillMaxWidth()
            .testTag("payment_order_card_${order.id}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Header: Folio, Método de Pago, Estado del Pago
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = order.folio,
                        fontWeight = FontWeight.Black,
                        fontSize = 13.5.sp,
                        color = AcaAzulDark
                    )
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = if (isTarjeta) Color(0xFFEFF6FF) else Color(0xFFF0FDF4)
                    ) {
                        Text(
                            text = if (isTarjeta) "TARJETA / APP" else "EFECTIVO",
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isTarjeta) Color(0xFF1D4ED8) else Color(0xFF15803D),
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                        )
                    }
                }

                // Badge de Estado del Pago
                if (!isDepositado) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFFFEF3C7)
                    ) {
                        Text(
                            text = if (isTarjeta) "⏳ Por Dispersar" else "⏳ Por Liquidar",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFFB45309),
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                        )
                    }
                } else {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFFDCFCE7)
                    ) {
                        Text(
                            text = if (isTarjeta) "✓ Dispersado SPEI" else "✓ Cobrado en Mano",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF16A34A),
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                        )
                    }
                }
            }

            // Datos del Mandado (Título, Cliente y Ruta)
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = order.titulo,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp,
                    color = AcaAzulDark,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${order.puntoRecogida} ➔ ${order.puntoEntrega}",
                    fontSize = 11.sp,
                    color = AcaGrisSubtle,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            HorizontalDivider(color = Color(0xFFF1F5F9))

            // Desglose Financiero y Botones de Acción
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Ganancia flete: $${String.format("%.2f", order.tarifaEnvio)} MXN",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF047857)
                    )
                    if (order.costoProductosEstimado > 0) {
                        Text(
                            text = "+ Reembolso prod: $${String.format("%.2f", order.costoProductosEstimado)}",
                            fontSize = 10.sp,
                            color = AcaGrisSubtle
                        )
                    }
                }

                // Acciones: Procesar Pago vs Ver Comprobante
                if (!isDepositado) {
                    Button(
                        onClick = onProcessPayment,
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isTarjeta) AcaAzulDark else Color(0xFF047857)
                        ),
                        modifier = Modifier.testTag("btn_procesar_pago_${order.id}")
                    ) {
                        Icon(
                            imageVector = if (isTarjeta) Icons.Default.Bolt else Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isTarjeta) "Procesar SPEI" else "Confirmar Cobro",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    if (isTarjeta) {
                        OutlinedButton(
                            onClick = onViewReceipt,
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 3.dp),
                            border = BorderStroke(1.dp, AcaAzulConfianza),
                            modifier = Modifier.testTag("btn_ver_comprobante_${order.id}")
                        ) {
                            Icon(Icons.Default.Receipt, contentDescription = null, modifier = Modifier.size(13.dp), tint = AcaAzulConfianza)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Recibo SPEI", fontSize = 11.sp, color = AcaAzulConfianza, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Text(
                            text = "Liquidado $${String.format("%.2f", totalMonto)}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF15803D)
                        )
                    }
                }
            }
        }
    }
}
