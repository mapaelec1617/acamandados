package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.NotificationItem
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationCenterSheet(
    notifications: List<NotificationItem>,
    onDismiss: () -> Unit,
    onMarkAsRead: (Long) -> Unit,
    onMarkAllAsRead: () -> Unit,
    onSelectNotification: (NotificationItem) -> Unit
) {
    var selectedFilter by remember { mutableStateOf("TODAS") } // TODAS, NO_LEIDAS
    val timeFormat = remember { SimpleDateFormat("hh:mm a • dd MMM", Locale("es", "MX")) }

    val filteredList = remember(notifications, selectedFilter) {
        if (selectedFilter == "NO_LEIDAS") notifications.filter { !it.leida } else notifications
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header
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
                            .background(AcaAzulContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = null,
                            tint = AcaAzulConfianza,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "Centro de Notificaciones",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = AcaAzulDark
                        )
                        Text(
                            text = "Alertas en tiempo real y avisos de ruta",
                            fontSize = 11.5.sp,
                            color = AcaGrisSubtle
                        )
                    }
                }

                if (notifications.any { !it.leida }) {
                    TextButton(onClick = onMarkAllAsRead) {
                        Icon(Icons.Outlined.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Leer todas", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = AcaTurquesaDark)
                    }
                }
            }

            // Filtros rápidos
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val unreadCount = notifications.count { !it.leida }
                FilterChip(
                    selected = selectedFilter == "TODAS",
                    onClick = { selectedFilter = "TODAS" },
                    label = { Text("Todas (${notifications.size})", fontSize = 11.5.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = AcaAzulConfianza,
                        selectedLabelColor = Color.White
                    )
                )
                FilterChip(
                    selected = selectedFilter == "NO_LEIDAS",
                    onClick = { selectedFilter = "NO_LEIDAS" },
                    label = { Text("No leídas ($unreadCount)", fontSize = 11.5.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = AcaAzulConfianza,
                        selectedLabelColor = Color.White
                    )
                )
            }

            Divider(color = Color(0xFFE2E8F0))

            if (filteredList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Notifications,
                            contentDescription = null,
                            tint = Color(0xFFCBD5E1),
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            text = "No tienes notificaciones pendientes",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = AcaGrisEquilibrio
                        )
                        Text(
                            text = "Aquí verás cuando tu pedido cambie de estado o tu repartidor esté cerca.",
                            fontSize = 11.5.sp,
                            color = AcaGrisSubtle
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 450.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    items(filteredList, key = { it.id }) { item ->
                        NotificationItemCard(
                            item = item,
                            timeText = timeFormat.format(Date(item.fecha)),
                            onClick = {
                                onMarkAsRead(item.id)
                                onSelectNotification(item)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NotificationItemCard(
    item: NotificationItem,
    timeText: String,
    onClick: () -> Unit
) {
    val (icon, iconBg, iconTint) = getNotificationVisuals(item.tipo)

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (item.leida) Color(0xFFF8FAFC) else AcaAzulContainer.copy(alpha = 0.35f),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(18.dp)
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = item.titulo,
                        fontWeight = if (item.leida) FontWeight.SemiBold else FontWeight.Bold,
                        fontSize = 13.sp,
                        color = AcaAzulDark,
                        modifier = Modifier.weight(1f)
                    )

                    if (!item.leida) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(AcaTurquesaDark)
                        )
                    }
                }

                Text(
                    text = item.mensaje,
                    fontSize = 11.5.sp,
                    color = AcaGrisEquilibrio,
                    lineHeight = 15.sp
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = timeText,
                        fontSize = 10.sp,
                        color = AcaGrisSubtle
                    )

                    if (item.distanciaAprox != null) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = AcaTurquesaContainer
                        ) {
                            Text(
                                text = "Distancia: ${item.distanciaAprox}",
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = AcaTurquesaDark,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun getNotificationVisuals(tipo: String): Triple<ImageVector, Color, Color> {
    return when (tipo) {
        "NUEVA_SOLICITUD" -> Triple(Icons.Default.Radar, Color(0xFFFEF3C7), Color(0xFFD97706))
        "ASIGNADO" -> Triple(Icons.Default.ElectricScooter, AcaAzulContainer, AcaAzulConfianza)
        "RECOGIDO" -> Triple(Icons.Default.LocalShipping, AcaTurquesaContainer, AcaTurquesaDark)
        "CERCA" -> Triple(Icons.Default.NearMe, Color(0xFFFEE2E2), Color(0xFFDC2626))
        "ENTREGADO" -> Triple(Icons.Default.CheckCircle, Color(0xFFD1FAE5), Color(0xFF059669))
        "SPEI" -> Triple(Icons.Default.Paid, Color(0xFFE0E7FF), Color(0xFF4338CA))
        else -> Triple(Icons.Default.Notifications, Color(0xFFF1F5F9), Color(0xFF475569))
    }
}
