package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AcapulcoLocations
import com.example.ui.theme.*

@Composable
fun ClienteZonasScreen(
    modifier: Modifier = Modifier
) {
    var zonaOrigen by remember { mutableStateOf(AcapulcoLocations.ZONAS[0].nombre) }
    var zonaDestino by remember { mutableStateOf(AcapulcoLocations.ZONAS[5].nombre) } // Diamante
    val tarifa = remember(zonaOrigen, zonaDestino) {
        AcapulcoLocations.calcularTarifa(zonaOrigen, zonaDestino, "DOCUMENTACION")
    }

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
                    text = "Tarifas Transparentes Acapulco",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = AcaAzulDark
                )
                Text(
                    text = "Precios claros y justos. El 100% de la tarifa va para el repartidor.",
                    fontSize = 11.5.sp,
                    color = AcaGrisSubtle
                )
            }
        }

        // Calculadora Interactiva de Tarifa
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
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Calculadora Rápida entre Zonas",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color.White
                    )

                    // Origen
                    Column {
                        Text("Desde:", fontSize = 11.sp, color = AcaTurquesaLight)
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color.White.copy(alpha = 0.15f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = zonaOrigen,
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(10.dp),
                                fontSize = 13.sp
                            )
                        }
                    }

                    // Destino
                    Column {
                        Text("Hasta:", fontSize = 11.sp, color = AcaTurquesaLight)
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color.White.copy(alpha = 0.15f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = zonaDestino,
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(10.dp),
                                fontSize = 13.sp
                            )
                        }
                    }

                    HorizontalDivider(color = Color.White.copy(alpha = 0.2f))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Tarifa Estimada de Entrega", fontSize = 11.5.sp, color = Color.White.copy(alpha = 0.8f))
                            Text(
                                text = "$${String.format("%.2f", tarifa)} MXN",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Black,
                                color = AcaTurquesaLight
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(AcaTurquesa)
                                .padding(horizontal = 8.dp, vertical = 5.dp)
                        ) {
                            Text("Sin comisiones ocultas", color = Color(0xFF00382E), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        item {
            Text(
                text = "Zonas de Cobertura en Acapulco",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = AcaAzulDark
            )
        }

        // Lista de Zonas
        items(AcapulcoLocations.ZONAS) { zona ->
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(12.dp))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = zona.nombre,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.5.sp,
                            color = AcaAzulDark
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            TextButton(
                                onClick = { zonaOrigen = zona.nombre },
                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text("Origen", fontSize = 11.sp, color = AcaTurquesaDark)
                            }
                            TextButton(
                                onClick = { zonaDestino = zona.nombre },
                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text("Destino", fontSize = 11.sp, color = AcaAzulConfianza)
                            }
                        }
                    }

                    Text(
                        text = zona.descripcion,
                        fontSize = 12.sp,
                        color = AcaGrisSubtle
                    )

                    Text(
                        text = "Puntos clave: " + zona.puntosReferencia.joinToString(", "),
                        fontSize = 10.5.sp,
                        color = AcaGrisEquilibrio,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}
