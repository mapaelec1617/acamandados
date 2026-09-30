package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.MandadoOrder
import com.example.ui.theme.*

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CalificarServicioDialog(
    order: MandadoOrder,
    isDriverRatingClient: Boolean,
    onDismiss: () -> Unit,
    onSubmitRating: (estrellas: Int, comentario: String, tags: String) -> Unit
) {
    var estrellas by remember { mutableStateOf(5) }
    var comentario by remember { mutableStateOf("") }
    val selectedTags = remember { mutableStateListOf<String>() }

    val defaultDriverPraiseTags = listOf(
        "Súper puntual ⚡",
        "Amable y respetuoso 😊",
        "Cuidó el paquete 📦",
        "Manejo seguro 🛵",
        "Excelente comunicación 💬",
        "100% recomendado ⭐"
    )

    val defaultClientPraiseTags = listOf(
        "Pago puntual 💵",
        "Indicaciones claras 🗺️",
        "Amable y cordial 👍",
        "Respondió rápido 📲",
        "Excelente cliente ⭐"
    )

    val availableTags = if (isDriverRatingClient) defaultClientPraiseTags else defaultDriverPraiseTags
    val targetName = if (isDriverRatingClient) order.clienteNombre else (order.repartidorNombre ?: "Carlos Mendoza")

    val starDescription = when (estrellas) {
        1 -> "Pésimo servicio"
        2 -> "Regular"
        3 -> "Buen servicio"
        4 -> "Muy buen servicio"
        5 -> "¡Excelente servicio de 5 estrellas!"
        else -> ""
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color.White,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header con botón de cerrar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isDriverRatingClient) "Calificar al Cliente" else "Calificar al Repartidor",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = AcaAzulDark
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Outlined.Close, contentDescription = "Cerrar", tint = AcaGrisSubtle)
                    }
                }

                Text(
                    text = "¿Cómo fue tu experiencia con $targetName en el pedido ${order.folio}?",
                    fontSize = 13.sp,
                    color = AcaGrisSubtle,
                    textAlign = TextAlign.Center
                )

                // Selector interactivo de 5 Estrellas
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    for (i in 1..5) {
                        val isFilled = i <= estrellas
                        IconButton(
                            onClick = { estrellas = i },
                            modifier = Modifier.size(44.dp)
                        ) {
                            Icon(
                                imageVector = if (isFilled) Icons.Default.Star else Icons.Default.StarBorder,
                                contentDescription = "$i estrellas",
                                tint = if (isFilled) Color(0xFFF59E0B) else Color(0xFFCBD5E1),
                                modifier = Modifier.size(36.dp)
                            )
                        }
                    }
                }

                // Descripción de la calificación
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFFEF3C7)
                ) {
                    Text(
                        text = starDescription,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF92400E),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                }

                // Chips de elogios rápidos
                Text(
                    text = "Selecciona aspectos destacados:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = AcaGrisEquilibrio,
                    modifier = Modifier.align(Alignment.Start)
                )

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    availableTags.forEach { tag ->
                        val isSelected = selectedTags.contains(tag)
                        val chipBg by animateColorAsState(
                            targetValue = if (isSelected) AcaTurquesaDark else Color(0xFFF1F5F9),
                            label = "chipBg"
                        )
                        val chipText by animateColorAsState(
                            targetValue = if (isSelected) Color.White else AcaGrisEquilibrio,
                            label = "chipText"
                        )

                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = chipBg,
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .clickable {
                                    if (isSelected) selectedTags.remove(tag) else selectedTags.add(tag)
                                }
                        ) {
                            Text(
                                text = tag,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = chipText,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                // Campo de texto para reseña y comentarios
                OutlinedTextField(
                    value = comentario,
                    onValueChange = { comentario = it },
                    label = { Text("Escribe un comentario (opcional)", fontSize = 12.sp) },
                    placeholder = {
                        Text(
                            if (isDriverRatingClient) "Ej. Trato muy amable, pago rápido..."
                            else "Ej. Llegó súper rápido, el pedido venía intacto...",
                            fontSize = 11.5.sp
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AcaAzulConfianza,
                        unfocusedBorderColor = Color(0xFFCBD5E1)
                    )
                )

                // Botón Enviar Calificación
                Button(
                    onClick = {
                        val tagsString = selectedTags.joinToString(", ")
                        onSubmitRating(estrellas, comentario, tagsString)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isDriverRatingClient) AcaTurquesaDark else AcaAzulConfianza
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Enviar Calificación ($estrellas ★)",
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}
