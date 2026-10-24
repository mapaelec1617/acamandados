package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.MandadosViewModel
import com.example.ui.components.EliminarCuentaDialog
import com.example.ui.theme.*

/**
 * Pantalla completa de Aviso de Privacidad y Gestión de Autorización de Datos para AcaMandados.
 * Diseñada específicamente para los requerimientos de recolección y almacenamiento de datos
 * en servicios de mensajería y mandados locales en Acapulco (LFPDPPP y Google Play Data Safety).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacyPolicyScreen(
    viewModel: MandadosViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val userConsentDataAuth by viewModel.userConsentDataAuth.collectAsStateWithLifecycle()
    val userConsentGpsTracking by viewModel.userConsentGpsTracking.collectAsStateWithLifecycle()
    val userConsentPushNotifications by viewModel.userConsentPushNotifications.collectAsStateWithLifecycle()
    val userConsentPhoneMasking by viewModel.userConsentPhoneMasking.collectAsStateWithLifecycle()
    val userConsentPhotoEvidence by viewModel.userConsentPhotoEvidence.collectAsStateWithLifecycle()
    val showDeleteAccountDialog by viewModel.showDeleteAccountDialog.collectAsStateWithLifecycle()

    PrivacyPolicyContent(
        userConsentDataAuth = userConsentDataAuth,
        userConsentGpsTracking = userConsentGpsTracking,
        userConsentPushNotifications = userConsentPushNotifications,
        userConsentPhoneMasking = userConsentPhoneMasking,
        userConsentPhotoEvidence = userConsentPhotoEvidence,
        onSaveAuthorizations = { general, gps, notifs, phoneMask, photos ->
            viewModel.updateDataAuthorizations(
                generalAuth = general,
                gpsTracking = gps,
                pushNotifications = notifs,
                phoneMasking = phoneMask,
                photoEvidence = photos
            )
        },
        onRequestDeleteAccount = { viewModel.openDeleteAccountDialog() },
        onBack = onBack,
        modifier = modifier
    )

    if (showDeleteAccountDialog) {
        EliminarCuentaDialog(
            onConfirmDelete = { viewModel.deleteUserAccount() },
            onDismiss = { viewModel.closeDeleteAccountDialog() }
        )
    }
}

/**
 * Componente base reutilizable y testeable para la pantalla de Aviso de Privacidad.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacyPolicyContent(
    userConsentDataAuth: Boolean,
    userConsentGpsTracking: Boolean,
    userConsentPushNotifications: Boolean,
    userConsentPhoneMasking: Boolean = true,
    userConsentPhotoEvidence: Boolean = true,
    onSaveAuthorizations: (Boolean, Boolean, Boolean, Boolean, Boolean) -> Unit = { _, _, _, _, _ -> },
    onRequestDeleteAccount: () -> Unit = {},
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onBack)

    var selectedTab by remember { mutableIntStateOf(0) }
    val tabTitles = listOf("Autorizaciones", "Recolección", "Almacenamiento", "Derechos ARCO")

    // Estados locales para los conmutadores de autorización interactiva
    var localGeneralConsent by remember(userConsentDataAuth) { mutableStateOf(userConsentDataAuth) }
    var localGpsConsent by remember(userConsentGpsTracking) { mutableStateOf(userConsentGpsTracking) }
    var localNotifConsent by remember(userConsentPushNotifications) { mutableStateOf(userConsentPushNotifications) }
    var localPhoneMaskConsent by remember(userConsentPhoneMasking) { mutableStateOf(userConsentPhoneMasking) }
    var localPhotoConsent by remember(userConsentPhotoEvidence) { mutableStateOf(userConsentPhotoEvidence) }

    var showSavedBanner by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("privacy_policy_screen"),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Privacidad y Autorización",
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = AcaAzulDark
                        )
                        Text(
                            text = "AcaMandados • Protección de Datos",
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
                            .testTag("btn_back_privacy_policy")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Regresar",
                            tint = AcaAzulDark
                        )
                    }
                },
                actions = {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = AcaTurquesaContainer,
                        modifier = Modifier.padding(end = 12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.VerifiedUser,
                                contentDescription = null,
                                tint = AcaOnTurquesaContainer,
                                modifier = Modifier.size(13.dp)
                            )
                            Text(
                                text = "LFPDPPP",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = AcaOnTurquesaContainer
                            )
                        }
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Banner Hero Informativo
            item {
                PrivacyHeroHeader()
            }

            // Selector de Pestañas Interactivas
            item {
                PrimaryScrollableTabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color.Transparent,
                    contentColor = AcaAzulConfianza,
                    edgePadding = 0.dp,
                    divider = {}
                ) {
                    tabTitles.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = {
                                Text(
                                    text = title,
                                    fontSize = 12.5.sp,
                                    fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Medium
                                )
                            },
                            modifier = Modifier.testTag("tab_privacy_$index")
                        )
                    }
                }
            }

            // Alerta de confirmación al guardar autorizaciones
            item {
                AnimatedVisibility(
                    visible = showSavedBanner,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFD1FAE5),
                        border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Color(0xFF047857),
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "Tus autorizaciones de datos han sido guardadas y actualizadas conforme a la normativa.",
                                fontSize = 12.sp,
                                color = Color(0xFF065F46),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            // Contenido dinámico según la pestaña seleccionada
            when (selectedTab) {
                0 -> {
                    // Pestaña 0: Autorizaciones y Consentimientos Expresos
                    item {
                        DataAuthorizationsSection(
                            generalConsent = localGeneralConsent,
                            onGeneralChange = { localGeneralConsent = it },
                            gpsConsent = localGpsConsent,
                            onGpsChange = { localGpsConsent = it },
                            notifConsent = localNotifConsent,
                            onNotifChange = { localNotifConsent = it },
                            phoneMaskConsent = localPhoneMaskConsent,
                            onPhoneMaskChange = { localPhoneMaskConsent = it },
                            photoConsent = localPhotoConsent,
                            onPhotoChange = { localPhotoConsent = it },
                            onSaveClick = {
                                onSaveAuthorizations(
                                    localGeneralConsent,
                                    localGpsConsent,
                                    localNotifConsent,
                                    localPhoneMaskConsent,
                                    localPhotoConsent
                                )
                                showSavedBanner = true
                            },
                            onResetDefaults = {
                                localGeneralConsent = true
                                localGpsConsent = true
                                localNotifConsent = true
                                localPhoneMaskConsent = true
                                localPhotoConsent = true
                                onSaveAuthorizations(true, true, true, true, true)
                                showSavedBanner = true
                            }
                        )
                    }
                }
                1 -> {
                    // Pestaña 1: Recolección Detallada de Datos
                    item {
                        DataCollectionTermsSection()
                    }
                }
                2 -> {
                    // Pestaña 2: Almacenamiento y Seguridad
                    item {
                        DataStorageSecuritySection()
                    }
                }
                3 -> {
                    // Pestaña 3: Derechos ARCO y Eliminación de Datos
                    item {
                        ArcoRightsAndDeletionSection(
                            onRequestDeleteAccount = onRequestDeleteAccount
                        )
                    }
                }
            }

            // Pie de página de cumplimiento legal
            item {
                PrivacyFooterCompliance()
            }
        }
    }
}

/**
 * Cabecera hero que resalta el compromiso con la privacidad en Acapulco.
 */
