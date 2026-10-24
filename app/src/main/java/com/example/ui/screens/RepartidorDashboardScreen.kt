package com.example.ui.screens

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
import com.example.data.model.AcapulcoLocations
import com.example.data.model.MandadoOrder
import com.example.data.model.TipoMandado
import com.example.ui.MandadosViewModel
import com.example.ui.RepartidorTab
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class DashboardFilter(val label: String) {
    TODOS("Todos"),
    CERCANOS("Cercanos (< 4 km)"),
    CERCANOS_2KM("⚡ Muy Cerca (< 2 km)"),
    CERCANOS_8KM("🛵 Zona (< 8 km)"),
    MAYOR_PAGO("Mayor Pago (> $65)"),
    EFECTIVO("Efectivo"),
    TARJETA("Tarjeta")
}

enum class DashboardSort(val label: String) {
    DISTANCIA("Más Cercano"),
    PAGO("Mayor Ganancia"),
    RECIENTE("Más Reciente")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RepartidorDashboardScreen(
    viewModel: MandadosViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val availableOrders by viewModel.availableOrders.collectAsStateWithLifecycle()
    val driverProfile by viewModel.repartidorProfile.collectAsStateWithLifecycle()
    val driverLoc by viewModel.driverLocationState.collectAsStateWithLifecycle()
    val activeDriverOrder by viewModel.currentDriverActiveOrder.collectAsStateWithLifecycle()
    val completedOrders by viewModel.driverCompletedOrders.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()

    val profile = driverProfile
    val isSubscribed = profile?.suscripcionActiva == true
    val isOnline = profile?.disponibleParaMandados == true

    var selectedFilter by remember { mutableStateOf(DashboardFilter.TODOS) }
    var selectedSort by remember { mutableStateOf(DashboardSort.DISTANCIA) }
    var maxRadiusKm by remember { mutableFloatStateOf(20f) }
    var showLocationBasePicker by remember { mutableStateOf(false) }

    val currentBaseZoneName = remember(driverLoc.lat, driverLoc.lng) {
        val closest = AcapulcoLocations.ZONAS.minByOrNull { zona ->
            AcapulcoLocations.calcularDistanciaKm(driverLoc.lat, driverLoc.lng, zona.lat, zona.lng)
        }
        closest?.nombre ?: "Magallanes & Galerías"
    }

    // Métricas de la jornada de hoy
    val totalEarningsToday = remember(completedOrders) {
        completedOrders.sumOf { it.tarifaEnvio }
    }
    val completedTripsToday = completedOrders.size

    val dateFormatter = remember { SimpleDateFormat("dd MMM yyyy", Locale("es", "MX")) }

    // Filtrar y ordenar los pedidos entrantes por cercanía y filtros
    val processedOrders = remember(availableOrders, selectedFilter, selectedSort, driverLoc, maxRadiusKm) {
        val filtered = when (selectedFilter) {
            DashboardFilter.TODOS -> availableOrders
            DashboardFilter.CERCANOS -> availableOrders.filter { order ->
                val dist = AcapulcoLocations.calcularDistanciaKm(driverLoc.lat, driverLoc.lng, order.latRecogida, order.lngRecogida)
                dist <= 4.0
            }
            DashboardFilter.CERCANOS_2KM -> availableOrders.filter { order ->
                val dist = AcapulcoLocations.calcularDistanciaKm(driverLoc.lat, driverLoc.lng, order.latRecogida, order.lngRecogida)
                dist <= 2.0
            }
            DashboardFilter.CERCANOS_8KM -> availableOrders.filter { order ->
                val dist = AcapulcoLocations.calcularDistanciaKm(driverLoc.lat, driverLoc.lng, order.latRecogida, order.lngRecogida)
                dist <= 8.0
            }
            DashboardFilter.MAYOR_PAGO -> availableOrders.filter { it.tarifaEnvio >= 65.0 }
            DashboardFilter.EFECTIVO -> availableOrders.filter { it.metodoPago == "EFECTIVO" }
            DashboardFilter.TARJETA -> availableOrders.filter { it.metodoPago == "TARJETA" }
        }

        val radiusFiltered = if (maxRadiusKm < 20f) {
            filtered.filter { order ->
                AcapulcoLocations.calcularDistanciaKm(driverLoc.lat, driverLoc.lng, order.latRecogida, order.lngRecogida) <= maxRadiusKm
            }
        } else {
            filtered
        }

        when (selectedSort) {
            DashboardSort.DISTANCIA -> radiusFiltered.sortedBy { order ->
                AcapulcoLocations.calcularDistanciaKm(driverLoc.lat, driverLoc.lng, order.latRecogida, order.lngRecogida)
            }
            DashboardSort.PAGO -> radiusFiltered.sortedByDescending { it.tarifaEnvio }
            DashboardSort.RECIENTE -> radiusFiltered.sortedByDescending { it.fechaCreacion }
        }
    }

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = { viewModel.refreshData() },
        modifier = modifier
            .fillMaxSize()
            .testTag("pull_to_refresh_repartidor")
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(AcaBackground)
                .testTag("screen_repartidor_dashboard"),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 95.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
        // 1. Tarjeta de Estado del Repartidor (En línea / Conectado)
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                border = BorderStroke(1.dp, if (isOnline) Color(0xFF10B981).copy(alpha = 0.5f) else Color(0xFFE2E8F0)),
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
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Indicador luminoso con pulso si está en línea
                            Box(
                                modifier = Modifier
                                    .size(14.dp)
                                    .clip(CircleShape)
                                    .background(if (isOnline) Color(0xFF10B981) else Color(0xFF94A3B8))
                            )

                            Column {
                                Text(
                                    text = if (isOnline) "🟢 En Línea y Disponible" else "⚪ Desconectado",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 15.sp,
                                    color = if (isOnline) Color(0xFF065F46) else AcaGrisSubtle
                                )
                                Text(
                                    text = if (isOnline) "Recibiendo mandados en tiempo real en Acapulco" else "Activa el switch para recibir nuevos viajes",
                                    fontSize = 11.sp,
                                    color = AcaGrisSubtle
                                )
                            }
                        }

