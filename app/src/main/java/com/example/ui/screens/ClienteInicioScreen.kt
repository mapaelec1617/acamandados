package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.data.model.AcapulcoLocations
import com.example.data.model.MandadoOrder
import com.example.data.model.TipoMandado
import com.example.ui.ClienteTab
import com.example.ui.MandadosViewModel
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Pantalla de Inicio / Dashboard Principal para el rol 'Cliente'.
 * Se muestra inmediatamente después de la autenticación cuando el rol guardado en Firestore es 'Cliente'.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClienteInicioScreen(
    viewModel: MandadosViewModel,
    modifier: Modifier = Modifier
) {
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()
    val activeOrders by viewModel.activeOrders.collectAsStateWithLifecycle()
    val allOrders by viewModel.allOrders.collectAsStateWithLifecycle()
    val driverLoc by viewModel.driverLocationState.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()

    val activeOrder = activeOrders.firstOrNull()
    val completedOrders = remember(allOrders) {
        allOrders.filter { it.estado == "ENTREGADO" || it.estado == "COMPLETADO" }
    }

    // Saludo según la hora
    val saludo = remember {
        val hora = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        when (hora) {
            in 6..11 -> "¡Buenos días"
            in 12..18 -> "¡Buenas tardes"
            else -> "¡Buenas noches"
        }
    }

    val nombreCliente = remember(userProfile.nombre, currentUser?.displayName) {
        userProfile.nombre.ifBlank {
            currentUser?.displayName ?: "Cliente AcaMandados"
        }.split(" ").firstOrNull() ?: "Cliente"
    }

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = { viewModel.refreshData() },
        modifier = modifier
            .fillMaxSize()
            .testTag("pull_to_refresh_cliente_inicio")
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(AcaBackground)
                .testTag("screen_cliente_dashboard"),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 95.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
        // 1. HEADER DE BIENVENIDA CON ROL Y UBICACIÓN
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    Color(0xFF0F172A),
                                    AcaAzulDark,
                                    AcaAzulConfianza
                                )
                            )
                        )
                        .padding(18.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
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
                                Box(
                                    modifier = Modifier
                                        .size(46.dp)
                                        .clip(CircleShape)
                                        .background(AcaTurquesa),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = "Avatar Cliente",
                                        tint = Color(0xFF0F172A),
                                        modifier = Modifier.size(26.dp)
                                    )
                                }
                                Column {
                                    Text(
                                        text = "$saludo, $nombreCliente! 👋",
                                        color = Color.White,
                                        fontSize = 17.5.sp,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = AcaTurquesaLight.copy(alpha = 0.25f)
                                        ) {
                                            Text(
                                                text = "Rol: Cliente",
                                                color = AcaTurquesaLight,
                                                fontSize = 10.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                        Text(
                                            text = "Acapulco, Gro.",
                                            color = Color.White.copy(alpha = 0.8f),
                                            fontSize = 11.5.sp
                                        )
                                    }
                                }
                            }

                            // Chip interactivo para ir al perfil
                            FilledTonalButton(
                                onClick = { viewModel.setClienteTab(ClienteTab.PERFIL) },
                                colors = ButtonDefaults.filledTonalButtonColors(
                                    containerColor = Color.White.copy(alpha = 0.15f),
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Tune,
                                    contentDescription = null,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Mi Perfil", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }

                        HorizontalDivider(
                            color = Color.White.copy(alpha = 0.15f),
                            thickness = 1.dp,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )

                        // Barra informativa de estado del servicio en Acapulco
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF34D399))
                                )
                                Text(
                                    text = "Repartidores activos en Costera y Diamante",
                                    color = Color.White.copy(alpha = 0.9f),
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            Text(
                                text = "Base $35 MXN",
                                color = AcaTurquesaLight,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // 2. HERO / ESTADO DEL PEDIDO ACTIVO O INVITACIÓN A PEDIR
        item {
            if (activeOrder != null) {
                ActiveOrderDashboardCard(
                    order = activeOrder,
                    driverLoc = driverLoc,
                    onViewTracking = {
                        viewModel.openWebTracking(activeOrder)
                    },
                    onOpenOrdersList = {
                        viewModel.setClienteTab(ClienteTab.MIS_MANDADOS)
                    }
                )
            } else {
                NoActiveOrderHeroCard(
                    onSolicitarClick = {
                        viewModel.setClienteTab(ClienteTab.SOLICITAR)
                    }
                )
            }
        }

        // 3. PARRILLA DE SERVICIOS / CATEGORÍAS RÁPIDAS
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "¿Qué mandado necesitas hoy?",
                        fontSize = 15.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = AcaAzulDark
                    )
                    Text(
                        text = "Entrega promedio 25 min",
                        fontSize = 11.sp,
                        color = AcaTurquesaDark,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ServiceCategoryCard(
                        title = "Paquetería",
                        subtitle = "Sobres, llaves o paquetes",
                        badge = "Desde $35",
                        icon = Icons.Default.LocalShipping,
                        iconColor = AcaAzulConfianza,
                        bgColor = Color(0xFFEFF6FF),
                        modifier = Modifier.weight(1f),
                        testTag = "btn_servicio_paqueteria",
                        onClick = { viewModel.iniciarMandadoPorTipo(TipoMandado.DOCUMENTACION) }
                    )
                    ServiceCategoryCard(
                        title = "Súper y OXXO",
                        subtitle = "Despensa, bebidas y botanas",
                        badge = "Desde $40",
                        icon = Icons.Default.ShoppingCart,
                        iconColor = Color(0xFFD97706),
                        bgColor = Color(0xFFFFFBEB),
                        modifier = Modifier.weight(1f),
                        testTag = "btn_servicio_super",
                        onClick = { viewModel.iniciarMandadoPorTipo(TipoMandado.COMPRAS) }
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ServiceCategoryCard(
                        title = "Farmacia 24/7",
                        subtitle = "Medicinas urgentes",
                        badge = "Desde $35",
                        icon = Icons.Default.MedicalServices,
                        iconColor = Color(0xFF059669),
                        bgColor = Color(0xFFECFDF5),
                        modifier = Modifier.weight(1f),
                        testTag = "btn_servicio_farmacia",
                        onClick = { viewModel.iniciarMandadoPorTipo(TipoMandado.FARMACIA) }
                    )
                    ServiceCategoryCard(
                        title = "Comida Local",
                        subtitle = "Restaurantes y fondas",
                        badge = "Desde $40",
                        icon = Icons.Default.Restaurant,
                        iconColor = Color(0xFFEA580C),
                        bgColor = Color(0xFFFFF7ED),
                        modifier = Modifier.weight(1f),
                        testTag = "btn_servicio_comida",
                        onClick = { viewModel.iniciarMandadoPorTipo(TipoMandado.COMIDA) }
                    )
                }
            }
        }

        // 4. ZONAS POPULARES Y TARIFAS TRANSPARENTES DE ACAPULCO
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Zonas de Cobertura & Tarifas",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = AcaAzulDark
                    )
                    TextButton(
                        onClick = { viewModel.setClienteTab(ClienteTab.ZONAS_TARIFAS) },
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
                    ) {
                        Text("Ver todas", fontSize = 12.sp, color = AcaAzulConfianza, fontWeight = FontWeight.Bold)
                    }
                }

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(horizontal = 2.dp)
                ) {
                    val zonas = AcapulcoLocations.ZONAS.take(5)
                    items(zonas) { zona ->
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                            modifier = Modifier
                                .width(160.dp)
                                .clickable {
                                    viewModel.zonaEntregaInput.value = zona.nombre
                                    viewModel.setClienteTab(ClienteTab.SOLICITAR)
                                }
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Place,
                                        contentDescription = null,
                                        tint = AcaTurquesaDark,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    val tarifaEstimada = if (zona.macroZona == com.example.data.model.MacroZona.ZONA_A) 35 else 45
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = Color(0xFFE0F2FE)
                                    ) {
                                        Text(
                                            text = "$$tarifaEstimada MXN",
                                            color = Color(0xFF0369A1),
                                            fontSize = 10.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                        )
                                    }
                                }

                                Text(
                                    text = zona.nombre,
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AcaAzulDark,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )

                                Text(
                                    text = zona.descripcion,
                                    fontSize = 10.5.sp,
                                    color = AcaGrisSubtle,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                    lineHeight = 13.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // 5. HISTORIAL RECIENTE / PEDIR DE NUEVO
        if (completedOrders.isNotEmpty()) {
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Tus Mandados Recientes",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = AcaAzulDark
                        )
                        TextButton(
                            onClick = { viewModel.setClienteTab(ClienteTab.MIS_MANDADOS) },
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
                        ) {
                            Text("Ver historial", fontSize = 12.sp, color = AcaAzulConfianza, fontWeight = FontWeight.Bold)
                        }
                    }

                    val recentOrders = completedOrders.take(2)
                    recentOrders.forEach { order ->
                        RecentOrderCard(
                            order = order,
                            onReorder = { viewModel.reordenarMandado(order) }
                        )
                    }
                }
            }
        }

        // 6. GARANTÍA DE SEGURIDAD Y CONFIANZA
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
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = null,
                            tint = AcaAzulConfianza,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Garantía de Seguridad AcaMandados",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = AcaAzulDark
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        SecurityPillarItem(
                            icon = Icons.Default.Key,
                            title = "PIN Seguro",
                            desc = "Código único de 4 dígitos",
                            modifier = Modifier.weight(1f)
                        )
                        SecurityPillarItem(
                            icon = Icons.Default.MyLocation,
                            title = "GPS en Vivo",
                            desc = "Rastreo sin app para WhatsApp",
                            modifier = Modifier.weight(1f)
                        )
                        SecurityPillarItem(
                            icon = Icons.Default.Handshake,
                            title = "100% Acapulco",
                            desc = "Repartidores locales certificados",
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}
}

