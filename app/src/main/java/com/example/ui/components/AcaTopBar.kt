package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ElectricScooter
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.AppMode
import com.example.ui.theme.*

@Composable
fun AcaTopBar(
    currentMode: AppMode,
    unreadNotificationsCount: Int = 0,
    onOpenNotifications: () -> Unit = {},
    onOpenHistorial: () -> Unit = {},
    onToggleMode: (AppMode) -> Unit
) {
    Surface(
        color = AcaAzulConfianza,
        shadowElevation = 4.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Logo e Identidad AcaMandados
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color.White)
                            .padding(2.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.acamandados_logo),
                            contentDescription = "Logo AcaMandados",
                            modifier = Modifier.size(36.dp),
                            contentScale = ContentScale.Fit
                        )
                    }

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Aca",
                                color = Color.White,
                                fontSize = 21.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = (-0.5).sp
                            )
                            Text(
                                text = "Mandados",
                                color = AcaTurquesaLight,
                                fontSize = 21.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = (-0.5).sp
                            )
                        }
                        Text(
                            text = "Acapulco • De persona a persona",
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Acciones: Historial, Notificaciones y Selector de modo rápido (Cliente <-> Repartidor)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Botón Historial de Mandados Completados
                    IconButton(
                        onClick = onOpenHistorial,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("btn_topbar_historial")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ReceiptLong,
                            contentDescription = "Historial de Mandados Completados",
                            tint = Color.White,
                            modifier = Modifier.size(21.dp)
                        )
                    }

                    // Campana de notificaciones con badge
                    IconButton(
                        onClick = onOpenNotifications,
                        modifier = Modifier.size(36.dp)
                    ) {
                        BadgedBox(
                            badge = {
                                if (unreadNotificationsCount > 0) {
                                    Badge(
                                        containerColor = Color(0xFFEF4444),
                                        contentColor = Color.White
                                    ) {
                                        Text(
                                            text = if (unreadNotificationsCount > 9) "9+" else unreadNotificationsCount.toString(),
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = androidx.compose.material.icons.Icons.Default.Notifications,
                                contentDescription = "Notificaciones",
                                tint = Color.White,
                                modifier = Modifier.size(21.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(2.dp))

                    ModeSwitchBadge(
                        currentMode = currentMode,
                        onToggleMode = onToggleMode
                    )
                }
            }
        }
    }
}

@Composable
private fun ModeSwitchBadge(
    currentMode: AppMode,
    onToggleMode: (AppMode) -> Unit
) {
    val isRepartidor = currentMode == AppMode.REPARTIDOR
    val bgColor by animateColorAsState(
        targetValue = if (isRepartidor) AcaTurquesa else Color.White.copy(alpha = 0.18f),
        label = "modeBg"
    )
    val contentColor by animateColorAsState(
        targetValue = if (isRepartidor) Color(0xFF00382E) else Color.White,
        label = "modeContent"
    )

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = bgColor,
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .clickable {
                onToggleMode(if (isRepartidor) AppMode.CLIENTE else AppMode.REPARTIDOR)
            }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = if (isRepartidor) Icons.Default.ElectricScooter else Icons.Default.Person,
                contentDescription = "Cambiar modo",
                tint = contentColor,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = if (isRepartidor) "Repartidor" else "Cliente",
                color = contentColor,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
