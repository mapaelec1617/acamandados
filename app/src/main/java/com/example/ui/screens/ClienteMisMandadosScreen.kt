package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.MandadoOrder
import com.example.data.model.TipoMandado
import com.example.ui.ClienteTab
import com.example.ui.MandadosViewModel
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class EstadoEnvioFiltro(val label: String, val descripcion: String) {
    TODOS("Todos", "Todos tus envíos"),
    PENDIENTES("Pendientes", "Buscando repartidor"),
    EN_PROGRESO("En Progreso", "Repartidor asignado o en ruta"),
    ENTREGADOS("Entregados", "Completados con éxito"),
    CANCELADOS("Cancelados", "Cancelados")
}

/**
 * Pantalla de Historial de Mandados y Envíos para Clientes.
 * Permite a los clientes monitorear en tiempo real el estado actual (Pendiente, En Progreso, Entregado)
 * de todos sus pedidos, consultar el PIN de seguridad, acceder al rastreo GPS en vivo, ver recibos y reordenar.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClienteMisMandadosScreen(
    viewModel: MandadosViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val allOrders by viewModel.allOrders.collectAsStateWithLifecycle()
    val driverLoc by viewModel.driverLocationState.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()

    var selectedFilter by remember { mutableStateOf(EstadoEnvioFiltro.TODOS) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedOrderForDetail by remember { mutableStateOf<MandadoOrder?>(null) }

    // Clasificación determinista de estados
    val pendientes = remember(allOrders) {
        allOrders.filter { it.estado == "SOLICITADO" }
    }
    val enProgreso = remember(allOrders) {
        allOrders.filter { it.estado == "ACEPTADO" || it.estado == "ASIGNADO" || it.estado == "RECOGIDO" || it.estado == "EN_CAMINO" || it.estado == "EN_RUTA" }
    }
    val entregados = remember(allOrders) {
        allOrders.filter { it.estado == "ENTREGADO" || it.estado == "COMPLETADO" }
    }
    val cancelados = remember(allOrders) {
        allOrders.filter { it.estado == "CANCELADO" }
    }

    // Filtrado por estado y por texto de búsqueda
    val filteredOrders = remember(allOrders, selectedFilter, searchQuery) {
        val byStatus = when (selectedFilter) {
            EstadoEnvioFiltro.TODOS -> allOrders
            EstadoEnvioFiltro.PENDIENTES -> pendientes
            EstadoEnvioFiltro.EN_PROGRESO -> enProgreso
            EstadoEnvioFiltro.ENTREGADOS -> entregados
            EstadoEnvioFiltro.CANCELADOS -> cancelados
        }

        if (searchQuery.isBlank()) {
            byStatus.sortedByDescending { it.fechaCreacion }
        } else {
            val query = searchQuery.trim().lowercase()
            byStatus.filter { order ->
                order.folio.lowercase().contains(query) ||
                        order.titulo.lowercase().contains(query) ||
                        order.descripcion.lowercase().contains(query) ||
                        order.puntoRecogida.lowercase().contains(query) ||
                        order.puntoEntrega.lowercase().contains(query) ||
                        order.zonaRecogida.lowercase().contains(query) ||
                        order.zonaEntrega.lowercase().contains(query)
            }.sortedByDescending { it.fechaCreacion }
        }
    }

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = { viewModel.refreshData() },
        modifier = modifier
            .fillMaxSize()
            .testTag("pull_to_refresh_box")
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(AcaBackground)
                .testTag("screen_cliente_mis_mandados"),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 95.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
        // 1. Cabecera principal con botón de crear mandado
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Historial de Mandados",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = AcaAzulDark
                    )
                    Text(
                        text = "Estado en tiempo real de tus envíos en Acapulco",
                        fontSize = 11.5.sp,
                        color = AcaGrisSubtle
                    )
                }

                Button(
                    onClick = { viewModel.setClienteTab(ClienteTab.SOLICITAR) },
                    colors = ButtonDefaults.buttonColors(containerColor = AcaAzulConfianza),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                    modifier = Modifier.testTag("btn_nuevo_mandado_historial")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Nuevo", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // 2. Tarjetas de métricas rápidas (Resumen por estado: Pendientes, En Progreso, Entregados)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatusMetricCard(
                    title = "Pendientes",
                    count = pendientes.size,
                    icon = Icons.Default.HourglassTop,
                    color = Color(0xFFD97706),
                    bgColor = Color(0xFFFEF3C7),
                    isSelected = selectedFilter == EstadoEnvioFiltro.PENDIENTES,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        selectedFilter = if (selectedFilter == EstadoEnvioFiltro.PENDIENTES) EstadoEnvioFiltro.TODOS else EstadoEnvioFiltro.PENDIENTES
                    }
                )

                StatusMetricCard(
                    title = "En Progreso",
                    count = enProgreso.size,
                    icon = Icons.Default.TwoWheeler,
                    color = Color(0xFF0284C7),
                    bgColor = Color(0xFFE0F2FE),
                    isSelected = selectedFilter == EstadoEnvioFiltro.EN_PROGRESO,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        selectedFilter = if (selectedFilter == EstadoEnvioFiltro.EN_PROGRESO) EstadoEnvioFiltro.TODOS else EstadoEnvioFiltro.EN_PROGRESO
                    }
                )

                StatusMetricCard(
                    title = "Entregados",
                    count = entregados.size,
                    icon = Icons.Default.CheckCircle,
                    color = Color(0xFF059669),
                    bgColor = Color(0xFFDCFCE7),
                    isSelected = selectedFilter == EstadoEnvioFiltro.ENTREGADOS,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        selectedFilter = if (selectedFilter == EstadoEnvioFiltro.ENTREGADOS) EstadoEnvioFiltro.TODOS else EstadoEnvioFiltro.ENTREGADOS
                    }
                )
            }
        }

        // 3. Barra de búsqueda por folio o dirección
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Buscar por folio, destino o mandado...") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Buscar",
                        tint = AcaGrisSubtle
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotBlank()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Close, contentDescription = "Limpiar búsqueda")
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("campo_busqueda_mandados"),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedBorderColor = AcaAzulConfianza,
                    unfocusedBorderColor = Color(0xFFCBD5E1)
                ),
                singleLine = true
            )
        }

        // 4. Selector horizontal de pestañas de estado
        item {
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(EstadoEnvioFiltro.values()) { filtro ->
                    val isSelected = selectedFilter == filtro
                    val count = when (filtro) {
                        EstadoEnvioFiltro.TODOS -> allOrders.size
                        EstadoEnvioFiltro.PENDIENTES -> pendientes.size
                        EstadoEnvioFiltro.EN_PROGRESO -> enProgreso.size
                        EstadoEnvioFiltro.ENTREGADOS -> entregados.size
                        EstadoEnvioFiltro.CANCELADOS -> cancelados.size
                    }

                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = if (isSelected) AcaAzulConfianza else Color.White,
                        border = BorderStroke(
                            1.dp,
                            if (isSelected) AcaAzulConfianza else Color(0xFFCBD5E1)
                        ),
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .clickable { selectedFilter = filtro }
                            .testTag("tab_filtro_${filtro.name.lowercase()}")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "${filtro.label} ($count)",
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color.White else AcaGrisEquilibrio
                            )
                        }
                    }
                }
            }
        }

        // 5. Lista de Mandados según el filtro actual
        if (filteredOrders.isEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(AcaTurquesaContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = when (selectedFilter) {
                                    EstadoEnvioFiltro.PENDIENTES -> Icons.Default.HourglassEmpty
                                    EstadoEnvioFiltro.EN_PROGRESO -> Icons.Default.TwoWheeler
                                    EstadoEnvioFiltro.ENTREGADOS -> Icons.Default.CheckCircleOutline
                                    else -> Icons.Default.LocalShipping
                                },
                                contentDescription = null,
                                tint = AcaAzulConfianza,
                                modifier = Modifier.size(32.dp)
                            )
                        }

                        Text(
                            text = when (selectedFilter) {
                                EstadoEnvioFiltro.PENDIENTES -> "No tienes envíos pendientes"
                                EstadoEnvioFiltro.EN_PROGRESO -> "No hay mandados en ruta actualmente"
                                EstadoEnvioFiltro.ENTREGADOS -> "Aún no tienes mandados completados"
                                EstadoEnvioFiltro.CANCELADOS -> "No hay mandados cancelados"
                                else -> "No se encontraron mandados"
                            },
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = AcaAzulDark
                        )

                        Text(
                            text = "Solicita envíos de paquetes, compras en OXXO, medicinas de farmacia o comida local en Acapulco.",
                            fontSize = 12.sp,
                            color = AcaGrisSubtle,
                            textAlign = TextAlign.Center,
                            lineHeight = 16.sp
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Button(
                            onClick = { viewModel.setClienteTab(ClienteTab.SOLICITAR) },
                            colors = ButtonDefaults.buttonColors(containerColor = AcaAzulConfianza),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Pedir un Mandado Ahora", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }
            }
        } else {
            items(filteredOrders, key = { it.id }) { order ->
                ClienteHistorialOrderCard(
                    order = order,
                    driverLoc = driverLoc,
                    onOpenLiveTracking = { viewModel.openLiveTracking(order) },
                    onOpenWebTracking = { viewModel.openWebTrackingPreview(order) },
                    onReorder = { viewModel.reordenarMandado(order) },
                    onCancel = if (order.estado == "SOLICITADO") {
                        { viewModel.cancelarMandado(order.id) }
                    } else null,
                    onOpenRating = { ord -> viewModel.openRatingDialog(ord, isDriverRatingClient = false) },
                    onOpenDetail = { selectedOrderForDetail = order }
                )
            }
        }
    }
}

    // Modal de Detalle Completo de la Orden con Timeline
    selectedOrderForDetail?.let { order ->
        OrderDetailModalSheet(
            order = order,
            onDismiss = { selectedOrderForDetail = null },
            onOpenLiveTracking = {
                selectedOrderForDetail = null
                viewModel.openLiveTracking(order)
            },
            onOpenWebTracking = {
                selectedOrderForDetail = null
                viewModel.openWebTrackingPreview(order)
            },
            onReorder = {
                selectedOrderForDetail = null
                viewModel.reordenarMandado(order)
            }
        )
    }
}

/**
 * Tarjeta de estadísticas de estado en la parte superior.
 */