@Composable
private fun ActiveOrderDashboardCard(
    order: MandadoOrder,
    driverLoc: com.example.ui.DriverLocationState,
    onViewTracking: () -> Unit,
    onOpenOrdersList: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        border = BorderStroke(1.5.dp, AcaTurquesa),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("card_mandado_activo")
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
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF10B981))
                    )
                    Text(
                        text = when (order.estado) {
                            "EN_RUTA", "EN_CAMINO" -> "🛵 REPARTIDOR EN CAMINO"
                            "ASIGNADO" -> "📦 REPARTIDOR ASIGNADO"
                            "RECOGIDO" -> "🛍️ PAQUETE RECOGIDO"
                            else -> "⏳ BUSCANDO REPARTIDOR"
                        },
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF065F46)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFFF1F5F9)
                ) {
                    Text(
                        text = "Folio #${order.folio.ifBlank { order.id.toString() }}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = AcaAzulDark,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Column {
                Text(
                    text = order.titulo,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 16.sp,
                    color = AcaAzulDark
                )
                Text(
                    text = "Entrega en: ${order.puntoEntrega} (${order.zonaEntrega})",
                    fontSize = 12.sp,
                    color = AcaGrisSubtle
                )
            }

            // Banner del PIN de seguridad
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFEFF6FF),
                border = BorderStroke(1.dp, Color(0xFFBFDBFE)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Tu PIN de Entrega Seguro:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF1E40AF)
                        )
                        Text(
                            text = "Dáselo al repartidor al recibir",
                            fontSize = 10.sp,
                            color = AcaGrisSubtle
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = AcaAzulConfianza
                    ) {
                        Text(
                            text = order.pinSeguridad.ifBlank { "4892" },
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            letterSpacing = 3.sp,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            // ETA y Botones de acción
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onOpenOrdersList,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Detalles", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = onViewTracking,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AcaAzulConfianza),
                    modifier = Modifier.weight(1.4f)
                ) {
                    Icon(
                        imageVector = Icons.Default.ShareLocation,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Rastreo en Vivo", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun NoActiveOrderHeroCard(
    onSolicitarClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(AcaTurquesaContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.FlashOn,
                        contentDescription = null,
                        tint = AcaTurquesaDark,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Mandados Express en Acapulco",
                        fontSize = 15.5.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = AcaAzulDark
                    )
                    Text(
                        text = "Sin tarifas dinámicas abusivas. 100% de la tarifa para el repartidor local.",
                        fontSize = 11.5.sp,
                        color = AcaGrisSubtle,
                        lineHeight = 15.sp
                    )
                }
            }

            Button(
                onClick = onSolicitarClick,
                colors = ButtonDefaults.buttonColors(containerColor = AcaAzulConfianza),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("btn_crear_mandado_hero")
            ) {
                Icon(
                    imageVector = Icons.Default.AddCircle,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Pedir un Mandado Ahora",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
private fun ServiceCategoryCard(
    title: String,
    subtitle: String,
    badge: String,
    icon: ImageVector,
    iconColor: Color,
    bgColor: Color,
    modifier: Modifier = Modifier,
    testTag: String,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        shadowElevation = 1.dp,
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .testTag(testTag)
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
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(bgColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = AcaSurfaceVariant
                ) {
                    Text(
                        text = badge,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = AcaAzulDark,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Column {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = AcaAzulDark
                )
                Text(
                    text = subtitle,
                    fontSize = 10.5.sp,
                    color = AcaGrisSubtle,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun RecentOrderCard(
    order: MandadoOrder,
    onReorder: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFDCFCE7)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = Color(0xFF166534),
                    modifier = Modifier.size(20.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = order.titulo,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = AcaAzulDark,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "Entregado en ${order.zonaEntrega} • $${order.tarifaEnvio.toInt()} MXN",
                    fontSize = 11.sp,
                    color = AcaGrisSubtle
                )
            }

            TextButton(
                onClick = onReorder,
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "Pedir de nuevo",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = AcaAzulConfianza
                )
            }
        }
    }
}

@Composable
private fun SecurityPillarItem(
    icon: ImageVector,
    title: String,
    desc: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(Color(0xFFF1F5F9)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = AcaAzulConfianza,
                modifier = Modifier.size(18.dp)
            )
        }
        Text(
            text = title,
            fontSize = 11.5.sp,
            fontWeight = FontWeight.Bold,
            color = AcaAzulDark,
            textAlign = TextAlign.Center
        )
        Text(
            text = desc,
            fontSize = 9.5.sp,
            color = AcaGrisSubtle,
            textAlign = TextAlign.Center,
            lineHeight = 12.sp
        )
    }
}
