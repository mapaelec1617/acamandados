package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.MandadoOrder
import com.example.ui.theme.*

@Composable
fun PinValidationDialog(
    order: MandadoOrder,
    isOnRecipientDevice: Boolean = false,
    onConfirmDelivery: (pin: String, photoUri: String?) -> Unit,
    onDismiss: () -> Unit
) {
    var recipientDeviceMode by remember(isOnRecipientDevice) { mutableStateOf(isOnRecipientDevice) }
    var pinText by remember { mutableStateOf("") }
    var photoTaken by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val totalCobroEfectivo = order.getTotalCobroEfectivo()
    val isEfectivo = order.metodoPago == "EFECTIVO" || order.esCobroContraEntrega

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
                .testTag("pin_validation_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Selector de modo de ingreso de PIN (Dispositivo del Chofer vs Dispositivo del Receptor)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilterChip(
                        selected = !recipientDeviceMode,
                        onClick = { recipientDeviceMode = false },
                        label = { Text("🛵 Celular Chofer", fontSize = 10.5.sp) },
                        modifier = Modifier.testTag("chip_modo_chofer")
                    )
                    FilterChip(
                        selected = recipientDeviceMode,
                        onClick = { recipientDeviceMode = true },
                        label = { Text("📲 Celular Receptor", fontSize = 10.5.sp) },
                        modifier = Modifier.testTag("chip_modo_receptor")
                    )
                }

                // Cabecera con Icono de Candado / Seguridad
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (recipientDeviceMode) Color(0xFFDCFCE7) else AcaAzulContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (recipientDeviceMode) Icons.Default.VpnKey else Icons.Default.Lock,
                        contentDescription = null,
                        tint = if (recipientDeviceMode) Color(0xFF166534) else AcaAzulConfianza,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (recipientDeviceMode) "Validación en Celular del Receptor" else "Validación de Seguridad PIN",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Black,
                        color = AcaAzulDark,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = if (recipientDeviceMode) "El repartidor ingresa el código en este equipo • Pedido ${order.folio}" else "Estilo Uber Connect • Pedido ${order.folio}",
                        fontSize = 11.5.sp,
                        color = AcaGrisSubtle,
                        textAlign = TextAlign.Center
                    )
                }

                // Banner explicativo
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (recipientDeviceMode) Color(0xFFF0FDF4) else AcaSurfaceVariant,
                    border = if (recipientDeviceMode) androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBBF7D0)) else null,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.VerifiedUser,
                            contentDescription = null,
                            tint = if (recipientDeviceMode) Color(0xFF166534) else AcaAzulConfianza,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = if (recipientDeviceMode) {
                                "Pídele al repartidor que introduzca el PIN de 4 dígitos en este teléfono para autorizar y desbloquear la entrega presencial de ${order.getNombreDestinatario()}."
                            } else {
                                "Pídele a ${order.getNombreDestinatario()} el PIN de 4 dígitos que recibió por WhatsApp o en la app para liberar la entrega."
                            },
                            fontSize = 11.5.sp,
                            color = if (recipientDeviceMode) Color(0xFF14532D) else AcaAzulDark,
                            lineHeight = 15.sp
                        )
                    }
                }

                // Desglose de cobro para el repartidor (Efectivo vs Tarjeta)
                if (isEfectivo && totalCobroEfectivo > 0) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = AcaAmbarAlerta.copy(alpha = 0.15f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, AcaAmbarAlerta),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
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
                                        imageVector = Icons.Default.Payments,
                                        contentDescription = null,
                                        tint = Color(0xFFB45309),
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = "COBRAR EN EFECTIVO AL CLIENTE",
                                        fontWeight = FontWeight.Black,
                                        fontSize = 11.sp,
                                        color = Color(0xFFB45309)
                                    )
                                }
                                Text(
                                    text = "$${String.format("%.2f", totalCobroEfectivo)} MXN",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 16.sp,
                                    color = Color(0xFFB45309)
                                )
                            }
                            if (order.esCobroContraEntrega) {
                                Text(
                                    text = "• Mercancía producto: $${String.format("%.2f", order.montoProductoCobrar)} MXN\n• Tarifa de envío: $${String.format("%.2f", order.tarifaEnvio)} MXN",
                                    fontSize = 10.5.sp,
                                    color = Color(0xFF78350F)
                                )
                            }
                        }
                    }
                } else {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = AcaTurquesaContainer,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Default.CreditCard, contentDescription = null, tint = AcaTurquesaDark, modifier = Modifier.size(18.dp))
                            Text(
                                text = "✅ PAGADO CON TARJETA EN LA APP: NO cobrar efectivo. Tu tarifa de $${String.format("%.2f", order.tarifaEnvio)} MXN se deposita directo a tu CLABE.",
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = AcaTurquesaDark,
                                lineHeight = 14.sp
                            )
                        }
                    }
                }

                // Input del PIN (4 dígitos)
                OutlinedTextField(
                    value = pinText,
                    onValueChange = {
                        if (it.length <= 4 && it.all { char -> char.isDigit() }) {
                            pinText = it
                            errorMessage = null
                        }
                    },
                    label = { Text("PIN de 4 dígitos", textAlign = TextAlign.Center) },
                    placeholder = { Text("----", textAlign = TextAlign.Center) },
                    singleLine = true,
                    textStyle = LocalTextStyle.current.copy(
                        textAlign = TextAlign.Center,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 8.sp,
                        color = AcaAzulDark
                    ),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("pin_input_field")
                )

                // Botón de ayuda para testing rápido en emulador (Simula que el cliente dictó su PIN)
                TextButton(
                    onClick = {
                        pinText = order.pinSeguridad
                        errorMessage = null
                    },
                    modifier = Modifier.padding(vertical = 0.dp)
                ) {
                    Icon(Icons.Default.Key, contentDescription = null, modifier = Modifier.size(15.dp), tint = AcaTurquesaDark)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Probar con PIN del destinatario: [${order.pinSeguridad}]",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = AcaTurquesaDark
                    )
                }

                // Foto de Evidencia de Entrega (Opcional / Recomendada)
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (photoTaken) AcaTurquesaContainer else AcaSurfaceVariant,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .border(1.dp, if (photoTaken) AcaTurquesaDark else Color.Transparent, RoundedCornerShape(10.dp))
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = if (photoTaken) Icons.Default.CheckCircle else Icons.Default.CameraAlt,
                                contentDescription = null,
                                tint = if (photoTaken) AcaTurquesaDark else AcaGrisEquilibrio,
                                modifier = Modifier.size(20.dp)
                            )
                            Column {
                                Text(
                                    text = if (photoTaken) "Foto de entrega capturada ✅" else "Foto de entrega (fachada/puerta)",
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AcaAzulDark
                                )
                                Text(
                                    text = "Respaldo digital de entrega realizada",
                                    fontSize = 10.sp,
                                    color = AcaGrisSubtle
                                )
                            }
                        }

                        Button(
                            onClick = { photoTaken = !photoTaken },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (photoTaken) AcaTurquesaDark else AcaAzulConfianza
                            ),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = if (photoTaken) "Cambiar" else "Tomar Foto",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Mensaje de Error si el PIN no coincide
                AnimatedVisibility(visible = errorMessage != null) {
                    errorMessage?.let {
                        Text(
                            text = it,
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                // Botones de acción
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancelar", color = AcaGrisEquilibrio)
                    }

                    Button(
                        onClick = {
                            if (pinText.length != 4) {
                                errorMessage = "Ingresa los 4 dígitos del PIN de seguridad."
                            } else if (pinText != order.pinSeguridad) {
                                errorMessage = "PIN incorrecto. Pídele al receptor que verifique su código en WhatsApp o en la app."
                            } else {
                                val photoUri = if (photoTaken) "file://evidencia_entrega_${order.folio}.jpg" else null
                                onConfirmDelivery(pinText, photoUri)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AcaTurquesaDark),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1.5f)
                            .testTag("confirmar_entrega_pin_button")
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Validar y Entregar", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}
