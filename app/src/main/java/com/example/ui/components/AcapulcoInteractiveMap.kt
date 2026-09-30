package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

data class MapLocationPoint(
    val lat: Double,
    val lng: Double,
    val label: String,
    val sublabel: String = ""
)

/**
 * Mapa interactivo de Acapulco con soporte para:
 * - Zoom in / Zoom out y desplazamiento (pan).
 * - Renderizado de la Bahía de Acapulco (Bahía de Santa Lucía), Costera y mar.
 * - Marcadores animados de Repartidor (con pulso de radar y velocidad), Origen y Destino.
 * - Línea de ruta con progreso en tiempo real.
 * - HUD con telemetría en vivo (distancia, ETA, velocidad).
 */
@Composable
fun AcapulcoInteractiveMap(
    driverLat: Double,
    driverLng: Double,
    driverSpeedKmh: Int,
    driverBearing: Float,
    pickupPoint: MapLocationPoint,
    deliveryPoint: MapLocationPoint,
    targetType: String, // "PICKUP" o "DELIVERY"
    distanceRemainingKm: Double,
    etaMinutes: Int,
    modifier: Modifier = Modifier,
    driverName: String = "Carlos Mendoza",
    onOpenExternalMap: (() -> Unit)? = null
) {
    val context = LocalContext.current

    // Límites de Acapulco para proyección en Canvas
    // Lat: 16.74 a 16.92 (~20km norte-sur)
    // Lng: -100.02 a -99.72 (~30km oeste-este)
    val minLat = 16.72
    val maxLat = 16.92
    val minLng = -100.00
    val maxLng = -99.72

    // Estado interactivo de Zoom y Pan
    var scale by remember { mutableStateOf(1.0f) }
    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }

    // Pulso animado para el radar del repartidor
    val infiniteTransition = rememberInfiniteTransition(label = "radarPulse")
    val pulseRadius by infiniteTransition.animateFloat(
        initialValue = 12f,
        targetValue = 38f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulseRadius"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 0.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulseAlpha"
    )

    // Dash phase para la ruta animada
    val dashPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 40f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "dashPhase"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFFE5E7EB))
            .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(16.dp))
    ) {
        // Lienzo del mapa con gestos táctiles de arrastre y zoom
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        scale = (scale * zoom).coerceIn(0.7f, 3.5f)
                        offsetX += pan.x
                        offsetY += pan.y
                    }
                }
        ) {
            val width = size.width
            val height = size.height

            fun project(lat: Double, lng: Double): Offset {
                // Invertimos latitud porque en pantalla Y crece hacia abajo
                val normX = ((lng - minLng) / (maxLng - minLng)).toFloat()
                val normY = (1f - ((lat - minLat) / (maxLat - minLat)).toFloat())

                val baseCenterX = width * 0.5f
                val baseCenterY = height * 0.5f

                val projX = normX * width
                val projY = normY * height

                // Aplicar transformación de escala y pan centrada
                val finalX = baseCenterX + (projX - baseCenterX) * scale + offsetX
                val finalY = baseCenterY + (projY - baseCenterY) * scale + offsetY
                return Offset(finalX, finalY)
            }

            // 1. Fondo del mar Pacífico y Bahía de Acapulco
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF0F4C81), Color(0xFF0B3A64), Color(0xFF072652))
                )
            )

            // 2. Continente y Costas de Acapulco
            drawAcapulcoCoastline(width, height, scale, offsetX, offsetY)

            // 3. Red de avenidas principales (Costera Miguel Alemán, Escénica, Blvd. de las Naciones, Cuauhtémoc)
            drawMainRoads(::project, scale)

            // 4. Proyección de puntos
            val pDriver = project(driverLat, driverLng)
            val pPickup = project(pickupPoint.lat, pickupPoint.lng)
            val pDelivery = project(deliveryPoint.lat, deliveryPoint.lng)

            // 5. Dibujar línea de ruta animada
            val routePath = Path().apply {
                moveTo(pDriver.x, pDriver.y)
                if (targetType == "PICKUP") {
                    // Repartidor -> Origen -> Destino
                    lineTo(pPickup.x, pPickup.y)
                    lineTo(pDelivery.x, pDelivery.y)
                } else {
                    // Repartidor -> Destino
                    lineTo(pDelivery.x, pDelivery.y)
                }
            }

            // Línea base de ruta
            drawPath(
                path = routePath,
                color = Color(0x6600BFA5),
                style = Stroke(width = 8f * scale)
            )

            // Línea con animación dashed
            drawPath(
                path = routePath,
                color = AcaTurquesaDark,
                style = Stroke(
                    width = 4f * scale,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(15f, 10f), dashPhase)
                )
            )

            // 6. Marcador de Origen (Recogida)
            drawPickupMarker(pPickup, pickupPoint.label, targetType == "PICKUP")

            // 7. Marcador de Destino (Entrega)
            drawDeliveryMarker(pDelivery, deliveryPoint.label, targetType == "DELIVERY")

            // 8. Marcador del Repartidor con Pulso de Radar
            drawCircle(
                color = AcaTurquesa.copy(alpha = pulseAlpha),
                radius = pulseRadius * scale,
                center = pDriver
            )
            drawCircle(
                color = Color.White,
                radius = 12f * scale,
                center = pDriver
            )
            drawCircle(
                color = AcaAzulConfianza,
                radius = 9f * scale,
                center = pDriver
            )

            // Flecha indicadora de dirección (bearing)
            val rad = Math.toRadians(driverBearing.toDouble())
            val arrowEnd = Offset(
                pDriver.x + (18f * scale * Math.sin(rad)).toFloat(),
                pDriver.y - (18f * scale * Math.cos(rad)).toFloat()
            )
            drawLine(
                color = AcaTurquesa,
                start = pDriver,
                end = arrowEnd,
                strokeWidth = 4f * scale
            )
        }

        // Overlay Superior: Indicador en tiempo real
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color.Black.copy(alpha = 0.72f),
            modifier = Modifier
                .padding(10.dp)
                .align(Alignment.TopStart)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(AcaGreenSuccess)
                )
                Text(
                    text = "GPS en vivo • Acapulco (Actualizado)",
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Controles de Mapa Flotantes (Zoom +, Zoom -, Reset, Google Maps)
        Column(
            modifier = Modifier
                .padding(10.dp)
                .align(Alignment.TopEnd),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            SmallMapControlButton(icon = Icons.Default.Add, contentDescription = "Zoom in") {
                scale = (scale * 1.3f).coerceAtMost(3.5f)
            }
            SmallMapControlButton(icon = Icons.Default.Remove, contentDescription = "Zoom out") {
                scale = (scale / 1.3f).coerceAtLeast(0.7f)
            }
            SmallMapControlButton(icon = Icons.Default.MyLocation, contentDescription = "Centrar Repartidor") {
                scale = 1.3f
                offsetX = 0f
                offsetY = 0f
            }
            SmallMapControlButton(
                icon = Icons.Default.OpenInNew,
                contentDescription = "Abrir mapa externo",
                tint = AcaTurquesaDark
            ) {
                onOpenExternalMap?.invoke() ?: run {
                    val dest = if (targetType == "PICKUP") pickupPoint else deliveryPoint
                    openExternalMap(context, dest.lat, dest.lng, dest.label)
                }
            }
        }

        // HUD Inferior con Telemetría en Vivo
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color.White.copy(alpha = 0.96f),
            shadowElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
                .align(Alignment.BottomCenter)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
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
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(AcaTurquesaContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.ElectricScooter,
                                contentDescription = null,
                                tint = AcaTurquesaDark,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Column {
                            Text(
                                text = driverName,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.5.sp,
                                color = AcaAzulDark
                            )
                            Text(
                                text = if (targetType == "PICKUP") "Hacia: ${pickupPoint.label}" else "Hacia: ${deliveryPoint.label}",
                                fontSize = 10.5.sp,
                                color = AcaGrisSubtle
                            )
                        }
                    }

                    // Badge de Velocidad en Vivo
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = AcaAzulContainer
                    ) {
                        Text(
                            text = "$driverSpeedKmh km/h",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = AcaAzulConfianza,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                        )
                    }
                }

                Divider(color = Color(0xFFE2E8F0), thickness = 0.8.dp)

                // Métricas: Distancia y Tiempo estimado (ETA)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    MetricItem(
                        icon = Icons.Default.Straighten,
                        label = "Distancia restante",
                        value = if (distanceRemainingKm < 1.0) "${(distanceRemainingKm * 1000).toInt()} m" else String.format("%.1f km", distanceRemainingKm)
                    )
                    MetricItem(
                        icon = Icons.Default.Schedule,
                        label = "Llegada estimada",
                        value = "aprox. $etaMinutes min"
                    )
                }
            }
        }
    }
}

