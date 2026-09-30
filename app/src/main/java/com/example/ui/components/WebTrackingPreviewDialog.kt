package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.AcapulcoLocations
import com.example.data.model.MandadoOrder
import com.example.ui.theme.*

/**
 * Diálogo interactivo que simula y visualiza la página web de rastreo en tiempo real
 * generada mediante una URL única para destinatarios sin la aplicación instalada.
 */
@Composable
fun WebTrackingPreviewDialog(
    order: MandadoOrder,
    driverLat: Double? = null,
    driverLng: Double? = null,
    driverSpeedKmh: Int? = null,
    driverBearing: Float? = null,
    distanceRemainingKm: Double? = null,
    etaMinutes: Int? = null,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val webUrl = order.getWebTrackingUrl()
    var isRefreshing by remember { mutableStateOf(false) }
    var showUrlCopiedBanner by remember { mutableStateOf(false) }

    // Coordenadas y telemetría calculadas
    val currentDriverLat = driverLat ?: if (order.estado == "ACEPTADO") order.latRecogida else (order.latRecogida + order.latEntrega) / 2
    val currentDriverLng = driverLng ?: if (order.estado == "ACEPTADO") order.lngRecogida else (order.lngRecogida + order.lngEntrega) / 2
    val currentSpeed = driverSpeedKmh ?: if (order.estado == "EN_CAMINO") 38 else 0
    val currentBearing = driverBearing ?: 115f
    val currentDistance = distanceRemainingKm ?: 1.8
    val currentEta = etaMinutes ?: 7

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.92f)
                .testTag("web_tracking_preview_dialog")
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Barra simulada de Navegador Web Móvil (Chrome / Safari)
                Surface(
                    color = Color(0xFF0F172A),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Cabecera superior con botones tipo pestaña de navegador
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFEF4444))
                                )
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFF59E0B))
                                )
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF10B981))
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Navegador Web Destinatario • Sin App",
                                    color = Color.White.copy(alpha = 0.85f),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            IconButton(
                                onClick = onDismiss,
                                modifier = Modifier
                                    .size(26.dp)
                                    .testTag("btn_close_web_tracking")
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = Color.White)
                            }
                        }

                        // Barra de Dirección URL con Candado SSL, Copiar y Compartir
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFF1E293B),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = "SSL Seguro",
                                    tint = Color(0xFF10B981),
                                    modifier = Modifier.size(14.dp)
                                )

                                Text(
                                    text = webUrl,
                                    color = Color.White,
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Medium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("text_web_tracking_url")
                                )

                                // Botón Copiar URL al portapapeles
                                IconButton(
                                    onClick = {
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                                        val clip = ClipData.newPlainText("URL de Rastreo", webUrl)
                                        clipboard?.setPrimaryClip(clip)
                                        showUrlCopiedBanner = true
                                        Toast.makeText(context, "✅ Enlace web único copiado", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier
                                        .size(24.dp)
                                        .testTag("btn_copy_web_url")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ContentCopy,
                                        contentDescription = "Copiar URL",
                                        tint = AcaTurquesaLight,
                                        modifier = Modifier.size(15.dp)
                                    )
                                }

                                // Botón Compartir por Android Sharesheet
                                IconButton(
                                    onClick = {
                                        val sendIntent = Intent().apply {
                                            action = Intent.ACTION_SEND
                                            putExtra(Intent.EXTRA_TEXT, order.getShareableTrackingMessage())
                                            type = "text/plain"
                                        }
                                        val shareIntent = Intent.createChooser(sendIntent, "Compartir enlace de rastreo en vivo")
                                        context.startActivity(shareIntent)
                                    },
                                    modifier = Modifier
                                        .size(24.dp)
                                        .testTag("btn_share_native_web_url")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Share,
                                        contentDescription = "Compartir",
                                        tint = Color.White.copy(alpha = 0.8f),
                                        modifier = Modifier.size(15.dp)
                                    )
                                }

                                // Botón Abrir en Navegador Externo (Chrome, Safari, etc.)
                                IconButton(
                                    onClick = {
                                        try {
                                            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(webUrl))
                                            context.startActivity(browserIntent)
                                        } catch (e: Exception) {
                                            Toast.makeText(context, "URL: $webUrl", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    modifier = Modifier
                                        .size(24.dp)
                                        .testTag("btn_open_external_browser")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.OpenInBrowser,
                                        contentDescription = "Abrir en navegador",
                                        tint = Color.White.copy(alpha = 0.8f),
                                        modifier = Modifier.size(15.dp)
                                    )
                                }

                                // Botón Recargar / Refrescar estado
                                IconButton(
                                    onClick = {
                                        isRefreshing = true
                                        Toast.makeText(context, "🔄 Actualizando telemetría web...", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier
                                        .size(24.dp)
                                        .testTag("btn_refresh_web_tracking")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = "Refrescar",
                                        tint = Color.White.copy(alpha = 0.8f),
                                        modifier = Modifier.size(15.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Banner de URL Copiada
                AnimatedVisibility(visible = showUrlCopiedBanner) {
                    Surface(
                        color = Color(0xFFDCFCE7),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showUrlCopiedBanner = false }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "✓ Enlace único copiado: compártelo con el destinatario por WhatsApp o SMS.",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF166534)
                            )
                            Icon(Icons.Default.Close, contentDescription = null, tint = Color(0xFF166534), modifier = Modifier.size(14.dp))
                        }
                    }
                }

                // Contenido de la Página Web de Rastreo
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Header de la Página Web
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(AcaTurquesaDark),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.TwoWheeler, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
                            }
                            Column {
                                Text("AcaMandados Web", fontWeight = FontWeight.Black, fontSize = 17.sp, color = AcaAzulDark)
                                Text("Rastreo GPS en tiempo real • Sin descargar app", fontSize = 11.sp, color = AcaGrisSubtle)
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = Color(0xFFDCFCE7)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(7.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF16A34A))
                                )
                                Text(
                                    text = "EN VIVO",
                                    color = Color(0xFF166534),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }
                    }

                    // Card Prominente: PIN de Seguridad para la Entrega
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = AcaAzulDark),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(Icons.Default.VpnKey, contentDescription = null, tint = AcaTurquesaLight, modifier = Modifier.size(18.dp))
                                Text(
                                    text = "TU PIN DE SEGURIDAD PARA RECIBIR",
                                    color = Color.White.copy(alpha = 0.9f),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.5.sp
                                )
                            }
                            Text(
                                text = order.pinSeguridad,
                                color = AcaTurquesaLight,
                                fontSize = 38.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 8.sp,
                                modifier = Modifier.testTag("web_pin_display")
                            )
                            Text(
                                text = "Díctale o teclea este código de 4 dígitos cuando el repartidor llegue a tu domicilio para liberar tu entrega.",
                                color = Color.White.copy(alpha = 0.85f),
                                fontSize = 11.sp,
                                textAlign = TextAlign.Center,
                                lineHeight = 15.sp
                            )
                        }
                    }

                    // Card de Mapa en Vivo de Acapulco
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(Icons.Default.MyLocation, contentDescription = null, tint = AcaTurquesaDark, modifier = Modifier.size(16.dp))
                                    Text(
                                        text = "Ubicación en Tiempo Real",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = AcaAzulDark
                                    )
                                }
                                Text(
                                    text = if (order.estado == "EN_CAMINO") "Velocidad: $currentSpeed km/h" else "Acapulco, Gro.",
                                    fontSize = 11.sp,
                                    color = AcaTurquesaDark,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            // Mapa interactivo de Acapulco
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(220.dp)
                            ) {
                                AcapulcoInteractiveMap(
                                    driverLat = currentDriverLat,
                                    driverLng = currentDriverLng,
                                    driverSpeedKmh = currentSpeed,
                                    driverBearing = currentBearing,
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
                                    distanceRemainingKm = currentDistance,
                                    etaMinutes = currentEta,
                                    driverName = order.repartidorNombre ?: "Carlos Mendoza",
                                    modifier = Modifier.fillMaxSize(),
                                    onOpenExternalMap = {
                                        val lat = if (order.estado == "ACEPTADO") order.latRecogida else order.latEntrega
                                        val lng = if (order.estado == "ACEPTADO") order.lngRecogida else order.lngEntrega
                                        val label = if (order.estado == "ACEPTADO") order.puntoRecogida else order.puntoEntrega
                                        try {
                                            val uri = Uri.parse("geo:$lat,$lng?q=$lat,$lng(${Uri.encode(label)})")
                                            context.startActivity(Intent(Intent.ACTION_VIEW, uri))
                                        } catch (_: Exception) {}
                                    }
                                )
                            }
                        }
                    }

                    // Estado del Mandado & Stepper
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            val estadoTitulo = when (order.estado) {
                                "SOLICITADO" -> "🔍 Buscando repartidor en ${order.zonaRecogida}..."
                                "ACEPTADO" -> "🛵 Repartidor asignado, en camino al origen"
                                "EN_CAMINO" -> "📦 ¡En camino a tu entrega! (~$currentEta min)"
                                "ENTREGADO" -> "✅ Mandado entregado exitosamente"
                                else -> order.estado
                            }

                            Text(
                                text = estadoTitulo,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.5.sp,
                                color = AcaAzulDark
                            )

                            OrderProgressStepper(order.estado)
                        }
                    }

                    // Datos del Repartidor Asignado (con botones de contacto)
                    if (order.repartidorNombre != null && order.estado != "SOLICITADO") {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(42.dp)
                                            .clip(CircleShape)
                                            .background(AcaTurquesaDark),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.ElectricScooter, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
                                    }

                                    Column {
                                        Text(
                                            text = order.repartidorNombre,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.5.sp,
                                            color = AcaAzulDark
                                        )
                                        Text(
                                            text = "${order.repartidorVehiculo ?: "Moto"} • ${order.repartidorPlacas ?: "Acapulco"} • ⭐ 4.96",
                                            fontSize = 11.sp,
                                            color = AcaGrisSubtle
                                        )
                                    }
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    FilledTonalIconButton(
                                        onClick = {
                                            val phone = order.repartidorTelefono ?: "7441234567"
                                            val clean = phone.replace("[^0-9]".toRegex(), "")
                                            val url = "https://api.whatsapp.com/send?phone=52$clean&text=Hola%20${Uri.encode(order.repartidorNombre)},%20te%20escribo%20sobre%20mi%20mandado%20${order.folio}"
                                            try {
                                                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                                            } catch (_: Exception) {}
                                        },
                                        colors = IconButtonDefaults.filledTonalIconButtonColors(containerColor = Color(0xFF25D366), contentColor = Color.White),
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(Icons.Default.Chat, contentDescription = "WhatsApp", modifier = Modifier.size(18.dp))
                                    }

                                    FilledTonalIconButton(
                                        onClick = {
                                            val phone = order.repartidorTelefono ?: "7441234567"
                                            try {
                                                context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone")))
                                            } catch (_: Exception) {}
                                        },
                                        colors = IconButtonDefaults.filledTonalIconButtonColors(containerColor = AcaAzulConfianza, contentColor = Color.White),
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(Icons.Default.Phone, contentDescription = "Llamar", modifier = Modifier.size(18.dp))
                                    }
                                }
                            }
                        }
                    }

                    // Detalles de Entrega y Referencias
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text("Detalles de Entrega en Acapulco", fontWeight = FontWeight.Bold, fontSize = 13.5.sp, color = AcaAzulDark)

                            Row(
                                verticalAlignment = Alignment.Top,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Default.LocationOn, contentDescription = null, tint = AcaTurquesaDark, modifier = Modifier.size(18.dp))
                                Column {
                                    Text("Entrega a: ${order.getNombreDestinatario()}", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    Text(order.puntoEntrega, fontSize = 11.5.sp, color = AcaGrisEquilibrio)
                                    if (order.referenciasDomicilio.isNotBlank()) {
                                        Text("Ref: ${order.referenciasDomicilio}", fontSize = 11.sp, color = AcaTurquesaDark, fontWeight = FontWeight.Medium)
                                    }
                                }
                            }

                            HorizontalDivider(color = Color(0xFFF1F5F9))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Forma de Pago", fontSize = 10.5.sp, color = AcaGrisSubtle)
                                    Text(
                                        text = if (order.metodoPago == "TARJETA") "Tarjeta en App (Pagado ✅)" else "Efectivo al recibir 💵",
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (order.metodoPago == "TARJETA") AcaTurquesaDark else Color(0xFFB45309)
                                    )
                                }

                                if (order.esCobroContraEntrega) {
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text("Cobro por mercancía (COD)", fontSize = 10.5.sp, color = AcaGrisSubtle)
                                        Text(
                                            text = "$${String.format("%.2f", order.montoProductoCobrar)} MXN",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Black,
                                            color = Color(0xFFB45309)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Botones de Compartir Enlace Web
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
                                } catch (e: Exception) {
                                    Toast.makeText(context, "URL: $webUrl", Toast.LENGTH_LONG).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("btn_share_whatsapp_web_tracking")
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Enviar Enlace Web por WhatsApp a ${order.getNombreDestinatario()}",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.5.sp
                            )
                        }

                        OutlinedButton(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                                val clip = ClipData.newPlainText("URL de Rastreo", webUrl)
                                clipboard?.setPrimaryClip(clip)
                                Toast.makeText(context, "✅ Enlace web único copiado al portapapeles", Toast.LENGTH_SHORT).show()
                            },
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, AcaAzulConfianza),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("btn_copy_link_bottom")
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, tint = AcaAzulConfianza, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Copiar Enlace Web Único",
                                color = AcaAzulConfianza,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
