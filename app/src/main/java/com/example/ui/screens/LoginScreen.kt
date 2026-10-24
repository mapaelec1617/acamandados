package com.example.ui.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.AppMode
import com.example.ui.MandadosViewModel
import com.example.ui.theme.*
import com.example.util.FirebaseAuthHelper
import kotlinx.coroutines.launch

private enum class AuthTab {
    INICIAR_SESION,
    REGISTRARSE
}

@Composable
fun LoginScreen(
    viewModel: MandadosViewModel,
    modifier: Modifier = Modifier,
    initialRole: AppMode = AppMode.CLIENTE,
    initialTabIsRegister: Boolean = false,
    onBackToInicio: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val coroutineScope = rememberCoroutineScope()

    if (onBackToInicio != null) {
        BackHandler {
            onBackToInicio()
        }
    }

    var selectedRole by remember(initialRole) { mutableStateOf(initialRole) }
    var currentAuthTab by remember(initialTabIsRegister) {
        mutableStateOf(if (initialTabIsRegister) AuthTab.REGISTRARSE else AuthTab.INICIAR_SESION)
    }
    var showEmailForm by remember(initialTabIsRegister) { mutableStateOf(initialTabIsRegister) }

    // Campos de formulario
    var emailInput by remember { mutableStateOf("") }
    var passwordInput by remember { mutableStateOf("") }
    var confirmPasswordInput by remember { mutableStateOf("") }
    var nombreInput by remember { mutableStateOf("") }
    var telefonoInput by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var termsAndDataAccepted by remember { mutableStateOf(false) }

    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }
    var showGoogleAccountDialog by remember { mutableStateOf(false) }
    var showResetPasswordDialog by remember { mutableStateOf(false) }
    var resetEmailInput by remember { mutableStateOf("") }

    val defaultWebClientId = stringResource(id = R.string.default_web_client_id)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        AcaAzulDark,
                        AcaAzulConfianza,
                        Color(0xFF0F172A)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 18.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            // Botón de regreso a Pantalla de Inicio si fue proporcionado
            if (onBackToInicio != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBackToInicio,
                        modifier = Modifier.testTag("btn_volver_a_inicio")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver al inicio",
                            tint = Color.White
                        )
                    }
                    Text(
                        text = "Volver a Inicio de AcaMandados",
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            // Header con Logo y Marca
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 10.dp, bottom = 18.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(22.dp),
                    color = Color.White,
                    shadowElevation = 8.dp,
                    modifier = Modifier.size(76.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.padding(10.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.acamandados_logo),
                            contentDescription = "Logo AcaMandados",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Fit
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Aca",
                        color = Color.White,
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = (-0.5).sp
                    )
                    Text(
                        text = "Mandados",
                        color = AcaTurquesaLight,
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = (-0.5).sp
                    )
                }

                Text(
                    text = "Acapulco • De persona a persona",
                    color = AcaTurquesaContainer,
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Inicia sesión con Firebase Auth y Google Sign-In",
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center
                )
            }

            // Sección 1: Selección de Rol
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "1. SELECCIONA TU TIPO DE CUENTA",
                    color = AcaTurquesaLight,
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.8.sp
                )

                // Tarjeta Opción Cliente
                RoleSelectionCard(
                    title = "Cuenta Cliente",
                    subtitle = "Envía paquetes, encarga súper, comida de la Costera o medicinas con rastreo y PIN de seguridad.",
                    badgeText = "Hacer Envíos y Compras",
                    icon = Icons.Default.ShoppingBag,
                    isSelected = selectedRole == AppMode.CLIENTE,
                    accentColor = AcaTurquesa,
                    onClick = { selectedRole = AppMode.CLIENTE },
                    testTag = "role_cliente_button"
                )

                // Tarjeta Opción Repartidor
                RoleSelectionCard(
                    title = "Cuenta Repartidor",
                    subtitle = "Gana el 100% de la tarifa de cada viaje sin comisiones. Suscripción semanal y cobro diario.",
                    badgeText = "100% Tarifa Directa",
                    icon = Icons.Default.TwoWheeler,
                    isSelected = selectedRole == AppMode.REPARTIDOR,
                    accentColor = Color(0xFF38BDF8),
                    onClick = { selectedRole = AppMode.REPARTIDOR },
                    testTag = "role_repartidor_button"
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Sección 2: Métodos de Autenticación
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "2. ACCESO SEGURO CON FIREBASE",
                    color = AcaTurquesaLight,
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.8.sp
                )

                // Botón Principal: Google Sign-In con Credential Manager y Firebase
                Button(
                    onClick = {
                        isLoading = true
                        errorMessage = null
                        successMessage = null
                        focusManager.clearFocus()
                        coroutineScope.launch {
                            val result = FirebaseAuthHelper.signInWithGoogleCredentialManager(
                                context = context,
                                serverClientId = defaultWebClientId
                            )

                            isLoading = false
                            result.onSuccess { user ->
                                viewModel.loginWithFirebaseUser(user, selectedRole, isGoogle = true)
                            }.onFailure { error ->
                                if (error is com.example.util.GoogleNoCredentialsException ||
                                    error.message?.contains("No credentials", ignoreCase = true) == true
                                ) {
                                    showGoogleAccountDialog = true
                                } else {
                                    errorMessage = FirebaseAuthHelper.getFriendlyAuthErrorMessage(error)
                                }
                            }
                        }
                    },
                    enabled = !isLoading,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = Color(0xFF1F2937)
                    ),
                    shape = RoundedCornerShape(14.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("google_sign_in_button")
                ) {
                    if (isLoading && !showEmailForm) {
                        CircularProgressIndicator(
                            color = AcaAzulConfianza,
                            modifier = Modifier.size(22.dp),
                            strokeWidth = 2.5.dp
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Conectando con Google...",
                            color = Color(0xFF1F2937),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.5.sp
                        )
                    } else {
                        Image(
                            painter = painterResource(id = R.drawable.ic_google_logo),
                            contentDescription = "Logo de Google",
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Continuar con Google",
                            color = Color(0xFF1F2937),
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                }

                // Separador o conmutador a formulario de Correo
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    HorizontalDivider(
                        modifier = Modifier.weight(1f),
                        color = Color.White.copy(alpha = 0.25f)
                    )
                    Text(
                        text = "o con correo electrónico",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 11.5.sp,
                        modifier = Modifier.padding(horizontal = 10.dp)
                    )
                    HorizontalDivider(
                        modifier = Modifier.weight(1f),
                        color = Color.White.copy(alpha = 0.25f)
                    )
                }

                // Tarjeta colapsable / expandible de Correo y Contraseña
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFF1E293B).copy(alpha = 0.95f),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        // Selector Iniciar Sesión / Registrarse
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFF0F172A))
                                .padding(4.dp)
                        ) {
                            TabButton(
                                text = "Iniciar Sesión",
                                isSelected = currentAuthTab == AuthTab.INICIAR_SESION,
                                onClick = {
                                    currentAuthTab = AuthTab.INICIAR_SESION
                                    showEmailForm = true
                                    errorMessage = null
                                },
                                modifier = Modifier.weight(1f)
                            )
                            TabButton(
                                text = "Crear Cuenta",
                                isSelected = currentAuthTab == AuthTab.REGISTRARSE,
                                onClick = {
                                    currentAuthTab = AuthTab.REGISTRARSE
                                    showEmailForm = true
                                    errorMessage = null
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }

                        if (selectedRole == AppMode.REPARTIDOR) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = AcaAzulConfianza.copy(alpha = 0.25f),
                                border = BorderStroke(1.dp, AcaTurquesa.copy(alpha = 0.4f)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable { viewModel.openRegistroRepartidorScreen() }
                                    .testTag("btn_link_registro_simplificado_repartidor")
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.TwoWheeler,
                                        contentDescription = null,
                                        tint = AcaTurquesaLight,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "¿Eres nuevo repartidor?",
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.5.sp
                                        )
                                        Text(
                                            text = "Ir al Registro Simplificado (Nombre, Teléfono y Vehículo)",
                                            color = AcaTurquesaLight,
                                            fontSize = 10.5.sp
                                        )
                                    }
                                    Icon(
                                        imageVector = Icons.Default.ChevronRight,
                                        contentDescription = null,
                                        tint = AcaTurquesaLight,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Campos si está en modo registro
                        if (currentAuthTab == AuthTab.REGISTRARSE) {
                            OutlinedTextField(
                                value = nombreInput,
                                onValueChange = { nombreInput = it },
                                label = { Text("Nombre Completo") },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = null,
                                        tint = AcaTurquesaLight
                                    )
                                },
                                singleLine = true,
                                colors = customTextFieldColors(),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("auth_nombre_input")
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            OutlinedTextField(
                                value = telefonoInput,
                                onValueChange = { telefonoInput = it },
                                label = { Text("Teléfono de Acapulco (10 dígitos)") },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Phone,
                                        contentDescription = null,
                                        tint = AcaTurquesaLight
                                    )
                                },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                singleLine = true,
                                colors = customTextFieldColors(),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("auth_phone_input")
                            )

                            Spacer(modifier = Modifier.height(10.dp))
                        }

                        // Campo Correo Electrónico
                        OutlinedTextField(
                            value = emailInput,
                            onValueChange = {
                                emailInput = it
                                errorMessage = null
                            },
                            label = { Text("Correo Electrónico") },
                            placeholder = { Text("ejemplo@correo.com") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Email,
                                    contentDescription = null,
                                    tint = AcaTurquesaLight
                                )
                            },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Email,
                                imeAction = ImeAction.Next
                            ),
                            singleLine = true,
                            colors = customTextFieldColors(),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("auth_email_input")
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Campo Contraseña
                        OutlinedTextField(
                            value = passwordInput,
                            onValueChange = {
                                passwordInput = it
                                errorMessage = null
                            },
                            label = { Text("Contraseña") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = AcaTurquesaLight
                                )
                            },
                            trailingIcon = {
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(
                                        imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = if (passwordVisible) "Ocultar contraseña" else "Mostrar contraseña",
                                        tint = Color.White.copy(alpha = 0.7f)
                                    )
                                }
                            },
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = if (currentAuthTab == AuthTab.REGISTRARSE) ImeAction.Next else ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                            singleLine = true,
                            colors = customTextFieldColors(),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("auth_password_input")
                        )

                        // Confirmar Contraseña (solo registro)
                        if (currentAuthTab == AuthTab.REGISTRARSE) {
                            Spacer(modifier = Modifier.height(10.dp))

                            OutlinedTextField(
                                value = confirmPasswordInput,
                                onValueChange = {
                                    confirmPasswordInput = it
                                    errorMessage = null
                                },
                                label = { Text("Confirmar Contraseña") },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Key,
                                        contentDescription = null,
                                        tint = AcaTurquesaLight
                                    )
                                },
                                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Password,
                                    imeAction = ImeAction.Done
                                ),
                                keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                                singleLine = true,
                                colors = customTextFieldColors(),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("auth_confirm_password_input")
                            )
                        }

                        // Olvidé mi contraseña (solo inicio de sesión)
                        if (currentAuthTab == AuthTab.INICIAR_SESION) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                TextButton(
                                    onClick = {
                                        resetEmailInput = emailInput.trim()
                                        showResetPasswordDialog = true
                                    },
                                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "¿Olvidaste tu contraseña?",
                                        color = AcaTurquesaLight,
                                        fontSize = 11.5.sp
                                    )
                                }
                            }
                        }

                        if (currentAuthTab == AuthTab.REGISTRARSE) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color.White.copy(alpha = 0.08f),
                                border = BorderStroke(1.dp, if (termsAndDataAccepted) AcaTurquesa else Color.White.copy(alpha = 0.2f)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Checkbox(
                                        checked = termsAndDataAccepted,
                                        onCheckedChange = { termsAndDataAccepted = it },
                                        colors = CheckboxDefaults.colors(
                                            checkedColor = AcaTurquesa,
                                            uncheckedColor = Color.White.copy(alpha = 0.7f),
                                            checkmarkColor = Color(0xFF00382E)
                                        ),
                                        modifier = Modifier.testTag("checkbox_auth_terms")
                                    )
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Autorizo el tratamiento de mis datos personales y acepto las Políticas de Seguridad y Privacidad.",
                                            color = Color.White,
                                            fontSize = 11.sp,
                                            lineHeight = 14.sp
                                        )
                                        Text(
                                            text = "Ver Políticas y Aviso de Privacidad",
                                            color = AcaTurquesaLight,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier
                                                .clickable { viewModel.openPrivacyPolicyScreen() }
                                                .testTag("btn_auth_ver_politicas")
                                        )
                                    }
                                }
                            }
                        } else {
                            Spacer(modifier = Modifier.height(14.dp))
                        }

                        // Botón de Acción Email
                        Button(
                            onClick = {
                                focusManager.clearFocus()
                                val email = emailInput.trim()
                                val password = passwordInput.trim()

                                if (email.isBlank() || !email.contains("@")) {
                                    errorMessage = "Por favor ingresa un correo electrónico válido."
                                    return@Button
                                }
                                if (password.length < 6) {
                                    errorMessage = "La contraseña debe tener al menos 6 caracteres."
                                    return@Button
                                }

                                if (currentAuthTab == AuthTab.REGISTRARSE) {
                                    if (!termsAndDataAccepted) {
                                        errorMessage = "Debes autorizar el tratamiento de tus datos personales y aceptar las políticas de seguridad para registrarte."
                                        return@Button
                                    }
                                    val confirm = confirmPasswordInput.trim()
                                    if (password != confirm) {
                                        errorMessage = "Las contraseñas no coinciden."
                                        return@Button
                                    }
                                    if (nombreInput.isBlank()) {
                                        errorMessage = "Por favor ingresa tu nombre completo."
                                        return@Button
                                    }

                                    isLoading = true
                                    errorMessage = null
                                    viewModel.registerWithEmailAndPassword(
                                        email = email,
                                        pass = password,
                                        displayName = nombreInput.trim(),
                                        phone = telefonoInput.trim(),
                                        role = selectedRole
                                    ) { result ->
                                        isLoading = false
                                        result.onFailure { e ->
                                            errorMessage = FirebaseAuthHelper.getFriendlyAuthErrorMessage(e)
                                        }
                                    }
                                } else {
                                    isLoading = true
                                    errorMessage = null
                                    viewModel.loginWithEmailAndPassword(
                                        email = email,
                                        pass = password,
                                        role = selectedRole
                                    ) { result ->
                                        isLoading = false
                                        result.onFailure { e ->
                                            errorMessage = FirebaseAuthHelper.getFriendlyAuthErrorMessage(e)
                                        }
                                    }
                                }
                            },
                            enabled = !isLoading,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (selectedRole == AppMode.CLIENTE) AcaTurquesa else Color(0xFF0284C7)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("email_auth_submit_button")
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    color = Color.White,
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Procesando en Firebase...", color = Color.White, fontSize = 13.5.sp)
                            } else {
                                Text(
                                    text = if (currentAuthTab == AuthTab.INICIAR_SESION)
                                        "Entrar como ${if (selectedRole == AppMode.CLIENTE) "Cliente" else "Repartidor"}"
                                    else
                                        "Crear Cuenta de ${if (selectedRole == AppMode.CLIENTE) "Cliente" else "Repartidor"}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }

                // Mensajes de Éxito o Error
                AnimatedVisibility(visible = errorMessage != null, enter = fadeIn(), exit = fadeOut()) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0x33EF4444),
                        border = BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = Color(0xFFFCA5A5),
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = errorMessage ?: "",
                                color = Color(0xFFFCA5A5),
                                fontSize = 12.sp,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                AnimatedVisibility(visible = successMessage != null, enter = fadeIn(), exit = fadeOut()) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0x3310B981),
                        border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Color(0xFF6EE7B7),
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = successMessage ?: "",
                                color = Color(0xFF6EE7B7),
                                fontSize = 12.sp,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Acceso Rápido Directo (Cuenta de Google Detectada / Emulador)
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White.copy(alpha = 0.08f),
                    border = BorderStroke(1.dp, AcaTurquesa.copy(alpha = 0.4f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable {
                            viewModel.loginWithGoogleProfile(
                                email = "luisalbertotrevino66@gmail.com",
                                displayName = "Luis Alberto Treviño",
                                role = selectedRole
                            )
                        }
                        .testTag("direct_google_account_chip")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_google_logo),
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Acceso directo: luisalbertotrevino66@gmail.com",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Entrar como ${if (selectedRole == AppMode.CLIENTE) "Cliente" else "Repartidor"}",
                                color = AcaTurquesaLight,
                                fontSize = 11.sp
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = null,
                            tint = AcaTurquesaLight,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                // Botón de acceso de prueba instantáneo
                OutlinedButton(
                    onClick = {
                        viewModel.loginDemoUser(selectedRole)
                    },
                    enabled = !isLoading,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.3f)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .testTag("quick_access_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.FlashOn,
                        contentDescription = null,
                        tint = AcaTurquesaLight,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Acceso Rápido de Prueba (${if (selectedRole == AppMode.CLIENTE) "Cliente" else "Repartidor"})",
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                }

                // Políticas de Seguridad y Privacidad
                TextButton(
                    onClick = { viewModel.openPrivacyPolicyScreen() },
                    modifier = Modifier.testTag("btn_footer_politicas_seguridad")
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        tint = AcaTurquesaLight,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Políticas de Seguridad y Privacidad de Datos",
                        color = AcaTurquesaLight,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // Footer de seguridad
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.5f),
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = "Firebase Auth • Google Credential Manager • Firestore Sync",
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 10.sp
                    )
                }
            }
        }

        // Diálogo para restablecer contraseña
        if (showResetPasswordDialog) {
            AlertDialog(
                onDismissRequest = { showResetPasswordDialog = false },
                icon = {
                    Icon(
                        imageVector = Icons.Default.LockReset,
                        contentDescription = null,
                        tint = AcaAzulConfianza,
                        modifier = Modifier.size(32.dp)
                    )
                },
                title = {
                    Text(
                        text = "Recuperar Contraseña",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.5.sp,
                        textAlign = TextAlign.Center
                    )
                },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "Ingresa tu correo registrado y Firebase te enviará un enlace para restablecer tu contraseña:",
                            fontSize = 12.sp,
                            color = AcaGrisSubtle
                        )

                        OutlinedTextField(
                            value = resetEmailInput,
                            onValueChange = { resetEmailInput = it },
                            label = { Text("Correo Electrónico") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val email = resetEmailInput.trim()
                            if (email.isNotBlank() && email.contains("@")) {
                                showResetPasswordDialog = false
                                viewModel.sendPasswordReset(email) { res ->
                                    res.onSuccess {
                                        successMessage = "Correo de recuperación enviado a $email"
                                    }.onFailure { err ->
                                        errorMessage = FirebaseAuthHelper.getFriendlyAuthErrorMessage(err)
                                    }
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AcaAzulConfianza)
                    ) {
                        Text("Enviar Enlace")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showResetPasswordDialog = false }) {
                        Text("Cancelar")
                    }
                }
            )
        }

        // Diálogo si no hay cuenta registrada en el emulador
        if (showGoogleAccountDialog) {
            AlertDialog(
                onDismissRequest = { showGoogleAccountDialog = false },
                icon = {
                    Image(
                        painter = painterResource(id = R.drawable.ic_google_logo),
                        contentDescription = null,
                        modifier = Modifier.size(32.dp)
                    )
                },
                title = {
                    Text(
                        text = "Iniciar Sesión con Google",
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        textAlign = TextAlign.Center
                    )
                },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "En este dispositivo o emulador no se detectó una cuenta de Google activa en Google Play Services. Puedes ingresar directamente con tu cuenta:",
                            fontSize = 12.5.sp,
                            color = AcaGrisSubtle
                        )

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFF1F5F9),
                            border = BorderStroke(1.dp, AcaTurquesa),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    showGoogleAccountDialog = false
                                    viewModel.loginWithGoogleProfile(
                                        email = "luisalbertotrevino66@gmail.com",
                                        displayName = "Luis Alberto Treviño",
                                        role = selectedRole
                                    )
                                }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(AcaAzulConfianza),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "L",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    )
                                }

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Luis Alberto Treviño",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.5.sp,
                                        color = AcaAzulDark
                                    )
                                    Text(
                                        text = "luisalbertotrevino66@gmail.com",
                                        fontSize = 11.5.sp,
                                        color = AcaGrisSubtle
                                    )
                                }

                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = AcaTurquesaContainer
                                ) {
                                    Text(
                                        text = if (selectedRole == AppMode.CLIENTE) "Cliente" else "Repartidor",
                                        color = AcaOnTurquesaContainer,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showGoogleAccountDialog = false
                            viewModel.loginWithGoogleProfile(
                                email = "luisalbertotrevino66@gmail.com",
                                displayName = "Luis Alberto Treviño",
                                role = selectedRole
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AcaAzulConfianza)
                    ) {
                        Text("Acceder con esta Cuenta")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showGoogleAccountDialog = false }) {
                        Text("Cancelar")
                    }
                }
            )
        }
    }
}

