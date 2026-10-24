package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.AdminDailyStats
import com.example.data.model.AdminTab
import com.example.data.model.DriverTelemetryInfo
import com.example.data.model.MacroZona
import com.example.data.model.MandadoOrder
import com.example.data.model.TipoMandado
import com.example.ui.MandadosViewModel
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Vista de Resumen para el Administrador de AcaMandados en Acapulco.
 * Proporciona supervisión en tiempo real de:
 *  1. Mandados activos y pedidos en curso (despacho, asignación, estados).
 *  2. Ubicación y telemetría de los repartidores (posicionamiento GPS en Acapulco).
 *  3. Estado general de las entregas del día (KPIs, SLA, tasa de éxito y volumen financiero).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    viewModel: MandadosViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val adminTab by viewModel.adminTab.collectAsStateWithLifecycle()
    val stats by viewModel.adminDailyStats.collectAsStateWithLifecycle()
    val activeOrders by viewModel.activeOrders.collectAsStateWithLifecycle()
    val allOrders by viewModel.allOrders.collectAsStateWithLifecycle()
    val fleetDrivers by viewModel.fleetDrivers.collectAsStateWithLifecycle()

    var activeOrderFilter by remember { mutableStateOf("TODOS") } // "TODOS", "SOLICITADO", "EN_RECOLECCION", "EN_RUTA"
    var driverFilterState by remember { mutableStateOf("TODOS") } // "TODOS", "DISPONIBLE", "EN_RUTA"
    var selectedDriverForMap by remember { mutableStateOf<DriverTelemetryInfo?>(null) }

    val filteredActiveOrders = remember(activeOrders, activeOrderFilter) {
        when (activeOrderFilter) {
            "SOLICITADO" -> activeOrders.filter { it.estado == "SOLICITADO" }
            "EN_RECOLECCION" -> activeOrders.filter { it.estado == "EN_RECOLECCION" }
            "EN_RUTA" -> activeOrders.filter { it.estado == "EN_RUTA" }
            else -> activeOrders
        }
    }

    val filteredDrivers = remember(fleetDrivers, driverFilterState) {
        when (driverFilterState) {
            "DISPONIBLE" -> fleetDrivers.filter { it.estado == "DISPONIBLE" }
            "EN_RUTA" -> fleetDrivers.filter { it.estado == "EN_RUTA" || it.estado == "EN_RECOLECCION" }
            else -> fleetDrivers
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .testTag("screen_admin_dashboard")
    ) {
        // 1. Header de Control Administrativo
        AdminTopHeader(
            activeOrdersCount = activeOrders.size,
            driversCount = fleetDrivers.count { it.estado != "DESCONECTADO" },
            onSimulateOrder = {
                viewModel.simularNuevoMandadoCliente()
                Toast.makeText(context, "Mandado de prueba simulado en Acapulco", Toast.LENGTH_SHORT).show()
            }
        )

        // 2. Barra de Pestañas de Navegación del Administrador
        PrimaryTabRow(
            selectedTabIndex = adminTab.ordinal,
            containerColor = Color.White,
            contentColor = AcaAzulDark,
            modifier = Modifier.fillMaxWidth()
        ) {
            Tab(
                selected = adminTab == AdminTab.RESUMEN_METRICAS,
                onClick = { viewModel.setAdminTab(AdminTab.RESUMEN_METRICAS) },
                text = {
                    Text(
                        text = "📊 Resumen Hoy",
                        fontWeight = if (adminTab == AdminTab.RESUMEN_METRICAS) FontWeight.Bold else FontWeight.Normal,
                        fontSize = 12.sp
                    )
                },
                modifier = Modifier.testTag("tab_admin_resumen")
            )
            Tab(
                selected = adminTab == AdminTab.MANDADOS_ACTIVOS,
                onClick = { viewModel.setAdminTab(AdminTab.MANDADOS_ACTIVOS) },
                text = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "📦 Activos",
                            fontWeight = if (adminTab == AdminTab.MANDADOS_ACTIVOS) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 12.sp
                        )
                        if (activeOrders.isNotEmpty()) {
                            Badge(containerColor = Color(0xFFEF4444), contentColor = Color.White) {
                                Text(activeOrders.size.toString(), fontSize = 10.sp)
                            }
                        }
                    }
                },
                modifier = Modifier.testTag("tab_admin_mandados")
            )
            Tab(
                selected = adminTab == AdminTab.UBICACION_REPARTIDORES,
                onClick = { viewModel.setAdminTab(AdminTab.UBICACION_REPARTIDORES) },
                text = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "🛵 Repartidores",
                            fontWeight = if (adminTab == AdminTab.UBICACION_REPARTIDORES) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 12.sp
                        )
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFF10B981),
                            modifier = Modifier.size(8.dp)
                        ) {}
                    }
                },
                modifier = Modifier.testTag("tab_admin_repartidores")
            )
        }

        // 3. Contenido según la pestaña seleccionada
        Box(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
        ) {
            when (adminTab) {
                AdminTab.RESUMEN_METRICAS -> {
                    AdminResumenMetricasView(
                        stats = stats,
                        allOrders = allOrders,
                        activeOrders = activeOrders,
                        onNavigateToActiveOrders = { viewModel.setAdminTab(AdminTab.MANDADOS_ACTIVOS) },
                        onNavigateToDrivers = { viewModel.setAdminTab(AdminTab.UBICACION_REPARTIDORES) }
                    )
                }
                AdminTab.MANDADOS_ACTIVOS -> {
                    AdminMandadosActivosView(
                        activeOrders = filteredActiveOrders,
                        selectedFilter = activeOrderFilter,
                        onSelectFilter = { activeOrderFilter = it },
                        onTrackOrder = { order -> viewModel.openLiveTracking(order) },
                        onViewWebTracking = { order -> viewModel.openWebTrackingPreview(order) },
                        onSimulateOrder = { viewModel.simularNuevoMandadoCliente() }
                    )
                }
                AdminTab.UBICACION_REPARTIDORES -> {
                    AdminUbicacionRepartidoresView(
                        drivers = filteredDrivers,
                        selectedFilter = driverFilterState,
                        onSelectFilter = { driverFilterState = it },
                        selectedDriver = selectedDriverForMap,
                        onSelectDriver = { selectedDriverForMap = it },
                        onCallDriver = { phone ->
                            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone"))
                            context.startActivity(intent)
                        },
                        onOpenActiveOrder = { folio ->
                            val order = activeOrders.find { it.folio.equals(folio, ignoreCase = true) }
                            if (order != null) {
                                viewModel.openLiveTracking(order)
                            } else {
                                Toast.makeText(context, "Mandado $folio en curso", Toast.LENGTH_SHORT).show()
                            }
                        }
                    )
                }
            }
        }
    }
}