@Composable
private fun StatusMetricCard(
    title: String,
    count: Int,
    icon: ImageVector,
    color: Color,
    bgColor: Color,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = Color.White,
        border = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) color else Color(0xFFE2E8F0)
        ),
        shadowElevation = if (isSelected) 3.dp else 1.dp,
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(bgColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(16.dp)
                    )
                }

                Text(
                    text = count.toString(),
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Black,
                    color = AcaAzulDark
                )
            }

            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) color else AcaGrisSubtle
            )
        }
    }
}

/**
 * Tarjeta de cada mandado en el historial, destacando el estado actual:
 * PENDIENTE, EN PROGRESO o ENTREGADO.
 */
@Composable
private fun ClienteHistorialOrderCard(
    order: MandadoOrder,
    driverLoc: com.example.ui.DriverLocationState,
    onOpenLiveTracking: () -> Unit,
    onOpenWebTracking: () -> Unit,
    onReorder: () -> Unit,
    onCancel: (() -> Unit)?,
    onOpenRating: (MandadoOrder) -> Unit,
    onOpenDetail: () -> Unit
) {
    val dateFormatter = remember { SimpleDateFormat("dd MMM, hh:mm a", Locale("es", "MX")) }
    val formattedDate = remember(order.fechaCreacion) {
        dateFormatter.format(Date(order.fechaCreacion))
    }

    val tipoEnum = remember(order.tipo) {
        TipoMandado.values().find { it.id == order.tipo } ?: TipoMandado.EXPRESS
    }

    val isPendiente = order.estado == "SOLICITADO"
    val isEnProgreso = order.estado == "ACEPTADO" || order.estado == "ASIGNADO" || order.estado == "RECOGIDO" || order.estado == "EN_CAMINO" || order.estado == "EN_RUTA"
    val isEntregado = order.estado == "ENTREGADO" || order.estado == "COMPLETADO"
    val isCancelado = order.estado == "CANCELADO"

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(
            1.dp,
            when {
                isEnProgreso -> AcaTurquesa
                isPendiente -> Color(0xFFFCD34D)
                isEntregado -> Color(0xFF86EFAC)
                else -> Color(0xFFE2E8F0)
            }
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onOpenDetail)
            .testTag("card_mandado_${order.id}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // 1. Fila de Encabezado: Folio, Tipo de Mandado y Badge de Estado Actual
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(
                                when {
                                    isEnProgreso -> AcaTurquesaContainer
                                    isPendiente -> Color(0xFFFEF3C7)
                                    isEntregado -> Color(0xFFDCFCE7)
                                    else -> Color(0xFFF1F5F9)
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = when (tipoEnum) {
                                TipoMandado.DOCUMENTACION -> Icons.Default.LocalShipping
                                TipoMandado.FARMACIA -> Icons.Default.MedicalServices
                                TipoMandado.COMIDA -> Icons.Default.Restaurant
                                TipoMandado.COMPRAS -> Icons.Default.ShoppingCart
                                else -> Icons.Default.ElectricBolt
                            },
                            contentDescription = null,
                            tint = when {
                                isEnProgreso -> AcaAzulConfianza
                                isPendiente -> Color(0xFFD97706)
                                isEntregado -> Color(0xFF059669)
                                else -> AcaGrisSubtle
                            },
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Column {
                        Text(
                            text = "Folio #${order.folio.ifBlank { order.id.toString() }}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.5.sp,
                            color = AcaAzulDark
                        )
                        Text(
                            text = formattedDate,
                            fontSize = 11.sp,
                            color = AcaGrisSubtle
                        )
                    }
                }

                // Badge de Estado Actual destacado
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = when {
                        isPendiente -> Color(0xFFFEF3C7)
                        isEnProgreso -> Color(0xFFE0F2FE)
                        isEntregado -> Color(0xFFDCFCE7)
                        else -> Color(0xFFFEE2E2)
                    },
                    modifier = Modifier.testTag("badge_estado_${order.id}")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(
                                    when {
                                        isPendiente -> Color(0xFFD97706)
                                        isEnProgreso -> Color(0xFF0284C7)
                                        isEntregado -> Color(0xFF059669)
                                        else -> Color(0xFFDC2626)
                                    }
                                )
                        )
                        Text(
                            text = when {
                                isPendiente -> "⏳ Pendiente"
                                isEnProgreso -> "🛵 En Progreso"
                                isEntregado -> "✅ Entregado"
                                else -> "❌ Cancelado"
                            },
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = when {
                                isPendiente -> Color(0xFFB45309)
                                isEnProgreso -> Color(0xFF0369A1)
                                isEntregado -> Color(0xFF047857)
                                else -> Color(0xFFB91C1C)
                            }
                        )
                    }
                }
            }

            // 2. Título y Descripción del Servicio
            Column {
                Text(
                    text = order.titulo,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
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
            }

            // 3. Origen y Destino del Envío
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = Color(0xFFF8FAFC),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Default.Place, contentDescription = null, tint = AcaTurquesaDark, modifier = Modifier.size(16.dp))
                        Text(
                            text = "De: ${order.puntoRecogida} (${order.zonaRecogida})",
                            fontSize = 11.5.sp,
                            color = AcaAzulDark,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = AcaAzulConfianza, modifier = Modifier.size(16.dp))
                        Text(
                            text = "Para: ${order.puntoEntrega} (${order.zonaEntrega})",
                            fontSize = 11.5.sp,
                            color = AcaAzulDark,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // 4. Banner especial si está EN PROGRESO (PIN de seguridad y Repartidor)
            if (isEnProgreso) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFFEFF6FF),
                    border = BorderStroke(1.dp, Color(0xFFBFDBFE)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Repartidor: ${order.repartidorNombre ?: "Carlos Mendoza (Moto)"}",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1E3A8A)
                            )
                            Text(
                                text = "PIN para recibir tu paquete:",
                                fontSize = 10.sp,
                                color = AcaGrisSubtle
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = AcaAzulConfianza
                        ) {
                            Text(
                                text = order.pinSeguridad.ifBlank { "4892" },
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White,
                                letterSpacing = 2.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }
                }
            }

            // 5. Pie de la tarjeta: Tarifa y Botones de Acción según estado
            HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 1.dp)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Tarifa de Envío:",
                        fontSize = 10.sp,
                        color = AcaGrisSubtle
                    )
                    Text(
                        text = "$${String.format("%.2f", order.tarifaEnvio)} MXN",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = AcaAzulDark
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    // Acciones cuando está PENDIENTE
                    if (isPendiente) {
                        if (onCancel != null) {
                            OutlinedButton(
                                onClick = onCancel,
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFDC2626)),
                                border = BorderStroke(1.dp, Color(0xFFFCA5A5))
                            ) {
                                Text("Cancelar", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // Acciones cuando está EN PROGRESO
                    if (isEnProgreso) {
                        FilledTonalButton(
                            onClick = onOpenWebTracking,
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("WhatsApp", fontSize = 11.sp)
                        }

                        Button(
                            onClick = onOpenLiveTracking,
                            colors = ButtonDefaults.buttonColors(containerColor = AcaAzulConfianza),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Icon(Icons.Default.ShareLocation, contentDescription = null, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Rastreo GPS", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Acciones cuando está ENTREGADO
                    if (isEntregado) {
                        OutlinedButton(
                            onClick = onReorder,
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Icon(Icons.Default.Replay, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Pedir de nuevo", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        if (!order.calificadoPorCliente) {
                            Button(
                                onClick = { onOpenRating(order) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706)),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Icon(Icons.Default.Star, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Calificar", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Modal BottomSheet con el Detalle Completo del Envío y Timeline de 4 Pasos.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun OrderDetailModalSheet(
    order: MandadoOrder,
    onDismiss: () -> Unit,
    onOpenLiveTracking: () -> Unit,
    onOpenWebTracking: () -> Unit,
    onReorder: () -> Unit
) {
    val isPendiente = order.estado == "SOLICITADO"
    val isEnProgreso = order.estado == "ACEPTADO" || order.estado == "ASIGNADO" || order.estado == "RECOGIDO" || order.estado == "EN_CAMINO" || order.estado == "EN_RUTA"
    val isEntregado = order.estado == "ENTREGADO" || order.estado == "COMPLETADO"

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Detalle del Envío",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = AcaAzulDark
                    )
                    Text(
                        text = "Folio #${order.folio.ifBlank { order.id.toString() }}",
                        fontSize = 12.sp,
                        color = AcaGrisSubtle
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = when {
                        isPendiente -> Color(0xFFFEF3C7)
                        isEnProgreso -> Color(0xFFE0F2FE)
                        isEntregado -> Color(0xFFDCFCE7)
                        else -> Color(0xFFFEE2E2)
                    }
                ) {
                    Text(
                        text = when {
                            isPendiente -> "⏳ Pendiente"
                            isEnProgreso -> "🛵 En Progreso"
                            isEntregado -> "✅ Entregado"
                            else -> "❌ Cancelado"
                        },
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = when {
                            isPendiente -> Color(0xFFB45309)
                            isEnProgreso -> Color(0xFF0369A1)
                            isEntregado -> Color(0xFF047857)
                            else -> Color(0xFFB91C1C)
                        },
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            // Timeline de Estado del Mandado
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFF8FAFC),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Línea de Tiempo del Servicio:",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = AcaAzulDark
                    )

                    TimelineStep(
                        step = "1",
                        title = "Solicitud Creada",
                        desc = "Registrado en Acapulco",
                        isDone = true
                    )
                    TimelineStep(
                        step = "2",
                        title = "Repartidor Asignado",
                        desc = order.repartidorNombre ?: "Buscando chofer cercano",
                        isDone = !isPendiente
                    )
                    TimelineStep(
                        step = "3",
                        title = "Paquete Recogido / En Ruta",
                        desc = if (order.estado == "RECOGIDO" || isEnProgreso || isEntregado) "En camino a destino" else "Pendiente de recolección",
                        isDone = order.estado == "RECOGIDO" || order.estado == "EN_CAMINO" || order.estado == "EN_RUTA" || isEntregado
                    )
                    TimelineStep(
                        step = "4",
                        title = "Entregado con PIN Seguro",
                        desc = if (isEntregado) "Completado satisfactoriamente" else "Código de 4 dígitos pendiente",
                        isDone = isEntregado
                    )
                }
            }

            // Información de Origen y Destino
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Ruta del Mandado:", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = AcaAzulDark)
                Text("• Recogida: ${order.puntoRecogida} (${order.zonaRecogida})", fontSize = 12.sp, color = AcaGrisEquilibrio)
                Text("• Entrega: ${order.puntoEntrega} (${order.zonaEntrega})", fontSize = 12.sp, color = AcaGrisEquilibrio)
                if (order.referenciasDomicilio.isNotBlank()) {
                    Text("• Referencias: ${order.referenciasDomicilio}", fontSize = 11.5.sp, color = AcaGrisSubtle)
                }
            }

            // PIN y Acciones
            if (isEnProgreso) {
                Button(
                    onClick = onOpenLiveTracking,
                    colors = ButtonDefaults.buttonColors(containerColor = AcaAzulConfianza),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) {
                    Icon(Icons.Default.ShareLocation, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Ver Rastreo GPS en Vivo", fontWeight = FontWeight.Bold)
                }
            }

            if (isEntregado) {
                Button(
                    onClick = onReorder,
                    colors = ButtonDefaults.buttonColors(containerColor = AcaTurquesa),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) {
                    Icon(Icons.Default.Replay, contentDescription = null, tint = Color(0xFF00382E))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Pedir este Mandado de Nuevo", color = Color(0xFF00382E), fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}

@Composable
private fun TimelineStep(
    step: String,
    title: String,
    desc: String,
    isDone: Boolean
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(if (isDone) Color(0xFF059669) else Color(0xFFCBD5E1)),
            contentAlignment = Alignment.Center
        ) {
            if (isDone) {
                Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
            } else {
                Text(step, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }

        Column {
            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = if (isDone) FontWeight.Bold else FontWeight.Medium,
                color = if (isDone) AcaAzulDark else AcaGrisSubtle
            )
            Text(
                text = desc,
                fontSize = 10.5.sp,
                color = AcaGrisSubtle
            )
        }
    }
}