                        Switch(
                            checked = isOnline,
                            onCheckedChange = { nuevoEstado ->
                                viewModel.setDisponibilidad(nuevoEstado)
                                if (nuevoEstado) {
                                    Toast.makeText(context, "¡Conectado! Listo para recibir mandados.", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = Color(0xFF059669)
                            ),
                            modifier = Modifier.testTag("switch_driver_online")
                        )
                    }

                    // Información rápida del perfil y ubicación
                    HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 1.dp)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.TwoWheeler,
                                contentDescription = null,
                                tint = AcaAzulConfianza,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "${profile?.modeloVehiculo ?: "Italika FT150"} • ${profile?.placas ?: "GRO-489A"}",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = AcaAzulDark
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = Color(0xFFF59E0B),
                                modifier = Modifier.size(15.dp)
                            )
                            Text(
                                text = "${String.format("%.1f", profile?.calificacion ?: 4.9)} Repartidor Top",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFB45309)
                            )
                        }
                    }
                }
            }
        }

        // 1.5. Tarjeta de Ubicación Base y Control de Radio de Cercanía
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("card_control_cercania_repartidor")
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
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(AcaTurquesaContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MyLocation,
                                    contentDescription = null,
                                    tint = AcaTurquesaDark,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "Base de Cercanía en Acapulco",
                                    fontSize = 11.sp,
                                    color = AcaGrisSubtle
                                )
                                Text(
                                    text = currentBaseZoneName,
                                    fontSize = 13.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AcaAzulDark,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        OutlinedButton(
                            onClick = { showLocationBasePicker = true },
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            border = BorderStroke(1.dp, AcaTurquesaDark)
                        ) {
                            Text("Cambiar Base", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = AcaTurquesaDark)
                        }
                    }

                    HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 1.dp)

                    // Slider de Radio de Cercanía
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(Icons.Default.Radar, contentDescription = null, tint = AcaAzulConfianza, modifier = Modifier.size(15.dp))
                                Text(
                                    text = "Radio de Cercanía:",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AcaAzulDark
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (maxRadiusKm < 20f) Color(0xFFE0F2FE) else Color(0xFFDCFCE7)
                            ) {
                                Text(
                                    text = if (maxRadiusKm >= 20f) "Toda la Bahía (Sin Límite)" else "< ${maxRadiusKm.toInt()} km a la redonda",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (maxRadiusKm < 20f) Color(0xFF0369A1) else Color(0xFF047857),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Slider(
                            value = maxRadiusKm,
                            onValueChange = { maxRadiusKm = it },
                            valueRange = 1f..20f,
                            steps = 18,
                            colors = SliderDefaults.colors(
                                thumbColor = AcaAzulConfianza,
                                activeTrackColor = AcaTurquesa
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("slider_radio_cercania")
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("1 km (Inmediatos)", fontSize = 10.sp, color = AcaGrisSubtle)
                            Text(
                                text = "${processedOrders.size} mandados en rango",
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = AcaAzulConfianza
                            )
                            Text("20 km (Toda Bahía)", fontSize = 10.sp, color = AcaGrisSubtle)
                        }
                    }
                }
            }
        }

        // 2. Banner de Suscripción Semanal $150 MXN (100% de Ganancia)
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isSubscribed) AcaAzulDark else Color(0xFF991B1B)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = if (isSubscribed) Icons.Default.Verified else Icons.Default.Warning,
                            contentDescription = null,
                            tint = if (isSubscribed) AcaTurquesaLight else Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                        Column {
                            Text(
                                text = if (isSubscribed) "Suscripción Semanal Activa ($150 MXN)" else "Suscripción Inactiva",
                                color = Color.White,
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (isSubscribed) "100% de ganancia para ti sin comisiones por viaje" else "Renueva para poder tomar pedidos",
                                color = Color.White.copy(alpha = 0.85f),
                                fontSize = 10.5.sp
                            )
                        }
                    }

                    if (!isSubscribed) {
                        Button(
                            onClick = { viewModel.setRepartidorTab(RepartidorTab.BILLETERA_SUSCRIPCION) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text("Activar", color = Color(0xFF991B1B), fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = AcaTurquesa.copy(alpha = 0.25f)
                        ) {
                            Text(
                                text = "0% Comisión",
                                color = AcaTurquesaLight,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            )
                        }
                    }
                }
            }
        }

        // 3. Tarjeta de Alerta si ya hay un mandado en curso
        if (activeDriverOrder != null) {
            item {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFFEFF6FF),
                    border = BorderStroke(1.5.dp, Color(0xFF3B82F6)),
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
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF3B82F6)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Navigation, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                            }
                            Column {
                                Text(
                                    text = "Mandado en curso: ${activeDriverOrder?.folio}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = Color(0xFF1E3A8A)
                                )
                                Text(
                                    text = "${activeDriverOrder?.puntoRecogida} ➔ ${activeDriverOrder?.puntoEntrega}",
                                    fontSize = 11.sp,
                                    color = Color(0xFF2563EB),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        Button(
                            onClick = { viewModel.setRepartidorTab(RepartidorTab.MI_RUTA) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text("Ver Ruta", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // 4. Panel de Métricas de Rendimiento Hoy (KPI Dashboard)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Métricas: Mandados entrantes
                KpiMetricCard(
                    title = "Entrantes",
                    value = availableOrders.size.toString(),
                    subtitle = "disponibles",
                    icon = Icons.Default.Bolt,
                    iconTint = Color(0xFF059669),
                    modifier = Modifier.weight(1f)
                )

                // Métricas: Ganancia hoy
                KpiMetricCard(
                    title = "Ganancia Hoy",
                    value = "$${totalEarningsToday.toInt()}",
                    subtitle = "100% neta",
                    icon = Icons.Default.Paid,
                    iconTint = AcaAzulConfianza,
                    modifier = Modifier.weight(1f)
                )

                // Métricas: Viajes entregados hoy
                KpiMetricCard(
                    title = "Viajes Hoy",
                    value = completedTripsToday.toString(),
                    subtitle = "completados",
                    icon = Icons.Default.CheckCircle,
                    iconTint = Color(0xFF6366F1),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // 5. Encabezado de la Sección de Mandados Entrantes y Botón Simular
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "Mandados Entrantes",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = AcaAzulDark
                        )
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (availableOrders.isNotEmpty()) Color(0xFF059669) else AcaGrisSubtle
                        ) {
                            Text(
                                text = "${availableOrders.size} activos",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Text(
                        text = "Selecciona un mandado, revisa distancia y payout, y acéptalo",
                        fontSize = 11.sp,
                        color = AcaGrisSubtle
                    )
                }

                FilledTonalButton(
                    onClick = { viewModel.simularNuevoMandadoCliente() },
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("btn_simulate_incoming_order")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("+ Simular", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // 6. Barra de Filtros y Ordenamiento
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 2.dp)
                ) {
                    items(DashboardFilter.values()) { filter ->
                        FilterChip(
                            selected = selectedFilter == filter,
                            onClick = { selectedFilter = filter },
                            label = { Text(filter.label, fontSize = 11.5.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = AcaAzulDark,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }

                // Opciones de Ordenamiento Rápido
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Ordenar por:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = AcaGrisSubtle
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        DashboardSort.values().forEach { sort ->
                            val isSelected = selectedSort == sort
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (isSelected) AcaTurquesaDark else Color(0xFFF1F5F9),
                                modifier = Modifier.clickable { selectedSort = sort }
                            ) {
                                Text(
                                    text = sort.label,
                                    fontSize = 10.5.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) Color.White else AcaGrisEquilibrio,
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // 7. Lista de Mandados Entrantes Activos (con Distancia, Payout y Botón 'Aceptar')
        if (processedOrders.isEmpty()) {
            item {
                RadarEmptyState(
                    onSimulateOrder = { viewModel.simularNuevoMandadoCliente() },
                    isOnline = isOnline,
                    onGoOnline = { viewModel.setDisponibilidad(true) }
                )
            }
        } else {
            items(processedOrders, key = { it.id }) { order ->
                IncomingOrderDashboardCard(
                    order = order,
                    driverLat = driverLoc.lat,
                    driverLng = driverLoc.lng,
                    isOnline = isOnline,
                    isSubscribed = isSubscribed,
                    onAccept = {
                        if (!isOnline) {
                            viewModel.setDisponibilidad(true)
                        }
                        viewModel.tomarMandado(order.id)
                    },
                    onRenewSubscription = {
                        viewModel.setRepartidorTab(RepartidorTab.BILLETERA_SUSCRIPCION)
                    }
                )
            }
        }
    }
}

    if (showLocationBasePicker) {
        DriverBaseSelectionDialog(
            currentZone = currentBaseZoneName,
            onSelectZone = { zona ->
                viewModel.updateDriverBaseLocation(zona.lat, zona.lng)
                showLocationBasePicker = false
                Toast.makeText(context, "Base actualizada a ${zona.nombre}", Toast.LENGTH_SHORT).show()
            },
            onDismiss = { showLocationBasePicker = false }
        )
    }
}

/**
 * Tarjeta individual para Mandados Entrantes en el Dashboard de Repartidor.
 * Muestra prominentemente la distancia calculada en km, el payout íntegro y un botón claro de 'Aceptar'.
 */
@Composable
private fun IncomingOrderDashboardCard(
    order: MandadoOrder,
    driverLat: Double,
    driverLng: Double,
    isOnline: Boolean,
    isSubscribed: Boolean,
    onAccept: () -> Unit,
    onRenewSubscription: () -> Unit,
    modifier: Modifier = Modifier
) {
    val tipoEnum = TipoMandado.values().find { it.id == order.tipo } ?: TipoMandado.EXPRESS

    // Distancias calculadas usando la fórmula Haversine de AcapulcoLocations
    val distToPickup = remember(driverLat, driverLng, order.latRecogida, order.lngRecogida) {
        AcapulcoLocations.calcularDistanciaKm(driverLat, driverLng, order.latRecogida, order.lngRecogida)
    }
    val tripDist = remember(order.latRecogida, order.lngRecogida, order.latEntrega, order.lngEntrega) {
        AcapulcoLocations.calcularDistanciaKm(order.latRecogida, order.lngRecogida, order.latEntrega, order.lngEntrega)
    }
    val totalDist = distToPickup + tripDist
    val estimatedDurationMin = kotlin.math.max(6, (totalDist * 2.8).toInt())

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        border = BorderStroke(1.5.dp, Color(0xFF10B981).copy(alpha = 0.6f)),
        modifier = modifier
            .fillMaxWidth()
            .testTag("card_mandado_disponible_${order.id}")
            .testTag("card_incoming_order_${order.id}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Fila Superior: Folio, Tipo de Mandado y Badge de Disponibilidad
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
                            .background(AcaAzulContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = getCategoryVector(order.tipo),
                            contentDescription = order.tipo,
                            tint = AcaAzulConfianza,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = order.folio,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 14.sp,
                                color = AcaAzulDark
                            )
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = Color(0xFFFEF3C7)
                            ) {
                                Text(
                                    text = "⏳ DISPONIBLE",
                                    color = Color(0xFFB45309),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = tipoEnum.label,
                            fontSize = 11.sp,
                            color = AcaGrisSubtle
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFFF1F5F9)
                ) {
                    Text(
                        text = "📦 ${order.tamanoPaquete}",
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = AcaGrisEquilibrio,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                    )
                }
            }

            // BADGE DE CERCANÍA DESTACADO
            val proximityBg = when {
                distToPickup <= 2.0 -> Color(0xFFDCFCE7)
                distToPickup <= 5.0 -> Color(0xFFE0F2FE)
                else -> Color(0xFFFEF3C7)
            }
            val proximityTextColor = when {
                distToPickup <= 2.0 -> Color(0xFF047857)
                distToPickup <= 5.0 -> Color(0xFF0369A1)
                else -> Color(0xFFB45309)
            }
            val proximityLabel = when {
                distToPickup <= 2.0 -> "🟢 A ${String.format("%.1f", distToPickup)} km de ti (Muy Cerca • ~${kotlin.math.max(2, (distToPickup * 3).toInt())} min)"
                distToPickup <= 5.0 -> "🔵 A ${String.format("%.1f", distToPickup)} km de ti (Cerca • ~${kotlin.math.max(5, (distToPickup * 2.8).toInt())} min)"
                else -> "🟡 A ${String.format("%.1f", distToPickup)} km de ti (~${(distToPickup * 2.8).toInt()} min)"
            }

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = proximityBg,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.NearMe,
                        contentDescription = null,
                        tint = proximityTextColor,
                        modifier = Modifier.size(15.dp)
                    )
                    Text(
                        text = proximityLabel,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = proximityTextColor
                    )
                }
            }

            // SECCIÓN DESTACADA: PAYOUT (GANANCIA)
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFECFDF5),
                border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Tu Payout (Ganancia Neta):",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF065F46)
                        )
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = "$${String.format("%.2f", order.tarifaEnvio)}",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF047857)
                            )
                            Text(
                                text = " MXN",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF047857),
                                modifier = Modifier.padding(bottom = 2.dp, start = 2.dp)
                            )
                        }
                        Text(
                            text = "100% para ti • 0% de comisión",
                            fontSize = 10.sp,
                            color = Color(0xFF059669)
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (order.metodoPago == "EFECTIVO") Color(0xFFFEF3C7) else Color(0xFFE0E7FF)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp)
                            ) {
                                Icon(
                                    imageVector = if (order.metodoPago == "EFECTIVO") Icons.Default.Payments else Icons.Default.CreditCard,
                                    contentDescription = null,
                                    tint = if (order.metodoPago == "EFECTIVO") Color(0xFFB45309) else Color(0xFF3730A3),
                                    modifier = Modifier.size(13.dp)
                                )
                                Text(
                                    text = if (order.metodoPago == "EFECTIVO") "Efectivo" else "Tarjeta App",
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (order.metodoPago == "EFECTIVO") Color(0xFF92400E) else Color(0xFF3730A3)
                                )
                            }
                        }

                        if (order.costoProductosEstimado > 0) {
                            Text(
                                text = "+$${order.costoProductosEstimado.toInt()} productos",
                                fontSize = 10.sp,
                                color = AcaGrisSubtle,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                    }
                }
            }

            // SECCIÓN DESTACADA: DISTANCIA Y TIEMPO ESTIMADO
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = Color(0xFFF8FAFC),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Distancia hacia el punto de recolección
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                            Icon(Icons.Default.LocationOn, contentDescription = null, tint = AcaTurquesaDark, modifier = Modifier.size(13.dp))
                            Text(text = "A recoger", fontSize = 10.sp, color = AcaGrisSubtle)
                        }
                        Text(
                            text = "${String.format("%.1f", distToPickup)} km",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = AcaAzulDark
                        )
                    }

                    Box(modifier = Modifier.width(1.dp).height(24.dp).background(Color(0xFFCBD5E1)))

                    // Distancia de ruta (Recogida a entrega)
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                            Icon(Icons.Default.Flag, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(13.dp))
                            Text(text = "Ruta", fontSize = 10.sp, color = AcaGrisSubtle)
                        }
                        Text(
                            text = "${String.format("%.1f", tripDist)} km",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = AcaAzulDark
                        )
                    }

                    Box(modifier = Modifier.width(1.dp).height(24.dp).background(Color(0xFFCBD5E1)))

                    // Distancia total y tiempo estimado en moto
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                            Icon(Icons.Default.Schedule, contentDescription = null, tint = AcaAzulConfianza, modifier = Modifier.size(13.dp))
                            Text(text = "Total viaje", fontSize = 10.sp, color = AcaGrisSubtle)
                        }
                        Text(
                            text = "${String.format("%.1f", totalDist)} km (~${estimatedDurationMin}m)",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = AcaAzulConfianza
                        )
                    }
                }
            }

            // DETALLES DE RUTA: Origen y Destino
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                // Punto de Recogida
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .padding(top = 2.dp)
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF10B981))
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Recoger: ${order.puntoRecogida}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = AcaGrisEquilibrio,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "Zona: ${order.zonaRecogida}",
                            fontSize = 10.5.sp,
                            color = AcaGrisSubtle
                        )
                    }
                }

                // Punto de Entrega
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .padding(top = 2.dp)
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFEF4444))
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Entregar: ${order.puntoEntrega}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = AcaGrisEquilibrio,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "Destino: ${order.zonaEntrega} (${order.destinatarioNombre.ifBlank { order.clienteNombre }})",
                            fontSize = 10.5.sp,
                            color = AcaGrisSubtle
                        )
                    }
                }

                if (order.descripcion.isNotBlank() && order.descripcion != order.titulo) {
                    Text(
                        text = "📝 Nota: \"${order.descripcion}\"",
                        fontSize = 10.5.sp,
                        color = AcaGrisSubtle,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(start = 18.dp)
                    )
                }
            }

            // BOTÓN CLARO Y PROMINENTE DE 'ACEPTAR MANDADO'
            if (!isSubscribed) {
                Button(
                    onClick = onRenewSubscription,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF991B1B)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("btn_renew_for_order_${order.id}")
                ) {
                    Icon(Icons.Default.Lock, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Activar Suscripción para Aceptar",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 14.sp,
                        color = Color.White
                    )
                }
            } else {
                Button(
                    onClick = onAccept,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF059669),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp, pressedElevation = 6.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("btn_accept_order_${order.id}")
                        .testTag("btn_aceptar_mandado_${order.id}")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Aceptar",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "ACEPTAR MANDADO • GANAR $${String.format("%.2f", order.tarifaEnvio)}",
                            fontWeight = FontWeight.Black,
                            fontSize = 13.5.sp,
                            letterSpacing = 0.2.sp
                        )
                    }
                }
            }
        }
    }
}