/**
 * Encabezado superior del Administrador con estado de operaciones y accesos rápidos.
 */
@Composable
private fun AdminTopHeader(
    activeOrdersCount: Int,
    driversCount: Int,
    onSimulateOrder: () -> Unit
) {
    Surface(
        color = Color.White,
        shadowElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFEF3C7)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AdminPanelSettings,
                        contentDescription = "Admin",
                        tint = Color(0xFFB45309),
                        modifier = Modifier.size(22.dp)
                    )
                }
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "Centro de Operaciones",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 15.sp,
                            color = AcaAzulDark
                        )
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Color(0xFFDCFCE7)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                Surface(shape = CircleShape, color = Color(0xFF16A34A), modifier = Modifier.size(6.dp)) {}
                                Text(
                                    text = "EN VIVO",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFF16A34A)
                                )
                            }
                        }
                    }
                    Text(
                        text = "Acapulco • $activeOrdersCount activos • $driversCount repartidores",
                        fontSize = 11.sp,
                        color = AcaGrisSubtle
                    )
                }
            }

            FilledTonalButton(
                onClick = onSimulateOrder,
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = AcaTurquesaContainer,
                    contentColor = AcaTurquesaDark
                ),
                modifier = Modifier.testTag("btn_admin_simular_mandado")
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(15.dp))
                Spacer(modifier = Modifier.width(3.dp))
                Text("+ Mandado", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

/**
 * Pestaña 1: Resumen General y Estado de las Entregas del Día (KPIs y SLA).
 */
@Composable
private fun AdminResumenMetricasView(
    stats: AdminDailyStats,
    allOrders: List<MandadoOrder>,
    activeOrders: List<MandadoOrder>,
    onNavigateToActiveOrders: () -> Unit,
    onNavigateToDrivers: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dateFormatter = remember { SimpleDateFormat("EEEE, dd 'de' MMMM yyyy", Locale("es", "MX")) }
    val todayDate = remember { dateFormatter.format(Date()).replaceFirstChar { it.uppercase() } }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Banner de Estado General de la Jornada
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = AcaAzulDark),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Estado General de Entregas",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                text = todayDate,
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 11.sp
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = AcaTurquesa.copy(alpha = 0.25f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(Icons.Default.Verified, contentDescription = null, tint = AcaTurquesaLight, modifier = Modifier.size(14.dp))
                                Text(
                                    text = "SLA: ${stats.tasaExitoPorcentaje}%",
                                    color = AcaTurquesaLight,
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    HorizontalDivider(color = Color.White.copy(alpha = 0.15f))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Tiempo promedio de entrega", color = Color.White.copy(alpha = 0.75f), fontSize = 10.5.sp)
                            Text("~${stats.tiempoPromedioMinutos} min en moto", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text("Volumen transaccionado", color = Color.White.copy(alpha = 0.75f), fontSize = 10.5.sp)
                            Text("$${String.format("%.2f", stats.totalTransaccionadoMxn)} MXN", color = Color(0xFF34D399), fontSize = 16.sp, fontWeight = FontWeight.Black)
                        }
                    }
                }
            }
        }

        // Métricas Principales (Grid de KPIs 2x3)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Métricas Clave del Día",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = AcaAzulDark
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AdminKpiCard(
                        title = "Total Pedidos",
                        value = stats.totalPedidosHoy.toString(),
                        subtitle = "registrados hoy",
                        icon = Icons.Default.AllInbox,
                        iconTint = AcaAzulConfianza,
                        modifier = Modifier.weight(1f)
                    )
                    AdminKpiCard(
                        title = "Completados",
                        value = stats.pedidosEntregadosHoy.toString(),
                        subtitle = "${stats.tasaExitoPorcentaje}% de éxito",
                        icon = Icons.Default.CheckCircle,
                        iconTint = Color(0xFF10B981),
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AdminKpiCard(
                        title = "En Curso",
                        value = stats.pedidosActivosHoy.toString(),
                        subtitle = "en ruta / recolección",
                        icon = Icons.Default.TwoWheeler,
                        iconTint = Color(0xFF6366F1),
                        onClick = onNavigateToActiveOrders,
                        modifier = Modifier.weight(1f)
                    )
                    AdminKpiCard(
                        title = "Por Asignar",
                        value = stats.pedidosPendientesAsignacion.toString(),
                        subtitle = if (stats.pedidosPendientesAsignacion > 0) "⚠️ Requieren atención" else "Al día",
                        icon = Icons.Default.HourglassTop,
                        iconTint = if (stats.pedidosPendientesAsignacion > 0) Color(0xFFEA580C) else AcaGrisSubtle,
                        onClick = onNavigateToActiveOrders,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AdminKpiCard(
                        title = "Tarifas de Envíos",
                        value = "$${stats.totalIngresosEnvioMxn.toInt()}",
                        subtitle = "ingresos por flete",
                        icon = Icons.Default.Paid,
                        iconTint = Color(0xFF047857),
                        modifier = Modifier.weight(1f)
                    )
                    AdminKpiCard(
                        title = "Flota en Acapulco",
                        value = "${stats.repartidoresConectados}",
                        subtitle = "${stats.repartidoresEnRuta} en ruta • ${stats.repartidoresDisponibles} disp.",
                        icon = Icons.Default.Navigation,
                        iconTint = AcaTurquesaDark,
                        onClick = onNavigateToDrivers,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Distribución Geográfica por MacroZonas de Acapulco
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Default.ShareLocation, contentDescription = null, tint = AcaTurquesaDark, modifier = Modifier.size(18.dp))
                            Text(
                                text = "Distribución por Zonas de Acapulco",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.5.sp,
                                color = AcaAzulDark
                            )
                        }
                        Text(
                            text = "${allOrders.size} mandados",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = AcaGrisSubtle
                        )
                    }

                    MacroZona.values().forEach { macro ->
                        val count = allOrders.count { order ->
                            when (macro) {
                                MacroZona.ZONA_A -> order.zonaEntrega.contains("Costera") || order.zonaEntrega.contains("Magallanes") || order.zonaEntrega.contains("Centro") || order.zonaEntrega.contains("Farallón")
                                MacroZona.ZONA_B -> order.zonaEntrega.contains("Diamante") || order.zonaEntrega.contains("Marqués") || order.zonaEntrega.contains("Barra")
                                MacroZona.ZONA_C -> order.zonaEntrega.contains("Pie") || order.zonaEntrega.contains("Cuesta") || order.zonaEntrega.contains("Poniente")
                                MacroZona.ZONA_D -> order.zonaEntrega.contains("Renacimiento") || order.zonaEntrega.contains("Zapata")
                            }
                        }
                        val fraction = if (allOrders.isNotEmpty()) count.toFloat() / allOrders.size.toFloat() else 0.25f

                        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(macro.titulo, fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold, color = AcaAzulDark)
                                Text("$count pedidos (${(fraction * 100).toInt()}%)", fontSize = 11.sp, color = AcaGrisSubtle)
                            }
                            LinearProgressIndicator(
                                progress = { fraction.coerceIn(0.05f, 1f) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = when (macro) {
                                    MacroZona.ZONA_A -> AcaAzulConfianza
                                    MacroZona.ZONA_B -> AcaTurquesa
                                    MacroZona.ZONA_C -> Color(0xFFF59E0B)
                                    MacroZona.ZONA_D -> Color(0xFF6366F1)
                                },
                                trackColor = Color(0xFFF1F5F9)
                            )
                        }
                    }
                }
            }
        }

        // Alertas Operativas en Tiempo Real
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2)),
                border = BorderStroke(1.dp, Color(0xFFFCA5A5)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = Color(0xFFDC2626), modifier = Modifier.size(18.dp))
                        Text(
                            text = "Alertas del Sistema y Tráfico",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = Color(0xFF991B1B)
                        )
                    }

                    Text(
                        text = "• Alta afluencia turística en Costera Miguel Alemán (Diana Cazadora y Papagayo). Las rutas en moto registran fluidez normal.\n" +
                               "• 100% de los mandados asignados con PIN de seguridad activo.",
                        fontSize = 11.5.sp,
                        color = Color(0xFF7F1D1D),
                        lineHeight = 16.sp
                    )
                }
            }
        }
    }
}