@Composable
private fun PrivacyHeroHeader() {
    Surface(
        shape = RoundedCornerShape(18.dp),
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
                .padding(18.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = AcaTurquesaLight,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "Aviso de Privacidad Integral",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "AcaMandados Express • Acapulco, Gro.",
                            color = AcaTurquesaLight,
                            fontSize = 11.5.sp
                        )
                    }
                }

                Text(
                    text = "Tu privacidad es indispensable. Consulta cómo recolectamos, protegemos y almacenamos tus datos en cada viaje de mandado de persona a persona.",
                    color = Color.White.copy(alpha = 0.88f),
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )
            }
        }
    }
}

/**
 * Sección interactiva de autorización y gestión de consentimientos.
 */
@Composable
private fun DataAuthorizationsSection(
    generalConsent: Boolean,
    onGeneralChange: (Boolean) -> Unit,
    gpsConsent: Boolean,
    onGpsChange: (Boolean) -> Unit,
    notifConsent: Boolean,
    onNotifChange: (Boolean) -> Unit,
    phoneMaskConsent: Boolean,
    onPhoneMaskChange: (Boolean) -> Unit,
    photoConsent: Boolean,
    onPhotoChange: (Boolean) -> Unit,
    onSaveClick: () -> Unit,
    onResetDefaults: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("card_data_authorizations")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Autorización Expresa de Datos",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = AcaAzulDark
                    )
                    Text(
                        text = "Controla los permisos activos para tu cuenta",
                        fontSize = 11.sp,
                        color = AcaGrisSubtle
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (generalConsent && gpsConsent) Color(0xFFDCFCE7) else Color(0xFFFEF3C7)
                ) {
                    Text(
                        text = if (generalConsent && gpsConsent) "Conforme" else "Parcial",
                        color = if (generalConsent && gpsConsent) Color(0xFF15803D) else Color(0xFFB45309),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            HorizontalDivider(color = Color(0xFFF1F5F9))

            // 1. Tratamiento General
            AuthorizationItemRow(
                icon = Icons.Default.PrivacyTip,
                title = "Tratamiento Operativo de Mandados",
                subtitle = "Requerido para generar pedidos, enlazar clientes con repartidores en Acapulco y calcular costos.",
                badge = "Servicio Principal",
                badgeColor = AcaAzulConfianza,
                isChecked = generalConsent,
                onCheckedChange = onGeneralChange,
                testTag = "switch_auth_general"
            )

            // 2. Geolocalización en Tiempo Real
            AuthorizationItemRow(
                icon = Icons.Default.LocationOn,
                title = "Geolocalización en Tiempo Real (GPS)",
                subtitle = "Rastreo activo de la ruta en Costera, Diamante y colonias de Acapulco únicamente mientras la orden esté en curso.",
                badge = "Rastreo en Vivo",
                badgeColor = AcaTurquesaDark,
                isChecked = gpsConsent,
                onCheckedChange = onGpsChange,
                testTag = "switch_auth_gps"
            )

            // 3. Notificaciones Push
            AuthorizationItemRow(
                icon = Icons.Default.Notifications,
                title = "Notificaciones y Alertas de Despacho",
                subtitle = "Avisos de llegada del repartidor, confirmación del PIN y avisos de seguridad en Acapulco.",
                badge = "Alertas Operativas",
                badgeColor = Color(0xFF7C3AED),
                isChecked = notifConsent,
                onCheckedChange = onNotifChange,
                testTag = "switch_auth_notifications"
            )

            // 4. Enmascaramiento de Teléfono
            AuthorizationItemRow(
                icon = Icons.Default.Phone,
                title = "Enmascaramiento de Contacto Telefónico",
                subtitle = "Oculta el número directo entre cliente y repartidor tras finalizar el servicio de entrega.",
                badge = "Antifraude",
                badgeColor = Color(0xFF0284C7),
                isChecked = phoneMaskConsent,
                onCheckedChange = onPhoneMaskChange,
                testTag = "switch_auth_phone_mask"
            )

            // 5. Evidencia Fotográfica
            AuthorizationItemRow(
                icon = Icons.Default.CameraAlt,
                title = "Evidencia Fotográfica de Entrega",
                subtitle = "Autorización para adjuntar foto del paquete o recibo de compra para constatar condición y entrega.",
                badge = "Garantía de Entrega",
                badgeColor = Color(0xFF059669),
                isChecked = photoConsent,
                onCheckedChange = onPhotoChange,
                testTag = "switch_auth_photo_proof"
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Botón Principal de Guardado
            Button(
                onClick = onSaveClick,
                colors = ButtonDefaults.buttonColors(containerColor = AcaAzulConfianza),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("btn_save_authorizations")
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Guardar y Confirmar Autorizaciones",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.5.sp
                )
            }

            // Botón Secundario de Valores Predeterminados
            OutlinedButton(
                onClick = onResetDefaults,
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .testTag("btn_reset_authorizations")
            ) {
                Text(
                    text = "Restablecer Autorizaciones Recomendadas",
                    fontSize = 12.sp,
                    color = AcaGrisSubtle,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

/**
 * Fila individual para un permiso o consentimiento interactivo.
 */
@Composable
private fun AuthorizationItemRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    badge: String,
    badgeColor: Color,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    testTag: String
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFFF8FAFC),
        border = BorderStroke(1.dp, if (isChecked) badgeColor.copy(alpha = 0.35f) else Color(0xFFE2E8F0)),
        modifier = Modifier.fillMaxWidth()
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
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(badgeColor.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = badgeColor,
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
                        fontSize = 12.5.sp,
                        color = AcaAzulDark
                    )
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = badgeColor.copy(alpha = 0.1f)
                    ) {
                        Text(
                            text = badge,
                            fontSize = 8.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = badgeColor,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    fontSize = 10.5.sp,
                    color = AcaGrisSubtle,
                    lineHeight = 14.sp
                )
            }

            Switch(
                checked = isChecked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = badgeColor,
                    uncheckedThumbColor = Color.White,
                    uncheckedTrackColor = Color(0xFFCBD5E1)
                ),
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .testTag(testTag)
            )
        }
    }
}

