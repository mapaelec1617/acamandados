package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.RepartidorProfile
import com.example.ui.RegistroRepartidorFormState
import com.example.ui.RegistroRepartidorViewModel
import com.example.ui.theme.*

/**
 * Pantalla de registro simplificada para nuevos repartidores de AcaMandados.
 * Enfocada en rapidez y facilidad de incorporación con los campos clave:
 * - Nombre completo
 * - Número de teléfono (10 dígitos)
 * - Vehículo (Tipo de transporte, modelo y placas)
 * - Términos y políticas de privacidad
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistroRepartidorScreen(
    onRegistroExitoso: (RepartidorProfile) -> Unit,
    onBack: () -> Unit,
    onOpenPoliticas: () -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: RegistroRepartidorViewModel = viewModel()
) {
    val formState by viewModel.formState.collectAsStateWithLifecycle()

    RegistroRepartidorContent(
        formState = formState,
        onNombreChange = { viewModel.updateNombre(it) },
        onTelefonoChange = { viewModel.updateTelefono(it) },
        onTipoTransporteChange = { viewModel.updateTipoTransporte(it) },
        onModeloVehiculoChange = { viewModel.updateModeloVehiculo(it) },
        onPlacasChange = { viewModel.updatePlacas(it) },
        onZonaPreferidaChange = { viewModel.updateZonaPreferida(it) },
        onTerminosChange = { viewModel.updateTerminosAceptados(it) },
        onSubmit = {
            viewModel.registrarRepartidor(onSuccess = onRegistroExitoso)
        },
        onBack = onBack,
        onOpenPoliticas = onOpenPoliticas,
        modifier = modifier
    )
}

/**
 * Contenido Composable desacoplado para testabilidad y previsualización.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistroRepartidorContent(
    formState: RegistroRepartidorFormState,
    onNombreChange: (String) -> Unit,
    onTelefonoChange: (String) -> Unit,
    onTipoTransporteChange: (String) -> Unit,
    onModeloVehiculoChange: (String) -> Unit,
    onPlacasChange: (String) -> Unit,
    onZonaPreferidaChange: (String) -> Unit,
    onTerminosChange: (Boolean) -> Unit,
    onSubmit: () -> Unit,
    onBack: () -> Unit,
    onOpenPoliticas: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onBack)
    val focusManager = LocalFocusManager.current
    val scrollState = rememberScrollState()

    val tiposTransporte = listOf(
        Triple("MOTO", "Motocicleta", Icons.Default.TwoWheeler),
        Triple("MOTONETA", "Motoneta", Icons.Default.ElectricScooter),
        Triple("AUTO", "Automóvil", Icons.Default.DirectionsCar),
        Triple("BICI", "Bicicleta", Icons.Default.DirectionsBike)
    )

    val zonasAcapulco = listOf(
        "Costera Miguel Alemán",
        "Zona Diamante",
        "Zona Tradicional / Caleta",
        "Cd. Renacimiento / Zapata",
        "Col. Progreso / Farallón"
    )

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("screen_registro_repartidor"),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Registro de Repartidor",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = AcaAzulDark
                        )
                        Text(
                            text = "Gana el 100% de la tarifa sin comisiones",
                            fontSize = 11.5.sp,
                            color = AcaGrisSubtle
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("btn_volver_registro_repartidor")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Regresar",
                            tint = AcaAzulDark
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = AcaAzulDark
                )
            )
        },
        containerColor = AcaBackground
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(horizontal = 18.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Hero Card de Beneficios
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = AcaAzulDark,
                shadowElevation = 3.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(AcaAzulDark, AcaAzulConfianza)
                            )
                        )
                        .padding(16.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.TwoWheeler,
                                    contentDescription = null,
                                    tint = AcaTurquesaLight,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "Únete a la Red Local de Acapulco",
                                    color = Color.White,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Registro Exprés • Listo en 1 minuto",
                                    color = AcaTurquesaLight,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        Text(
                            text = "Recibe pagos directos de clientes, notificaciones de mandados cercanos y conserva el 100% de tus viajes.",
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = 11.5.sp,
                            lineHeight = 15.sp
                        )
                    }
                }
            }

            // Alerta de error si aplica
            AnimatedVisibility(
                visible = formState.errorMessage != null,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFFEE2E2),
                    border = BorderStroke(1.dp, Color(0xFFEF4444)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ErrorOutline,
                            contentDescription = null,
                            tint = Color(0xFFDC2626),
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = formState.errorMessage ?: "",
                            color = Color(0xFF991B1B),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Formulario en Tarjeta Principal
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "Datos Personales y de Contacto",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = AcaAzulDark
                    )

                    // 1. Campo: Nombre Completo
                    OutlinedTextField(
                        value = formState.nombre,
                        onValueChange = onNombreChange,
                        label = { Text("Nombre completo") },
                        placeholder = { Text("Ej. Carlos Mendoza Santos") },
                        leadingIcon = {
                            Icon(Icons.Default.Person, contentDescription = null, tint = AcaAzulConfianza)
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Words,
                            keyboardType = KeyboardType.Text
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_registro_nombre")
                    )

                    // 2. Campo: Teléfono (10 dígitos)
                    OutlinedTextField(
                        value = formState.telefono,
                        onValueChange = onTelefonoChange,
                        label = { Text("Teléfono de contacto (10 dígitos)") },
                        placeholder = { Text("744 123 4567") },
                        leadingIcon = {
                            Icon(Icons.Default.Phone, contentDescription = null, tint = AcaTurquesaDark)
                        },
                        prefix = {
                            Text("+52 ", fontWeight = FontWeight.Bold, color = AcaAzulDark, fontSize = 13.sp)
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_registro_telefono")
                    )

                    HorizontalDivider(color = Color(0xFFF1F5F9))

                    // 3. Campo: Vehículo de Entrega
                    Text(
                        text = "Vehículo para Realizar Entregas",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = AcaAzulDark
                    )

                    // Selector visual de tipo de transporte
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        tiposTransporte.forEach { (tipoKey, label, icon) ->
                            val isSelected = formState.tipoTransporte == tipoKey
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) AcaAzulConfianza else Color(0xFFF8FAFC),
                                border = BorderStroke(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) AcaAzulConfianza else Color(0xFFE2E8F0)
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { onTipoTransporteChange(tipoKey) }
                                    .testTag("chip_transporte_$tipoKey")
                            ) {
                                Column(
                                    modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = label,
                                        tint = if (isSelected) Color.White else AcaGrisSubtle,
                                        modifier = Modifier.size(22.dp)
                                    )
                                    Text(
                                        text = label,
                                        fontSize = 10.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) Color.White else AcaAzulDark,
                                        textAlign = TextAlign.Center,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }

                    // Modelo del Vehículo
                    OutlinedTextField(
                        value = formState.modeloVehiculo,
                        onValueChange = onModeloVehiculoChange,
                        label = { Text("Marca y modelo") },
                        placeholder = {
                            Text(
                                if (formState.tipoTransporte == "BICI") "Ej. Bicicleta de montaña Alubike"
                                else "Ej. Italika FT150 / Vento Crossmax"
                            )
                        },
                        leadingIcon = {
                            Icon(Icons.Default.DirectionsCar, contentDescription = null, tint = AcaAzulConfianza)
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_registro_modelo_vehiculo")
                    )

                    // Placas (Solo si no es bicicleta)
                    if (formState.tipoTransporte != "BICI") {
                        OutlinedTextField(
                            value = formState.placas,
                            onValueChange = onPlacasChange,
                            label = { Text("Número de placas de circulación") },
                            placeholder = { Text("Ej. GRO-489A") },
                            leadingIcon = {
                                Icon(Icons.Default.Pin, contentDescription = null, tint = AcaTurquesaDark)
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_registro_placas")
                        )
                    }

                    HorizontalDivider(color = Color(0xFFF1F5F9))

                    // 4. Zona Preferida de Acapulco
                    Text(
                        text = "Zona Preferida de Trabajo en Acapulco",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = AcaAzulDark
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        zonasAcapulco.forEach { zona ->
                            val isSelected = formState.zonaAcapulcoPreferida == zona
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) AcaTurquesaContainer else Color(0xFFF8FAFC),
                                border = BorderStroke(
                                    width = 1.dp,
                                    color = if (isSelected) AcaTurquesaDark else Color(0xFFE2E8F0)
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable { onZonaPreferidaChange(zona) }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    RadioButton(
                                        selected = isSelected,
                                        onClick = { onZonaPreferidaChange(zona) },
                                        colors = RadioButtonDefaults.colors(selectedColor = AcaTurquesaDark)
                                    )
                                    Text(
                                        text = zona,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = AcaAzulDark
                                    )
                                }
                            }
                        }
                    }

                    HorizontalDivider(color = Color(0xFFF1F5F9))

                    // 5. Consentimiento de Términos y Políticas de Seguridad
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = formState.terminosAceptados,
                            onCheckedChange = onTerminosChange,
                            colors = CheckboxDefaults.colors(
                                checkedColor = AcaAzulConfianza,
                                checkmarkColor = Color.White
                            ),
                            modifier = Modifier.testTag("checkbox_registro_terminos")
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Acepto el tratamiento seguro de mis datos y las políticas de seguridad de AcaMandados.",
                                fontSize = 11.sp,
                                color = AcaGrisEquilibrio,
                                lineHeight = 14.sp
                            )
                            Text(
                                text = "Ver Políticas y Aviso de Privacidad",
                                color = AcaAzulConfianza,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .clickable { onOpenPoliticas() }
                                    .testTag("btn_registro_ver_politicas")
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // 6. Botón de Enviar Formulario
                    Button(
                        onClick = {
                            focusManager.clearFocus()
                            onSubmit()
                        },
                        enabled = !formState.isLoading,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AcaAzulConfianza,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("btn_completar_registro_repartidor")
                    ) {
                        if (formState.isLoading) {
                            CircularProgressIndicator(
                                color = Color.White,
                                strokeWidth = 2.5.dp,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Guardando perfil...", fontWeight = FontWeight.Bold)
                        } else {
                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Completar Registro y Empezar a Repartir",
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Pie de página informativo
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = null,
                    tint = AcaGrisSubtle,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "AcaMandados • Protección LFPDPPP y Cero Comisiones por Viaje",
                    fontSize = 10.5.sp,
                    color = AcaGrisSubtle
                )
            }
        }
    }
}