/**
 * Pestaña 2: Vista de Mandados Activos en Curso (Filtros, estado y detalle).
 */
@Composable
private fun AdminMandadosActivosView(
    activeOrders: List<MandadoOrder>,
    selectedFilter: String,
    onSelectFilter: (String) -> Unit,
    onTrackOrder: (MandadoOrder) -> Unit,
    onViewWebTracking: (MandadoOrder) -> Unit,
    onSimulateOrder: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Barra de Filtros Rápidos
        item {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Filtrar por Estado de Entrega:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = AcaAzulDark
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    contentPadding = PaddingValues(horizontal = 2.dp)
                ) {
                    val filterOptions = listOf(
                        "TODOS" to "Todos (${activeOrders.size})",
                        "SOLICITADO" to "⏳ Pendientes",
                        "EN_RECOLECCION" to "📦 En Recolección",
                        "EN_RUTA" to "🚀 En Ruta"
                    )
                    items(filterOptions) { (key, label) ->
                        FilterChip(
                            selected = selectedFilter == key,
                            onClick = { onSelectFilter(key) },
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

        if (activeOrders.isEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircleOutline,
                            contentDescription = null,
                            tint = Color(0xFF10B981),
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            text = "Sin mandados activos en este estado",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = AcaAzulDark
                        )
                        Text(
                            text = "Todos los pedidos actuales han sido entregados o no hay nuevos mandados con este filtro.",
                            fontSize = 11.5.sp,
                            color = AcaGrisSubtle,
                            textAlign = TextAlign.Center
                        )
                        Button(
                            onClick = onSimulateOrder,
                            colors = ButtonDefaults.buttonColors(containerColor = AcaTurquesaDark),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("+ Generar Mandado de Prueba", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        } else {
            items(activeOrders, key = { it.id }) { order ->
                AdminActiveOrderCard(
                    order = order,
                    onTrackOrder = { onTrackOrder(order) },
                    onViewWebTracking = { onViewWebTracking(order) }
                )
            }
        }
    }
}

/**
 * Tarjeta individual de Mandado Activo para el Administrador.
 */
@Composable
private fun AdminActiveOrderCard(
    order: MandadoOrder,
    onTrackOrder: () -> Unit,
    onViewWebTracking: () -> Unit,
    modifier: Modifier = Modifier
) {
    val tipoEnum = TipoMandado.values().find { it.id == order.tipo } ?: TipoMandado.EXPRESS

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, if (order.estado == "SOLICITADO") Color(0xFFF59E0B) else Color(0xFFE2E8F0)),
        modifier = modifier
            .fillMaxWidth()
            .testTag("admin_active_order_card_${order.id}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Header del Mandado: Folio, Tipo y Estado
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = order.folio,
                        fontWeight = FontWeight.Black,
                        fontSize = 14.sp,
                        color = AcaAzulDark
                    )
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = Color(0xFFEFF6FF)
                    ) {
                        Text(
                            text = tipoEnum.label,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1D4ED8),
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                        )
                    }
                }

                // Badge de Estado
                val (estadoLabel, estadoBg, estadoColor) = when (order.estado) {
                    "SOLICITADO" -> Triple("⏳ Pendiente Asignar", Color(0xFFFEF3C7), Color(0xFFB45309))
                    "ACEPTADO" -> Triple("🛵 Aceptado", Color(0xFFE0F2FE), Color(0xFF0369A1))
                    "EN_RECOLECCION" -> Triple("📦 En Recolección", Color(0xFFEDE9FE), Color(0xFF6D28D9))
                    "EN_RUTA" -> Triple("🚀 En Ruta Entrega", Color(0xFFDCFCE7), Color(0xFF047857))
                    else -> Triple(order.estado, Color(0xFFF1F5F9), AcaGrisEquilibrio)
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = estadoBg
                ) {
                    Text(
                        text = estadoLabel,
                        color = estadoColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                    )
                }
            }

            // Datos de Ruta (Origen ➔ Destino)
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Surface(shape = CircleShape, color = Color(0xFF10B981), modifier = Modifier.size(8.dp)) {}
                    Text(
                        text = "Recoger: ${order.puntoRecogida} (${order.zonaRecogida})",
                        fontSize = 11.5.sp,
                        color = AcaGrisEquilibrio,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Surface(shape = CircleShape, color = Color(0xFFEF4444), modifier = Modifier.size(8.dp)) {}
                    Text(
                        text = "Entregar: ${order.puntoEntrega} (${order.zonaEntrega})",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = AcaAzulDark,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 1.dp)

            // Datos del Repartidor, Cliente y PIN
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Cliente: ${order.clienteNombre}",
                        fontSize = 10.5.sp,
                        color = AcaGrisSubtle
                    )
                    Text(
                        text = if (order.repartidorNombre != null) "Repartidor: ${order.repartidorNombre}" else "⚠️ Sin repartidor asignado",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (order.repartidorNombre != null) AcaAzulDark else Color(0xFFDC2626)
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFFF1F5F9)
                    ) {
                        Text(
                            text = "PIN: ${order.pinSeguridad}",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = AcaAzulDark,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    Text(
                        text = "$${order.tarifaEnvio.toInt()} MXN",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF047857)
                    )
                }
            }

            // Botones de Acción de Administrador
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                OutlinedButton(
                    onClick = onViewWebTracking,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(vertical = 4.dp),
                    border = BorderStroke(1.dp, AcaAzulConfianza)
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(13.dp), tint = AcaAzulConfianza)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Link Web", fontSize = 11.sp, color = AcaAzulConfianza, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = onTrackOrder,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = AcaAzulDark),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(vertical = 4.dp)
                ) {
                    Icon(Icons.Default.Navigation, contentDescription = null, modifier = Modifier.size(13.dp), tint = Color.White)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Rastrear GPS", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

/**
 * Pestaña 3: Ubicación y Telemetría de los Repartidores en Acapulco.
 */
@Composable
private fun AdminUbicacionRepartidoresView(
    drivers: List<DriverTelemetryInfo>,
    selectedFilter: String,
    onSelectFilter: (String) -> Unit,
    selectedDriver: DriverTelemetryInfo?,
    onSelectDriver: (DriverTelemetryInfo) -> Unit,
    onCallDriver: (String) -> Unit,
    onOpenActiveOrder: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Radar Gráfico Interactivo de la Bahía de Acapulco
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("admin_radar_mapa_acapulco")
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
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Default.Radar, contentDescription = null, tint = AcaTurquesaLight, modifier = Modifier.size(18.dp))
                            Text(
                                text = "Radar de Flota • Bahía de Acapulco",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.5.sp
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFF10B981).copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = "${drivers.size} motos activas",
                                color = Color(0xFF34D399),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    // Canvas Visualizador del Radar de Acapulco
                    AcapulcoBayRadarCanvas(
                        drivers = drivers,
                        selectedDriver = selectedDriver,
                        onSelectDriver = onSelectDriver,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF020617))
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("📍 Costera • Magallanes • Diamante • Caleta", color = Color.White.copy(alpha = 0.6f), fontSize = 10.sp)
                        Text("Actualización en vivo cada 3s", color = AcaTurquesaLight, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Filtro de Repartidores
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Repartidores Conectados (${drivers.size})",
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = AcaAzulDark
                )

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("TODOS" to "Todos", "DISPONIBLE" to "Disp.", "EN_RUTA" to "En Ruta").forEach { (key, label) ->
                        FilterChip(
                            selected = selectedFilter == key,
                            onClick = { onSelectFilter(key) },
                            label = { Text(label, fontSize = 10.5.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = AcaTurquesaDark,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
            }
        }

        // Lista de Repartidores y su Telemetría
        items(drivers, key = { it.id }) { driver ->
            AdminDriverTelemetryCard(
                driver = driver,
                isSelected = selectedDriver?.id == driver.id,
                onClick = { onSelectDriver(driver) },
                onCall = { onCallDriver(driver.telefono) },
                onOpenOrder = { folio -> onOpenActiveOrder(folio) }
            )
        }
    }
}