/**
 * Sección descriptiva sobre los tipos de datos que se recolectan en AcaMandados.
 */
@Composable
private fun DataCollectionTermsSection() {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        DetailCard(
            title = "1. Información de Identidad y Contacto",
            icon = Icons.Default.Person,
            badge = "Cuenta",
            color = AcaAzulConfianza,
            paragraphs = listOf(
                "• Nombre completo o alias verificado para identificación en la entrega.",
                "• Correo electrónico y credencial Firebase UID para gestión segura de sesión con Google Credential Manager.",
                "• Número de teléfono de 10 dígitos de Acapulco (744) para la coordinación directa del mandado.",
                "• Para repartidores: Tipo de vehículo (motocicleta, motoneta, automóvil), placas y CLABE interbancaria para depósitos bancarios directos del 100% de la tarifa sin comisiones."
            )
        )

        DetailCard(
            title = "2. Geolocalización y Rutas en Acapulco",
            icon = Icons.Default.LocationOn,
            badge = "Ubicación en Vivo",
            color = AcaTurquesaDark,
            paragraphs = listOf(
                "• Coordenadas precisas (GPS) capturadas exclusivamente mientras un viaje esté en estado 'En Camino' o 'Comprando'.",
                "• Cobertura en corredores clave de Acapulco: Costera Miguel Alemán, Fraccionamiento Las Playas, Zona Diamante, Renacimiento, Emiliano Zapata, Progreso, Farallón y Costa Azul.",
                "• El rastreo se suspende de inmediato cuando el pedido es finalizado con el PIN de entrega."
            )
        )

        DetailCard(
            title = "3. Contenido del Mandado y Comprobantes",
            icon = Icons.Default.ReceiptLong,
            badge = "Operativo",
            color = Color(0xFF0284C7),
            paragraphs = listOf(
                "• Descripción del paquete, artículos de farmacia, súper o comida solicitada.",
                "• Método de pago seleccionado (Efectivo, Tarjeta con Stripe, SPEI/CoDi o Saldo de Billetera).",
                "• Fotografías de 'Proof of Condition' que documentan el estado físico de los paquetes entregados."
            )
        )
    }
}

