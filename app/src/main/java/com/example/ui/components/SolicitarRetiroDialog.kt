package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.RepartidorProfile
import com.example.ui.theme.*

/**
 * Diálogo modal para solicitar y procesar un retiro de ganancias bancario SPEI.
 */
@Composable
fun SolicitarRetiroDialog(
    saldoDisponible: Double,
    profile: RepartidorProfile?,
    onDismiss: () -> Unit,
    onConfirmRetiro: (Double) -> Unit,
    modifier: Modifier = Modifier
) {
    var montoInput by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val banco = profile?.banco ?: "BBVA México"
    val clabeTerm = profile?.clabeInterbancaria?.takeLast(4) ?: "1234"
    val repartidorNombre = profile?.nombre ?: "Carlos Mendoza"

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color.White,
            tonalElevation = 8.dp,
            modifier = modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .testTag("dialog_solicitar_retiro")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
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
                        Icon(
                            imageVector = Icons.Default.AccountBalance,
                            contentDescription = null,
                            tint = AcaAzulDark,
                            modifier = Modifier.size(24.dp)
                        )
                        Column {
                            Text(
                                text = "Retirar a Cuenta Bancaria",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = AcaAzulDark
                            )
                            Text(
                                text = "Dispersión SPEI el mismo día",
                                fontSize = 11.sp,
                                color = Color(0xFF16A34A),
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Outlined.Close, contentDescription = "Cerrar", tint = AcaGrisSubtle)
                    }
                }

                // Balance Disponible Actual
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFF8FAFC),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text("Saldo acumulado para retiro", fontSize = 11.sp, color = AcaGrisSubtle)
                        Text(
                            text = "$${String.format("%.2f", saldoDisponible)} MXN",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Black,
                            color = AcaAzulDark
                        )
                    }
                }

                // Campo de Monto a Retirar
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Monto a Retirar (MXN):",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = AcaAzulDark
                    )

                    OutlinedTextField(
                        value = montoInput,
                        onValueChange = {
                            montoInput = it.filter { char -> char.isDigit() || char == '.' }
                            errorMessage = null
                        },
                        prefix = { Text("$ ", fontWeight = FontWeight.Bold) },
                        placeholder = { Text("Ej: 200.00") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        isError = errorMessage != null,
                        supportingText = errorMessage?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_monto_retiro")
                    )

                    // Chips de montos rápidos
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(100.0, 200.0, 500.0).forEach { quickVal ->
                            val canSelect = saldoDisponible >= quickVal
                            AssistChip(
                                onClick = {
                                    montoInput = quickVal.toInt().toString()
                                    errorMessage = null
                                },
                                enabled = canSelect,
                                label = { Text("$$quickVal", fontSize = 10.5.sp) },
                                modifier = Modifier.weight(1f)
                            )
                        }

                        if (saldoDisponible > 0) {
                            AssistChip(
                                onClick = {
                                    montoInput = String.format("%.2f", saldoDisponible)
                                    errorMessage = null
                                },
                                label = { Text("Todo", fontSize = 10.5.sp, fontWeight = FontWeight.Bold) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                // Datos de la Cuenta Destino
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFFEFF6FF),
                    border = BorderStroke(1.dp, Color(0xFFBFDBFE)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = "Cuenta de Destino Registrada:",
                            fontSize = 10.5.sp,
                            color = Color(0xFF1D4ED8),
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "$banco •••• $clabeTerm",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = AcaAzulDark
                        )
                        Text(
                            text = "Beneficiario: $repartidorNombre",
                            fontSize = 11.sp,
                            color = AcaGrisSubtle
                        )
                    }
                }

                // Botones
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Cancelar", fontSize = 12.sp)
                    }

                    Button(
                        onClick = {
                            val parsed = montoInput.toDoubleOrNull()
                            if (parsed == null || parsed <= 0) {
                                errorMessage = "Ingresa un monto válido mayor a $0"
                                return@Button
                            }
                            if (saldoDisponible > 0 && parsed > saldoDisponible) {
                                errorMessage = "El monto excede tu saldo ($${String.format("%.2f", saldoDisponible)})"
                                return@Button
                            }
                            onConfirmRetiro(parsed)
                        },
                        modifier = Modifier
                            .weight(1.3f)
                            .testTag("btn_confirmar_retiro_spei"),
                        colors = ButtonDefaults.buttonColors(containerColor = AcaAzulDark),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Bolt, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Confirmar Retiro", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
