package com.example.ui.screens

import android.content.Context
import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.MandadoOrder
import com.example.ui.AppMode
import com.example.ui.MandadosViewModel
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

enum class HistorialViewMode {
    CLIENTE,
    REPARTIDOR
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistorialOrdenesScreen(
    viewModel: MandadosViewModel,
    onBack: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val allOrders by viewModel.allOrders.collectAsStateWithLifecycle()
    val currentAppMode by viewModel.currentMode.collectAsStateWithLifecycle()
    val driverCompletedOrders by viewModel.driverCompletedOrders.collectAsStateWithLifecycle()

    var viewMode by remember(currentAppMode) {
        mutableStateOf(if (currentAppMode == AppMode.REPARTIDOR) HistorialViewMode.REPARTIDOR else HistorialViewMode.CLIENTE)
    }

    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("TODOS") } // TODOS, EFECTIVO, TARJETA, COMIDA, FARMACIA, COMPRAS, EXPRESS
    var selectedOrderForReceipt by remember { mutableStateOf<MandadoOrder?>(null) }

    // Filtrado de órdenes según la perspectiva (Cliente o Repartidor)
    val baseOrders = remember(allOrders, driverCompletedOrders, viewMode) {
        if (viewMode == HistorialViewMode.REPARTIDOR) {
            // Órdenes completadas por el repartidor (o entregadas con repartidorId = 1 o nombre)
            val driverOrders = allOrders.filter { it.estado == "ENTREGADO" && (it.repartidorId == 1L || it.repartidorNombre != null) }
            if (driverOrders.isNotEmpty()) driverOrders else driverCompletedOrders
        } else {
            // Todos los mandados del cliente (Pendientes, En Progreso, Entregados)
            allOrders
        }
    }

    val filteredOrders = remember(baseOrders, searchQuery, selectedFilter) {
        baseOrders.filter { order ->
            val matchesSearch = searchQuery.isBlank() ||
                    order.folio.contains(searchQuery, ignoreCase = true) ||
                    order.titulo.contains(searchQuery, ignoreCase = true) ||
                    order.descripcion.contains(searchQuery, ignoreCase = true) ||
                    order.puntoRecogida.contains(searchQuery, ignoreCase = true) ||
                    order.puntoEntrega.contains(searchQuery, ignoreCase = true) ||
                    (order.repartidorNombre?.contains(searchQuery, ignoreCase = true) == true) ||
                    order.clienteNombre.contains(searchQuery, ignoreCase = true)

            val matchesFilter = when (selectedFilter) {
                "PENDIENTES" -> order.estado == "SOLICITADO"
                "EN_PROGRESO" -> order.estado == "ACEPTADO" || order.estado == "ASIGNADO" || order.estado == "RECOGIDO" || order.estado == "EN_CAMINO" || order.estado == "EN_RUTA"
                "ENTREGADOS" -> order.estado == "ENTREGADO" || order.estado == "COMPLETADO"
                "EFECTIVO" -> order.metodoPago == "EFECTIVO"
                "TARJETA" -> order.metodoPago == "TARJETA"
                "COMIDA" -> order.tipo == "COMIDA"
                "FARMACIA" -> order.tipo == "FARMACIA"
                "COMPRAS" -> order.tipo == "COMPRAS"
                "EXPRESS" -> order.tipo == "EXPRESS" || order.tipo == "DOCUMENTACION"
                else -> true
            }

            matchesSearch && matchesFilter
        }
    }

    // Estadísticas
    val totalOrdersCount = baseOrders.size
    val totalMontoInvertido = baseOrders.sumOf { it.costoProductosEstimado + it.tarifaEnvio }
    val totalGananciasRepartidor = baseOrders.sumOf { it.tarifaEnvio }
    val totalEfectivo = baseOrders.filter { it.metodoPago == "EFECTIVO" }.sumOf { it.tarifaEnvio }
    val totalTarjeta = baseOrders.filter { it.metodoPago == "TARJETA" }.sumOf { it.tarifaEnvio }