/**
 * Sección que describe el almacenamiento y las medidas de seguridad.
 */
@Composable
private fun DataStorageSecuritySection() {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        DetailCard(
            title = "Almacenamiento en Google Cloud Firestore",
            icon = Icons.Default.Storage,
            badge = "Cifrado AES-256",
            color = AcaAzulConfianza,
            paragraphs = listOf(
                "• Los perfiles, mandados y registros de saldo se almacenan en instancias seguras de Google Cloud Firestore.",
                "• Cifrado en tránsito mediante TLS 1.3 y cifrado en reposo con el estándar militar AES-256.",
                "• Reglas de seguridad estrictas que impiden que terceros no autenticados o ajenos al viaje consulten las coordenadas o los datos personales."
            )
        )

        DetailCard(
            title = "Política de Cero Venta de Datos",
            icon = Icons.Default.Gavel,
            badge = "Privacidad Estricta",
            color = Color(0xFF059669),
            paragraphs = listOf(
                "• AcaMandados no vende, renta ni comercializa bases de datos con agencias de publicidad externas.",
                "• La información se procesa única y exclusivamente con el fin de operar la paquetería de persona a persona en Acapulco.",
                "• Los repartidores únicamente tienen acceso a la dirección y teléfono durante el tiempo que dure el viaje."
            )
        )

        DetailCard(
            title = "Retención y Conservación de Datos",
            icon = Icons.Default.Lock,
            badge = "Temporalidad",
            color = Color(0xFF7C3AED),
            paragraphs = listOf(
                "• Las coordenadas en vivo se descartan del mapa al concluir la entrega.",
                "• El historial de recibos se preserva con fines contables y de aclaración para el usuario y repartidor.",
                "• Si solicitas la eliminación de tu cuenta, tu perfil y datos sensibles se borran permanentemente en segundos."
            )
        )
    }
}