@Composable
private fun TabButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bgColor by animateColorAsState(
        targetValue = if (isSelected) AcaAzulConfianza else Color.Transparent,
        label = "tabBg"
    )
    val textColor by animateColorAsState(
        targetValue = if (isSelected) Color.White else Color.White.copy(alpha = 0.6f),
        label = "tabText"
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = textColor,
            fontSize = 12.5.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
        )
    }
}

@Composable
private fun RoleSelectionCard(
    title: String,
    subtitle: String,
    badgeText: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    accentColor: Color,
    onClick: () -> Unit,
    testTag: String
) {
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) accentColor else Color.White.copy(alpha = 0.15f),
        label = "roleBorder"
    )
    val cardElevation by animateDpAsState(
        targetValue = if (isSelected) 6.dp else 1.dp,
        label = "roleElevation"
    )
    val containerBg by animateColorAsState(
        targetValue = if (isSelected) Color(0xFF1E293B) else Color(0xFF0F172A).copy(alpha = 0.7f),
        label = "roleBg"
    )

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = containerBg,
        border = BorderStroke(if (isSelected) 2.dp else 1.dp, borderColor),
        shadowElevation = cardElevation,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .testTag(testTag)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(if (isSelected) accentColor else Color.White.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isSelected) Color(0xFF0F172A) else Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = title,
                        color = Color.White,
                        fontSize = 14.5.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = accentColor.copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = badgeText,
                            color = accentColor,
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = subtitle,
                    color = Color.White.copy(alpha = 0.75f),
                    fontSize = 11.sp,
                    lineHeight = 15.sp
                )
            }

            RadioButton(
                selected = isSelected,
                onClick = onClick,
                colors = RadioButtonDefaults.colors(
                    selectedColor = accentColor,
                    unselectedColor = Color.White.copy(alpha = 0.4f)
                )
            )
        }
    }
}

@Composable
private fun customTextFieldColors(): TextFieldColors {
    return OutlinedTextFieldDefaults.colors(
        focusedTextColor = Color.White,
        unfocusedTextColor = Color.White,
        focusedContainerColor = Color(0xFF0F172A).copy(alpha = 0.7f),
        unfocusedContainerColor = Color(0xFF0F172A).copy(alpha = 0.4f),
        focusedBorderColor = AcaTurquesaLight,
        unfocusedBorderColor = Color.White.copy(alpha = 0.25f),
        focusedLabelColor = AcaTurquesaLight,
        unfocusedLabelColor = Color.White.copy(alpha = 0.7f),
        cursorColor = AcaTurquesaLight
    )
}
