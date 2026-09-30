package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.MandadoOrder
import com.example.ui.theme.*

@Composable
fun LiveTrackingSheet(
    order: MandadoOrder,
    driverLat: Double,
    driverLng: Double,
    driverSpeedKmh: Int,
    driverBearing: Float,
    distanceRemainingKm: Double,
    etaMinutes: Int,
    isDriverView: Boolean,
    onDismiss: () -> Unit,
    onOpenWebTracking: (() -> Unit)? = null,
    onOpenRecipientPinValidation: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val isNear = distanceRemainingKm <= 0.5

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color.White,
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.88f)
                .clip(RoundedCornerShape(20.dp))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Top Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "Rastreo en Vivo",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = AcaAzulDark
                            )
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (isNear) Color(0xFFFEE2E2) else AcaTurquesaContainer
                            ) {
                                Text(
                                    text = if (isNear) "¡Repartidor Cerca!" else "${order.estado}",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isNear) Color(0xFFDC2626) else AcaTurquesaDark,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = "Acapulco, Gro. • Folio ${order.folio}",
                            fontSize = 11.5.sp,
                            color = AcaGrisSubtle
                        )
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Outlined.Close, contentDescription = "Cerrar", tint = AcaGrisSubtle)
                    }
                }

                // Banner si el repartidor está cerca (< 500 metros)
                if (isNear) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFFFEF2F2),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFCA5A5)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.NearMe, contentDescription = null, tint = Color(0xFFDC2626))
                            Column {
                                Text(
                                    text = "¡El repartidor está a menos de 500 metros!",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = Color(0xFF991B1B)
                                )
                                Text(
                                    text = "Tiempo estimado de llegada: ~$etaMinutes min. Ten listo tu acceso o pago.",
                                    fontSize = 11.sp,
                                    color = Color(0xFFB91C1C)
                                )
                            }
                        }
                    }
                }

                // Mapa interactivo de Acapulco
                AcapulcoInteractiveMap(
                    driverLat = driverLat,
                    driverLng = driverLng,
                    driverSpeedKmh = driverSpeedKmh,
                    driverBearing = driverBearing,
                    pickupPoint = MapLocationPoint(
                        lat = order.latRecogida,
                        lng = order.lngRecogida,
                        label = order.zonaRecogida,
                        sublabel = order.puntoRecogida
                    ),
                    deliveryPoint = MapLocationPoint(
                        lat = order.latEntrega,
                        lng = order.lngEntrega,
                        label = order.zonaEntrega,
                        sublabel = order.puntoEntrega
                    ),
                    targetType = if (order.estado == "ACEPTADO") "PICKUP" else "DELIVERY",
                    distanceRemainingKm = distanceRemainingKm,
                    etaMinutes = etaMinutes,
                    driverName = order.repartidorNombre ?: "Carlos Mendoza",
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    onOpenExternalMap = {
                        val lat = if (order.estado == "ACEPTADO") order.latRecogida else order.latEntrega
                        val lng = if (order.estado == "ACEPTADO") order.lngRecogida else order.lngEntrega
                        val label = if (order.estado == "ACEPTADO") order.puntoRecogida else order.puntoEntrega
                        openGoogleMaps(context, lat, lng, label)
                    }
                )

                // Referencias de entrega (si existen)
                if (order.getReferencias().isNotBlank()) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = AcaSurfaceVariant,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Default.Home, contentDescription = null, tint = AcaAzulConfianza, modifier = Modifier.size(16.dp))
                            Text(
                                text = "Ref: ${order.getReferencias()}",
                                fontSize = 11.sp,
                                color = AcaAzulDark,
                                maxLines = 2,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                // Info rápida de contacto
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(if (isDriverView) AcaAzulContainer else AcaTurquesaContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isDriverView) Icons.Default.Person else Icons.Default.ElectricScooter,
                                contentDescription = null,
                                tint = if (isDriverView) AcaAzulConfianza else AcaTurquesaDark,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Column {
                            Text(
                                text = if (isDriverView) "Recibe: ${order.getNombreDestinatario()}" else (order.repartidorNombre ?: "Carlos Mendoza"),
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.5.sp,
                                color = AcaAzulDark,
                                maxLines = 1
                            )
                            Text(
                                text = if (isDriverView) "Tel: ${order.getTelefonoDestinatario()}" else "${order.repartidorVehiculo ?: "Moto"} • Placas ${order.repartidorPlacas ?: "GRO"}",
                                fontSize = 11.sp,
                                color = AcaGrisSubtle
                            )
                        }
                    }

                    Button(
                        onClick = {
                            val phone = if (isDriverView) order.getTelefonoDestinatario() else (order.repartidorTelefono ?: "7441234567")
                            openDialer(context, phone)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AcaAzulConfianza),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Llamar", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // Acciones rápidas para el repartidor avisar al destinatario o PIN para cliente
                if (isDriverView) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Button(
                            onClick = {
                                openWhatsApp(
                                    context = context,
                                    phone = order.getTelefonoDestinatario(),
                                    message = "¡Hola ${order.getNombreDestinatario()}! Soy tu repartidor de AcaMandados (${order.folio}). 🛵 Ya me encuentro afuera de tu domicilio (${order.puntoEntrega}) para entregarte tu mandado."
                                )
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("¡Ya estoy afuera!", fontSize = 10.5.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }

                        OutlinedButton(
                            onClick = {
                                openWhatsApp(
                                    context = context,
                                    phone = order.getTelefonoDestinatario(),
                                    message = "Hola ${order.getNombreDestinatario()}, soy tu repartidor de AcaMandados (${order.folio}). Ya estoy en tu colonia/zona (${order.zonaEntrega}), ¿me podrías dar más referencias de tu fachada o portón para ubicarte rápido? Gracias."
                                )
                            },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.HelpOutline, contentDescription = null, tint = AcaAzulConfianza, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Pedir referencias", fontSize = 10.5.sp, fontWeight = FontWeight.Bold, color = AcaAzulConfianza)
                        }
                    }
                } else {
                    // Vista Cliente en Rastreo en Vivo: Mostrar PIN y botón de Compartir Enlace Web
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = AcaAzulDark,
                            modifier = Modifier.weight(1f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(Icons.Default.Key, contentDescription = null, tint = AcaTurquesaLight, modifier = Modifier.size(16.dp))
                                    Text("PIN:", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                                Text(
                                    text = order.pinSeguridad,
                                    color = AcaTurquesaLight,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 2.sp
                                )
                            }
                        }

                        if (onOpenWebTracking != null) {
                            OutlinedButton(
                                onClick = onOpenWebTracking,
                                shape = RoundedCornerShape(10.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, AcaAzulConfianza),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp)
                            ) {
                                Icon(Icons.Default.Share, contentDescription = null, tint = AcaAzulConfianza, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Compartir Web", fontSize = 11.sp, color = AcaAzulConfianza, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    if (onOpenRecipientPinValidation != null && order.estado == "EN_CAMINO") {
                        Button(
                            onClick = onOpenRecipientPinValidation,
                            colors = ButtonDefaults.buttonColors(containerColor = AcaTurquesaDark),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("btn_tracking_validar_pin")
                        ) {
                            Icon(Icons.Default.VpnKey, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("📲 Ingresar PIN en este teléfono al recibir", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.White)
                        }
                    }
                }
            }
        }
    }
}

