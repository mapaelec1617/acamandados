package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.MandadosViewModel
import com.example.ui.theme.*

/**
 * Componente de Formulario "New Order" (Nuevo Mandado) construido exclusivamente con Material 3.
 * Permite a los usuarios ingresar:
 *  1. Origen / Punto de Recogida (con sugerencias de Acapulco).
 *  2. Destino / Punto de Entrega (con sugerencias y referencias de domicilio).
 *  3. Descripción del encargo o paquete a transportar (con plantillas rápidas y selector de tamaño).
 */
@Composable
fun NewOrderFormComponent(
    origen: String,
    onOrigenChange: (String) -> Unit,
    destino: String,
    onDestinoChange: (String) -> Unit,
    descripcion: String,
    onDescripcionChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    zonaOrigen: String = "Costera Miguel Alemán",
    onZonaOrigenClick: (() -> Unit)? = null,
    zonaDestino: String = "Costa Azul & Club Deportivo",
    onZonaDestinoClick: (() -> Unit)? = null,
    titulo: String = "",
    onTituloChange: ((String) -> Unit)? = null,
    referenciasDomicilio: String = "",
    onReferenciasDomicilioChange: ((String) -> Unit)? = null,
    remitenteNombre: String = "",
    onRemitenteNombreChange: ((String) -> Unit)? = null,
    remitenteTelefono: String = "",
    onRemitenteTelefonoChange: ((String) -> Unit)? = null,
    destinatarioNombre: String = "",
    onDestinatarioNombreChange: ((String) -> Unit)? = null,
    destinatarioTelefono: String = "",
    onDestinatarioTelefonoChange: ((String) -> Unit)? = null,
    onCopiarClienteARemitente: (() -> Unit)? = null,
    onCopiarClienteADestinatario: (() -> Unit)? = null,
    tamanoPaquete: String = "MEDIANO",
    onTamanoPaqueteChange: ((String) -> Unit)? = null,
    origenError: String? = null,
    destinoError: String? = null,
    descripcionError: String? = null,
    onSubmit: (() -> Unit)? = null,
    submitButtonText: String = "Continuar con el Mandado",
    isSubmitting: Boolean = false
) {
    var showAdditionalContactDetails by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        modifier = modifier
            .fillMaxWidth()
            .testTag("new_order_form_component")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Encabezado del Formulario con estilo Material 3
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(AcaTurquesaContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.TwoWheeler,
                        contentDescription = "Nuevo Mandado",
                        tint = AcaTurquesaDark,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Solicitar Nuevo Mandado",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = AcaAzulDark
                    )
                    Text(
                        text = "Ingresa origen, destino y descripción de tu paquete",
                        style = MaterialTheme.typography.bodySmall,
                        color = AcaGrisSubtle
                    )
                }
            }

            HorizontalDivider(color = Color(0xFFF1F5F9))

            // =========================================================================
            // 1. SECCIÓN DE ORIGEN (PUNTO DE RECOGIDA)
            // =========================================================================
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = AcaTurquesaContainer,
                            modifier = Modifier.size(22.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("1", fontSize = 11.sp, fontWeight = FontWeight.Black, color = AcaTurquesaDark)
                            }
                        }
                        Text(
                            text = "Punto de Origen (Recogida)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.5.sp,
                            color = AcaAzulDark
                        )
                    }

                    if (onZonaOrigenClick != null) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = AcaSurfaceVariant,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onZonaOrigenClick() }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = zonaOrigen,
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AcaTurquesaDark,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Icon(Icons.Default.ArrowDropDown, contentDescription = "Cambiar zona de origen", modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }

                // Campo de texto M3: Dirección de Origen
                OutlinedTextField(
                    value = origen,
                    onValueChange = onOrigenChange,
                    label = { Text("Dirección de Origen (Dónde recoger) *") },
                    placeholder = { Text("Ej. Av. Costera #180 / Farmacia del Ahorro") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Place,
                            contentDescription = "Punto de Origen",
                            tint = AcaTurquesaDark
                        )
                    },
                    trailingIcon = {
                        if (origen.isNotBlank()) {
                            IconButton(onClick = { onOrigenChange("") }) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Borrar origen",
                                    tint = AcaGrisSubtle
                                )
                            }
                        }
                    },
                    isError = origenError != null,
                    supportingText = {
                        if (origenError != null) {
                            Text(text = origenError, color = MaterialTheme.colorScheme.error, fontSize = 11.sp)
                        } else {
                            Text(text = "Indica calle, número o negocio de partida en Acapulco", color = AcaGrisSubtle, fontSize = 10.5.sp)
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AcaTurquesaDark,
                        focusedLabelColor = AcaTurquesaDark
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_order_origin")
                        .testTag("input_direccion_origen")
                )

                // Chips de sugerencia rápida de origen en Acapulco
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    contentPadding = PaddingValues(horizontal = 2.dp)
                ) {
                    val originSuggestions = listOf(
                        "🏖️ Costera #180" to "Av. Costera Miguel Alemán #180, Acapulco",
                        "🛒 OXXO Condesa" to "Tienda OXXO Condesa, Costera Miguel Alemán",
                        "💊 Farmacia Magallanes" to "Farmacia del Ahorro, Magallanes",
                        "🏬 Galerías Acapulco" to "Av. Wilfrido Massieu #32, Fracc. Magallanes"
                    )
                    items(originSuggestions) { (chipLabel, address) ->
                        SuggestionChip(
                            onClick = { onOrigenChange(address) },
                            label = { Text(chipLabel, fontSize = 11.sp) },
                            colors = SuggestionChipDefaults.suggestionChipColors(
                                containerColor = AcaSurfaceVariant
                            ),
                            border = SuggestionChipDefaults.suggestionChipBorder(
                                enabled = true,
                                borderColor = Color(0xFFE2E8F0)
                            )
                        )
                    }
                }
            }

            // Conector Visual de Ruta entre Origen y Destino
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 24.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(18.dp)
                        .background(Color(0xFFCBD5E1))
                )
                Icon(
                    imageVector = Icons.Default.ArrowDownward,
                    contentDescription = null,
                    tint = AcaGrisSubtle,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "Ruta directa de transporte local",
                    fontSize = 10.sp,
                    color = AcaGrisSubtle
                )
            }

            // =========================================================================
            // 2. SECCIÓN DE DESTINO (PUNTO DE ENTREGA)
            // =========================================================================
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = AcaAzulContainer,
                            modifier = Modifier.size(22.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("2", fontSize = 11.sp, fontWeight = FontWeight.Black, color = AcaAzulConfianza)
                            }
                        }
                        Text(
                            text = "Punto de Destino (Entrega)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.5.sp,
                            color = AcaAzulDark
                        )
                    }

                    if (onZonaDestinoClick != null) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = AcaSurfaceVariant,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onZonaDestinoClick() }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = zonaDestino,
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AcaAzulConfianza,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Icon(Icons.Default.ArrowDropDown, contentDescription = "Cambiar zona de destino", modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }

                // Campo de texto M3: Dirección de Destino
                OutlinedTextField(
                    value = destino,
                    onValueChange = onDestinoChange,
                    label = { Text("Dirección de Destino (Dónde entregar) *") },
                    placeholder = { Text("Ej. Condominio Torre de Mar #402, Costa Azul") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "Punto de Destino",
                            tint = AcaAzulConfianza
                        )
                    },
                    trailingIcon = {
                        if (destino.isNotBlank()) {
                            IconButton(onClick = { onDestinoChange("") }) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Borrar destino",
                                    tint = AcaGrisSubtle
                                )
                            }
                        }
                    },
                    isError = destinoError != null,
                    supportingText = {
                        if (destinoError != null) {
                            Text(text = destinoError, color = MaterialTheme.colorScheme.error, fontSize = 11.sp)
                        } else {
                            Text(text = "Domicilio exacto, número interior o nombre de condominio", color = AcaGrisSubtle, fontSize = 10.5.sp)
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AcaAzulConfianza,
                        focusedLabelColor = AcaAzulConfianza
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_order_destination")
                        .testTag("input_direccion_destino")
                )

                // Chips de sugerencia rápida de destino
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    contentPadding = PaddingValues(horizontal = 2.dp)
                ) {
                    val destinationSuggestions = listOf(
                        "🏢 Costa Azul" to "Condominio Costa Azul Torre B #402, Acapulco",
                        "🌴 Zona Diamante" to "Blvd. de las Naciones Km 12, Fracc. Granjas del Marqués",
                        "⛵ Puerto Marqués" to "Calle Miguel Alemán s/n, Puerto Marqués",
                        "🏙️ Hornos Insurgentes" to "Av. Cuauhtémoc #45, Col. Hornos Insurgentes"
                    )
                    items(destinationSuggestions) { (chipLabel, address) ->
                        SuggestionChip(
                            onClick = { onDestinoChange(address) },
                            label = { Text(chipLabel, fontSize = 11.sp) },
                            colors = SuggestionChipDefaults.suggestionChipColors(
                                containerColor = AcaSurfaceVariant
                            ),
                            border = SuggestionChipDefaults.suggestionChipBorder(
                                enabled = true,
                                borderColor = Color(0xFFE2E8F0)
                            )
                        )
                    }
                }
            }

            HorizontalDivider(color = Color(0xFFF1F5F9))

            // =========================================================================
            // 3. SECCIÓN DE DESCRIPCIÓN DEL ENCARGO O PAQUETE
            // =========================================================================
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFFFEF3C7),
                        modifier = Modifier.size(22.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("3", fontSize = 11.sp, fontWeight = FontWeight.Black, color = Color(0xFFB45309))
                        }
                    }
                    Text(
                        text = "Descripción del Encargo o Paquete",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.5.sp,
                        color = AcaAzulDark
                    )
                }

                // Título corto opcional
                if (onTituloChange != null) {
                    OutlinedTextField(
                        value = titulo,
                        onValueChange = onTituloChange,
                        label = { Text("Título corto o concepto") },
                        placeholder = { Text("Ej. Entrega de llaves / Compra farmacia") },
                        leadingIcon = {
                            Icon(Icons.Default.Label, contentDescription = null, tint = AcaGrisSubtle, modifier = Modifier.size(18.dp))
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_order_title")
                    )
                }

                // Campo multilínea M3: Descripción detallada del encargo
                OutlinedTextField(
                    value = descripcion,
                    onValueChange = onDescripcionChange,
                    label = { Text("¿Qué artículos transportará el repartidor? *") },
                    placeholder = { Text("Describe los artículos o encargo: medicamentos con receta, comida sellada, sobre con documentos, ropa o paquete de tienda.") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Inventory2,
                            contentDescription = "Descripción del paquete",
                            tint = Color(0xFFB45309)
                        )
                    },
                    trailingIcon = {
                        if (descripcion.isNotBlank()) {
                            IconButton(onClick = { onDescripcionChange("") }) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Borrar descripción",
                                    tint = AcaGrisSubtle
                                )
                            }
                        }
                    },
                    isError = descripcionError != null,
                    supportingText = {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            if (descripcionError != null) {
                                Text(text = descripcionError, color = MaterialTheme.colorScheme.error, fontSize = 11.sp)
                            } else {
                                Text(text = "Sé claro sobre fragilidad o instrucciones especiales", color = AcaGrisSubtle, fontSize = 10.5.sp)
                            }
                            Text(text = "${descripcion.length} caracteres", color = AcaGrisSubtle, fontSize = 10.sp)
                        }
                    },
                    minLines = 2,
                    maxLines = 4,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFB45309),
                        focusedLabelColor = Color(0xFFB45309)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_order_description")
                        .testTag("input_descripcion_servicio")
                )

                // Chips rápidos de plantilla de descripción
                Text(
                    text = "Plantillas rápidas de encargo:",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = AcaGrisSubtle
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    contentPadding = PaddingValues(horizontal = 2.dp)
                ) {
                    val packagePresets = listOf(
                        "📄 Documentos / Llaves" to "Entrega de documentos sellados y llaves de departamento.",
                        "💊 Farmacia / Medicinas" to "Comprar medicamento para dolor y suero oral en farmacia.",
                        "🍔 Comida / Despensa" to "Comprar 2 garrafones de agua y botana en tienda.",
                        "📦 Paquete Chico (< 3 kg)" to "Paquete sellado chico de tienda comercial.",
                        "🛍️ Ropa / Regalo" to "Bolsa con prendas de vestir o paquete de regalo."
                    )
                    items(packagePresets) { (label, presetText) ->
                        AssistChip(
                            onClick = { onDescripcionChange(presetText) },
                            label = { Text(label, fontSize = 11.sp) },
                            leadingIcon = {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                            }
                        )
                    }
                }

                // Selector de Tamaño de Carga (Roadie en moto)
                if (onTamanoPaqueteChange != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Tamaño estimado del paquete:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = AcaGrisSubtle
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val sizes = listOf(
                            "CHICO" to "Chico (< 3 kg)",
                            "MEDIANO" to "Mediano (< 8 kg)",
                            "GRANDE" to "Grande (< 12 kg)"
                        )
                        sizes.forEach { (key, sizeLabel) ->
                            val isSelected = tamanoPaquete == key
                            FilterChip(
                                selected = isSelected,
                                onClick = { onTamanoPaqueteChange(key) },
                                label = { Text(sizeLabel, fontSize = 11.sp) },
                                leadingIcon = if (isSelected) {
                                    { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp)) }
                                } else null,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // =========================================================================
            // SECCIÓN COLAPSIBLE DE CONTACTOS Y REFERENCIAS
            // =========================================================================
            if (onReferenciasDomicilioChange != null || onRemitenteNombreChange != null || onDestinatarioNombreChange != null) {
                HorizontalDivider(color = Color(0xFFF1F5F9))

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = AcaSurfaceVariant,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { showAdditionalContactDetails = !showAdditionalContactDetails }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.Contacts, contentDescription = null, tint = AcaAzulDark, modifier = Modifier.size(18.dp))
                            Text(
                                text = "Contactos y Referencias de Entrega",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.5.sp,
                                color = AcaAzulDark
                            )
                        }
                        Icon(
                            imageVector = if (showAdditionalContactDetails) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = "Expandir contactos",
                            tint = AcaAzulDark
                        )
                    }
                }

                AnimatedVisibility(
                    visible = showAdditionalContactDetails,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Remitente
                        if (onRemitenteNombreChange != null && onRemitenteTelefonoChange != null) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Datos de quien entrega en origen", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = AcaAzulDark)
                                if (onCopiarClienteARemitente != null) {
                                    TextButton(onClick = onCopiarClienteARemitente, contentPadding = PaddingValues(0.dp)) {
                                        Text("Soy yo", fontSize = 11.sp, color = AcaTurquesaDark, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = remitenteNombre,
                                    onValueChange = onRemitenteNombreChange,
                                    label = { Text("Nombre remitente") },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true,
                                    shape = RoundedCornerShape(10.dp)
                                )
                                OutlinedTextField(
                                    value = remitenteTelefono,
                                    onValueChange = onRemitenteTelefonoChange,
                                    label = { Text("Teléfono") },
                                    modifier = Modifier.weight(1f),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                    singleLine = true,
                                    shape = RoundedCornerShape(10.dp)
                                )
                            }
                        }

                        // Destinatario
                        if (onDestinatarioNombreChange != null && onDestinatarioTelefonoChange != null) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Datos de quien recibe en destino", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = AcaAzulDark)
                                if (onCopiarClienteADestinatario != null) {
                                    TextButton(onClick = onCopiarClienteADestinatario, contentPadding = PaddingValues(0.dp)) {
                                        Text("Recibo yo", fontSize = 11.sp, color = AcaAzulConfianza, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = destinatarioNombre,
                                    onValueChange = onDestinatarioNombreChange,
                                    label = { Text("Nombre destinatario") },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true,
                                    shape = RoundedCornerShape(10.dp)
                                )
                                OutlinedTextField(
                                    value = destinatarioTelefono,
                                    onValueChange = onDestinatarioTelefonoChange,
                                    label = { Text("Teléfono") },
                                    modifier = Modifier.weight(1f),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                    singleLine = true,
                                    shape = RoundedCornerShape(10.dp)
                                )
                            }
                        }

                        // Referencias
                        if (onReferenciasDomicilioChange != null) {
                            OutlinedTextField(
                                value = referenciasDomicilio,
                                onValueChange = onReferenciasDomicilioChange,
                                label = { Text("Referencias del domicilio (Fachada, portón, timbre)") },
                                placeholder = { Text("Ej. Casa blanca con portón café, timbre en la entrada.") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp)
                            )
                        }
                    }
                }
            }

            // =========================================================================
            // AVISO DE SEGURIDAD Y PROTECCIÓN DE DATOS
            // =========================================================================
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = Color(0xFFF0FDF4),
                border = BorderStroke(1.dp, Color(0xFFBBF7D0)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("banner_seguridad_formulario")
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.GppGood,
                        contentDescription = null,
                        tint = Color(0xFF16A34A),
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Tus datos de recogida y entrega están cifrados y protegidos por PIN de seguridad de 4 dígitos (estilo Uber Connect).",
                        fontSize = 11.sp,
                        color = Color(0xFF15803D),
                        lineHeight = 14.sp
                    )
                }
            }

            // =========================================================================
            // BOTÓN DE ACCIÓN / CONFIRMACIÓN (SI SE PROPORCIONA onSubmit)
            // =========================================================================
            if (onSubmit != null) {
                Button(
                    onClick = onSubmit,
                    enabled = !isSubmitting,
                    colors = ButtonDefaults.buttonColors(containerColor = AcaTurquesaDark),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("btn_submit_new_order")
                ) {
                    if (isSubmitting) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Procesando...", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    } else {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = submitButtonText,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

/**
 * Sobrecarga que conecta [NewOrderFormComponent] directamente con [MandadosViewModel].
 */
@Composable
fun NewOrderFormComponent(
    viewModel: MandadosViewModel,
    modifier: Modifier = Modifier,
    attemptedSubmit: Boolean = false,
    onZonaOrigenClick: (() -> Unit)? = null,
    onZonaDestinoClick: (() -> Unit)? = null,
    onSubmit: (() -> Unit)? = null,
    submitButtonText: String = "Continuar con el Mandado"
) {
    val origen by viewModel.puntoRecogidaInput.collectAsStateWithLifecycle()
    val destino by viewModel.puntoEntregaInput.collectAsStateWithLifecycle()
    val descripcion by viewModel.descripcionInput.collectAsStateWithLifecycle()
    val zonaOrigen by viewModel.zonaRecogidaInput.collectAsStateWithLifecycle()
    val zonaDestino by viewModel.zonaEntregaInput.collectAsStateWithLifecycle()
    val titulo by viewModel.tituloInput.collectAsStateWithLifecycle()
    val referencias by viewModel.referenciasDomicilioInput.collectAsStateWithLifecycle()
    val remitenteNombre by viewModel.remitenteNombreInput.collectAsStateWithLifecycle()
    val remitenteTelefono by viewModel.remitenteTelefonoInput.collectAsStateWithLifecycle()
    val destinatarioNombre by viewModel.destinatarioNombreInput.collectAsStateWithLifecycle()
    val destinatarioTelefono by viewModel.destinatarioTelefonoInput.collectAsStateWithLifecycle()
    val tamanoPaquete by viewModel.tamanoPaqueteInput.collectAsStateWithLifecycle()

    val origenError = if (attemptedSubmit && origen.trim().isBlank()) "⚠️ La dirección de origen es obligatoria" else null
    val destinoError = if (attemptedSubmit && destino.trim().isBlank()) "⚠️ La dirección de destino es obligatoria" else null
    val descripcionError = if (attemptedSubmit && descripcion.trim().isBlank()) "⚠️ La descripción del mandado es obligatoria" else null

    NewOrderFormComponent(
        origen = origen,
        onOrigenChange = { viewModel.puntoRecogidaInput.value = it },
        destino = destino,
        onDestinoChange = { viewModel.puntoEntregaInput.value = it },
        descripcion = descripcion,
        onDescripcionChange = { viewModel.descripcionInput.value = it },
        zonaOrigen = zonaOrigen,
        onZonaOrigenClick = onZonaOrigenClick,
        zonaDestino = zonaDestino,
        onZonaDestinoClick = onZonaDestinoClick,
        titulo = titulo,
        onTituloChange = { viewModel.tituloInput.value = it },
        referenciasDomicilio = referencias,
        onReferenciasDomicilioChange = { viewModel.referenciasDomicilioInput.value = it },
        remitenteNombre = remitenteNombre,
        onRemitenteNombreChange = { viewModel.remitenteNombreInput.value = it },
        remitenteTelefono = remitenteTelefono,
        onRemitenteTelefonoChange = { viewModel.remitenteTelefonoInput.value = it },
        destinatarioNombre = destinatarioNombre,
        onDestinatarioNombreChange = { viewModel.destinatarioNombreInput.value = it },
        destinatarioTelefono = destinatarioTelefono,
        onDestinatarioTelefonoChange = { viewModel.destinatarioTelefonoInput.value = it },
        onCopiarClienteARemitente = { viewModel.copiarDatosClienteARemitente() },
        onCopiarClienteADestinatario = { viewModel.copiarDatosClienteADestinatario() },
        tamanoPaquete = tamanoPaquete,
        onTamanoPaqueteChange = { viewModel.tamanoPaqueteInput.value = it },
        origenError = origenError,
        destinoError = destinoError,
        descripcionError = descripcionError,
        onSubmit = onSubmit,
        submitButtonText = submitButtonText,
        modifier = modifier
    )
}
