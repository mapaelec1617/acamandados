package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.ui.AppMode
import com.example.ui.MandadosViewModel
import com.example.ui.theme.*

@Composable
fun PerfilScreen(
    viewModel: MandadosViewModel,
    isDriver: Boolean,
    modifier: Modifier = Modifier
) {
    val profile by viewModel.repartidorProfile.collectAsStateWithLifecycle()
    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()
    val clienteNombre by viewModel.clienteNombreInput.collectAsStateWithLifecycle()
    val clienteTelefono by viewModel.clienteTelefonoInput.collectAsStateWithLifecycle()

    val context = LocalContext.current

    var editNombre by remember(profile) { mutableStateOf(profile?.nombre ?: "Carlos Mendoza") }
    var editTelefono by remember(profile) { mutableStateOf(profile?.telefono ?: "7441234567") }
    var editVehiculo by remember(profile) { mutableStateOf(profile?.tipoTransporte ?: "MOTO") }
    var editModelo by remember(profile) { mutableStateOf(profile?.modeloVehiculo ?: "Italika FT150") }
    var editPlacas by remember(profile) { mutableStateOf(profile?.placas ?: "GRO-489A") }
    var editClabe by remember(profile) { mutableStateOf(profile?.clabeInterbancaria ?: "012180015678901234") }
    var editBanco by remember(profile) { mutableStateOf(profile?.banco ?: "BBVA México") }

    var selectedRoleChoice by remember(userProfile.rol, isDriver) {
        mutableStateOf(if (userProfile.rol == "REPARTIDOR" || isDriver) AppMode.REPARTIDOR else AppMode.CLIENTE)
    }
    var isSavingRole by remember { mutableStateOf(false) }
    var roleSaveSuccessMessage by remember { mutableStateOf<String?>(null) }

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
                    text = if (isDriver) "Perfil de AcaRepartidor" else "Mi Cuenta de Cliente",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = AcaAzulDark
                )
                Text(
                    text = if (isDriver) "Tus datos operativos y cuenta para depósitos del mismo día" else "Datos para tus pedidos y confirmación",
                    fontSize = 11.5.sp,
                    color = AcaGrisSubtle
                )
            }
        }

        // Card de Avatar y Resumen
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
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
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(if (isDriver) AcaTurquesaDark else AcaAzulConfianza),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isDriver) Icons.Default.ElectricScooter else Icons.Default.Person,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(30.dp)
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isDriver) (profile?.nombre ?: "Carlos Mendoza") else clienteNombre,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = AcaAzulDark
                        )
                        Text(
                            text = if (isDriver) "Repartidor Acapulco • ⭐ 4.96" else "Cliente AcaMandados",
                            fontSize = 11.5.sp,
                            color = AcaTurquesaDark,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Tel: ${if (isDriver) (profile?.telefono ?: "7441234567") else clienteTelefono}",
                            fontSize = 11.sp,
                            color = AcaGrisSubtle
                        )
                    }

                    // Botón para alternar modo
                    FilledTonalButton(
                        onClick = {
                            viewModel.switchMode(if (isDriver) AppMode.CLIENTE else AppMode.REPARTIDOR)
                        },
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = AcaSurfaceVariant,
                            contentColor = AcaAzulDark
                        ),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(if (isDriver) "Ir a Cliente" else "Ir a Repartidor", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Card Interactiva: Elección de Rol (Cliente o Repartidor) y Guardado en Firestore
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                border = BorderStroke(1.5.dp, if (selectedRoleChoice == AppMode.CLIENTE) AcaTurquesa else Color(0xFF0284C7)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("card_preferencia_rol_firestore")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
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
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFFE0F2FE)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.SwitchAccount,
                                    contentDescription = null,
                                    tint = Color(0xFF0284C7),
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "Tipo de Cuenta (Rol en AcaMandados)",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.5.sp,
                                    color = AcaAzulDark
                                )
                                Text(
                                    text = "Preferencia sincronizada con Firebase Firestore",
                                    fontSize = 11.sp,
                                    color = Color(0xFF166534),
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (userProfile.rol == "REPARTIDOR") Color(0xFFE0F2FE) else Color(0xFFDCFCE7)
                        ) {
                            Text(
                                text = "En Nube: ${if (userProfile.rol == "REPARTIDOR") "Repartidor" else "Cliente"}",
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (userProfile.rol == "REPARTIDOR") Color(0xFF0369A1) else Color(0xFF166534),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Text(
                        text = "Elige si deseas operar como Cliente (para pedir y encargar mandados) o Repartidor (para tomar viajes y generar ingresos). Tu selección se guarda en Firestore y adapta tu experiencia.",
                        fontSize = 12.sp,
                        color = AcaGrisSubtle,
                        lineHeight = 16.sp
                    )

                    HorizontalDivider(color = Color(0xFFF1F5F9))

                    // Opción 1: Cliente
                    RoleOptionSelector(
                        title = "Cliente AcaMandados",
                        subtitle = "Solicita envíos de paquetería, encarga súper, comida y medicinas en Acapulco con rastreo en vivo y PIN seguro.",
                        badge = "Para Enviar y Comprar",
                        icon = Icons.Default.ShoppingBag,
                        isSelected = selectedRoleChoice == AppMode.CLIENTE,
                        activeColor = AcaTurquesa,
                        onClick = {
                            selectedRoleChoice = AppMode.CLIENTE
                            roleSaveSuccessMessage = null
                        },
                        testTag = "radio_rol_cliente"
                    )

                    // Opción 2: Repartidor
                    RoleOptionSelector(
                        title = "Repartidor AcaMandados",
                        subtitle = "Acepta viajes activos en Costera, Diamante y Renacimiento. Cobro 100% directo de tarifa diaria sin comisiones.",
                        badge = "Para Repartir y Ganar",
                        icon = Icons.Default.TwoWheeler,
                        isSelected = selectedRoleChoice == AppMode.REPARTIDOR,
                        activeColor = Color(0xFF0284C7),
                        onClick = {
                            selectedRoleChoice = AppMode.REPARTIDOR
                            roleSaveSuccessMessage = null
                        },
                        testTag = "radio_rol_repartidor"
                    )

                    // Mensaje de éxito al guardar
                    AnimatedVisibility(
                        visible = roleSaveSuccessMessage != null,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFDCFCE7),
                            border = BorderStroke(1.dp, Color(0xFF86EFAC)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = Color(0xFF166534),
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = roleSaveSuccessMessage ?: "",
                                    fontSize = 12.sp,
                                    color = Color(0xFF166534),
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    // Botón para Guardar Preferencia en Firestore
                    Button(
                        onClick = {
                            isSavingRole = true
                            roleSaveSuccessMessage = null
                            viewModel.cambiarYGuardarRol(selectedRoleChoice) { success ->
                                isSavingRole = false
                                if (success) {
                                    roleSaveSuccessMessage = "✅ Preferencia guardada en Firestore: Ahora eres ${if (selectedRoleChoice == AppMode.CLIENTE) "Cliente" else "Repartidor"}."
                                } else {
                                    Toast.makeText(context, "Rol actualizado localmente", Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        enabled = !isSavingRole,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selectedRoleChoice == AppMode.CLIENTE) AcaTurquesaDark else Color(0xFF0284C7)
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .testTag("btn_guardar_rol_firestore")
                    ) {
                        if (isSavingRole) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Guardando en Firestore...",
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.CloudUpload,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Guardar Preferencia de Rol en Firestore",
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // Card de Información de Contacto & Preferencias de Entrega
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.openEditProfile() }
                    .testTag("btn_abrir_editar_perfil")
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
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFFEFF6FF)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Tune,
                                    contentDescription = null,
                                    tint = AcaAzulConfianza,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "Contacto & Preferencias de Entrega",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.5.sp,
                                    color = AcaAzulDark
                                )
                                Text(
                                    text = "Persistencia activa en Firebase Firestore",
                                    fontSize = 11.sp,
                                    color = Color(0xFF166534),
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Editar",
                            tint = AcaAzulConfianza,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    HorizontalDivider(color = Color(0xFFF1F5F9))

                    // Domicilio frecuente
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("📍 Domicilio:", fontSize = 11.5.sp, color = AcaGrisSubtle)
                        Text(
                            text = "${userProfile.direccionFrecuente.ifBlank { "Costera Miguel Alemán" }} (${userProfile.zonaFrecuente.take(16)})",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = AcaAzulDark
                        )
                    }

                    // Pago preferido
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("💳 Pago preferido:", fontSize = 11.5.sp, color = AcaGrisSubtle)
                        Text(
                            text = if (userProfile.metodoPagoPreferido == "TARJETA") "Tarjeta en App" else "Efectivo al recibir",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = AcaAzulDark
                        )
                    }

                    // PIN
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("🛡️ PIN de 4 dígitos:", fontSize = 11.5.sp, color = AcaGrisSubtle)
                        Text(
                            text = if (userProfile.exigirPinSeguridad) "Requerido en entrega" else "Opcional",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (userProfile.exigirPinSeguridad) Color(0xFF166534) else AcaGrisSubtle
                        )
                    }

                    // Paquete frecuente
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("📦 Tamaño habitual:", fontSize = 11.5.sp, color = AcaGrisSubtle)
                        Text(
                            text = userProfile.tamanoPaquetePreferido,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = AcaAzulDark
                        )
                    }

                    Button(
                        onClick = { viewModel.openEditProfile() },
                        colors = ButtonDefaults.buttonColors(containerColor = AcaAzulConfianza),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(42.dp)
                            .testTag("btn_editar_perfil_accion")
                    ) {
                        Icon(imageVector = Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Editar Contacto y Preferencias", fontSize = 12.5.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Card de Cuenta & Sesión de Firebase
        item {
            val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
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
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = null,
                                tint = AcaAzulConfianza,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "Autenticación & Sesión",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = AcaAzulDark
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (currentUser?.isGoogleAuth == true) Color(0xFFEBF5FF) else AcaSurfaceVariant
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                if (currentUser?.isGoogleAuth == true) {
                                    Image(
                                        painter = painterResource(id = R.drawable.ic_google_logo),
                                        contentDescription = null,
                                        modifier = Modifier.size(13.dp)
                                    )
                                }
                                Text(
                                    text = if (currentUser?.isGoogleAuth == true) "Google Auth" else "Demo Mode",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (currentUser?.isGoogleAuth == true) Color(0xFF1E40AF) else AcaGrisSubtle
                                )
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
                            Text(
                                text = "Cuenta autenticada:",
                                fontSize = 11.sp,
                                color = AcaGrisSubtle
                            )
                            Text(
                                text = currentUser?.email ?: "usuario@acamandados.com",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = AcaAzulDark
                            )
                            Text(
                                text = "UID: ${currentUser?.uid?.take(14) ?: "local_session"}...",
                                fontSize = 10.sp,
                                color = AcaGrisSubtle
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = AcaTurquesaContainer
                        ) {
                            Text(
                                text = if (isDriver) "Rol Repartidor" else "Rol Cliente",
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = AcaOnTurquesaContainer,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    OutlinedButton(
                        onClick = { viewModel.logout() },
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color(0xFFEF4444)
                        ),
                        border = BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(42.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Logout,
                            contentDescription = "Cerrar sesión",
                            tint = Color(0xFFEF4444),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Cerrar Sesión",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.5.sp,
                            color = Color(0xFFEF4444)
                        )
                    }
                }
            }
        }

        if (isDriver) {
            // Formulario de Datos del Repartidor (Vehículo & Cuenta Bancaria)
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
                        Text(
                            text = "Datos de tu Vehículo y Cuenta Bancaria (SPEI)",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = AcaAzulDark
                        )

                        OutlinedTextField(
                            value = editNombre,
                            onValueChange = { editNombre = it },
                            label = { Text("Nombre completo") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = editModelo,
                                onValueChange = { editModelo = it },
                                label = { Text("Vehículo / Modelo") },
                                modifier = Modifier.weight(1.2f),
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp)
                            )
                            OutlinedTextField(
                                value = editPlacas,
                                onValueChange = { editPlacas = it },
                                label = { Text("Placas") },
                                modifier = Modifier.weight(0.8f),
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp)
                            )
                        }

                        HorizontalDivider(color = Color(0xFFE2E8F0))

                        Text(
                            text = "Cuenta para depósitos el mismo día de pagos con tarjeta",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = AcaAzulConfianza
                        )

                        OutlinedTextField(
                            value = editBanco,
                            onValueChange = { editBanco = it },
                            label = { Text("Banco destino (Ej. BBVA, Banamex, STP)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp)
                        )

                        OutlinedTextField(
                            value = editClabe,
                            onValueChange = { editClabe = it },
                            label = { Text("CLABE Interbancaria (18 dígitos)") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp)
                        )

                        Button(
                            onClick = {
                                viewModel.actualizarPerfilRepartidor(
                                    nombre = editNombre,
                                    telefono = editTelefono,
                                    tipoTransporte = editVehiculo,
                                    modelo = editModelo,
                                    placas = editPlacas,
                                    clabe = editClabe,
                                    banco = editBanco
                                )
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = AcaAzulConfianza),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Guardar Cambios", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        } else {
            // Datos del cliente
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
                        Text(
                            text = "Tus Datos de Contacto",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = AcaAzulDark
                        )

                        OutlinedTextField(
                            value = clienteNombre,
                            onValueChange = { viewModel.clienteNombreInput.value = it },
                            label = { Text("Nombre para entregas") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp)
                        )

                        OutlinedTextField(
                            value = clienteTelefono,
                            onValueChange = { viewModel.clienteTelefonoInput.value = it },
                            label = { Text("WhatsApp de contacto") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp)
                        )
                    }
                }
            }
        }

        // Acceso directo a Historial de Mandados Completados
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.openCompletedOrdersScreen() }
                    .testTag("menu_historial_perfil")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFFEEF2FF)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.ReceiptLong,
                                contentDescription = null,
                                tint = AcaAzulConfianza,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Historial de Mandados Completados",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = AcaAzulDark
                            )
                            Text(
                                text = if (isDriver) "Revisa tus entregas, ganancias y recibos" else "Consulta tus compras pasadas y recibos fiscales",
                                fontSize = 11.5.sp,
                                color = AcaGrisSubtle
                            )
                        }
                    }
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = AcaGrisSubtle
                    )
                }
            }
        }

        // Contacto / Soporte AcaMandados
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
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Soporte Local AcaMandados",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "Estamos en Acapulco para apoyarte con cualquier duda en tus mandados o suscripción.",
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 11.5.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Button(
                        onClick = {
                            val uri = Uri.parse("https://api.whatsapp.com/send?phone=5217441234567&text=Hola%20soporte%20AcaMandados")
                            context.startActivity(Intent(Intent.ACTION_VIEW, uri))
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Chat, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Chatear con Soporte por WhatsApp", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Sección de Calificaciones y Reseñas de la Comunidad
        item {
            val allReviews by viewModel.allReviews.collectAsStateWithLifecycle()
            val targetUserName = if (isDriver) (profile?.nombre ?: "Carlos Mendoza") else clienteNombre
            val userReviews = remember(allReviews, targetUserName, isDriver) {
                if (isDriver) {
                    allReviews.filter { it.tipo == "CLIENTE_A_REPARTIDOR" }
                } else {
                    allReviews.filter { it.tipo == "REPARTIDOR_A_CLIENTE" }
                }
            }

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
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Calificaciones y Reseñas",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = AcaAzulDark
                            )
                            Text(
                                text = if (isDriver) "Opiniones de clientes que han pedido contigo" else "Opiniones de repartidores sobre tus entregas",
                                fontSize = 11.sp,
                                color = AcaGrisSubtle
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFFEF3C7)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFD97706), modifier = Modifier.size(16.dp))
                                Text(
                                    text = if (isDriver) "${profile?.calificacion ?: 4.96} ★" else "5.0 ★",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = Color(0xFF92400E)
                                )
                            }
                        }
                    }

                    Divider(color = Color(0xFFE2E8F0))

                    if (userReviews.isEmpty()) {
                        Text(
                            text = "Aún no hay reseñas registradas para este perfil.",
                            fontSize = 12.sp,
                            color = AcaGrisSubtle,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    } else {
                        userReviews.forEach { review ->
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFFF8FAFC),
                                modifier = Modifier.fillMaxWidth()
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
                                        Text(
                                            text = review.autorNombre,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = AcaAzulDark
                                        )

                                        Row {
                                            for (i in 1..review.estrellas) {
                                                Icon(
                                                    imageVector = Icons.Default.Star,
                                                    contentDescription = null,
                                                    tint = Color(0xFFF59E0B),
                                                    modifier = Modifier.size(13.dp)
                                                )
                                            }
                                        }
                                    }

                                    if (review.tags.isNotBlank()) {
                                        Text(
                                            text = review.tags,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = AcaTurquesaDark
                                        )
                                    }

                                    if (review.comentario.isNotBlank()) {
                                        Text(
                                            text = "\"${review.comentario}\"",
                                            fontSize = 11.5.sp,
                                            color = AcaGrisEquilibrio
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RoleOptionSelector(
    title: String,
    subtitle: String,
    badge: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    activeColor: Color,
    onClick: () -> Unit,
    testTag: String
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) activeColor.copy(alpha = 0.08f) else Color(0xFFF8FAFC),
        border = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) activeColor else Color(0xFFE2E8F0)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .testTag(testTag)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            RadioButton(
                selected = isSelected,
                onClick = onClick,
                colors = RadioButtonDefaults.colors(
                    selectedColor = activeColor,
                    unselectedColor = Color(0xFF94A3B8)
                )
            )

            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(if (isSelected) activeColor else Color(0xFFE2E8F0)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isSelected) Color.White else Color(0xFF64748B),
                    modifier = Modifier.size(20.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.5.sp,
                        color = AcaAzulDark
                    )
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = if (isSelected) activeColor.copy(alpha = 0.15f) else Color(0xFFE2E8F0)
                    ) {
                        Text(
                            text = badge,
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) activeColor else Color(0xFF64748B),
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                        )
                    }
                }
                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    color = AcaGrisSubtle,
                    lineHeight = 14.5.sp
                )
            }
        }
    }
}