/**
 * Sección de Derechos ARCO y botón de eliminación de cuenta.
 */
@Composable
private fun ArcoRightsAndDeletionSection(
    onRequestDeleteAccount: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
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
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Ejercicio de Derechos ARCO (LFPDPPP)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.5.sp,
                    color = AcaAzulDark
                )
                Text(
                    text = "Como titular de tus datos personales, tienes derecho a ejercer en cualquier momento:",
                    fontSize = 11.5.sp,
                    color = AcaGrisSubtle
                )

                ArcoItem(
                    letter = "A",
                    title = "Acceso",
                    desc = "Conocer qué datos tenemos tuyos, su origen y el estado de tus viajes."
                )
                ArcoItem(
                    letter = "R",
                    title = "Rectificación",
                    desc = "Actualizar en cualquier momento tu nombre, teléfono, vehículo o CLABE desde tu Perfil."
                )
                ArcoItem(
                    letter = "C",
                    title = "Cancelación",
                    desc = "Solicitar el cese en el tratamiento de tus datos y la eliminación total de tu registro."
                )
                ArcoItem(
                    letter = "O",
                    title = "Oposición",
                    desc = "Negarte al uso de tus datos para finalidades no indispensables (notificaciones promocionales)."
                )
            }
        }

        // Card para Eliminación de Cuenta conforme a Google Play
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2)),
            border = BorderStroke(1.dp, Color(0xFFFCA5A5)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteForever,
                        contentDescription = null,
                        tint = Color(0xFFDC2626),
                        modifier = Modifier.size(22.dp)
                    )
                    Text(
                        text = "Eliminación de Cuenta y Olvido de Datos",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color(0xFF991B1B)
                    )
                }

                Text(
                    text = "Cumpliendo con las políticas de Google Play Data Safety, puedes solicitar la eliminación inmediata de tu perfil, historial personal y credenciales de autenticación.",
                    fontSize = 11.5.sp,
                    color = Color(0xFF7F1D1D),
                    lineHeight = 15.sp
                )

                Button(
                    onClick = onRequestDeleteAccount,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFDC2626),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .testTag("btn_delete_account_policy")
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteForever,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Solicitar Eliminación de Mi Cuenta y Datos",
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

/**
 * Fila visual para cada letra de los derechos ARCO.
 */
@Composable
private fun ArcoItem(
    letter: String,
    title: String,
    desc: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(AcaAzulConfianza),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = letter,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 12.5.sp,
                color = AcaAzulDark
            )
            Text(
                text = desc,
                fontSize = 11.sp,
                color = AcaGrisSubtle
            )
        }
    }
}

/**
 * Tarjeta genérica para desplegar bloques explicativos del aviso de privacidad.
 */
@Composable
private fun DetailCard(
    title: String,
    icon: ImageVector,
    badge: String,
    color: Color,
    paragraphs: List<String>
) {
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
            verticalArrangement = Arrangement.spacedBy(8.dp)
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
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(color.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = color,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Text(
                        text = title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.5.sp,
                        color = AcaAzulDark
                    )
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = color.copy(alpha = 0.1f)
                ) {
                    Text(
                        text = badge,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = color,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            HorizontalDivider(color = Color(0xFFF1F5F9))

            paragraphs.forEach { paragraph ->
                Text(
                    text = paragraph,
                    fontSize = 11.5.sp,
                    color = AcaGrisEquilibrio,
                    lineHeight = 16.sp
                )
            }
        }
    }
}

/**
 * Pie de página que ratifica la vigencia y datos de contacto de privacidad.
 */
@Composable
private fun PrivacyFooterCompliance() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = null,
                tint = AcaGrisSubtle,
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = "Última actualización: Octubre 2026 • Versión 2.4",
                fontSize = 10.5.sp,
                color = AcaGrisSubtle
            )
        }
        Text(
            text = "AcaMandados Express Acapulco • Contacto: privacidad@acamandados.com",
            fontSize = 10.sp,
            color = AcaGrisSubtle,
            textAlign = TextAlign.Center
        )
    }
}
