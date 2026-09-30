package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.MandadoOrder
import com.example.data.model.TipoMandado
import com.example.ui.theme.*

@Composable
fun MandadoCard(
    order: MandadoOrder,
    isDriverView: Boolean = false,
    onTakeOrder: (() -> Unit)? = null,
    onAdvanceStatus: ((String) -> Unit)? = null,
    onCancel: (() -> Unit)? = null,
    onOpenTracking: ((MandadoOrder) -> Unit)? = null,
    onOpenRating: ((MandadoOrder, Boolean) -> Unit)? = null,
    onOpenPinValidation: ((MandadoOrder) -> Unit)? = null,
    onOpenRecipientPinValidation: ((MandadoOrder) -> Unit)? = null,
    onOpenProofOfCondition: ((MandadoOrder) -> Unit)? = null,
    onOpenWebTracking: ((MandadoOrder) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val tipoEnum = TipoMandado.values().find { it.id == order.tipo } ?: TipoMandado.EXPRESS

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(16.dp))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header: Folio, Tipo, Estado
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
                            .background(AcaAzulContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = getCategoryIcon(order.tipo),
                            contentDescription = order.tipo,
                            tint = AcaAzulConfianza,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Column {
                        Text(
                            text = order.folio,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = AcaAzulDark
                        )
                        Text(
                            text = tipoEnum.label,
                            fontSize = 11.5.sp,
                            color = AcaGrisSubtle
                        )
                    }
                }

                StatusBadge(status = order.estado)
            }

            // Botón Destacado en la Parte Superior para Aceptar el Servicio Inmediatamente
            if (isDriverView && order.estado == "SOLICITADO") {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFECFDF5),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFF10B981)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
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
                                Icon(
                                    imageVector = Icons.Default.ElectricBolt,
                                    contentDescription = null,
                                    tint = Color(0xFF059669),
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = "¡Mandado disponible para tomar!",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = Color(0xFF065F46)
                                )
                            }
                            Text(
                                text = "$${String.format("%.2f", order.tarifaEnvio)} MXN",
                                fontWeight = FontWeight.Black,
                                fontSize = 18.sp,
                                color = Color(0xFF059669)
                            )
                        }

                        Button(
                            onClick = { onTakeOrder?.invoke() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("btn_aceptar_servicio_top_${order.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "ACEPTAR ESTE SERVICIO AHORA",
                                fontSize = 14.5.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )
                        }
                    }
                }
            }

            // Badges de Carga Roadie, Geofencing Recargo y COD
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = AcaSurfaceVariant
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(Icons.Default.Inventory2, contentDescription = null, tint = AcaAzulDark, modifier = Modifier.size(13.dp))
                        Text(
                            text = "Tamaño: ${order.tamanoPaquete}",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = AcaAzulDark
                        )
                    }
                }

                if (order.recargoZonaLejana > 0) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = AcaAmbarAlerta.copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = "⚡ Cruce Lejano: +$${order.recargoZonaLejana.toInt()}",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF92400E),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        )
                    }
                }

                if (order.esCobroContraEntrega) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFFFEF3C7)
                    ) {
                        Text(
                            text = "🛍️ COD: $${order.montoProductoCobrar.toInt()} MXN",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF92400E),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        )
                    }
                }
            }

            // Título y descripción
            Text(
                text = order.titulo,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                color = AcaGrisEquilibrio,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            if (order.descripcion.isNotBlank()) {
                Text(
                    text = order.descripcion,
                    fontSize = 12.5.sp,
                    color = AcaGrisSubtle,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Ruta Origen -> Destino en Acapulco
            RouteVisualizer(
                origen = order.puntoRecogida,
                zonaOrigen = order.zonaRecogida,
                destino = order.puntoEntrega,
                zonaDestino = order.zonaEntrega
            )

            // Timeline de progreso (para pedidos activos)
            if (order.estado != "CANCELADO") {
                OrderProgressStepper(order.estado)
            }

            // Banner del PIN de Seguridad para el Cliente (Estilo Uber Connect)
            if (!isDriverView && order.estado != "CANCELADO") {
                if (order.estado == "ENTREGADO") {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFFD1FAE5),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.Verified, contentDescription = null, tint = Color(0xFF065F46), modifier = Modifier.size(18.dp))
                            Text(
                                text = "Entrega verificada exitosamente con PIN de 4 dígitos [${order.pinSeguridad}]",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF065F46)
                            )
                        }
                    }
                } else {
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = AcaAzulDark),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
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
                                    Icon(Icons.Default.Key, contentDescription = null, tint = AcaTurquesaLight, modifier = Modifier.size(16.dp))
                                    Text(
                                        text = "PIN para recibir:",
                                        color = Color.White,
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Text(
                                    text = order.pinSeguridad,
                                    color = AcaTurquesaLight,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 4.sp
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Díctaselo al chofer al llegar.",
                                    color = Color.White.copy(alpha = 0.7f),
                                    fontSize = 10.5.sp
                                )

                                TextButton(
                                    onClick = { onOpenWebTracking?.invoke(order) },
                                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Icon(Icons.Default.Public, contentDescription = null, tint = AcaTurquesaLight, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Rastreo Web (Sin App)",
                                        color = AcaTurquesaLight,
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            if (order.estado == "EN_CAMINO" && onOpenRecipientPinValidation != null) {
                                Button(
                                    onClick = { onOpenRecipientPinValidation(order) },
                                    colors = ButtonDefaults.buttonColors(containerColor = AcaTurquesaDark),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("btn_validar_pin_receptor")
                                ) {
                                    Icon(Icons.Default.VpnKey, contentDescription = null, tint = Color.White, modifier = Modifier.size(15.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("📲 Ingresar PIN en este teléfono", fontWeight = FontWeight.Bold, fontSize = 11.5.sp, color = Color.White)
                                }
                            }
                        }
                    }
                }
            }

            // Si el repartidor tomó foto de recolección en origen, mostrar aviso
            if (order.fotoRecoleccionUri != null) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = AcaTurquesaContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Default.PhotoCamera, contentDescription = null, tint = AcaTurquesaDark, modifier = Modifier.size(16.dp))
                        Text(
                            text = "📸 Foto de condición capturada en origen (Roadie style)",
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = AcaTurquesaDark
                        )
                    }
                }
            }

            // Desglose de Pago & Ganancia
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = AcaSurfaceVariant,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (isDriverView) "Tu Ganancia (100%)" else "Tarifa de Envío",
                            fontSize = 11.sp,
                            color = AcaGrisSubtle,
                            fontWeight = FontWeight.Medium
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "$${String.format("%.2f", order.tarifaEnvio)} MXN",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isDriverView) AcaTurquesaDark else AcaAzulConfianza
                            )
                            if (order.costoProductosEstimado > 0) {
                                Text(
                                    text = " (+$${String.format("%.2f", order.costoProductosEstimado)} productos)",
                                    fontSize = 11.sp,
                                    color = AcaGrisSubtle
                                )
                            }
                        }
                        if (order.esCobroContraEntrega) {
                            Text(
                                text = "Cobro mercancía: $${String.format("%.2f", order.montoProductoCobrar)} MXN",
                                fontSize = 10.5.sp,
                                color = Color(0xFF92400E),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    PaymentMethodBadge(
                        metodo = order.metodoPago,
                        isDepositado = order.pagoDepositadoAlRepartidor
                    )
                }
            }

            // Info del Repartidor asignado (para vista Cliente)
            if (!isDriverView && order.repartidorNombre != null && order.estado != "SOLICITADO" && order.estado != "CANCELADO") {
                DriverInfoBanner(
                    order = order,
                    onContactWhatsApp = {
                        openWhatsApp(
                            context = context,
                            phone = order.repartidorTelefono ?: "7441234567",
                            message = "¡Hola ${order.repartidorNombre}! Te escribo sobre mi mandado de AcaMandados (${order.folio})."
                        )
                    },
                    onCall = {
                        openDialer(context, order.repartidorTelefono ?: "7441234567")
                    }
                )
            }

            // Botón de Rastreo en Tiempo Real en el Mapa (para pedidos activos)
            if ((order.estado == "ACEPTADO" || order.estado == "EN_CAMINO") && onOpenTracking != null) {
                Button(
                    onClick = { onOpenTracking(order) },
                    colors = ButtonDefaults.buttonColors(containerColor = AcaAzulConfianza),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Map,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isDriverView) "Ver Navegación y Mapa GPS en Vivo" else "Ver Repartidor en Vivo en el Mapa",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = Color.White
                    )
                }
            }

            // Botón para acceder al Rastreo Web (Sin App) y compartir el enlace único
            if (!isDriverView && onOpenWebTracking != null && order.estado != "CANCELADO") {
                OutlinedButton(
                    onClick = { onOpenWebTracking(order) },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = AcaTurquesaDark),
                    border = BorderStroke(1.dp, AcaTurquesaDark),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("btn_ver_rastreo_web_${order.folio}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Public,
                        contentDescription = null,
                        tint = AcaTurquesaDark,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "🔗 Enlace de Rastreo Web (Sin App)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = AcaTurquesaDark
                    )
                }
            }

            // Sistema de Calificación y Reseñas para pedidos ENTREGADOS
            if (order.estado == "ENTREGADO") {
                if (!isDriverView) {
                    // Vista Cliente
                    if (order.calificadoPorCliente) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFFFEF3C7),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFD97706), modifier = Modifier.size(18.dp))
                                Column {
                                    Text(
                                        text = "Calificaste el servicio con ${order.calificacionClienteEstrellas ?: 5} ★",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = Color(0xFF92400E)
                                    )
                                    if (!order.calificacionClienteComentario.isNullOrBlank()) {
                                        Text(
                                            text = "\"${order.calificacionClienteComentario}\"",
                                            fontSize = 11.sp,
                                            color = Color(0xFF78350F)
                                        )
                                    }
                                }
                            }
                        }
                    } else if (onOpenRating != null) {
                        OutlinedButton(
                            onClick = { onOpenRating(order, false) },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFD97706)),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF59E0B)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.StarRate, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Calificar a ${order.repartidorNombre ?: "tu repartidor"} ⭐",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.5.sp
                            )
                        }
                    }
                } else {
                    // Vista Repartidor
                    if (order.calificadoPorRepartidor) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = AcaTurquesaContainer,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Default.Star, contentDescription = null, tint = AcaTurquesaDark, modifier = Modifier.size(18.dp))
                                Text(
                                    text = "Calificaste a ${order.clienteNombre} con ${order.calificacionRepartidorEstrellas ?: 5} ★",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = AcaTurquesaDark
                                )
                            }
                        }
                    } else if (onOpenRating != null) {
                        OutlinedButton(
                            onClick = { onOpenRating(order, true) },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = AcaTurquesaDark),
                            border = androidx.compose.foundation.BorderStroke(1.dp, AcaTurquesa),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.StarRate, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Calificar al cliente (${order.clienteNombre}) ⭐",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.5.sp
                            )
                        }
                    }
                }
            }

            // Contactos y Referencias de Entrega (Quien manda y Quien recibe)
            ContactosMandadoSection(
                order = order,
                isDriverView = isDriverView,
                context = context
            )

            // Acciones según el rol y estado
            if (isDriverView) {
                DriverActionButtons(
                    order = order,
                    onTakeOrder = onTakeOrder,
                    onAdvanceStatus = onAdvanceStatus,
                    onOpenPinValidation = onOpenPinValidation,
                    onOpenProofOfCondition = onOpenProofOfCondition
                )
            } else if (order.estado == "SOLICITADO" && onCancel != null) {
                OutlinedButton(
                    onClick = onCancel,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFDC2626)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Cancelar Solicitud", fontSize = 12.5.sp)
                }
            }
        }
    }
}

