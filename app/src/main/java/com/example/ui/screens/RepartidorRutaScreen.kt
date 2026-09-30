package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.MandadosViewModel
import com.example.ui.RepartidorTab
import com.example.ui.components.MandadoCard
import com.example.ui.theme.*

@Composable
fun RepartidorRutaScreen(
    viewModel: MandadosViewModel,
    modifier: Modifier = Modifier
) {
    val activeOrder by viewModel.currentDriverActiveOrder.collectAsStateWithLifecycle()
    val completedOrders by viewModel.driverCompletedOrders.collectAsStateWithLifecycle()
    val driverLoc by viewModel.driverLocationState.collectAsStateWithLifecycle()
    val context = LocalContext.current

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
                    text = "Mi Ruta Activa",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = AcaAzulDark
                )
                Text(
                    text = "Gestión del mandado en curso y navegación GPS en tiempo real",
                    fontSize = 11.5.sp,
                    color = AcaGrisSubtle
                )
            }
        }

        val order = activeOrder
        if (order != null) {
            // Mapa Interactivo de la ruta en curso
            item {
                com.example.ui.components.AcapulcoInteractiveMap(
                    driverLat = driverLoc.lat,
                    driverLng = driverLoc.lng,
                    driverSpeedKmh = driverLoc.speedKmh,
                    driverBearing = driverLoc.bearing,
                    pickupPoint = com.example.ui.components.MapLocationPoint(
                        lat = order.latRecogida,
                        lng = order.lngRecogida,
                        label = order.zonaRecogida,
                        sublabel = order.puntoRecogida
                    ),
                    deliveryPoint = com.example.ui.components.MapLocationPoint(
                        lat = order.latEntrega,
                        lng = order.lngEntrega,
                        label = order.zonaEntrega,
                        sublabel = order.puntoEntrega
                    ),
                    targetType = if (order.estado == "ACEPTADO") "PICKUP" else "DELIVERY",
                    distanceRemainingKm = driverLoc.distanceRemainingKm,
                    etaMinutes = driverLoc.etaMinutes,
                    driverName = "Tú (${order.repartidorVehiculo ?: "Moto"})",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp)
                )
            }

            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = AcaAzulDark),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "En Progreso • ${order.folio}",
                                color = AcaTurquesaLight,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "Ganancia: $${String.format("%.2f", order.tarifaEnvio)} MXN",
                                color = Color.White,
                                fontWeight = FontWeight.Black,
                                fontSize = 14.sp
                            )
                        }

                        // Botones de Navegación GPS
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    openMapDirections(context, "${order.puntoRecogida}, Acapulco, Guerrero")
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = AcaTurquesa),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Directions,
                                    contentDescription = null,
                                    tint = Color(0xFF00382E),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Navegar Origen", fontSize = 11.sp, color = Color(0xFF00382E), fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = {
                                    openMapDirections(context, "${order.puntoEntrega}, Acapulco, Guerrero")
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.2f)),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Navigation,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Navegar Destino", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            item {
                MandadoCard(
                    order = order,
                    isDriverView = true,
                    onAdvanceStatus = { nextStatus ->
                        viewModel.avanzarEstado(order.id, nextStatus)
                    },
                    onOpenTracking = { viewModel.openLiveTracking(it) },
                    onOpenRating = { ord, isDriver -> viewModel.openRatingDialog(ord, isDriver) },
                    onOpenPinValidation = { viewModel.openPinValidation(it) },
                    onOpenProofOfCondition = { viewModel.openProofOfCondition(it) }
                )
            }
        } else {
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircleOutline,
                            contentDescription = null,
                            tint = AcaTurquesaDark,
                            modifier = Modifier.size(44.dp)
                        )
                        Text(
                            text = "No tienes ningún mandado asignado",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = AcaGrisEquilibrio
                        )
                        Text(
                            text = "Revisa la lista de mandados disponibles para tomar un pedido y comenzar tu ruta.",
                            fontSize = 11.5.sp,
                            color = AcaGrisSubtle
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Button(
                            onClick = { viewModel.setRepartidorTab(RepartidorTab.DISPONIBLES) },
                            colors = ButtonDefaults.buttonColors(containerColor = AcaAzulConfianza),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Ver Mandados Disponibles", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Historial de Viajes Completados
        item {
            Text(
                text = "Tus Entregas Completadas (${completedOrders.size})",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = AcaAzulDark,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        if (completedOrders.isNotEmpty()) {
            items(completedOrders, key = { it.id }) { completed ->
                MandadoCard(
                    order = completed,
                    isDriverView = true,
                    onOpenRating = { ord, isDriver -> viewModel.openRatingDialog(ord, isDriver) }
                )
            }
        }
    }
}

private fun openMapDirections(context: Context, address: String) {
    try {
        val uri = Uri.parse("geo:0,0?q=" + Uri.encode(address))
        val intent = Intent(Intent.ACTION_VIEW, uri)
        context.startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(context, "Destino: $address", Toast.LENGTH_SHORT).show()
    }
}
