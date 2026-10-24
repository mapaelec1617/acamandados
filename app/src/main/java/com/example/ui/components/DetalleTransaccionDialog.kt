package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.TransaccionBilletera
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Diálogo modal para visualizar el detalle completo de una transacción de la billetera:
 * - Ingresos por mandados completados (flete + producto)
 * - Retiros a cuenta bancaria CLABE (SPEI)
 * - Pagos de suscripción semanal
 */
@Composable
fun DetalleTransaccionDialog(
    transaccion: TransaccionBilletera,
    onDismiss: () -> Unit,
    onViewOrderReceipt: ((String) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val dateFormatter = remember { SimpleDateFormat("dd 'de' MMMM yyyy, HH:mm 'hrs'", Locale("es", "MX")) }
    val fechaStr = remember(transaccion.fecha) {
        dateFormatter.format(Date(transaccion.fecha))
    }

    val isIngreso = transaccion.isIngreso
    val isRetiro = transaccion.tipo == "RETIRO"

    val (badgeBg, badgeText, badgeColor, iconVector) = when (transaccion.tipo) {
        "INGRESO" -> Quadruple(
            Color(0xFFDCFCE7),
            "INGRESO ACREDITADO",
            Color(0xFF16A34A),
            Icons.Default.ArrowDownward
        )
        "RETIRO" -> Quadruple(
            Color(0xFFFEE2E2),
            "RETIRO SPEI DISPERSADO",
            Color(0xFFDC2626),
            Icons.Default.ArrowUpward
        )
        else -> Quadruple(
            Color(0xFFEFF6FF),
            "SUSCRIPCIÓN SEMANAL",
            Color(0xFF2563EB),
            Icons.Default.Autorenew
        )
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color.White,
            tonalElevation = 8.dp,
            modifier = modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .testTag("dialog_detalle_transaccion")
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
                                .background(badgeBg),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = iconVector,
                                contentDescription = null,
                                tint = badgeColor,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Detalle de Transacción",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 14.5.sp,
                                color = AcaAzulDark
                            )
                            Text(
                                text = "Billetera AcaMandados Repartidor",
                                fontSize = 10.5.sp,
                                color = AcaGrisSubtle
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

                // Monto Principal de la Transacción
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
                            text = if (isIngreso) "Monto Acreditado" else "Monto Dispersado / Debitado",
                            fontSize = 11.sp,
                            color = AcaGrisSubtle
                        )
                        Text(
                            text = "${if (isIngreso) "+" else "-"}$${String.format("%.2f", transaccion.monto)} MXN",
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Black,
                            color = if (isIngreso) Color(0xFF047857) else Color(0xFF991B1B)
                        )
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = badgeBg
                        ) {
                            Text(
                                text = badgeText,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = badgeColor,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                // Desglose de Información
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "Datos de la Operación",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = AcaAzulDark
                    )

                    DetailItemRow(label = "Concepto", value = transaccion.concepto)
                    DetailItemRow(label = "Detalle", value = transaccion.detalle)
                    if (transaccion.orderFolio != null) {
                        DetailItemRow(label = "Folio del Mandado", value = transaccion.orderFolio)
                    }
                    DetailItemRow(label = "Método / Canal", value = transaccion.metodoPago)
                    DetailItemRow(label = "Fecha y Hora", value = fechaStr)
                    DetailItemRow(label = "Estado", value = transaccion.estado, isHighlighted = true)
                }

                // Si fue vía SPEI / Transferencia
                if (transaccion.claveRastreoSpei != null) {
                    HorizontalDivider(color = Color(0xFFF1F5F9))

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Información Bancaria SPEI",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = AcaAzulDark
                        )
                        if (transaccion.bancoDestino != null) {
                            DetailItemRow(label = "Banco Receptor", value = transaccion.bancoDestino)
                        }
                        if (transaccion.clabeTerminacion != null) {
                            DetailItemRow(label = "Cuenta CLABE", value = "•••• •••• •••• ${transaccion.clabeTerminacion}")
                        }

                        // Caja con clave SPEI copiable
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFEFF6FF),
                            border = BorderStroke(1.dp, Color(0xFFBFDBFE)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Clave de Rastreo SPEI", fontSize = 9.5.sp, color = Color(0xFF1D4ED8))
                                    Text(
                                        text = transaccion.claveRastreoSpei,
                                        fontSize = 11.sp,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold,
                                        color = AcaAzulDark
                                    )
                                }
                                IconButton(
                                    onClick = {
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        clipboard.setPrimaryClip(ClipData.newPlainText("Clave SPEI", transaccion.claveRastreoSpei))
                                        Toast.makeText(context, "Clave SPEI copiada al portapapeles", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ContentCopy,
                                        contentDescription = "Copiar",
                                        tint = Color(0xFF2563EB),
                                        modifier = Modifier.size(15.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Botones de Acción
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            val shareText = "📄 Comprobante de Movimiento AcaMandados\n" +
                                    "• Concepto: ${transaccion.concepto}\n" +
                                    "• Monto: ${if (isIngreso) "+" else "-"}$${String.format("%.2f", transaccion.monto)} MXN\n" +
                                    "• Tipo: ${transaccion.tipo}\n" +
                                    "• Canal: ${transaccion.metodoPago}\n" +
                                    (if (transaccion.claveRastreoSpei != null) "• Clave SPEI: ${transaccion.claveRastreoSpei}\n" else "") +
                                    "• Fecha: $fechaStr\n" +
                                    "AcaMandados Repartidor • Acapulco"
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_SUBJECT, "Comprobante - ${transaccion.concepto}")
                                putExtra(Intent.EXTRA_TEXT, shareText)
                            }
                            context.startActivity(Intent.createChooser(intent, "Compartir Transacción"))
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, AcaAzulConfianza)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(14.dp), tint = AcaAzulConfianza)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Compartir", fontSize = 11.sp, color = AcaAzulConfianza, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = AcaAzulDark),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Cerrar", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

@Composable
private fun DetailItemRow(
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
            color = AcaGrisSubtle
        )
        Text(
            text = value,
            fontSize = 11.5.sp,
            fontWeight = if (isHighlighted) FontWeight.Bold else FontWeight.SemiBold,
            color = if (isHighlighted) Color(0xFF047857) else AcaAzulDark
        )
    }
}
