package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.*

/**
 * Diálogo interactivo para gestión y autorización expresa del tratamiento de datos personales,
 * geolocalización, notificaciones operativas y resguardo de privacidad de acuerdo a LFPDPPP y Google Play.
 */
@Composable
fun AutorizacionDatosDialog(
    userConsentDataAuth: Boolean,
    onConsentDataAuthChange: (Boolean) -> Unit,
    userConsentGpsTracking: Boolean,
    onConsentGpsTrackingChange: (Boolean) -> Unit,
    userConsentPushNotifications: Boolean,
    onConsentPushNotificationsChange: (Boolean) -> Unit,
    onDismiss: () -> Unit,
    onOpenPoliticasFull: () -> Unit,
    modifier: Modifier = Modifier
) {
    var sharePhoneTemp by remember { mutableStateOf(true) }
    var allowPhotoProof by remember { mutableStateOf(true) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.85f)
                .testTag("dialog_autorizacion_datos")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Cabecera
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(AcaTurquesaDark.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PrivacyTip,
                                contentDescription = null,
                                tint = AcaTurquesaDark,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Column {
                            Text(
                                text = "Autorización de Datos",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = AcaAzulDark
                            )
                            Text(
                                text = "Consentimiento y Privacidad",
                                fontSize = 11.5.sp,
                                color = AcaGrisSubtle
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("btn_close_autorizacion")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cerrar",
                            tint = Color(0xFF64748B)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Scroll de opciones de autorización
                val scrollState = rememberScrollState()
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(scrollState),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFF0FDF4),
                        border = BorderStroke(1.dp, Color(0xFFBBF7D0)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.GppGood,
                                contentDescription = null,
                                tint = Color(0xFF16A34A),
                                modifier = Modifier.size(24.dp)
                            )
                            Column {
                                Text(
                                    text = "Tratamiento Protegido y Cifrado",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.5.sp,
                                    color = Color(0xFF166534)
                                )
                                Text(
                                    text = "Tus datos personales nunca se venden ni se ceden a terceros para fines comerciales o publicidad.",
                                    fontSize = 11.sp,
                                    color = Color(0xFF15803D),
                                    lineHeight = 14.sp
                                )
                            }
                        }
                    }

                    // Toggle: Tratamiento General de Datos Personales
                    ConsentSwitchItem(
                        icon = Icons.Default.Badge,
                        title = "Tratamiento de Datos para Mandados",
                        subtitle = "Permite procesar tu nombre, teléfono y direcciones para gestionar tus entregas locales en Acapulco.",
                        checked = userConsentDataAuth,
                        onCheckedChange = onConsentDataAuthChange,
                        color = AcaAzulConfianza,
                        testTag = "switch_consent_data"
                    )

                    // Toggle: Geolocalización en Tiempo Real
                    ConsentSwitchItem(
                        icon = Icons.Default.MyLocation,
                        title = "Geolocalización en Tiempo Real",
                        subtitle = "Necesario para calcular la ruta exacta de la moto, tiempo de llegada (ETA) y rastreo en vivo.",
                        checked = userConsentGpsTracking,
                        onCheckedChange = onConsentGpsTrackingChange,
                        color = Color(0xFF0284C7),
                        testTag = "switch_consent_gps"
                    )

                    // Toggle: Notificaciones Push Operativas
                    ConsentSwitchItem(
                        icon = Icons.Default.NotificationsActive,
                        title = "Notificaciones Push de Seguridad",
                        subtitle = "Alertas cuando tu mandado sea aceptado, repartidor en camino, PIN de seguridad y comprobante.",
                        checked = userConsentPushNotifications,
                        onCheckedChange = onConsentPushNotificationsChange,
                        color = Color(0xFFD97706),
                        testTag = "switch_consent_push"
                    )

                    // Toggle: Compartición Temporal con Repartidor
                    ConsentSwitchItem(
                        icon = Icons.Default.PhoneInTalk,
                        title = "Contacto Directo con Repartidor Asignado",
                        subtitle = "Permite que el repartidor en moto te llame o envíe WhatsApp exclusivamente mientras la entrega esté en curso.",
                        checked = sharePhoneTemp,
                        onCheckedChange = { sharePhoneTemp = it },
                        color = Color(0xFF059669),
                        testTag = "switch_consent_phone"
                    )

                    // Toggle: Evidencia Fotográfica de Entrega
                    ConsentSwitchItem(
                        icon = Icons.Default.CameraAlt,
                        title = "Comprobantes Fotográficos de Condición",
                        subtitle = "Respaldo visual para verificar que el paquete fue entregado íntegro en puerta o caseta de seguridad.",
                        checked = allowPhotoProof,
                        onCheckedChange = { allowPhotoProof = it },
                        color = Color(0xFF7C3AED),
                        testTag = "switch_consent_photo"
                    )

                    // Enlace a Políticas Completas
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFF8FAFC),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                        modifier = Modifier.fillMaxWidth()
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
                                    text = "Aviso de Privacidad Completo",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.5.sp,
                                    color = AcaAzulDark
                                )
                                Text(
                                    text = "Consulta el texto legal completo y el ejercicio de derechos ARCO.",
                                    fontSize = 11.sp,
                                    color = AcaGrisSubtle
                                )
                            }
                            TextButton(
                                onClick = onOpenPoliticasFull,
                                modifier = Modifier.testTag("btn_ver_aviso_completo")
                            ) {
                                Text("Leer Más", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AcaTurquesaDark)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Botón Guardar
                Button(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AcaAzulConfianza),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("btn_guardar_autorizaciones")
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Guardar Preferencias de Privacidad", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun ConsentSwitchItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    color: Color,
    testTag: String
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = Color.White,
        border = BorderStroke(1.dp, if (checked) color.copy(alpha = 0.4f) else Color(0xFFE2E8F0)),
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
                    .background(color.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(18.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.5.sp,
                    color = AcaAzulDark
                )
                Text(
                    text = subtitle,
                    fontSize = 10.5.sp,
                    color = AcaGrisSubtle,
                    lineHeight = 13.5.sp
                )
            }

            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = color
                ),
                modifier = Modifier.testTag(testTag)
            )
        }
    }
}
