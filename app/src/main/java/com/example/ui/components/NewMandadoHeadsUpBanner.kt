package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ElectricScooter
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Paid
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.NewMandadoDriverAlert
import com.example.ui.theme.*
import kotlinx.coroutines.delay

/**
 * Banner flotante "Heads-Up" en primer plano que notifica a los repartidores
 * de inmediato en cuanto se crea una nueva solicitud de mandado en Acapulco.
 */
@Composable
fun NewMandadoHeadsUpBanner(
    alert: NewMandadoDriverAlert?,
    onAcceptOrView: (Long) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Auto-descartar después de 12 segundos si no se interactúa
    LaunchedEffect(alert?.timestamp) {
        if (alert != null) {
            delay(12000)
            onDismiss()
        }
    }

    AnimatedVisibility(
        visible = alert != null,
        enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
        modifier = modifier
    ) {
        alert?.let { item ->
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = Color(0xFF0F172A),
                border = BorderStroke(1.5.dp, AcaTurquesa),
                shadowElevation = 8.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp)
                    .testTag("heads_up_new_mandado_banner")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Encabezado de alerta con badge pulsante y botón de cerrar
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
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(AcaTurquesa),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ElectricScooter,
                                    contentDescription = null,
                                    tint = Color(0xFF00382E),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text(
                                        text = "🚨 ¡NUEVA SOLICITUD DISPONIBLE!",
                                        color = AcaTurquesaLight,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = Color(0xFF1E293B)
                                    ) {
                                        Text(
                                            text = item.folio,
                                            color = Color.White,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = "Mandado creado recién en Acapulco",
                                    color = Color.White.copy(alpha = 0.75f),
                                    fontSize = 10.sp
                                )
                            }
                        }

                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Cerrar alerta",
                                tint = Color.White.copy(alpha = 0.7f),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    // Detalle de la ruta y paquete
                    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Text(
                            text = item.titulo,
                            color = Color.White,
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(Icons.Default.Navigation, contentDescription = null, tint = AcaTurquesa, modifier = Modifier.size(12.dp))
                            Text(
                                text = "${item.zonaRecogida} ➔ ${item.zonaEntrega}",
                                color = Color(0xFFE2E8F0),
                                fontSize = 11.5.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    // Ganancia y método de pago
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(Icons.Default.Paid, contentDescription = null, tint = Color(0xFF86EFAC), modifier = Modifier.size(16.dp))
                            Text(
                                text = "Ganancia 100%: $${String.format("%.2f", item.tarifaEnvio)} MXN",
                                color = Color(0xFF86EFAC),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Black
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (item.metodoPago == "TARJETA") Color(0xFF1E3A8A) else Color(0xFF064E3B)
                        ) {
                            Text(
                                text = if (item.metodoPago == "TARJETA") "💳 Tarjeta en App" else "💵 Efectivo al entregar",
                                color = if (item.metodoPago == "TARJETA") Color(0xFF93C5FD) else Color(0xFFA7F3D0),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    // Botón de acción principal directo para aceptar / ver
                    Button(
                        onClick = { onAcceptOrView(item.orderId) },
                        colors = ButtonDefaults.buttonColors(containerColor = AcaTurquesa),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("btn_accept_heads_up_mandado")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ElectricScooter,
                            contentDescription = null,
                            tint = Color(0xFF00382E),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "🛵 Ver y Aceptar Mandado ($${String.format("%.2f", item.tarifaEnvio)} MXN)",
                            color = Color(0xFF00382E),
                            fontWeight = FontWeight.Black,
                            fontSize = 12.5.sp
                        )
                    }
                }
            }
        }
    }
}