/**
 * Radar interactivo en Canvas que dibuja la bahía de Acapulco y los puntos de los repartidores.
 */
@Composable
private fun AcapulcoBayRadarCanvas(
    drivers: List<DriverTelemetryInfo>,
    selectedDriver: DriverTelemetryInfo?,
    onSelectDriver: (DriverTelemetryInfo) -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "radarPulse")
    val pulseRadius by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse"
    )

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height

        // Círculos concéntricos de radar
        val center = Offset(width * 0.5f, height * 0.5f)
        val maxR = minOf(width, height) * 0.45f

        drawCircle(
            color = Color(0xFF0F766E).copy(alpha = 0.35f),
            radius = maxR,
            center = center,
            style = Stroke(width = 1.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f))
        )
        drawCircle(
            color = Color(0xFF0F766E).copy(alpha = 0.25f),
            radius = maxR * 0.65f,
            center = center,
            style = Stroke(width = 1.dp.toPx())
        )
        drawCircle(
            color = Color(0xFF0F766E).copy(alpha = 0.15f),
            radius = maxR * 0.3f,
            center = center,
            style = Stroke(width = 1.dp.toPx())
        )

        // Onda expansiva de barrido
        drawCircle(
            color = Color(0xFF14B8A6).copy(alpha = (1f - pulseRadius) * 0.3f),
            radius = maxR * pulseRadius,
            center = center,
            style = Stroke(width = 2.dp.toPx())
        )

        // Líneas axiales
        drawLine(
            color = Color(0xFF0F766E).copy(alpha = 0.2f),
            start = Offset(center.x, center.y - maxR),
            end = Offset(center.x, center.y + maxR),
            strokeWidth = 1.dp.toPx()
        )
        drawLine(
            color = Color(0xFF0F766E).copy(alpha = 0.2f),
            start = Offset(center.x - maxR, center.y),
            end = Offset(center.x + maxR, center.y),
            strokeWidth = 1.dp.toPx()
        )

        // Dibujar posiciones de los repartidores en el canvas escaladas para Acapulco
        // Rango aproximado: lat 16.78 a 16.91, lng -99.96 a -99.78
        val minLat = 16.77
        val maxLat = 16.92
        val minLng = -99.97
        val maxLng = -99.77

        drivers.forEach { driver ->
            val normX = ((driver.lng - minLng) / (maxLng - minLng)).coerceIn(0.1, 0.9).toFloat()
            // Invertir Y porque latitud norte crece hacia arriba
            val normY = (1f - ((driver.lat - minLat) / (maxLat - minLat)).coerceIn(0.1, 0.9)).toFloat()

            val posX = width * (0.15f + normX * 0.7f)
            val posY = height * (0.15f + normY * 0.7f)
            val isSelected = selectedDriver?.id == driver.id

            val markerColor = when (driver.estado) {
                "DISPONIBLE" -> Color(0xFF10B981)
                "EN_RUTA" -> Color(0xFF38BDF8)
                "EN_RECOLECCION" -> Color(0xFFA855F7)
                else -> Color(0xFF94A3B8)
            }

            // Halo si está seleccionado
            if (isSelected) {
                drawCircle(
                    color = Color.White.copy(alpha = 0.4f),
                    radius = 12.dp.toPx(),
                    center = Offset(posX, posY)
                )
            }

            // Punto del repartidor
            drawCircle(
                color = markerColor,
                radius = if (isSelected) 6.dp.toPx() else 4.5.dp.toPx(),
                center = Offset(posX, posY)
            )
            drawCircle(
                color = Color.White,
                radius = 2.dp.toPx(),
                center = Offset(posX, posY)
            )
        }
    }
}