private fun openWhatsApp(context: Context, phone: String, message: String) {
    try {
        val cleanPhone = phone.replace("+", "").replace(" ", "").replace("-", "")
        val fullPhone = if (cleanPhone.length == 10) "521$cleanPhone" else cleanPhone
        val uri = Uri.parse("https://api.whatsapp.com/send?phone=$fullPhone&text=${Uri.encode(message)}")
        val intent = Intent(Intent.ACTION_VIEW, uri)
        context.startActivity(intent)
    } catch (e: Exception) {
        val smsIntent = Intent(Intent.ACTION_VIEW, Uri.parse("sms:$phone")).apply {
            putExtra("sms_body", message)
        }
        context.startActivity(smsIntent)
    }
}

private fun openGoogleMaps(context: Context, lat: Double, lng: Double, label: String) {
    try {
        val uri = Uri.parse("google.navigation:q=$lat,$lng")
        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
            setPackage("com.google.android.apps.maps")
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        val genericUri = Uri.parse("geo:$lat,$lng?q=$lat,$lng(${Uri.encode(label)})")
        context.startActivity(Intent(Intent.ACTION_VIEW, genericUri))
    }
}

private fun openDialer(context: Context, phone: String) {
    try {
        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone"))
        context.startActivity(intent)
    } catch (_: Exception) {
        Toast.makeText(context, "Teléfono: $phone", Toast.LENGTH_SHORT).show()
    }
}