@Composable
private fun RouteVisualizer(
    origen: String,
    zonaOrigen: String,
    destino: String,
    zonaDestino: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // Punto A: Recogida
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(AcaTurquesa)
            )
            Column {
                Text(
                    text = "Recoger: $zonaOrigen",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = AcaGrisEquilibrio
                )
                if (origen.isNotBlank()) {
                    Text(
                        text = origen,
                        fontSize = 11.sp,
                        color = AcaGrisSubtle,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        // Línea punteada
        Box(
            modifier = Modifier
                .padding(start = 4.dp)
                .width(2.dp)
                .height(12.dp)
                .background(Color(0xFFCBD5E1))
        )

        // Punto B: Entrega
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(AcaAzulConfianza)
            )
            Column {
                Text(
                    text = "Entregar: $zonaDestino",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = AcaGrisEquilibrio
                )
                if (destino.isNotBlank()) {
                    Text(
                        text = destino,
                        fontSize = 11.sp,
                        color = AcaGrisSubtle,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
fun OrderProgressStepper(estado: String) {
    val step = when (estado) {
        "SOLICITADO" -> 1
        "ACEPTADO" -> 2
        "EN_CAMINO" -> 3
        "ENTREGADO" -> 4
        else -> 0
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        StepDot(number = "1", label = "Buscando", active = step >= 1)
        StepLine(active = step >= 2, modifier = Modifier.weight(1f))
        StepDot(number = "2", label = "Asignado", active = step >= 2)
        StepLine(active = step >= 3, modifier = Modifier.weight(1f))
        StepDot(number = "3", label = "En camino", active = step >= 3)
        StepLine(active = step >= 4, modifier = Modifier.weight(1f))
        StepDot(number = "4", label = "Entregado", active = step >= 4)
    }
}

@Composable
private fun StepDot(number: String, label: String, active: Boolean) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(20.dp)
                .clip(CircleShape)
                .background(if (active) AcaTurquesaDark else Color(0xFFE2E8F0)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = number,
                color = if (active) Color.White else AcaGrisSubtle,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Text(
            text = label,
            fontSize = 9.sp,
            color = if (active) AcaAzulConfianza else AcaGrisSubtle,
            fontWeight = if (active) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
private fun StepLine(active: Boolean, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .padding(horizontal = 4.dp)
            .height(2.dp)
            .background(if (active) AcaTurquesa else Color(0xFFE2E8F0))
    )
}

@Composable
private fun StatusBadge(status: String) {
    val (bgColor, textColor, label) = when (status) {
        "SOLICITADO" -> Triple(Color(0xFFFEF3C7), Color(0xFF92400E), "Buscando Chofer")
        "ACEPTADO" -> Triple(AcaAzulContainer, AcaAzulConfianza, "Asignado")
        "EN_CAMINO" -> Triple(AcaTurquesaContainer, Color(0xFF004D40), "En Camino")
        "ENTREGADO" -> Triple(Color(0xFFD1FAE5), Color(0xFF065F46), "Entregado")
        "CANCELADO" -> Triple(Color(0xFFFEE2E2), Color(0xFF991B1B), "Cancelado")
        else -> Triple(AcaSurfaceVariant, AcaGrisEquilibrio, status)
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = textColor
        )
    }
}

@Composable
private fun PaymentMethodBadge(metodo: String, isDepositado: Boolean) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = if (metodo == "TARJETA") Icons.Default.CreditCard else Icons.Default.Payments,
            contentDescription = null,
            tint = if (metodo == "TARJETA") AcaAzulConfianza else AcaTurquesaDark,
            modifier = Modifier.size(16.dp)
        )
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = if (metodo == "TARJETA") "Tarjeta / SPEI" else "Efectivo",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = AcaGrisEquilibrio
            )
            if (metodo == "TARJETA") {
                Text(
                    text = if (isDepositado) "Depositado hoy" else "Depósito el mismo día",
                    fontSize = 9.sp,
                    color = if (isDepositado) AcaGreenSuccess else AcaTurquesaDark,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun DriverInfoBanner(
    order: MandadoOrder,
    onContactWhatsApp: () -> Unit,
    onCall: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = AcaTurquesaContainer.copy(alpha = 0.5f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(AcaTurquesaDark),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ElectricScooter,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Column {
                    Text(
                        text = order.repartidorNombre ?: "Repartidor AcaMandados",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.5.sp,
                        color = AcaAzulDark
                    )
                    Text(
                        text = "${order.repartidorVehiculo ?: "Moto"} • ${order.repartidorPlacas ?: ""}",
                        fontSize = 10.5.sp,
                        color = AcaGrisSubtle
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                FilledTonalIconButton(
                    onClick = onContactWhatsApp,
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = Color(0xFF25D366),
                        contentColor = Color.White
                    ),
                    modifier = Modifier.size(34.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Chat,
                        contentDescription = "WhatsApp Repartidor",
                        modifier = Modifier.size(18.dp)
                    )
                }
                FilledTonalIconButton(
                    onClick = onCall,
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = AcaAzulConfianza,
                        contentColor = Color.White
                    ),
                    modifier = Modifier.size(34.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Phone,
                        contentDescription = "Llamar Repartidor",
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ContactosMandadoSection(
    order: MandadoOrder,
    isDriverView: Boolean,
    context: Context
) {
    val nombreRemitente = order.getNombreRemitente()
    val telRemitente = order.getTelefonoRemitente()
    val nombreDestinatario = order.getNombreDestinatario()
    val telDestinatario = order.getTelefonoDestinatario()
    val referencias = order.getReferencias()

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = AcaSurfaceVariant.copy(alpha = 0.7f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Fila de Quien Manda
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(AcaTurquesaContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Upload, contentDescription = null, tint = AcaTurquesaDark, modifier = Modifier.size(16.dp))
                    }
                    Column {
                        Text("Quien Manda (Origen)", fontSize = 10.sp, color = AcaGrisSubtle)
                        Text(
                            text = "$nombreRemitente • $telRemitente",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = AcaAzulDark,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                if (isDriverView && (order.estado == "ACEPTADO" || order.estado == "EN_CAMINO")) {
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        FilledTonalIconButton(
                            onClick = {
                                openWhatsApp(
                                    context = context,
                                    phone = telRemitente,
                                    message = "¡Hola $nombreRemitente! Soy tu repartidor de AcaMandados (${order.folio}). Voy en camino a recoger el pedido en ${order.puntoRecogida}."
                                )
                            },
                            colors = IconButtonDefaults.filledTonalIconButtonColors(
                                containerColor = Color(0xFF25D366),
                                contentColor = Color.White
                            ),
                            modifier = Modifier.size(30.dp)
                        ) {
                            Icon(Icons.Default.Chat, contentDescription = "WhatsApp Remitente", modifier = Modifier.size(15.dp))
                        }

                        FilledTonalIconButton(
                            onClick = { openDialer(context, telRemitente) },
                            colors = IconButtonDefaults.filledTonalIconButtonColors(
                                containerColor = AcaAzulConfianza,
                                contentColor = Color.White
                            ),
                            modifier = Modifier.size(30.dp)
                        ) {
                            Icon(Icons.Default.Phone, contentDescription = "Llamar Remitente", modifier = Modifier.size(15.dp))
                        }
                    }
                }
            }

            HorizontalDivider(color = Color(0xFFE2E8F0))

            // Fila de Quien Recibe
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(AcaAzulContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null, tint = AcaAzulConfianza, modifier = Modifier.size(16.dp))
                    }
                    Column {
                        Text("Quien Recibe (Destino)", fontSize = 10.sp, color = AcaGrisSubtle)
                        Text(
                            text = "$nombreDestinatario • $telDestinatario",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = AcaAzulDark,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                if (isDriverView && (order.estado == "ACEPTADO" || order.estado == "EN_CAMINO")) {
                    FilledTonalIconButton(
                        onClick = { openDialer(context, telDestinatario) },
                        colors = IconButtonDefaults.filledTonalIconButtonColors(
                            containerColor = AcaAzulConfianza,
                            contentColor = Color.White
                        ),
                        modifier = Modifier.size(30.dp)
                    ) {
                        Icon(Icons.Default.Phone, contentDescription = "Llamar Destinatario", modifier = Modifier.size(15.dp))
                    }
                }
            }

            // Referencias del domicilio si existen
            if (referencias.isNotBlank()) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color.White,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Home,
                            contentDescription = null,
                            tint = AcaAzulConfianza,
                            modifier = Modifier.size(16.dp)
                        )
                        Column {
                            Text(
                                text = "Referencias del domicilio:",
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = AcaAzulDark
                            )
                            Text(
                                text = referencias,
                                fontSize = 11.sp,
                                color = AcaGrisEquilibrio,
                                lineHeight = 15.sp
                            )
                        }
                    }
                }
            }

            // Botones de 1-toque exclusivos para el repartidor avisar al llegar o pedir referencias
            if (isDriverView && (order.estado == "ACEPTADO" || order.estado == "EN_CAMINO")) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Button(
                        onClick = {
                            openWhatsApp(
                                context = context,
                                phone = telDestinatario,
                                message = "¡Hola $nombreDestinatario! Soy tu repartidor de AcaMandados (${order.folio}). 🛵 Ya me encuentro afuera de tu domicilio (${order.puntoEntrega}) para entregarte tu mandado."
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = Color.White, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("¡Ya estoy afuera!", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }

                    OutlinedButton(
                        onClick = {
                            openWhatsApp(
                                context = context,
                                phone = telDestinatario,
                                message = "Hola $nombreDestinatario, soy tu repartidor de AcaMandados (${order.folio}). Ya estoy cerca de tu zona (${order.zonaEntrega}), ¿me podrías dar más referencias de tu fachada, portón o timbre para ubicarte rápido? Gracias."
                            )
                        },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.HelpOutline, contentDescription = null, tint = AcaAzulConfianza, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Pedir referencias", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = AcaAzulConfianza)
                    }
                }
            }
        }
    }
}

@Composable
private fun DriverActionButtons(
    order: MandadoOrder,
    onTakeOrder: (() -> Unit)?,
    onAdvanceStatus: ((String) -> Unit)?,
    onOpenPinValidation: ((MandadoOrder) -> Unit)? = null,
    onOpenProofOfCondition: ((MandadoOrder) -> Unit)? = null
) {
    when (order.estado) {
        "SOLICITADO" -> {
            Button(
                onClick = { onTakeOrder?.invoke() },
                colors = ButtonDefaults.buttonColors(containerColor = AcaTurquesaDark),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Aceptar Mandado • Ganar $${String.format("%.2f", order.tarifaEnvio)} MXN",
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
        "ACEPTADO" -> {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                if (order.fotoRecoleccionUri == null && onOpenProofOfCondition != null) {
                    Button(
                        onClick = { onOpenProofOfCondition(order) },
                        colors = ButtonDefaults.buttonColors(containerColor = AcaAzulConfianza),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "📸 Foto de Condición en Origen (Roadie)",
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    Text(
                        text = "Toma una foto del paquete antes de arrancar la moto para protegerte de reclamos de daño.",
                        fontSize = 10.5.sp,
                        color = AcaGrisSubtle
                    )
                } else {
                    Button(
                        onClick = { onAdvanceStatus?.invoke("EN_CAMINO") },
                        colors = ButtonDefaults.buttonColors(containerColor = AcaAzulConfianza),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Navigation,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Iniciar Ruta (Marcar: En Camino)",
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
        "EN_CAMINO" -> {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Button(
                    onClick = {
                        if (onOpenPinValidation != null) {
                            onOpenPinValidation(order)
                        } else {
                            onAdvanceStatus?.invoke("ENTREGADO")
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AcaGreenSuccess),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "🔐 Validar PIN del Destinatario (Uber Connect)",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                Text(
                    text = "Pide el PIN de 4 dígitos a quien recibe para desbloquear la entrega y registrar tu pago.",
                    fontSize = 10.5.sp,
                    color = AcaGrisSubtle
                )
            }
        }
    }
}

private fun getCategoryIcon(tipo: String): ImageVector {
    return when (tipo) {
        "DOCUMENTACION" -> Icons.Default.Description
        "FARMACIA" -> Icons.Default.Medication
        "COMIDA" -> Icons.Default.Restaurant
        "COMPRAS" -> Icons.Default.ShoppingCart
        else -> Icons.Default.FlashOn
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
        Toast.makeText(context, "No se pudo abrir WhatsApp. Enviando SMS...", Toast.LENGTH_SHORT).show()
        val smsIntent = Intent(Intent.ACTION_VIEW, Uri.parse("sms:$phone")).apply {
            putExtra("sms_body", message)
        }
        context.startActivity(smsIntent)
    }
}

private fun openDialer(context: Context, phone: String) {
    try {
        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone"))
        context.startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(context, "Teléfono: $phone", Toast.LENGTH_SHORT).show()
    }
}