/**
 * Tarjeta individual de telemetría de Repartidor en la lista del Administrador.
 */
@Composable
private fun AdminDriverTelemetryCard(
    driver: DriverTelemetryInfo,
    isSelected: Boolean,
    onClick: () -> Unit,
    onCall: () -> Unit,
    onOpenOrder: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(
            1.5.dp,
            if (isSelected) AcaTurquesaDark else Color(0xFFE2E8F0)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 4.dp else 1.dp),
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .testTag("admin_driver_card_${driver.id}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Fila Superior: Nombre, Moto, Estado
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
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(if (driver.estado == "DISPONIBLE") Color(0xFFDCFCE7) else Color(0xFFE0F2FE)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.TwoWheeler,
                            contentDescription = null,
                            tint = if (driver.estado == "DISPONIBLE") Color(0xFF047857) else Color(0xFF0369A1),
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = driver.nombre,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.5.sp,
                                color = AcaAzulDark
                            )
                            Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(13.dp))
                            Text("${driver.calificacion}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFB45309))
                        }
                        Text(
                            text = "${driver.vehiculo} • ${driver.placas}",
                            fontSize = 11.sp,
                            color = AcaGrisSubtle
                        )
                    }
                }

                // Badge de Estado del Repartidor
                val (statusText, statusBg, statusColor) = when (driver.estado) {
                    "DISPONIBLE" -> Triple("🟢 DISPONIBLE", Color(0xFFDCFCE7), Color(0xFF047857))
                    "EN_RUTA" -> Triple("🚀 EN RUTA", Color(0xFFE0F2FE), Color(0xFF0369A1))
                    "EN_RECOLECCION" -> Triple("📦 RECOGIENDO", Color(0xFFEDE9FE), Color(0xFF6D28D9))
                    else -> Triple("⚪ DESCONECTADO", Color(0xFFF1F5F9), AcaGrisSubtle)
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = statusBg
                ) {
                    Text(
                        text = statusText,
                        color = statusColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                    )
                }
            }

            // Datos de Telemetría: Ubicación en Acapulco, Velocidad, Batería
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFFF8FAFC),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(Icons.Default.Place, contentDescription = null, tint = AcaTurquesaDark, modifier = Modifier.size(13.dp))
                        Text(
                            text = driver.zonaNombre,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = AcaAzulDark
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                            Icon(Icons.Default.Speed, contentDescription = null, tint = AcaAzulConfianza, modifier = Modifier.size(13.dp))
                            Text("${driver.velocidadKmh} km/h", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = AcaAzulConfianza)
                        }

                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                            Icon(Icons.Default.BatteryChargingFull, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(13.dp))
                            Text("${driver.bateriaPorcentaje}%", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF065F46))
                        }
                    }
                }
            }

            // Si tiene mandado asignado
            if (driver.mandadoFolioAsignado != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFFEFF6FF))
                        .clickable { onOpenOrder(driver.mandadoFolioAsignado) }
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(Icons.Default.Assignment, contentDescription = null, tint = Color(0xFF2563EB), modifier = Modifier.size(13.dp))
                        Text("Mandado Asignado: ${driver.mandadoFolioAsignado}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1D4ED8))
                    }
                    Text("Ver ruta >", fontSize = 10.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2563EB))
                }
            }

            // Botones de acción rápida: Llamar / Mensaje
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${driver.pedidosHoy} viajes hoy • ${driver.pedidosCompletadosTotal} en total",
                    fontSize = 10.5.sp,
                    color = AcaGrisSubtle
                )

                OutlinedButton(
                    onClick = onCall,
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                    border = BorderStroke(1.dp, Color(0xFFCBD5E1))
                ) {
                    Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(13.dp), tint = AcaAzulDark)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Llamar", fontSize = 11.sp, color = AcaAzulDark)
                }
            }
        }
    }
}

/**
 * Tarjeta de Métrica KPI para la vista de Resumen del Administrador.
 */
@Composable
private fun AdminKpiCard(
    title: String,
    value: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = modifier
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = AcaGrisSubtle
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(18.dp)
                )
            }

            Text(
                text = value,
                fontSize = 20.sp,
                fontWeight = FontWeight.Black,
                color = AcaAzulDark
            )

            Text(
                text = subtitle,
                fontSize = 10.sp,
                color = AcaGrisSubtle,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
