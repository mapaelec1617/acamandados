package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.MandadoOrder
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ProofOfConditionDialog(
    order: MandadoOrder,
    onPhotoConfirmed: (photoUri: String) -> Unit,
    onDismiss: () -> Unit
) {
    var photoCaptured by remember { mutableStateOf(false) }
    val timeStamp = remember { SimpleDateFormat("HH:mm:ss dd/MM/yyyy", Locale.getDefault()).format(Date()) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
                .testTag("proof_of_condition_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Icono de Cámara de Seguridad
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(AcaTurquesaContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = null,
                        tint = AcaTurquesaDark,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Foto de Condición al Recoger",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Black,
                        color = AcaAzulDark,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "Proof of Condition (Estilo Roadie) • ${order.folio}",
                        fontSize = 11.5.sp,
                        color = AcaGrisSubtle,
                        textAlign = TextAlign.Center
                    )
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = AcaSurfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Default.Inventory, contentDescription = null, tint = AcaAzulConfianza, modifier = Modifier.size(16.dp))
                            Text(
                                text = "Paquete: ${order.tamanoPaquete} • ${order.titulo}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = AcaAzulDark
                            )
                        }
                        Text(
                            text = "Punto de recogida: ${order.puntoRecogida} (${order.zonaRecogida})",
                            fontSize = 11.sp,
                            color = AcaGrisEquilibrio
                        )
                    }
                }

                // Área interactiva del visor de foto
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (photoCaptured) Color(0xFF1E293B) else Color(0xFFF1F5F9))
                        .border(
                            2.dp,
                            if (photoCaptured) AcaTurquesa else Color(0xFFCBD5E1),
                            RoundedCornerShape(16.dp)
                        )
                        .clickable { photoCaptured = !photoCaptured },
                    contentAlignment = Alignment.Center
                ) {
                    if (photoCaptured) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = AcaTurquesaLight,
                                modifier = Modifier.size(40.dp)
                            )
                            Text(
                                text = "¡Foto de estado capturada!",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color.Black.copy(alpha = 0.6f)
                            ) {
                                Text(
                                    text = "📍 Acapulco • $timeStamp • Folio ${order.folio}",
                                    color = AcaTurquesaLight,
                                    fontSize = 9.5.sp,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                            Text(
                                text = "Toca para volver a capturar si deseas",
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 10.sp
                            )
                        }
                    } else {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PhotoCamera,
                                contentDescription = null,
                                tint = AcaAzulConfianza,
                                modifier = Modifier.size(38.dp)
                            )
                            Text(
                                text = "Toca aquí para tomar foto al paquete",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = AcaAzulDark
                            )
                            Text(
                                text = "Asegúrate de mostrar cómo te lo entregaron antes de meterlo a tu mochila o parrilla.",
                                fontSize = 10.5.sp,
                                color = AcaGrisSubtle,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                Text(
                    text = "🛡️ Esta foto deslinda tu responsabilidad ante el cliente en caso de que el producto ya viniera maltratado de origen.",
                    fontSize = 10.5.sp,
                    color = AcaGrisEquilibrio,
                    textAlign = TextAlign.Center,
                    lineHeight = 14.sp
                )

                // Botones
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
                            val uri = "file://recoleccion_${order.folio}_${System.currentTimeMillis()}.jpg"
                            onPhotoConfirmed(uri)
                        },
                        enabled = photoCaptured,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AcaTurquesaDark,
                            disabledContainerColor = Color(0xFFCBD5E1)
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1.4f)
                            .testTag("confirmar_foto_condicion_button")
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Iniciar Ruta", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}
