package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.AcapulcoLocations
import com.example.data.model.MetodoPago
import com.example.data.model.TamanoPaquete
import com.example.data.model.TarifaDesglose
import com.example.data.model.TipoMandado
import com.example.ui.MandadosViewModel
import com.example.ui.components.AcaBannerBadges
import com.example.ui.components.NewOrderFormComponent
import com.example.ui.components.PaymentSelectionComponent
import com.example.ui.components.PaymentToggleButtons
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClienteSolicitarScreen(
    viewModel: MandadosViewModel,
    modifier: Modifier = Modifier
) {
    val selectedTipo by viewModel.selectedTipo.collectAsStateWithLifecycle()
    val titulo by viewModel.tituloInput.collectAsStateWithLifecycle()
    val descripcion by viewModel.descripcionInput.collectAsStateWithLifecycle()
    val puntoRecogida by viewModel.puntoRecogidaInput.collectAsStateWithLifecycle()
    val zonaRecogida by viewModel.zonaRecogidaInput.collectAsStateWithLifecycle()
    val puntoEntrega by viewModel.puntoEntregaInput.collectAsStateWithLifecycle()
    val zonaEntrega by viewModel.zonaEntregaInput.collectAsStateWithLifecycle()
    val costoProductos by viewModel.costoProductosInput.collectAsStateWithLifecycle()
    val selectedMetodo by viewModel.selectedMetodoPago.collectAsStateWithLifecycle()
    val clienteNombre by viewModel.clienteNombreInput.collectAsStateWithLifecycle()
    val clienteTelefono by viewModel.clienteTelefonoInput.collectAsStateWithLifecycle()
    val remitenteNombre by viewModel.remitenteNombreInput.collectAsStateWithLifecycle()
    val remitenteTelefono by viewModel.remitenteTelefonoInput.collectAsStateWithLifecycle()
    val destinatarioNombre by viewModel.destinatarioNombreInput.collectAsStateWithLifecycle()
    val destinatarioTelefono by viewModel.destinatarioTelefonoInput.collectAsStateWithLifecycle()
    val referenciasDomicilio by viewModel.referenciasDomicilioInput.collectAsStateWithLifecycle()
    val notas by viewModel.notasInput.collectAsStateWithLifecycle()

    // Estados para Categorización de Carga Roadie, COD y Pago con Tarjeta
    val tamanoPaquete by viewModel.tamanoPaqueteInput.collectAsStateWithLifecycle()
    val confirmacionDimensiones by viewModel.confirmacionDimensionesMoto.collectAsStateWithLifecycle()
    val esCobroContraEntrega by viewModel.esCobroContraEntrega.collectAsStateWithLifecycle()
    val montoCobroProducto by viewModel.montoCobroProductoInput.collectAsStateWithLifecycle()
    val tarjetaNumero by viewModel.tarjetaNumeroInput.collectAsStateWithLifecycle()
    val tarjetaTitular by viewModel.tarjetaTitularInput.collectAsStateWithLifecycle()
    val tarjetaExpiracion by viewModel.tarjetaExpiracionInput.collectAsStateWithLifecycle()
    val tarjetaCvv by viewModel.tarjetaCvvInput.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()
    var attemptedSubmit by remember { mutableStateOf(false) }
    var showZonaOrigenPicker by remember { mutableStateOf(false) }
    var showZonaDestinoPicker by remember { mutableStateOf(false) }

    val feeDesglose: TarifaDesglose = remember(zonaRecogida, zonaEntrega, selectedTipo) {
        viewModel.getCalculatedFeeDesglose()
    }
    val calculatedFee = feeDesglose.total

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(AcaBackground),
        contentPadding = PaddingValues(bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            AcaBannerBadges(compact = false)
        }

        item {
            AnimatedVisibility(
                visible = attemptedSubmit && (puntoRecogida.trim().isBlank() || puntoEntrega.trim().isBlank() || descripcion.trim().isBlank())
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFFEF2F2),
                    border = BorderStroke(1.dp, Color(0xFFFCA5A5)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .testTag("banner_error_validacion")
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ErrorOutline,
                            contentDescription = null,
                            tint = Color(0xFFDC2626),
                            modifier = Modifier.size(22.dp)
                        )
                        Column {
                            Text(
                                text = "Campos obligatorios requeridos",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = Color(0xFF991B1B)
                            )
                            Text(
                                text = "Por favor ingresa la dirección de origen (dónde recoger), la dirección de destino (dónde entregar) y describe el servicio.",
                                fontSize = 11.5.sp,
                                color = Color(0xFFB91C1C),
                                lineHeight = 15.sp
                            )
                        }
                    }
                }
            }
        }

        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "¿Qué mandado necesitas en Acapulco?",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = AcaAzulDark
                )

                // Carrusel de Categorías
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(TipoMandado.values()) { tipo ->
                        val isSelected = selectedTipo == tipo
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) AcaAzulConfianza else Color.White,
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .border(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) AcaTurquesa else Color(0xFFE2E8F0),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clickable { viewModel.selectedTipo.value = tipo }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = getTipoIcon(tipo),
                                    contentDescription = tipo.label,
                                    tint = if (isSelected) AcaTurquesaLight else AcaAzulConfianza,
                                    modifier = Modifier.size(18.dp)
                                )
                                Column {
                                    Text(
                                        text = tipo.label,
                                        fontSize = 12.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) Color.White else AcaGrisEquilibrio
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Formulario Material 3 de Nuevo Mandado (Origen, Destino y Descripción del Encargo o Paquete)
        item {
            Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                NewOrderFormComponent(
                    viewModel = viewModel,
                    attemptedSubmit = attemptedSubmit,
                    onZonaOrigenClick = { showZonaOrigenPicker = true },
                    onZonaDestinoClick = { showZonaDestinoPicker = true }
                )
            }
        }

        // Presupuesto de compra estimado e instrucciones adicionales (para compras en farmacia, súper o comida)
        if (selectedTipo == TipoMandado.FARMACIA || selectedTipo == TipoMandado.COMPRAS || selectedTipo == TipoMandado.COMIDA || notas.isNotBlank()) {
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(16.dp))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "Presupuesto de Compra y Notas",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = AcaAzulDark
                        )

                        if (selectedTipo == TipoMandado.FARMACIA || selectedTipo == TipoMandado.COMPRAS || selectedTipo == TipoMandado.COMIDA) {
                            OutlinedTextField(
                                value = costoProductos,
                                onValueChange = { viewModel.costoProductosInput.value = it },
                                label = { Text("Presupuesto estimado de la compra (MXN)") },
                                placeholder = { Text("Ej. 250.00 (opcional)") },
                                modifier = Modifier.fillMaxWidth(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                leadingIcon = { Text("$", fontWeight = FontWeight.Bold) },
                                shape = RoundedCornerShape(10.dp)
                            )
                        }

                        OutlinedTextField(
                            value = notas,
                            onValueChange = { viewModel.notasInput.value = it },
                            label = { Text("Instrucciones o notas adicionales") },
                            placeholder = { Text("Ej. Avisar en caseta de seguridad / Timbre derecho") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp)
                        )
                    }
                }
            }
        }

        // Declaración y Categorización de Carga (Estilo Roadie)
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(16.dp))
                    .testTag("cargo_size_section")
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
                            text = "Tamaño y Carga del Paquete",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = AcaAzulDark
                        )
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = AcaTurquesaContainer
                        ) {
                            Text(
                                text = "Límite Moto: 12 kg",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = AcaTurquesaDark,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Text(
                        text = "Selecciona el tamaño exacto para que el repartidor en motocicleta cuente con la capacidad adecuada.",
                        fontSize = 11.sp,
                        color = AcaGrisSubtle
                    )

                    // Tarjetas de Selección de Tamaño
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TamanoPaquete.values().forEach { tamano ->
                            val isSelected = tamanoPaquete == tamano.id
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) AcaAzulContainer else Color(0xFFF8FAFC),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.5.dp,
                                    if (isSelected) AcaAzulConfianza else Color(0xFFE2E8F0)
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { viewModel.tamanoPaqueteInput.value = tamano.id }
                                    .padding(8.dp)
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = when (tamano) {
                                            TamanoPaquete.CHICO -> Icons.Default.Email
                                            TamanoPaquete.MEDIANO -> Icons.Default.Inventory2
                                            TamanoPaquete.GRANDE -> Icons.Default.Luggage
                                        },
                                        contentDescription = tamano.label,
                                        tint = if (isSelected) AcaAzulConfianza else AcaGrisEquilibrio,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Text(
                                        text = tamano.label,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = if (isSelected) AcaAzulDark else AcaGrisEquilibrio
                                    )
                                    Text(
                                        text = tamano.limite,
                                        fontSize = 9.5.sp,
                                        color = if (isSelected) AcaAzulConfianza else AcaGrisSubtle,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }

                    // Descripción del tamaño seleccionado
                    val currentTamano = TamanoPaquete.values().firstOrNull { it.id == tamanoPaquete } ?: TamanoPaquete.MEDIANO
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = AcaSurfaceVariant,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.Info, contentDescription = null, tint = AcaAzulConfianza, modifier = Modifier.size(18.dp))
                            Column {
                                Text(
                                    text = "${currentTamano.label}: ${currentTamano.descripcion}",
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AcaAzulDark
                                )
                                Text(
                                    text = "Capacidad en moto: ${currentTamano.capacidadMoto}",
                                    fontSize = 10.5.sp,
                                    color = AcaGrisSubtle
                                )
                            }
                        }
                    }

                    // Checkbox Obligatorio de Confirmación de Dimensiones
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (confirmacionDimensiones) AcaTurquesaContainer.copy(alpha = 0.6f) else Color(0xFFFFFBEB),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (confirmacionDimensiones) AcaTurquesaDark else Color(0xFFFDE68A)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { viewModel.confirmacionDimensionesMoto.value = !confirmacionDimensiones }
                    ) {
                        Row(
                            modifier = Modifier.padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Checkbox(
                                checked = confirmacionDimensiones,
                                onCheckedChange = { viewModel.confirmacionDimensionesMoto.value = it },
                                colors = CheckboxDefaults.colors(checkedColor = AcaTurquesaDark),
                                modifier = Modifier.testTag("checkbox_confirmacion_dimensiones")
                            )
                            Text(
                                text = "Confirmo que mi paquete no excede 12 kg y cabe en una mochila o parrilla de motocicleta.",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (confirmacionDimensiones) AcaAzulDark else Color(0xFF92400E),
                                lineHeight = 15.sp
                            )
                        }
                    }
                }
            }
        }

        // Cobro Contra Entrega (COD) para Comercios / Vendedores Locales
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(16.dp))
                    .testTag("cod_section")
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
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Default.Storefront, contentDescription = null, tint = Color(0xFFB45309), modifier = Modifier.size(20.dp))
                            Text(
                                text = "Cobro Contra Entrega (COD)",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = AcaAzulDark
                            )
                        }

                        Switch(
                            checked = esCobroContraEntrega,
                            onCheckedChange = { viewModel.esCobroContraEntrega.value = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = AcaTurquesaDark
                            ),
                            modifier = Modifier.testTag("switch_cod")
                        )
                    }

                    Text(
                        text = "Para vendedores de Marketplace, bazares o tiendas locales en Acapulco: el repartidor cobrará el producto en efectivo al entregar.",
                        fontSize = 11.sp,
                        color = AcaGrisSubtle,
                        lineHeight = 15.sp
                    )

                    AnimatedVisibility(visible = esCobroContraEntrega) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            OutlinedTextField(
                                value = montoCobroProducto,
                                onValueChange = {
                                    if (it.isEmpty() || it.all { ch -> ch.isDigit() || ch == '.' }) {
                                        viewModel.montoCobroProductoInput.value = it
                                    }
                                },
                                label = { Text("Valor del producto a cobrar ($ MXN)") },
                                placeholder = { Text("Ej. 250.00 (Máximo $500 MXN en piloto)") },
                                leadingIcon = { Text("$", fontWeight = FontWeight.Bold, color = Color(0xFFB45309)) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("input_monto_cod")
                            )

                            val codMontoNum = montoCobroProducto.toDoubleOrNull() ?: 0.0
                            val totalCobroDestinatario = codMontoNum + calculatedFee

                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFFFFFBEB),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFDE68A)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(10.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = "💰 Desglose que cobrará el repartidor al recibir:",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.5.sp,
                                        color = Color(0xFF92400E)
                                    )
                                    Text(
                                        text = "• Mercancía producto: $${String.format("%.2f", codMontoNum)} MXN\n• Tarifa de envío: $${String.format("%.2f", calculatedFee)} MXN\n• TOTAL EN EFECTIVO: $${String.format("%.2f", totalCobroDestinatario)} MXN",
                                        fontSize = 11.sp,
                                        color = Color(0xFF78350F),
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = "⚠️ Límite piloto: máximo $500 MXN para proteger al chofer.",
                                        fontSize = 10.sp,
                                        color = Color(0xFFB45309)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 5. PaymentSelectionComponent (Credit/Debit Card vs Cash Payment)
        item {
            PaymentSelectionComponent(
                selectedMetodo = selectedMetodo,
                onMetodoSelected = { metodo ->
                    viewModel.selectedMetodoPago.value = metodo
                },
                tarjetaNumero = tarjetaNumero,
                tarjetaExpiracion = tarjetaExpiracion,
                tarjetaCvv = tarjetaCvv,
                tarjetaTitular = tarjetaTitular,
                clienteNombre = clienteNombre,
                onTarjetaNumeroChange = { viewModel.tarjetaNumeroInput.value = it },
                onTarjetaExpiracionChange = { viewModel.tarjetaExpiracionInput.value = it },
                onTarjetaCvvChange = { viewModel.tarjetaCvvInput.value = it },
                totalTarifa = calculatedFee,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            )
        }

        // Tus Datos de Contacto (Sencillos)
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(16.dp))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Datos de Contacto (Para confirmación por WhatsApp)",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = AcaAzulDark
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = clienteNombre,
                            onValueChange = { viewModel.clienteNombreInput.value = it },
                            label = { Text("Tu Nombre") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp)
                        )

                        OutlinedTextField(
                            value = clienteTelefono,
                            onValueChange = { viewModel.clienteTelefonoInput.value = it },
                            label = { Text("WhatsApp (10 dígitos)") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp)
                        )
                    }
                }
            }
        }

        // Resumen y Confirmación con Geofencing Acapulco y PIN
        item {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = AcaAzulDark,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Macro-Zonas Geofencing Info
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Geofencing Acapulco",
                                color = AcaTurquesaLight,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${feeDesglose.macroZonaOrigen.titulo} ➔ ${feeDesglose.macroZonaDestino.titulo}",
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 10.5.sp
                            )
                        }

                        if (feeDesglose.cruceMacroZonas) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = AcaAmbarAlerta.copy(alpha = 0.3f)
                            ) {
                                Text(
                                    text = "Recargo Cruce Lejano: +$25",
                                    color = Color(0xFFFDE68A),
                                    fontSize = 9.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    HorizontalDivider(color = Color.White.copy(alpha = 0.15f))

                    // Desglose de Tarifas
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Tarifa Estimada de Envío",
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 12.sp
                            )
                            Text(
                                text = "$${String.format("%.2f", calculatedFee)} MXN",
                                color = AcaTurquesaLight,
                                fontSize = 26.sp,
                                fontWeight = FontWeight.Black
                            )
                            if (feeDesglose.cruceMacroZonas) {
                                Text(
                                    text = "Base $${feeDesglose.base.toInt()} + Plus $${feeDesglose.plusTipoServicio.toInt()} + Zona lejana $${feeDesglose.recargoZonaLejana.toInt()}",
                                    color = Color.White.copy(alpha = 0.6f),
                                    fontSize = 10.sp
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(AcaTurquesa)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "100% para el chofer",
                                color = Color(0xFF00382E),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    HorizontalDivider(color = Color.White.copy(alpha = 0.15f))

                    // Selector Rápido de Pago en la Tarjeta Final
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Selecciona método de pago para este viaje:",
                            color = Color.White,
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Bold
                        )

                        PaymentToggleButtons(
                            selectedMetodo = selectedMetodo,
                            onMetodoSelected = { metodo ->
                                viewModel.selectedMetodoPago.value = metodo
                            },
                            onDarkBackground = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Text(
                            text = if (selectedMetodo == MetodoPago.EFECTIVO)
                                "✓ Pagarás $${String.format("%.2f", calculatedFee)} MXN en efectivo al repartidor al entregar/recibir."
                            else
                                "✓ Cobro 100% digital a tarjeta •••• 4820 (Ana Karen Valenzuela). Repartidor no te cobrará efectivo.",
                            color = AcaTurquesaLight,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    // Recordatorio del PIN estilo Uber Connect
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color.White.copy(alpha = 0.1f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = AcaTurquesaLight, modifier = Modifier.size(18.dp))
                            Text(
                                text = "🔐 Se generará un PIN de seguridad de 4 dígitos para entrega sin fraude y un enlace de rastreo web sin app para WhatsApp.",
                                fontSize = 10.5.sp,
                                color = Color.White.copy(alpha = 0.9f),
                                lineHeight = 14.sp
                            )
                        }
                    }

                    Button(
                        onClick = {
                            attemptedSubmit = true
                            if (puntoRecogida.trim().isNotBlank() && puntoEntrega.trim().isNotBlank() && descripcion.trim().isNotBlank()) {
                                viewModel.crearNuevoMandado {}
                            } else {
                                Toast.makeText(
                                    context,
                                    "⚠️ Por favor ingresa origen, destino y descripción.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        },
                        enabled = true,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AcaTurquesa
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("confirmar_mandado_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = null,
                            tint = Color(0xFF00382E),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Solicitar Mandado en Acapulco ($${String.format("%.2f", calculatedFee)} MXN)",
                            color = Color(0xFF00382E),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }
        }
    }

    // Modal de selección de Zona de Origen
    if (showZonaOrigenPicker) {
        ZonaSelectionDialog(
            titulo = "Seleccionar Zona de Recogida",
            zonas = AcapulcoLocations.ZONAS,
            zonaActual = zonaRecogida,
            onSelectZona = {
                viewModel.zonaRecogidaInput.value = it.nombre
                showZonaOrigenPicker = false
            },
            onDismiss = { showZonaOrigenPicker = false }
        )
    }

    // Modal de selección de Zona de Destino
    if (showZonaDestinoPicker) {
        ZonaSelectionDialog(
            titulo = "Seleccionar Zona de Entrega",
            zonas = AcapulcoLocations.ZONAS,
            zonaActual = zonaEntrega,
            onSelectZona = {
                viewModel.zonaEntregaInput.value = it.nombre
                showZonaDestinoPicker = false
            },
            onDismiss = { showZonaDestinoPicker = false }
        )
    }
}

@Composable
private fun ZonaSelectionDialog(
    titulo: String,
    zonas: List<com.example.data.model.ZonaAcapulco>,
    zonaActual: String,
    onSelectZona: (com.example.data.model.ZonaAcapulco) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(titulo, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = AcaAzulDark)
        },
        text = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 380.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(zonas) { zona ->
                    val isSelected = zona.nombre == zonaActual
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) AcaTurquesaContainer else AcaSurfaceVariant,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectZona(zona) }
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = zona.nombre,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = if (isSelected) AcaTurquesaDark else AcaGrisEquilibrio
                            )
                            Text(
                                text = zona.descripcion,
                                fontSize = 11.sp,
                                color = AcaGrisSubtle
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cerrar")
            }
        }
    )
}

private fun getTipoIcon(tipo: TipoMandado): ImageVector {
    return when (tipo) {
        TipoMandado.DOCUMENTACION -> Icons.Default.Description
        TipoMandado.FARMACIA -> Icons.Default.Medication
        TipoMandado.COMIDA -> Icons.Default.Restaurant
        TipoMandado.COMPRAS -> Icons.Default.ShoppingCart
        TipoMandado.EXPRESS -> Icons.Default.FlashOn
    }
}
