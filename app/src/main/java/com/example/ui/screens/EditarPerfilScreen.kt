package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.AcapulcoLocations
import com.example.ui.AppMode
import com.example.ui.MandadosViewModel
import com.example.ui.theme.*

/**
 * Pantalla para editar la información de contacto y preferencias de entrega del usuario,
 * sincronizando automáticamente los cambios en Firebase Firestore a través de UserProfileRepository.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditarPerfilScreen(
    viewModel: MandadosViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentMode by viewModel.currentMode.collectAsStateWithLifecycle()
    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()
    val repartidorProfile by viewModel.repartidorProfile.collectAsStateWithLifecycle()
    val isDriver = currentMode == AppMode.REPARTIDOR

    // Estados de Contacto
    var nombre by remember(userProfile) { mutableStateOf(userProfile.nombre.ifBlank { if (isDriver) repartidorProfile?.nombre ?: "Carlos Mendoza" else "Ana Karen Valenzuela" }) }
    var telefono by remember(userProfile) { mutableStateOf(userProfile.telefono.ifBlank { if (isDriver) repartidorProfile?.telefono ?: "7441234567" else "7445209911" }) }
    var email by remember(userProfile) { mutableStateOf(userProfile.email.ifBlank { "usuario@acamandados.com" }) }
    var direccionFrecuente by remember(userProfile) { mutableStateOf(userProfile.direccionFrecuente.ifBlank { "Av. Costera Miguel Alemán #240" }) }
    var zonaFrecuente by remember(userProfile) { mutableStateOf(userProfile.zonaFrecuente.ifBlank { AcapulcoLocations.ZONAS[0].nombre }) }
    var referenciasDomicilio by remember(userProfile) { mutableStateOf(userProfile.referenciasDomicilio.ifBlank { "Edificio blanco con portón café" }) }
    var notificacionesWhatsApp by remember(userProfile) { mutableStateOf(userProfile.notificacionesWhatsApp) }

    // Estados de Preferencias de Entrega
    var metodoPagoPreferido by remember(userProfile) { mutableStateOf(userProfile.metodoPagoPreferido) }
    var exigirPinSeguridad by remember(userProfile) { mutableStateOf(userProfile.exigirPinSeguridad) }
    var tamanoPaquetePreferido by remember(userProfile) { mutableStateOf(userProfile.tamanoPaquetePreferido) }
    var dejarEnPuertaSiNoContesta by remember(userProfile) { mutableStateOf(userProfile.dejarEnPuertaSiNoContesta) }
    var notificacionesPush by remember(userProfile) { mutableStateOf(userProfile.notificacionesPush) }
    var propinaDefaultPorcentaje by remember(userProfile) { mutableStateOf(userProfile.propinaDefaultPorcentaje) }

    // Estados para Repartidor
    var tipoTransporte by remember(userProfile, repartidorProfile) { mutableStateOf(repartidorProfile?.tipoTransporte ?: userProfile.tipoTransporte) }
    var modeloVehiculo by remember(userProfile, repartidorProfile) { mutableStateOf(repartidorProfile?.modeloVehiculo ?: userProfile.modeloVehiculo.ifBlank { "Italika FT150 Blanca" }) }
    var placas by remember(userProfile, repartidorProfile) { mutableStateOf(repartidorProfile?.placas ?: userProfile.placas.ifBlank { "GRO-489A" }) }
    var clabe by remember(userProfile, repartidorProfile) { mutableStateOf(repartidorProfile?.clabeInterbancaria ?: userProfile.clabeInterbancaria.ifBlank { "012180015678901234" }) }
    var banco by remember(userProfile, repartidorProfile) { mutableStateOf(repartidorProfile?.banco ?: userProfile.banco.ifBlank { "BBVA México" }) }

    var expandedZonaDropdown by remember { mutableStateOf(false) }
    var selectedRole by remember(userProfile.rol, isDriver) {
        mutableStateOf(if (userProfile.rol == "REPARTIDOR" || isDriver) AppMode.REPARTIDOR else AppMode.CLIENTE)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Editar Perfil y Preferencias",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = AcaAzulDark
                        )
                        Text(
                            text = if (isDriver) "Datos operativos y de entrega • Acapulco" else "Contacto y preferencias de entrega habituales",
                            fontSize = 11.5.sp,
                            color = AcaGrisSubtle
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("btn_back_editar_perfil")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Regresar al perfil",
                            tint = AcaAzulDark
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        },
        bottomBar = {
            Surface(
                color = Color.White,
                tonalElevation = 8.dp,
                shadowElevation = 8.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onBack,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(0.4f)
                            .height(52.dp)
                            .testTag("btn_cancelar_edicion")
                    ) {
                        Text("Cancelar", fontWeight = FontWeight.SemiBold, color = AcaGrisSubtle)
                    }

                    Button(
                        onClick = {
                            viewModel.guardarPerfil(
                                nombre = nombre,
                                telefono = telefono,
                                email = email,
                                direccionFrecuente = direccionFrecuente,
                                zonaFrecuente = zonaFrecuente,
                                referenciasDomicilio = referenciasDomicilio,
                                notificacionesWhatsApp = notificacionesWhatsApp,
                                metodoPagoPreferido = metodoPagoPreferido,
                                exigirPinSeguridad = exigirPinSeguridad,
                                tamanoPaquetePreferido = tamanoPaquetePreferido,
                                dejarEnPuertaSiNoContesta = dejarEnPuertaSiNoContesta,
                                notificacionesPush = notificacionesPush,
                                propinaDefaultPorcentaje = propinaDefaultPorcentaje,
                                tipoTransporte = tipoTransporte,
                                modeloVehiculo = modeloVehiculo,
                                placas = placas,
                                clabe = clabe,
                                banco = banco,
                                rolElegido = selectedRole,
                                onSuccess = onBack
                            )
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AcaAzulConfianza
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(0.6f)
                            .height(52.dp)
                            .testTag("btn_guardar_perfil")
                    ) {
                        Icon(imageVector = Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Guardar Cambios", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
            }
        },
        modifier = modifier
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(AcaBackground)
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header con Avatar & Indicador de Sincronización Firestore
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(16.dp))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(54.dp)
                                .clip(CircleShape)
                                .background(if (isDriver) AcaTurquesaDark else AcaAzulConfianza),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isDriver) Icons.Default.ElectricScooter else Icons.Default.Person,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (nombre.isNotBlank()) nombre else "Usuario AcaMandados",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = AcaAzulDark
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Surface(
                                    color = Color(0xFFDCFCE7),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = "☁️ Firestore Activo",
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF166534),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                                Text(
                                    text = if (isDriver) "Rol Repartidor" else "Rol Cliente",
                                    fontSize = 11.sp,
                                    color = AcaGrisSubtle
                                )
                            }
                        }
                    }
                }
            }

            // SECCIÓN 0: PREFERENCIA DE ROL (CLIENTE O REPARTIDOR)
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.5.dp, if (selectedRole == AppMode.CLIENTE) AcaTurquesa else Color(0xFF0284C7), RoundedCornerShape(16.dp))
                        .testTag("edit_card_seleccion_rol")
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.SwitchAccount,
                                contentDescription = null,
                                tint = AcaAzulConfianza,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "Elige tu Rol (Tipo de Cuenta)",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = AcaAzulDark
                            )
                        }

                        Text(
                            text = "Define si operas como Cliente o como Repartidor. Esta preferencia se guardará en tu perfil de Firestore al presionar 'Guardar Cambios'.",
                            fontSize = 11.5.sp,
                            color = AcaGrisSubtle
                        )

                        // Selector Cliente
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (selectedRole == AppMode.CLIENTE) AcaTurquesa.copy(alpha = 0.08f) else Color(0xFFF8FAFC),
                            border = BorderStroke(
                                width = if (selectedRole == AppMode.CLIENTE) 2.dp else 1.dp,
                                color = if (selectedRole == AppMode.CLIENTE) AcaTurquesa else Color(0xFFE2E8F0)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { selectedRole = AppMode.CLIENTE }
                                .testTag("edit_radio_cliente")
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                RadioButton(
                                    selected = selectedRole == AppMode.CLIENTE,
                                    onClick = { selectedRole = AppMode.CLIENTE },
                                    colors = RadioButtonDefaults.colors(selectedColor = AcaTurquesa)
                                )
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Cliente AcaMandados",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.5.sp,
                                        color = AcaAzulDark
                                    )
                                    Text(
                                        text = "Pedir mandados, envíos de compras, súper y medicinas con PIN seguro.",
                                        fontSize = 11.sp,
                                        color = AcaGrisSubtle
                                    )
                                }
                            }
                        }

                        // Selector Repartidor
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (selectedRole == AppMode.REPARTIDOR) Color(0xFF0284C7).copy(alpha = 0.08f) else Color(0xFFF8FAFC),
                            border = BorderStroke(
                                width = if (selectedRole == AppMode.REPARTIDOR) 2.dp else 1.dp,
                                color = if (selectedRole == AppMode.REPARTIDOR) Color(0xFF0284C7) else Color(0xFFE2E8F0)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { selectedRole = AppMode.REPARTIDOR }
                                .testTag("edit_radio_repartidor")
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                RadioButton(
                                    selected = selectedRole == AppMode.REPARTIDOR,
                                    onClick = { selectedRole = AppMode.REPARTIDOR },
                                    colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF0284C7))
                                )
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Repartidor AcaMandados",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.5.sp,
                                        color = AcaAzulDark
                                    )
                                    Text(
                                        text = "Tomar pedidos activos por zonas de Acapulco. Cobro directo 100% sin comisiones.",
                                        fontSize = 11.sp,
                                        color = AcaGrisSubtle
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // SECCIÓN 1: INFORMACIÓN DE CONTACTO
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(16.dp))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContactPhone,
                                contentDescription = null,
                                tint = AcaAzulConfianza,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "Información de Contacto",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = AcaAzulDark
                            )
                        }
                        Text(
                            text = "Datos usados para coordinar la recogida y entrega de tus mandados en Acapulco.",
                            fontSize = 11.5.sp,
                            color = AcaGrisSubtle
                        )

                        // Nombre
                        OutlinedTextField(
                            value = nombre,
                            onValueChange = { nombre = it },
                            label = { Text("Nombre Completo") },
                            leadingIcon = {
                                Icon(Icons.Default.Person, contentDescription = null, tint = AcaAzulConfianza)
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_edit_nombre")
                        )

                        // Teléfono
                        OutlinedTextField(
                            value = telefono,
                            onValueChange = { telefono = it },
                            label = { Text("Teléfono de Contacto (10 dígitos)") },
                            leadingIcon = {
                                Icon(Icons.Default.Phone, contentDescription = null, tint = AcaAzulConfianza)
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_edit_telefono")
                        )

                        // Correo
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = { Text("Correo Electrónico") },
                            leadingIcon = {
                                Icon(Icons.Default.Email, contentDescription = null, tint = AcaAzulConfianza)
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_edit_email")
                        )

                        HorizontalDivider(color = Color(0xFFF1F5F9))

                        // Domicilio habitual en Acapulco
                        Text(
                            text = "📍 Domicilio Habitual en Acapulco",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp,
                            color = AcaAzulDark
                        )

                        OutlinedTextField(
                            value = direccionFrecuente,
                            onValueChange = { direccionFrecuente = it },
                            label = { Text("Calle, número exterior y colonia") },
                            leadingIcon = {
                                Icon(Icons.Default.Home, contentDescription = null, tint = AcaAzulConfianza)
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_edit_direccion")
                        )

                        // Selector de Zona de Acapulco
                        ExposedDropdownMenuBox(
                            expanded = expandedZonaDropdown,
                            onExpandedChange = { expandedZonaDropdown = it }
                        ) {
                            OutlinedTextField(
                                value = zonaFrecuente,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Macro-Zona Habitual") },
                                trailingIcon = {
                                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedZonaDropdown)
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = AcaAzulConfianza)
                                },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .menuAnchor()
                                    .fillMaxWidth()
                                    .testTag("selector_edit_zona")
                            )
                            ExposedDropdownMenu(
                                expanded = expandedZonaDropdown,
                                onDismissRequest = { expandedZonaDropdown = false }
                            ) {
                                AcapulcoLocations.ZONAS.forEach { zona ->
                                    DropdownMenuItem(
                                        text = {
                                            Column {
                                                Text(zona.nombre, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                                Text(zona.descripcion, fontSize = 11.sp, color = AcaGrisSubtle)
                                            }
                                        },
                                        onClick = {
                                            zonaFrecuente = zona.nombre
                                            expandedZonaDropdown = false
                                        }
                                    )
                                }
                            }
                        }

                        // Referencias del domicilio
                        OutlinedTextField(
                            value = referenciasDomicilio,
                            onValueChange = { referenciasDomicilio = it },
                            label = { Text("Referencias de fachada o entrega (timbre, portón, etc.)") },
                            leadingIcon = {
                                Icon(Icons.Default.Info, contentDescription = null, tint = AcaAzulConfianza)
                            },
                            singleLine = false,
                            maxLines = 3,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_edit_referencias")
                        )

                        // Switch WhatsApp
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFFF8FAFC))
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Avisos por WhatsApp",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = AcaAzulDark
                                )
                                Text(
                                    text = "Recibir el enlace de rastreo en vivo y PIN al solicitar mandados",
                                    fontSize = 11.sp,
                                    color = AcaGrisSubtle
                                )
                            }
                            Switch(
                                checked = notificacionesWhatsApp,
                                onCheckedChange = { notificacionesWhatsApp = it },
                                modifier = Modifier.testTag("switch_whatsapp")
                            )
                        }
                    }
                }
            }

            // SECCIÓN 2: PREFERENCIAS DE ENTREGA & SEGURIDAD
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(16.dp))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocalShipping,
                                contentDescription = null,
                                tint = AcaAzulConfianza,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "Preferencias de Entrega",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = AcaAzulDark
                            )
                        }

                        // Método de Pago Preferido
                        Text(
                            text = "Método de Pago Habitual",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp,
                            color = AcaAzulDark
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            FilterChip(
                                selected = metodoPagoPreferido == "EFECTIVO",
                                onClick = { metodoPagoPreferido = "EFECTIVO" },
                                label = { Text("💵 Efectivo al recibir") },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("chip_pago_efectivo")
                            )
                            FilterChip(
                                selected = metodoPagoPreferido == "TARJETA",
                                onClick = { metodoPagoPreferido = "TARJETA" },
                                label = { Text("💳 Tarjeta en la app") },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("chip_pago_tarjeta")
                            )
                        }

                        HorizontalDivider(color = Color(0xFFF1F5F9))

                        // Switch PIN de Seguridad estilo Uber Connect
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFFF0FDF4))
                                .border(1.dp, Color(0xFFBBF7D0), RoundedCornerShape(10.dp))
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "🛡️ Exigir PIN de 4 Dígitos",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = Color(0xFF166534)
                                )
                                Text(
                                    text = "El repartidor debe ingresar tu código para finalizar la entrega.",
                                    fontSize = 11.sp,
                                    color = Color(0xFF15803D)
                                )
                            }
                            Switch(
                                checked = exigirPinSeguridad,
                                onCheckedChange = { exigirPinSeguridad = it },
                                modifier = Modifier.testTag("switch_pin_seguridad")
                            )
                        }

                        // Tamaño de Paquete Preferido
                        Text(
                            text = "Tamaño de Carga Frecuente (Estilo Roadie)",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp,
                            color = AcaAzulDark
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                selected = tamanoPaquetePreferido == "CHICO",
                                onClick = { tamanoPaquetePreferido = "CHICO" },
                                label = { Text("Chico (≤1 kg)") },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("chip_tamano_chico")
                            )
                            FilterChip(
                                selected = tamanoPaquetePreferido == "MEDIANO",
                                onClick = { tamanoPaquetePreferido = "MEDIANO" },
                                label = { Text("Mediano (≤5 kg)") },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("chip_tamano_mediano")
                            )
                            FilterChip(
                                selected = tamanoPaquetePreferido == "GRANDE",
                                onClick = { tamanoPaquetePreferido = "GRANDE" },
                                label = { Text("Grande (≤12 kg)") },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("chip_tamano_grande")
                            )
                        }

                        // Switch Dejar en puerta
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFFF8FAFC))
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Dejar en puerta o caseta",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = AcaAzulDark
                                )
                                Text(
                                    text = "Autoriza al repartidor a dejar el paquete si no atiendes de inmediato",
                                    fontSize = 11.sp,
                                    color = AcaGrisSubtle
                                )
                            }
                            Switch(
                                checked = dejarEnPuertaSiNoContesta,
                                onCheckedChange = { dejarEnPuertaSiNoContesta = it },
                                modifier = Modifier.testTag("switch_dejar_puerta")
                            )
                        }

                        // Switch Notificaciones Push
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFFF8FAFC))
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Notificaciones Push en Vivo",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = AcaAzulDark
                                )
                                Text(
                                    text = "Alertas cuando el repartidor esté a menos de 500 metros",
                                    fontSize = 11.sp,
                                    color = AcaGrisSubtle
                                )
                            }
                            Switch(
                                checked = notificacionesPush,
                                onCheckedChange = { notificacionesPush = it },
                                modifier = Modifier.testTag("switch_notificaciones_push")
                            )
                        }

                        // Propina sugerida default
                        Text(
                            text = "Propina Predeterminada para el Repartidor",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp,
                            color = AcaAzulDark
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(0, 10, 15, 20).forEach { pct ->
                                FilterChip(
                                    selected = propinaDefaultPorcentaje == pct,
                                    onClick = { propinaDefaultPorcentaje = pct },
                                    label = { Text(if (pct == 0) "Sin propina" else "$pct%") },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("chip_propina_$pct")
                                )
                            }
                        }
                    }
                }
            }

            // SECCIÓN 3: DATOS DEL REPARTIDOR (Si corresponde)
            if (isDriver) {
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(16.dp))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.TwoWheeler,
                                    contentDescription = null,
                                    tint = AcaTurquesaDark,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = "Datos Operativos del Repartidor",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AcaAzulDark
                                )
                            }

                            OutlinedTextField(
                                value = modeloVehiculo,
                                onValueChange = { modeloVehiculo = it },
                                label = { Text("Modelo de Vehículo") },
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("input_edit_modelo")
                            )

                            OutlinedTextField(
                                value = placas,
                                onValueChange = { placas = it },
                                label = { Text("Placas") },
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("input_edit_placas")
                            )

                            HorizontalDivider(color = Color(0xFFF1F5F9))

                            Text(
                                text = "Cuenta Bancaria para Depósitos SPEI (Mismo Día)",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp,
                                color = AcaAzulDark
                            )

                            OutlinedTextField(
                                value = banco,
                                onValueChange = { banco = it },
                                label = { Text("Banco Receptor") },
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("input_edit_banco")
                            )

                            OutlinedTextField(
                                value = clabe,
                                onValueChange = { clabe = it },
                                label = { Text("CLABE Interbancaria (18 dígitos)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("input_edit_clabe")
                            )
                        }
                    }
                }
            }
        }
    }
}
