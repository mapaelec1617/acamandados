package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.MandadoOrder
import com.example.ui.theme.*

/**
 * Diálogo de confirmación mostrado al crear una orden, presentando la URL única generada
 * y el PIN de entrega para compartir inmediatamente con el destinatario sin app.
 */
@Composable
fun OrderCreatedConfirmationDialog(
    order: MandadoOrder,
    onDismiss: () -> Unit,
    onOpenWebTracking: () -> Unit
) {
    val context = LocalContext.current
    val webUrl = order.getWebTrackingUrl()

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
                .testTag("dialog_order_created_confirmation")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Icono animado de éxito
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFDCFCE7)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Color(0xFF16A34A),
                        modifier = Modifier.size(32.dp)
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "¡Mandado Solicitado!",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = AcaAzulDark
                    )
                    Text(
                        text = "Folio: ${order.folio} • Generamos tu URL única de rastreo",
                        fontSize = 12.sp,
                        color = AcaGrisSubtle,
                        textAlign = TextAlign.Center
                    )
                }

                // Card con URL Única Generada
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = AcaSurfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Default.Link, contentDescription = null, tint = AcaTurquesaDark, modifier = Modifier.size(16.dp))
                            Text(
                                text = "Enlace Web Único (Sin Descargar App)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.5.sp,
                                color = AcaAzulDark
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color.White,
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = webUrl,
                                    fontSize = 11.sp,
                                    color = AcaGrisEquilibrio,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f)
                                )

                                IconButton(
                                    onClick = {
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                                        val clip = ClipData.newPlainText("URL de Rastreo", webUrl)
                                        clipboard?.setPrimaryClip(clip)
                                        Toast.makeText(context, "✅ Enlace web único copiado", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ContentCopy,
                                        contentDescription = "Copiar",
                                        tint = AcaTurquesaDark,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }

                        Text(
                            text = "El destinatario (${order.getNombreDestinatario()}) podrá ver la moto en vivo en el mapa de Acapulco y el estado sin necesidad de instalar la app.",
                            fontSize = 10.5.sp,
                            color = AcaGrisSubtle,
                            lineHeight = 14.sp
                        )
                    }
                }

                // PIN de Seguridad de la Entrega
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = AcaAzulDark,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "PIN DE ENTREGA",
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Para recibir en mano",
                                color = Color.White.copy(alpha = 0.6f),
                                fontSize = 9.5.sp
                            )
                        }

                        Text(
                            text = order.pinSeguridad,
                            color = AcaTurquesaLight,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 4.sp
                        )
                    }
                }

                // Botones de Acción
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            val tel = order.getTelefonoDestinatario().replace("[^0-9]".toRegex(), "")
                            val msg = order.getShareableTrackingMessage()
                            val url = if (tel.length == 10) {
                                "https://api.whatsapp.com/send?phone=52$tel&text=" + Uri.encode(msg)
                            } else {
                                "https://api.whatsapp.com/send?text=" + Uri.encode(msg)
                            }
                            try {
                                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                            } catch (_: Exception) {
                                Toast.makeText(context, "URL: $webUrl", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                            .testTag("btn_confirm_dialog_share_whatsapp")
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Enviar por WhatsApp al Destinatario", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                onDismiss()
                                onOpenWebTracking()
                            },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .testTag("btn_confirm_dialog_view_web")
                        ) {
                            Icon(Icons.Default.Public, contentDescription = null, tint = AcaAzulConfianza, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Ver Web", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = AcaAzulConfianza)
                        }

                        Button(
                            onClick = onDismiss,
                            colors = ButtonDefaults.buttonColors(containerColor = AcaAzulConfianza),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .testTag("btn_confirm_dialog_done")
                        ) {
                            Text("Ir a Mandados", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