/**
 * Tarjeta de métrica rápida para el KPI bar superior del repartidor.
 */
@Composable
private fun KpiMetricCard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    iconTint: Color,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        modifier = modifier
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
                Text(
                    text = title,
                    fontSize = 10.5.sp,
                    color = AcaGrisSubtle,
                    fontWeight = FontWeight.SemiBold
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(16.dp)
                )
            }
            Text(
                text = value,
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                color = AcaAzulDark
            )
            Text(
                text = subtitle,
                fontSize = 9.5.sp,
                color = AcaGrisSubtle
            )
        }
    }
}

/**
 * Estado vacío interactivo con radar escaneando la bahía de Acapulco
 */
@Composable
private fun RadarEmptyState(
    onSimulateOrder: () -> Unit,
    isOnline: Boolean,
    onGoOnline: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "radarPulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(22.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Animación de Radar
            Box(
                modifier = Modifier
                    .size((70 * pulseScale).dp)
                    .clip(CircleShape)
                    .background(if (isOnline) Color(0xFFD1FAE5) else Color(0xFFF1F5F9)),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(if (isOnline) Color(0xFF10B981) else Color(0xFF94A3B8)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Radar,
                        contentDescription = "Radar",
                        tint = Color.White,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }

            Text(
                text = if (isOnline) "Radar Activo: Buscando Mandados..." else "Te encuentras desconectado",
                fontWeight = FontWeight.ExtraBold,
                fontSize = 16.sp,
                color = AcaAzulDark,
                textAlign = TextAlign.Center
            )

            Text(
                text = if (isOnline) {
                    "Tu app está conectada esperando solicitudes en Costera, Magallanes, Farallón y Diamante. ¡Te avisaremos con sonido en cuanto caiga uno!"
                } else {
                    "Conéctate para recibir mandados cercanos y ganar el 100% de la tarifa sin comisiones."
                },
                fontSize = 12.sp,
                color = AcaGrisSubtle,
                textAlign = TextAlign.Center,
                lineHeight = 16.sp,
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            if (!isOnline) {
                Button(
                    onClick = onGoOnline,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().height(46.dp)
                ) {
                    Icon(Icons.Default.PowerSettingsNew, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Conectarme Ahora", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            } else {
                Button(
                    onClick = onSimulateOrder,
                    colors = ButtonDefaults.buttonColors(containerColor = AcaTurquesaDark),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("btn_simular_vacio")
                ) {
                    Icon(Icons.Default.AddCircleOutline, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Generar Mandado Entrante de Prueba", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }

            // Recomendación de hotspots en Acapulco
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = Color(0xFFF8FAFC),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.LocalFireDepartment, contentDescription = null, tint = Color(0xFFEA580C), modifier = Modifier.size(18.dp))
                    Text(
                        text = "Hotspots alta demanda: Galerías Diana, Chedraui Costera y Zona Diamante.",
                        fontSize = 10.5.sp,
                        color = Color(0xFF9A3412),
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

private fun getCategoryVector(tipo: String): ImageVector {
    return when (tipo) {
        "COMIDA" -> Icons.Default.Restaurant
        "FARMACIA" -> Icons.Default.LocalPharmacy
        "DOCUMENTACION" -> Icons.Default.Description
        "COMPRAS" -> Icons.Default.ShoppingCart
        else -> Icons.Default.ElectricScooter
    }
}

@Composable
private fun DriverBaseSelectionDialog(
    currentZone: String,
    onSelectZone: (com.example.data.model.ZonaAcapulco) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.Place, contentDescription = null, tint = AcaTurquesaDark)
                Text(
                    text = "Seleccionar Base en Acapulco",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = AcaAzulDark
                )
            }
        },
        text = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 380.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(AcapulcoLocations.ZONAS) { zona ->
                    val isSelected = zona.nombre == currentZone
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) AcaTurquesaContainer else Color(0xFFF8FAFC),
                        border = BorderStroke(1.dp, if (isSelected) AcaTurquesaDark else Color(0xFFE2E8F0)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { onSelectZone(zona) }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = zona.nombre,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = if (isSelected) AcaAzulDark else AcaGrisEquilibrio
                                )
                                Text(
                                    text = zona.descripcion,
                                    fontSize = 10.5.sp,
                                    color = AcaGrisSubtle,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            if (isSelected) {
                                Icon(
                                    Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = AcaTurquesaDark,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cerrar", color = AcaTurquesaDark, fontWeight = FontWeight.Bold)
            }
        }
    )
}
