package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.MandadosViewModel
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun RepartidorBilleteraScreen(
    viewModel: MandadosViewModel,
    modifier: Modifier = Modifier
) {
    val profile by viewModel.repartidorProfile.collectAsStateWithLifecycle()
    val depositos by viewModel.depositosLog.collectAsStateWithLifecycle()
    val completedOrders by viewModel.driverCompletedOrders.collectAsStateWithLifecycle()

    val totalEfectivo = remember(completedOrders) {
        completedOrders.filter { it.metodoPago == "EFECTIVO" }.sumOf { it.tarifaEnvio }
    }
    val totalTarjeta = remember(completedOrders) {
        completedOrders.filter { it.metodoPago == "TARJETA" }.sumOf { it.tarifaEnvio }
    }

    val dateFormatter = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("es", "MX")) }
    val dateOnlyFormatter = remember { SimpleDateFormat("EEEE dd MMMM", Locale("es", "MX")) }

    var showRenovarDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(AcaBackground),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Column {
                Text(
                    text = "Mi Billetera & Suscripción",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = AcaAzulDark
                )
                Text(
                    text = "Control de ganancias 100% tuyas y suscripción semanal",
                    fontSize = 11.5.sp,
                    color = AcaGrisSubtle
                )
            }
        }

        // Tarjeta Principal de Suscripción Semanal ($150 MXN)
        item {
            val isActiva = profile?.suscripcionActiva == true
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isActiva) AcaAzulDark else Color(0xFF7F1D1D)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Suscripción Semanal Repartidor",
                                color = Color.White.copy(alpha = 0.85f),
                                fontSize = 12.sp
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "$150.00",
                                    color = AcaTurquesaLight,
                                    fontSize = 26.sp,
                                    fontWeight = FontWeight.Black
                                )
                                Text(
                                    text = " MXN / semana",
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = if (isActiva) AcaGreenSuccess else Color.White
                        ) {
                            Text(
                                text = if (isActiva) "✓ Activa" else "Inactiva",
                                color = if (isActiva) Color.White else Color(0xFF7F1D1D),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }

                    HorizontalDivider(color = Color.White.copy(alpha = 0.2f))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Stars,
                            contentDescription = null,
                            tint = AcaTurquesaLight,
                            modifier = Modifier.size(22.dp)
                        )
                        Text(
                            text = "¡100% de los cobros al cliente son para ti! Cero comisiones por viaje.",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    if (isActiva && profile != null) {
                        Text(
                            text = "Válida hasta: ${dateOnlyFormatter.format(Date(profile!!.suscripcionFechaFin))}",
                            color = AcaTurquesaContainer,
                            fontSize = 11.5.sp
                        )
                    }

                    Button(
                        onClick = { showRenovarDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = AcaTurquesa),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Autorenew,
                            contentDescription = null,
                            tint = Color(0xFF00382E),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isActiva) "Renovar Suscripción (+7 días por $150 MXN)" else "Pagar Suscripción ($150 MXN)",
                            color = Color(0xFF00382E),
                            fontWeight = FontWeight.Black,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }

        // Resumen de Ganancias
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Ganancia Efectivo
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier
                        .weight(1f)
                        .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(14.dp))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Payments,
                                contentDescription = null,
                                tint = AcaTurquesaDark,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Efectivo Directo", fontSize = 11.sp, color = AcaGrisSubtle)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "$${String.format("%.2f", totalEfectivo)}",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = AcaGrisEquilibrio
                        )
                        Text(
                            text = "Cobrado en mano",
                            fontSize = 9.5.sp,
                            color = AcaGreenSuccess,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Ganancia Tarjeta (SPEI Mismo Día)
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier
                        .weight(1f)
                        .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(14.dp))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CreditCard,
                                contentDescription = null,
                                tint = AcaAzulConfianza,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Tarjeta / App", fontSize = 11.sp, color = AcaGrisSubtle)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "$${String.format("%.2f", totalTarjeta + depositos.sumOf { it.monto })}",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = AcaAzulDark
                        )
                        Text(
                            text = "Depósito hoy mismo",
                            fontSize = 9.5.sp,
                            color = AcaAzulConfianza,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Tarjeta Informativa: Política de Depósito el Mismo Día
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = AcaTurquesaContainer),
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
                            .clip(RoundedCornerShape(8.dp))
                            .background(AcaTurquesaDark),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountBalance,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Column {
                        Text(
                            text = "Depósitos el Mismo Día vía SPEI",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = Color(0xFF00382E)
                        )
                        Text(
                            text = "Cuando el cliente pague con tarjeta en la app, tu ganancia se deposita hoy mismo a tu cuenta: ${profile?.banco ?: "BBVA"} •• ${profile?.clabeInterbancaria?.takeLast(4) ?: "1234"}",
                            fontSize = 11.sp,
                            color = Color(0xFF004D40)
                        )
                    }
                }
            }
        }

        // Historial de Depósitos Bancarios SPEI del Día
        item {
            Text(
                text = "Depósitos Bancarios Registrados Hoy (${depositos.size})",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = AcaAzulDark,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        if (depositos.isEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Sin depósitos de tarjeta pendientes hoy",
                            fontSize = 12.5.sp,
                            color = AcaGrisSubtle
                        )
                    }
                }
            }
        } else {
            items(depositos) { dep ->
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(12.dp))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(AcaAzulContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = AcaAzulConfianza,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Column {
                                Text(
                                    text = "Mandado ${dep.orderFolio}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = AcaAzulDark
                                )
                                Text(
                                    text = "SPEI: ${dep.claveRastreoSpei}",
                                    fontSize = 10.sp,
                                    color = AcaGrisSubtle
                                )
                                Text(
                                    text = "${dep.bancoDestino} •••• ${dep.clabeTerminacion}",
                                    fontSize = 10.5.sp,
                                    color = AcaGrisEquilibrio
                                )
                            }
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "+$${String.format("%.2f", dep.monto)} MXN",
                                fontWeight = FontWeight.Black,
                                fontSize = 14.5.sp,
                                color = AcaGreenSuccess
                            )
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFFD1FAE5)
                            ) {
                                Text(
                                    text = "Depositado Hoy",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF065F46),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            OutlinedButton(
                onClick = { viewModel.openCompletedOrdersScreen() },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = AcaTurquesaDark),
                border = BorderStroke(1.dp, AcaTurquesaDark),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("btn_historial_desde_billetera")
            ) {
                Icon(
                    imageVector = Icons.Default.ReceiptLong,
                    contentDescription = null,
                    tint = AcaTurquesaDark,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Ver Historial Completo de Mandados Entregados",
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }

    if (showRenovarDialog) {
        AlertDialog(
            onDismissRequest = { showRenovarDialog = false },
            title = {
                Text("Suscripción Semanal AcaMandados", fontWeight = FontWeight.Bold, color = AcaAzulDark)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Monto semanal: $150.00 MXN",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = AcaTurquesaDark
                    )
                    Text(
                        text = "Beneficios:\n• 100% de ganancia en cada viaje para ti.\n• Sin comisiones por mandado.\n• Depósitos bancarios de tarjeta el mismo día.\n• Soporte y coordinación local en Acapulco.",
                        fontSize = 12.sp,
                        color = AcaGrisEquilibrio
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.renovarSuscripcion()
                        showRenovarDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AcaTurquesa)
                ) {
                    Text("Confirmar Pago de $150 MXN", color = Color(0xFF00382E), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showRenovarDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}
