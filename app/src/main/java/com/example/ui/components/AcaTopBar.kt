package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
    darkThemeConfig: com.example.ui.DarkThemeConfig = com.example.ui.DarkThemeConfig.SYSTEM,
    onToggleDarkTheme: () -> Unit = {},
    onOpenNotifications: () -> Unit = {},
    onOpenHistorial: () -> Unit = {},
    onOpenInicio: () -> Unit = {},
    onToggleMode: (AppMode) -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.primary,
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
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onOpenInicio() }
                        .testTag("topbar_acamandados_logo_btn")
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

                // Acciones: Tema Oscuro/Claro, Historial, Notificaciones y Selector de modo rápido
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    // Botón para alternar Modo Oscuro / Modo Claro
                    IconButton(
                        onClick = onToggleDarkTheme,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("btn_topbar_theme_toggle")
                    ) {
                        Icon(
                            imageVector = when (darkThemeConfig) {
                                com.example.ui.DarkThemeConfig.DARK -> Icons.Default.DarkMode
                                com.example.ui.DarkThemeConfig.LIGHT -> Icons.Default.LightMode
                                com.example.ui.DarkThemeConfig.SYSTEM -> Icons.Default.BrightnessAuto
                            },
                            contentDescription = "Cambiar modo oscuro (${darkThemeConfig.label})",
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    // Botón Historial de Mandados Completados
                    IconButton(
                        onClick = onOpenHistorial,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("btn_topbar_historial")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.ReceiptLong,
                            contentDescription = "Historial de Mandados Completados",
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    // Campana de notificaciones con badge
                    IconButton(
                        onClick = onOpenNotifications,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("btn_topbar_notifications")
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
                                modifier = Modifier.size(22.dp)
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
    var showMenu by remember { mutableStateOf(false) }

    val badgeText = when (currentMode) {
        AppMode.CLIENTE -> "Cliente"
        AppMode.REPARTIDOR -> "Repartidor"
        AppMode.ADMIN -> "Admin"
    }
    val badgeIcon = when (currentMode) {
        AppMode.CLIENTE -> Icons.Default.Person
        AppMode.REPARTIDOR -> Icons.Default.ElectricScooter
        AppMode.ADMIN -> Icons.Default.AdminPanelSettings
    }
    val badgeBg = when (currentMode) {
        AppMode.CLIENTE -> Color.White.copy(alpha = 0.18f)
        AppMode.REPARTIDOR -> AcaTurquesa
        AppMode.ADMIN -> Color(0xFFFEF3C7)
    }
    val badgeContentColor = when (currentMode) {
        AppMode.CLIENTE -> Color.White
        AppMode.REPARTIDOR -> Color(0xFF00382E)
        AppMode.ADMIN -> Color(0xFF92400E)
    }

    Box {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = badgeBg,
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .clickable { showMenu = true }
                .testTag("btn_switch_app_mode")
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 9.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = badgeIcon,
                    contentDescription = "Modo: $badgeText",
                    tint = badgeContentColor,
                    modifier = Modifier.size(15.dp)
                )
                Text(
                    text = badgeText,
                    color = badgeContentColor,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = null,
                    tint = badgeContentColor,
                    modifier = Modifier.size(14.dp)
                )
            }
        }

        DropdownMenu(
            expanded = showMenu,
            onDismissRequest = { showMenu = false }
        ) {
            DropdownMenuItem(
                text = { Text("👤 Modo Cliente", fontSize = 13.sp, fontWeight = if (currentMode == AppMode.CLIENTE) FontWeight.Bold else FontWeight.Normal) },
                onClick = {
                    onToggleMode(AppMode.CLIENTE)
                    showMenu = false
                },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = AcaAzulConfianza) }
            )
            DropdownMenuItem(
                text = { Text("🛵 Modo Repartidor", fontSize = 13.sp, fontWeight = if (currentMode == AppMode.REPARTIDOR) FontWeight.Bold else FontWeight.Normal) },
                onClick = {
                    onToggleMode(AppMode.REPARTIDOR)
                    showMenu = false
                },
                leadingIcon = { Icon(Icons.Default.ElectricScooter, contentDescription = null, tint = AcaTurquesaDark) }
            )
            DropdownMenuItem(
                text = { Text("👔 Panel Administrador", fontSize = 13.sp, fontWeight = if (currentMode == AppMode.ADMIN) FontWeight.Bold else FontWeight.Normal) },
                onClick = {
                    onToggleMode(AppMode.ADMIN)
                    showMenu = false
                },
                leadingIcon = { Icon(Icons.Default.AdminPanelSettings, contentDescription = null, tint = Color(0xFFD97706)) }
            )
        }
    }
}