@Composable
private fun MetricItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(icon, contentDescription = null, tint = AcaTurquesaDark, modifier = Modifier.size(15.dp))
        Column {
            Text(label, fontSize = 9.5.sp, color = AcaGrisSubtle)
            Text(value, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AcaGrisEquilibrio)
        }
    }
}

@Composable
private fun SmallMapControlButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    tint: Color = AcaAzulConfianza,
    onClick: () -> Unit
) {
    FilledTonalIconButton(
        onClick = onClick,
        shape = CircleShape,
        colors = IconButtonDefaults.filledTonalIconButtonColors(
            containerColor = Color.White.copy(alpha = 0.95f),
            contentColor = tint
        ),
        modifier = Modifier.size(32.dp)
    ) {
        Icon(icon, contentDescription = contentDescription, modifier = Modifier.size(16.dp))
    }
}

private fun DrawScope.drawAcapulcoCoastline(
    width: Float,
    height: Float,
    scale: Float,
    offsetX: Float,
    offsetY: Float
) {
    // Dibujamos la silueta de tierra de Acapulco (zona continental al norte/este y bahía semicircular)
    val landPath = Path().apply {
        moveTo(0f, 0f)
        lineTo(width, 0f)
        lineTo(width, height * 0.78f)
        // Curva de Barra Vieja / Diamante
        quadraticBezierTo(width * 0.85f, height * 0.72f, width * 0.72f, height * 0.65f)
        // Puerto Marqués
        quadraticBezierTo(width * 0.62f, height * 0.60f, width * 0.58f, height * 0.50f)
        // Bahía de Santa Lucía (Costera - semicírculo icónico de Acapulco)
        cubicTo(
            width * 0.52f, height * 0.38f,
            width * 0.38f, height * 0.38f,
            width * 0.30f, height * 0.52f
        )
        // Península de Las Playas / Caleta y Caletilla
        quadraticBezierTo(width * 0.24f, height * 0.62f, width * 0.18f, height * 0.56f)
        // La Quebrada y Pie de la Cuesta hacia el noroeste
        quadraticBezierTo(width * 0.12f, height * 0.42f, 0f, height * 0.32f)
        close()
    }

    // Dibujar tierra firme con tono cálido y agradable
    drawPath(
        path = landPath,
        color = Color(0xFFF1F5F9)
    )

    // Línea de orilla costera
    drawPath(
        path = landPath,
        color = Color(0xFFCBD5E1),
        style = Stroke(width = 3f * scale)
    )

    // Arena de playa a lo largo de la bahía
    drawPath(
        path = landPath,
        color = Color(0xFFFDE68A).copy(alpha = 0.5f),
        style = Stroke(width = 6f * scale)
    )
}

