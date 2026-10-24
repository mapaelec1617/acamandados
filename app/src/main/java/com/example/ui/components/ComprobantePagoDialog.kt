package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.MandadoOrder
import com.example.data.model.RepartidorProfile
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Diálogo modal de Comprobante / Recibo Digital de Liquidación SPEI para el Repartidor.
 * Muestra el desglose de la tarifa ganada, reembolso de productos, cuenta CLABE de depósito
 * y clave de rastreo bancaria oficial.
 */
@Composable
fun ComprobantePagoDialog(
    order: MandadoOrder,
    profile: RepartidorProfile?,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val dateFormatter = remember { SimpleDateFormat("dd 'de' MMMM yyyy, HH:mm 'hrs'", Locale("es", "MX")) }
    val fechaStr = remember(order.fechaEntrega) {
        val f = order.fechaEntrega ?: System.currentTimeMillis()
        dateFormatter.format(Date(f))
    }

    val speiKey = order.claveRastreoSpei ?: "STP${order.id}${order.folio.filter { it.isDigit() }}"
    val totalMonto = order.tarifaEnvio + order.costoProductosEstimado
    val banco = profile?.banco ?: "BBVA México"
    val clabeTerm = profile?.clabeInterbancaria?.takeLast(4) ?: "1234"
    val repartidorNombre = profile?.nombre ?: (order.repartidorNombre ?: "Carlos Mendoza")

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color.White,
            tonalElevation = 8.dp,
            modifier = modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .testTag("dialog_comprobante_pago_spei")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header con botón de cerrar
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
                                .background(Color(0xFFDCFCE7)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Verified,
                                contentDescription = null,
                                tint = Color(0xFF16A34A),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Comprobante de Pago SPEI",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 14.5.sp,
                                color = AcaAzulDark
                            )
                            Text(
                                text = "Depósito el mismo día • Liquidado",
                                fontSize = 10.5.sp,
                                color = Color(0xFF16A34A),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(Icons.Outlined.Close, contentDescription = "Cerrar", tint = AcaGrisSubtle)
                    }
                }

                // Monto Principal Acreditado
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFFF8FAFC),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Monto Total Acreditado al Repartidor",
                            fontSize = 11.sp,
                            color = AcaGrisSubtle
                        )
                        Text(
                            text = "$${String.format("%.2f", totalMonto)} MXN",
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF047857)
                        )
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFFD1FAE5)
                        ) {
                            Text(
                                text = "TRANSFERENCIA INTERBANCARIA EXITOSA",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF065F46),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                // Desglose de Ganancias
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "Desglose de Ganancia",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = AcaAzulDark
                    )

                    ReceiptRow(label = "Mandado / Folio", value = order.folio)
                    ReceiptRow(label = "Tarifa de Envío (100% tuya)", value = "+$${String.format("%.2f", order.tarifaEnvio)} MXN")
                    if (order.costoProductosEstimado > 0) {
                        ReceiptRow(label = "Reembolso por Compra", value = "+$${String.format("%.2f", order.costoProductosEstimado)} MXN")
                    }
                    ReceiptRow(label = "Comisión AcaMandados", value = "-$0.00 MXN", isHighlighted = true)
                }

                HorizontalDivider(color = Color(0xFFF1F5F9))

                // Datos de la Transferencia Bancaria
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "Datos Bancarios del Depósito",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = AcaAzulDark
                    )

                    ReceiptRow(label = "Beneficiario", value = repartidorNombre)
                    ReceiptRow(label = "Banco Receptor", value = banco)
                    ReceiptRow(label = "Cuenta CLABE", value = "•••• •••• •••• $clabeTerm")
                    ReceiptRow(label = "Fecha y Hora", value = fechaStr)
                }

                // Clave de Rastreo SPEI con botón de copiar
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFFEFF6FF),
                    border = BorderStroke(1.dp, Color(0xFFBFDBFE)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Clave de Rastreo SPEI / STP",
                                fontSize = 10.sp,
                                color = Color(0xFF1D4ED8),
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = speiKey,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = AcaAzulDark
                            )
                        }

                        IconButton(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                clipboard.setPrimaryClip(ClipData.newPlainText("Clave SPEI", speiKey))
                                Toast.makeText(context, "Clave SPEI copiada al portapapeles", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copiar clave",
                                tint = Color(0xFF2563EB),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                // Botones de Acción: Compartir Comprobante y Cerrar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            val shareText = "📄 Comprobante de Depósito AcaMandados\n" +
                                    "• Mandado: ${order.folio}\n" +
                                    "• Monto depositado: $${String.format("%.2f", totalMonto)} MXN\n" +
                                    "• Banco: $banco (••$clabeTerm)\n" +
                                    "• Clave de Rastreo SPEI: $speiKey\n" +
                                    "• Beneficiario: $repartidorNombre\n" +
                                    "• Fecha: $fechaStr\n" +
                                    "¡Depósito procesado el mismo día sin comisiones!"
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_SUBJECT, "Comprobante SPEI - Mandado ${order.folio}")
                                putExtra(Intent.EXTRA_TEXT, shareText)
                            }
                            context.startActivity(Intent.createChooser(intent, "Compartir Comprobante SPEI"))
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, AcaAzulConfianza)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(15.dp), tint = AcaAzulConfianza)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Compartir", fontSize = 11.5.sp, color = AcaAzulConfianza, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = AcaAzulDark),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Entendido", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun ReceiptRow(
    label: String,
    value: String,
    isHighlighted: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            color = if (isHighlighted) Color(0xFF047857) else AcaGrisSubtle,
            fontWeight = if (isHighlighted) FontWeight.Bold else FontWeight.Normal
        )
        Text(
            text = value,
            fontSize = 11.5.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (isHighlighted) Color(0xFF047857) else AcaAzulDark
        )
    }
}