    Scaffold(
        topBar = {
            Surface(
                color = AcaAzulConfianza,
                shadowElevation = 4.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (onBack != null) {
                                IconButton(
                                    onClick = onBack,
                                    modifier = Modifier
                                        .size(36.dp)
                                        .testTag("btn_historial_back")
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "Regresar",
                                        tint = Color.White
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                            }
                            Column {
                                Text(
                                    text = "Historial de Mandados",
                                    fontSize = 19.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "Entregas completadas, precios, fechas y recibos",
                                    fontSize = 11.sp,
                                    color = Color.White.copy(alpha = 0.8f)
                                )
                            }
                        }

                        // Botón para simular nuevo pedido completado y probar
                        FilledTonalButton(
                            onClick = { viewModel.simularEntregaCompletada() },
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = AcaTurquesa,
                                contentColor = Color(0xFF00382E)
                            ),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.testTag("btn_simular_completado")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("+ Simular", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Conmutador de Perspectiva: [ 👤 Vista Cliente ] | [ 🛵 Vista Repartidor ]
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color.White.copy(alpha = 0.15f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(4.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            val isCliente = viewMode == HistorialViewMode.CLIENTE
                            val clientBg by animateColorAsState(
                                targetValue = if (isCliente) Color.White else Color.Transparent,
                                label = "clientBg"
                            )
                            val clientTextColor by animateColorAsState(
                                targetValue = if (isCliente) AcaAzulDark else Color.White,
                                label = "clientText"
                            )

                            Surface(
                                shape = RoundedCornerShape(9.dp),
                                color = clientBg,
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { viewMode = HistorialViewMode.CLIENTE }
                                    .testTag("tab_historial_cliente")
                            ) {
                                Row(
                                    modifier = Modifier.padding(vertical = 8.dp),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = null,
                                        tint = clientTextColor,
                                        modifier = Modifier.size(17.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Vista Cliente",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.5.sp,
                                        color = clientTextColor
                                    )
                                }
                            }

                            val isRepartidor = viewMode == HistorialViewMode.REPARTIDOR
                            val repBg by animateColorAsState(
                                targetValue = if (isRepartidor) AcaTurquesa else Color.Transparent,
                                label = "repBg"
                            )
                            val repTextColor by animateColorAsState(
                                targetValue = if (isRepartidor) Color(0xFF00382E) else Color.White,
                                label = "repText"
                            )

                            Surface(
                                shape = RoundedCornerShape(9.dp),
                                color = repBg,
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { viewMode = HistorialViewMode.REPARTIDOR }
                                    .testTag("tab_historial_repartidor")
                            ) {
                                Row(
                                    modifier = Modifier.padding(vertical = 8.dp),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.TwoWheeler,
                                        contentDescription = null,
                                        tint = repTextColor,
                                        modifier = Modifier.size(17.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Vista Repartidor",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.5.sp,
                                        color = repTextColor
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        modifier = modifier
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(AcaBackground)
                .padding(innerPadding),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 90.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 1. Tarjeta de Métricas y Resumen Financiero
            item {
                if (viewMode == HistorialViewMode.CLIENTE) {
                    ClienteMetricsCard(
                        totalOrders = totalOrdersCount,
                        totalInvertido = totalMontoInvertido,
                        preferenciaPago = if (baseOrders.count { it.metodoPago == "TARJETA" } >= baseOrders.count { it.metodoPago == "EFECTIVO" }) "Tarjeta" else "Efectivo"
                    )
                } else {
                    RepartidorMetricsCard(
                        totalOrders = totalOrdersCount,
                        totalGanancias = totalGananciasRepartidor,
                        efectivoEnMano = totalEfectivo,
                        transferenciasSpei = totalTarjeta
                    )
                }
            }

            // 2. Barra de Búsqueda y Filtros
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_buscar_historial"),
                        placeholder = {
                            Text("Buscar por #folio, comercio, dirección o nombre...", fontSize = 13.sp)
                        },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = null, tint = AcaGrisSubtle)
                        },
                        trailingIcon = {
                            if (searchQuery.isNotBlank()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Close, contentDescription = "Limpiar", tint = AcaGrisSubtle)
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedBorderColor = AcaAzulConfianza,
                            unfocusedBorderColor = Color(0xFFE2E8F0)
                        )
                    )

                    // Filtros horizontales
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        item {
                            HistorialFilterChip(
                                label = "Todos (${baseOrders.size})",
                                isSelected = selectedFilter == "TODOS",
                                onClick = { selectedFilter = "TODOS" }
                            )
                        }
                        item {
                            HistorialFilterChip(
                                label = "⏳ Pendientes (${baseOrders.count { it.estado == "SOLICITADO" }})",
                                isSelected = selectedFilter == "PENDIENTES",
                                onClick = { selectedFilter = "PENDIENTES" }
                            )
                        }
                        item {
                            HistorialFilterChip(
                                label = "🛵 En Progreso (${baseOrders.count { it.estado == "ACEPTADO" || it.estado == "ASIGNADO" || it.estado == "RECOGIDO" || it.estado == "EN_CAMINO" || it.estado == "EN_RUTA" }})",
                                isSelected = selectedFilter == "EN_PROGRESO",
                                onClick = { selectedFilter = "EN_PROGRESO" }
                            )
                        }
                        item {
                            HistorialFilterChip(
                                label = "✅ Entregados (${baseOrders.count { it.estado == "ENTREGADO" || it.estado == "COMPLETADO" }})",
                                isSelected = selectedFilter == "ENTREGADOS",
                                onClick = { selectedFilter = "ENTREGADOS" }
                            )
                        }
                        item {
                            HistorialFilterChip(
                                label = "💵 Efectivo",
                                isSelected = selectedFilter == "EFECTIVO",
                                onClick = { selectedFilter = "EFECTIVO" }
                            )
                        }
                        item {
                            HistorialFilterChip(
                                label = "💳 Tarjeta Digital",
                                isSelected = selectedFilter == "TARJETA",
                                onClick = { selectedFilter = "TARJETA" }
                            )
                        }
                        item {
                            HistorialFilterChip(
                                label = "🍲 Comida",
                                isSelected = selectedFilter == "COMIDA",
                                onClick = { selectedFilter = "COMIDA" }
                            )
                        }
                        item {
                            HistorialFilterChip(
                                label = "💊 Farmacia",
                                isSelected = selectedFilter == "FARMACIA",
                                onClick = { selectedFilter = "FARMACIA" }
                            )
                        }
                        item {
                            HistorialFilterChip(
                                label = "🛒 Súper / Compras",
                                isSelected = selectedFilter == "COMPRAS",
                                onClick = { selectedFilter = "COMPRAS" }
                            )
                        }
                        item {
                            HistorialFilterChip(
                                label = "📦 Envíos / Express",
                                isSelected = selectedFilter == "EXPRESS",
                                onClick = { selectedFilter = "EXPRESS" }
                            )
                        }
                    }
                }
            }

            // 3. Lista de Mandados Completados o Estado Vacío
            if (filteredOrders.isEmpty()) {
                item {
                    EmptyHistorialView(
                        isFiltered = searchQuery.isNotBlank() || selectedFilter != "TODOS",
                        onClearFilters = {
                            searchQuery = ""
                            selectedFilter = "TODOS"
                        },
                        onSimulate = { viewModel.simularEntregaCompletada() }
                    )
                }
            } else {
                items(filteredOrders, key = { it.id }) { order ->
                    CompletedOrderCard(
                        order = order,
                        viewMode = viewMode,
                        onViewReceipt = { selectedOrderForReceipt = order },
                        onReorder = { viewModel.reordenarMandado(order) },
                        onRate = { viewModel.openRatingDialog(order, isDriverRatingClient = false) }
                    )
                }
            }
        }
    }

    // Modal de Recibo Digital
    selectedOrderForReceipt?.let { order ->
        DigitalReceiptDialog(
            order = order,
            viewMode = viewMode,
            onDismiss = { selectedOrderForReceipt = null }
        )
    }
}

// -------------------------------------------------------------
// Componente de Tarjeta de Métricas para Cliente
// -------------------------------------------------------------
@Composable
private fun ClienteMetricsCard(
    totalOrders: Int,
    totalInvertido: Double,
    preferenciaPago: String
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = AcaAzulDark),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.TaskAlt,
                        contentDescription = null,
                        tint = AcaTurquesaLight,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Tus Compras y Envíos Realizados",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.5.sp
                    )
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color.White.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "Cliente AcaMandados",
                        color = AcaTurquesaLight,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(text = "Entregas Exitosas", color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp)
                    Text(
                        text = "$totalOrders mandados",
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 18.sp
                    )
                }
                Column {
                    Text(text = "Total Invertido", color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp)
                    Text(
                        text = "$${String.format("%.2f", totalInvertido)} MXN",
                        color = AcaTurquesaLight,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 18.sp
                    )
                }
                Column {
                    Text(text = "Método Preferido", color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp)
                    Text(
                        text = preferenciaPago,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}

// -------------------------------------------------------------
// Componente de Tarjeta de Métricas para Repartidor
// -------------------------------------------------------------
@Composable
private fun RepartidorMetricsCard(
    totalOrders: Int,
    totalGanancias: Double,
    efectivoEnMano: Double,
    transferenciasSpei: Double
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF00382E)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Payments,
                        contentDescription = null,
                        tint = AcaTurquesa,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Tus Ganancias 100% Acumuladas",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.5.sp
                    )
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = AcaTurquesa.copy(alpha = 0.2f)
                ) {
                    Text(
                        text = "0% Comisión AcaMandados",
                        color = AcaTurquesa,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(text = "Ganancia Neta", color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp)
                    Text(
                        text = "$${String.format("%.2f", totalGanancias)}",
                        color = AcaTurquesa,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 18.sp
                    )
                }
                Column {
                    Text(text = "Efectivo Cobrado", color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp)
                    Text(
                        text = "$${String.format("%.2f", efectivoEnMano)}",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
                Column {
                    Text(text = "SPEI en Cuenta", color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp)
                    Text(
                        text = "$${String.format("%.2f", transferenciasSpei)}",
                        color = Color(0xFF93C5FD),
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}

// -------------------------------------------------------------
// Tarjeta individual de orden completada
// -------------------------------------------------------------
@Composable
fun CompletedOrderCard(
    order: MandadoOrder,
    viewMode: HistorialViewMode,
    onViewReceipt: () -> Unit,
    onReorder: () -> Unit,
    onRate: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dateFormatter = remember { SimpleDateFormat("dd MMM yyyy • hh:mm a", Locale("es", "MX")) }
    val formattedDate = remember(order.fechaEntrega, order.fechaCreacion) {
        val timestamp = order.fechaEntrega ?: order.fechaCreacion
        dateFormatter.format(Date(timestamp))
    }

    val totalCobro = order.costoProductosEstimado + order.tarifaEnvio

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
            .fillMaxWidth()
            .testTag("completed_order_card_${order.folio}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Cabecera: Folio, Estado y Fecha
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFFEEF2FF)
                    ) {
                        Text(
                            text = order.folio,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 12.sp,
                            color = AcaAzulConfianza,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = order.tipo,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = AcaGrisSubtle
                    )
                }

                // Status Badge "ENTREGADO"
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFDCFCE7),
                    border = BorderStroke(1.dp, Color(0xFF86EFAC))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color(0xFF15803D),
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "ENTREGADO",
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF14532D)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Fecha y hora
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Event,
                    contentDescription = null,
                    tint = AcaGrisSubtle,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = formattedDate,
                    fontSize = 11.5.sp,
                    color = AcaGrisSubtle
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Título y descripción
            Text(
                text = order.titulo,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = AcaAzulDark
            )
            if (order.descripcion.isNotBlank()) {
                Text(
                    text = order.descripcion,
                    fontSize = 12.sp,
                    color = AcaGrisEquilibrio,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Ruta de Entrega (Puntos y MacroZonas de Acapulco)
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = AcaSurfaceVariant,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(verticalAlignment = Alignment.Top) {
                        Icon(
                            imageVector = Icons.Default.Place,
                            contentDescription = null,
                            tint = AcaAzulConfianza,
                            modifier = Modifier.size(16.dp).padding(top = 1.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text(
                                text = "Origen: ${order.puntoRecogida}",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = AcaAzulDark
                            )
                            Text(
                                text = order.zonaRecogida,
                                fontSize = 10.sp,
                                color = AcaGrisSubtle
                            )
                        }
                    }

                    HorizontalDivider(color = Color(0xFFE2E8F0), thickness = 0.8.dp)

                    Row(verticalAlignment = Alignment.Top) {
                        Icon(
                            imageVector = Icons.Default.Flag,
                            contentDescription = null,
                            tint = AcaTurquesaDark,
                            modifier = Modifier.size(16.dp).padding(top = 1.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text(
                                text = "Entrega: ${order.puntoEntrega}",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = AcaAzulDark
                            )
                            Text(
                                text = order.zonaEntrega,
                                fontSize = 10.sp,
                                color = AcaGrisSubtle
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Desglose de Precios y Método de Pago
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFF8FAFC),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    // Tarifa de envío
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Tarifa de Envío:",
                                fontSize = 12.sp,
                                color = AcaGrisEquilibrio
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = Color(0xFFECFDF5)
                            ) {
                                Text(
                                    text = "100% Repartidor",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF047857),
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = "$${String.format("%.2f", order.tarifaEnvio)} MXN",
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = AcaAzulDark
                        )
                    }

                    if (order.costoProductosEstimado > 0) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Costo de Productos / Mandado:",
                                fontSize = 12.sp,
                                color = AcaGrisEquilibrio
                            )
                            Text(
                                text = "$${String.format("%.2f", order.costoProductosEstimado)} MXN",
                                fontSize = 12.sp,
                                color = AcaGrisEquilibrio
                            )
                        }
                    }

                    HorizontalDivider(
                        color = Color(0xFFE2E8F0),
                        thickness = 0.8.dp,
                        modifier = Modifier.padding(vertical = 6.dp)
                    )

                    // Total & Método de pago
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = if (viewMode == HistorialViewMode.REPARTIDOR) "Tu Ganancia Neta:" else "Total Pagado:",
                                fontSize = 11.sp,
                                color = AcaGrisSubtle,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = if (viewMode == HistorialViewMode.REPARTIDOR) {
                                    "$${String.format("%.2f", order.tarifaEnvio)} MXN"
                                } else {
                                    "$${String.format("%.2f", totalCobro)} MXN"
                                },
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (viewMode == HistorialViewMode.REPARTIDOR) Color(0xFF047857) else AcaAzulDark
                            )
                        }

                        // Badge de Método de Pago
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (order.metodoPago == "TARJETA") Color(0xFFEEF2FF) else Color(0xFFECFDF5),
                            border = BorderStroke(
                                1.dp,
                                if (order.metodoPago == "TARJETA") Color(0xFFC7D2FE) else Color(0xFFA7F3D0)
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (order.metodoPago == "TARJETA") Icons.Default.CreditCard else Icons.Default.Payments,
                                    contentDescription = null,
                                    tint = if (order.metodoPago == "TARJETA") AcaAzulConfianza else Color(0xFF047857),
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (order.metodoPago == "TARJETA") "Tarjeta en App" else "Efectivo en Mano",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (order.metodoPago == "TARJETA") AcaAzulConfianza else Color(0xFF065F46)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Información de Entrega & Verificación de Seguridad
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // PIN Verificado
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.VpnKey,
                        contentDescription = null,
                        tint = AcaTurquesaDark,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "PIN Validador: ${order.pinSeguridad}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = AcaGrisEquilibrio
                    )
                }

                // Asignado / Cliente
                Text(
                    text = if (viewMode == HistorialViewMode.CLIENTE) {
                        "Repartidor: ${order.repartidorNombre ?: "Carlos M."}"
                    } else {
                        "Cliente: ${order.clienteNombre}"
                    },
                    fontSize = 11.sp,
                    color = AcaGrisSubtle
                )
            }

            // Calificación si existe
            if (order.calificadoPorCliente && order.calificacionClienteEstrellas != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFFFFBEB),
                    border = BorderStroke(1.dp, Color(0xFFFDE68A)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row {
                            repeat(order.calificacionClienteEstrellas) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = null,
                                    tint = Color(0xFFF59E0B),
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = order.calificacionClienteComentario ?: "Servicio calificado con 5 estrellas",
                            fontSize = 11.sp,
                            color = Color(0xFF92400E),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Botones de Acción
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Botón Ver Recibo Digital
                OutlinedButton(
                    onClick = onViewReceipt,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = AcaAzulConfianza
                    ),
                    border = BorderStroke(1.dp, AcaAzulConfianza),
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp)
                        .testTag("btn_recibo_${order.folio}")
                ) {
                    Icon(
                        imageVector = Icons.Default.ReceiptLong,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Ver Recibo",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Botón secundario según rol
                if (viewMode == HistorialViewMode.CLIENTE) {
                    Button(
                        onClick = onReorder,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AcaAzulConfianza),
                        modifier = Modifier
                            .weight(1f)
                            .height(38.dp)
                            .testTag("btn_reordenar_${order.folio}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Replay,
                            contentDescription = null,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Pedir de nuevo",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    Button(
                        onClick = onViewReceipt,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00382E)),
                        modifier = Modifier
                            .weight(1f)
                            .height(38.dp)
                            .testTag("btn_comprobante_${order.folio}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Checklist,
                            contentDescription = null,
                            tint = AcaTurquesa,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Comprobante",
                            fontSize = 11.5.sp,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// Diálogo de Recibo Digital Formateado
// -------------------------------------------------------------
@Composable
fun DigitalReceiptDialog(
    order: MandadoOrder,
    viewMode: HistorialViewMode,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val dateFormatter = remember { SimpleDateFormat("EEEE, dd 'de' MMMM yyyy • hh:mm:ss a", Locale("es", "MX")) }
    val formattedDate = remember(order.fechaEntrega, order.fechaCreacion) {
        val timestamp = order.fechaEntrega ?: order.fechaCreacion
        dateFormatter.format(Date(timestamp))
    }
    val totalCobro = order.costoProductosEstimado + order.tarifaEnvio

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .testTag("dialog_recibo_digital")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Encabezado del ticket
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Aca",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                color = AcaAzulDark
                            )
                            Text(
                                text = "Mandados",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                color = AcaTurquesaDark
                            )
                        }
                        Text(
                            text = "Comprobante de Servicio • Acapulco",
                            fontSize = 10.5.sp,
                            color = AcaGrisSubtle
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = AcaGrisSubtle)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Estado de completado & Folio
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
                            Text(text = "Folio Oficial:", fontSize = 10.sp, color = AcaGrisSubtle)
                            Text(
                                text = order.folio,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = AcaAzulDark,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFFDCFCE7)
                        ) {
                            Text(
                                text = "✓ SERVICIO PAGADO",
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF15803D),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Fecha y Hora
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AccessTime, contentDescription = null, tint = AcaGrisSubtle, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = formattedDate,
                        fontSize = 10.5.sp,
                        color = AcaGrisEquilibrio
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = Color(0xFFE2E8F0), thickness = 1.dp)
                Spacer(modifier = Modifier.height(12.dp))

                // Puntos de entrega
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(text = "DETALLE DE RUTA", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = AcaGrisSubtle)
                    Text(
                        text = "• Recolección: ${order.puntoRecogida} (${order.zonaRecogida})",
                        fontSize = 11.5.sp,
                        color = AcaAzulDark
                    )
                    Text(
                        text = "• Entrega: ${order.puntoEntrega} (${order.zonaEntrega})",
                        fontSize = 11.5.sp,
                        color = AcaAzulDark
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = Color(0xFFE2E8F0), thickness = 1.dp)
                Spacer(modifier = Modifier.height(12.dp))

                // Desglose Numérico de Precios
                Text(text = "DESGLOSE DE PAGO", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = AcaGrisSubtle)
                Spacer(modifier = Modifier.height(6.dp))

                if (order.costoProductosEstimado > 0) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Mercancía / Artículos:", fontSize = 12.sp, color = AcaGrisEquilibrio)
                        Text(text = "$${String.format("%.2f", order.costoProductosEstimado)} MXN", fontSize = 12.sp, color = AcaAzulDark)
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Tarifa de Envío AcaMandados:", fontSize = 12.sp, color = AcaGrisEquilibrio)
                    Text(text = "$${String.format("%.2f", order.tarifaEnvio)} MXN", fontSize = 12.sp, color = AcaAzulDark)
                }

                if (order.recargoZonaLejana > 0) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Recargo Zona Lejana:", fontSize = 12.sp, color = AcaGrisEquilibrio)
                        Text(text = "$${String.format("%.2f", order.recargoZonaLejana)} MXN", fontSize = 12.sp, color = AcaAzulDark)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(color = Color(0xFFE2E8F0), thickness = 1.dp)
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "TOTAL DEL SERVICIO:",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = AcaAzulDark
                    )
                    Text(
                        text = "$${String.format("%.2f", totalCobro)} MXN",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Black,
                        color = AcaAzulConfianza
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Método de pago y clave SPEI si aplica
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (order.metodoPago == "TARJETA") Color(0xFFEEF2FF) else Color(0xFFECFDF5),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text(
                            text = if (order.metodoPago == "TARJETA") {
                                "💳 Método: Tarjeta Bancaria (In-App •••• ${order.tarjetaUltimos4.ifBlank { "4820" }})"
                            } else {
                                "💵 Método: Efectivo contra entrega (Cobro en mano)"
                            },
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (order.metodoPago == "TARJETA") AcaAzulConfianza else Color(0xFF047857)
                        )
                        if (order.claveRastreoSpei != null) {
                            Text(
                                text = "Rastreo SPEI Repartidor: ${order.claveRastreoSpei}",
                                fontSize = 10.sp,
                                color = AcaGrisSubtle
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Botones de acción del diálogo: Compartir y Copiar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            val ticketText = """
                                📦 AcaMandados • Recibo de Servicio
                                Folio: ${order.folio}
                                Fecha: $formattedDate
                                Estado: ENTREGADO
                                Origen: ${order.puntoRecogida}
                                Destino: ${order.puntoEntrega}
                                Tarifa de Envío: $${String.format("%.2f", order.tarifaEnvio)} MXN
                                Total: $${String.format("%.2f", totalCobro)} MXN
                                Método de Pago: ${order.metodoPago}
                                PIN de Seguridad: ${order.pinSeguridad}
                                https://acamandados.com/t/${order.folio}
                            """.trimIndent()
                            clipboardManager.setText(AnnotatedString(ticketText))
                        },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp)
                            .testTag("btn_copiar_recibo")
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Copiar", fontSize = 12.sp)
                    }

                    Button(
                        onClick = {
                            val ticketText = """
                                📦 AcaMandados • Recibo Oficial
                                Folio: ${order.folio}
                                Fecha: $formattedDate
                                Servicio: ${order.titulo}
                                Origen: ${order.puntoRecogida}
                                Destino: ${order.puntoEntrega}
                                Total Pagado: $${String.format("%.2f", totalCobro)} MXN
                                Método: ${order.metodoPago}
                                Repartidor: ${order.repartidorNombre ?: "Carlos Mendoza"}
                                Cliente: ${order.clienteNombre}
                                ¡Gracias por apoyar la economía local de Acapulco!
                            """.trimIndent()

                            val sendIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, ticketText)
                                type = "text/plain"
                            }
                            val shareIntent = Intent.createChooser(sendIntent, "Compartir Recibo AcaMandados")
                            context.startActivity(shareIntent)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AcaAzulConfianza),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp)
                            .testTag("btn_compartir_recibo")
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Compartir", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// Componente de Chip de Filtro
// -------------------------------------------------------------
@Composable
private fun HistorialFilterChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = if (isSelected) AcaAzulConfianza else Color.White,
        border = BorderStroke(1.dp, if (isSelected) AcaAzulConfianza else Color(0xFFE2E8F0)),
        modifier = Modifier.clickable { onClick() }
    ) {
        Text(
            text = label,
            fontSize = 11.5.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) Color.White else AcaGrisEquilibrio,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
        )
    }
}