private fun DrawScope.drawMainRoads(
    project: (Double, Double) -> Offset,
    scale: Float
) {
    // Eje Costera Miguel Alemán
    val costeraPoints = listOf(
        project(16.8340, -99.9070), // Caleta
        project(16.8430, -99.9010), // Centro / Zócalo
        project(16.8531, -99.8820), // Papagayo
        project(16.8580, -99.8760), // Magallanes / Galerías
        project(16.8531, -99.8705), // Diana
        project(16.8480, -99.8510), // Costa Azul
        project(16.8320, -99.8400), // Base Naval
        project(16.8040, -99.8250), // Puerto Marqués
        project(16.7820, -99.7890)  // Diamante
    )

    val costeraPath = Path().apply {
        costeraPoints.forEachIndexed { index, pt ->
            if (index == 0) moveTo(pt.x, pt.y) else lineTo(pt.x, pt.y)
        }
    }

    drawPath(
        path = costeraPath,
        color = Color(0xFFE2E8F0),
        style = Stroke(width = 6f * scale)
    )
    drawPath(
        path = costeraPath,
        color = Color(0xFF94A3B8),
        style = Stroke(width = 2.5f * scale)
    )
}

private fun DrawScope.drawPickupMarker(pt: Offset, label: String, isActiveTarget: Boolean) {
    // Halo
    drawCircle(
        color = Color(0xFFF59E0B).copy(alpha = 0.25f),
        radius = 16f,
        center = pt
    )
    // Círculo exterior
    drawCircle(
        color = Color.White,
        radius = 9f,
        center = pt
    )
    // Círculo interior
    drawCircle(
        color = Color(0xFFF59E0B),
        radius = 6.5f,
        center = pt
    )
}

private fun DrawScope.drawDeliveryMarker(pt: Offset, label: String, isActiveTarget: Boolean) {
    // Halo
    drawCircle(
        color = AcaAzulConfianza.copy(alpha = 0.25f),
        radius = 16f,
        center = pt
    )
    // Círculo exterior
    drawCircle(
        color = Color.White,
        radius = 9f,
        center = pt
    )
    // Círculo interior
    drawCircle(
        color = AcaAzulConfianza,
        radius = 6.5f,
        center = pt
    )
}

private fun openExternalMap(context: Context, lat: Double, lng: Double, label: String) {
    try {
        val uri = Uri.parse("geo:$lat,$lng?q=$lat,$lng(${Uri.encode(label)})")
        val intent = Intent(Intent.ACTION_VIEW, uri)
        context.startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(context, "Ubicación: $label ($lat, $lng)", Toast.LENGTH_SHORT).show()
    }
}