// -------------------------------------------------------------
// Vista de Estado Vacío
// -------------------------------------------------------------
@Composable
private fun EmptyHistorialView(
    isFiltered: Boolean,
    onClearFilters: () -> Unit,
    onSimulate: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                imageVector = Icons.Default.ReceiptLong,
                contentDescription = null,
                tint = AcaTurquesaDark,
                modifier = Modifier.size(54.dp)
            )

            Text(
                text = if (isFiltered) "No hay resultados para esta búsqueda" else "Aún no tienes mandados completados",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = AcaAzulDark,
                textAlign = TextAlign.Center
            )

            Text(
                text = if (isFiltered) {
                    "Intenta cambiar los términos de búsqueda o limpiar los filtros activos."
                } else {
                    "Los mandados entregados con éxito por clientes y repartidores aparecerán aquí con sus recibos y montos detallados."
                },
                fontSize = 12.5.sp,
                color = AcaGrisSubtle,
                textAlign = TextAlign.Center,
                lineHeight = 17.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            if (isFiltered) {
                OutlinedButton(
                    onClick = onClearFilters,
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Limpiar Filtros", fontSize = 12.sp)
                }
            } else {
                Button(
                    onClick = onSimulate,
                    colors = ButtonDefaults.buttonColors(containerColor = AcaTurquesa),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = "Generar Mandado de Muestra",
                        color = Color(0xFF00382E),
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.5.sp
                    )
                }
            }
        }
    }
}
