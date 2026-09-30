package com.example.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.AcaTopBar
import com.example.ui.components.CalificarServicioDialog
import com.example.ui.components.LiveTrackingSheet
import com.example.ui.components.NotificationCenterSheet
import com.example.ui.components.OrderCreatedConfirmationDialog
import com.example.ui.components.PinValidationDialog
import com.example.ui.components.ProofOfConditionDialog
import com.example.ui.components.WebTrackingPreviewDialog
import com.example.ui.screens.*
import com.example.ui.theme.*

@Composable
fun MainAppScreen(
    viewModel: MandadosViewModel
) {
    val context = LocalContext.current
    val currentMode by viewModel.currentMode.collectAsStateWithLifecycle()
    val clienteTab by viewModel.clienteTab.collectAsStateWithLifecycle()
    val repartidorTab by viewModel.repartidorTab.collectAsStateWithLifecycle()
    val userMessage by viewModel.userMessage.collectAsStateWithLifecycle()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()

    // Estados de las tres nuevas características
    val driverLoc by viewModel.driverLocationState.collectAsStateWithLifecycle()
    val unreadCount by viewModel.unreadNotificationsCount.collectAsStateWithLifecycle()
    val allNotifications by viewModel.allNotifications.collectAsStateWithLifecycle()
    val isNotificationSheetOpen by viewModel.isNotificationSheetOpen.collectAsStateWithLifecycle()
    val activeOrderForTracking by viewModel.activeOrderForTracking.collectAsStateWithLifecycle()
    val activeOrderForRating by viewModel.activeOrderForRating.collectAsStateWithLifecycle()
    val activePinOrder by viewModel.activePinValidationOrder.collectAsStateWithLifecycle()
    val activeProofConditionOrder by viewModel.activeProofOfConditionOrder.collectAsStateWithLifecycle()
    val activeWebTrackingOrder by viewModel.activeWebTrackingOrder.collectAsStateWithLifecycle()
    val recentlyCreatedOrder by viewModel.recentlyCreatedOrder.collectAsStateWithLifecycle()
    val showCompletedOrders by viewModel.showCompletedOrdersScreen.collectAsStateWithLifecycle()
    val showEditProfile by viewModel.showEditProfileScreen.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }

    // Manejador de permiso de notificaciones push en Android 13+
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { /* Permiso otorgado o denegado */ }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    LaunchedEffect(userMessage) {
        userMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearUserMessage()
        }
    }

    if (currentUser == null) {
        Box(modifier = Modifier.fillMaxSize()) {
            LoginScreen(viewModel = viewModel)
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 24.dp)
            )
        }
        return
    }

    if (showCompletedOrders) {
        HistorialOrdenesScreen(
            viewModel = viewModel,
            onBack = { viewModel.closeCompletedOrdersScreen() },
            modifier = Modifier.fillMaxSize()
        )
        return
    }

    if (showEditProfile) {
        EditarPerfilScreen(
            viewModel = viewModel,
            onBack = { viewModel.closeEditProfile() },
            modifier = Modifier.fillMaxSize()
        )
        return
    }

    Scaffold(
        topBar = {
            AcaTopBar(
                currentMode = currentMode,
                unreadNotificationsCount = unreadCount,
                onOpenNotifications = { viewModel.toggleNotificationSheet(true) },
                onOpenHistorial = { viewModel.openCompletedOrdersScreen() },
                onToggleMode = { viewModel.switchMode(it) }
            )
        },
        bottomBar = {
            AcaBottomNav(
                currentMode = currentMode,
                clienteTab = clienteTab,
                repartidorTab = repartidorTab,
                onSelectClienteTab = { viewModel.setClienteTab(it) },
                onSelectRepartidorTab = { viewModel.setRepartidorTab(it) }
            )
        },
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.padding(bottom = 75.dp)
            )
        },
        contentWindowInsets = WindowInsets.systemBars
    ) { innerPadding ->
        Crossfade(
            targetState = currentMode,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            label = "modeTransition"
        ) { mode ->
            when (mode) {
                AppMode.CLIENTE -> {
                    when (clienteTab) {
                        ClienteTab.INICIO -> ClienteInicioScreen(viewModel = viewModel)
                        ClienteTab.SOLICITAR -> ClienteSolicitarScreen(viewModel = viewModel)
                        ClienteTab.MIS_MANDADOS -> ClienteMisMandadosScreen(viewModel = viewModel)
                        ClienteTab.ZONAS_TARIFAS -> ClienteZonasScreen()
                        ClienteTab.PERFIL -> PerfilScreen(viewModel = viewModel, isDriver = false)
                    }
                }
                AppMode.REPARTIDOR -> {
                    when (repartidorTab) {
                        RepartidorTab.DISPONIBLES -> RepartidorDashboardScreen(viewModel = viewModel)
                        RepartidorTab.MI_RUTA -> RepartidorRutaScreen(viewModel = viewModel)
                        RepartidorTab.BILLETERA_SUSCRIPCION -> RepartidorBilleteraScreen(viewModel = viewModel)
                        RepartidorTab.PERFIL -> PerfilScreen(viewModel = viewModel, isDriver = true)
                    }
                }
            }
        }
    }

    // Modal de Hoja de Notificaciones Push
    if (isNotificationSheetOpen) {
        NotificationCenterSheet(
            notifications = allNotifications,
            onDismiss = { viewModel.toggleNotificationSheet(false) },
            onMarkAsRead = { id -> viewModel.markNotificationAsRead(id) },
            onMarkAllAsRead = { viewModel.markAllNotificationsAsRead() },
            onSelectNotification = { notification ->
                viewModel.markNotificationAsRead(notification.id)
                viewModel.toggleNotificationSheet(false)
                if (notification.tipo == "NUEVO_MANDADO") {
                    viewModel.switchMode(AppMode.REPARTIDOR)
                    viewModel.setRepartidorTab(RepartidorTab.DISPONIBLES)
                } else if (notification.tipo == "CERCA" || notification.tipo == "RECOGIDO") {
                    viewModel.switchMode(AppMode.CLIENTE)
                    viewModel.setClienteTab(ClienteTab.MIS_MANDADOS)
                }
            }
        )
    }

    // Modal de Rastreo en Vivo en Mapa de Acapulco
    activeOrderForTracking?.let { order ->
        LiveTrackingSheet(
            order = order,
            driverLat = driverLoc.lat,
            driverLng = driverLoc.lng,
            driverSpeedKmh = driverLoc.speedKmh,
            driverBearing = driverLoc.bearing,
            distanceRemainingKm = driverLoc.distanceRemainingKm,
            etaMinutes = driverLoc.etaMinutes,
            isDriverView = currentMode == AppMode.REPARTIDOR,
            onDismiss = { viewModel.closeLiveTracking() },
            onOpenWebTracking = { viewModel.openWebTrackingPreview(order) },
            onOpenRecipientPinValidation = {
                viewModel.closeLiveTracking()
                viewModel.openRecipientDevicePinValidation(order)
            }
        )
    }

    // Diálogo de Calificación y Reseñas
    activeOrderForRating?.let { (order, isDriverRatingClient) ->
        CalificarServicioDialog(
            order = order,
            isDriverRatingClient = isDriverRatingClient,
            onDismiss = { viewModel.closeRatingDialog() },
            onSubmitRating = { stars, comment, tags ->
                viewModel.calificarServicio(
                    orderId = order.id,
                    estrellas = stars,
                    comentario = comment,
                    tags = tags
                )
            }
        )
    }

    // Diálogo de Validación de Seguridad mediante PIN (Uber Connect)
    activePinOrder?.let { order ->
        val onRecipientDevice by viewModel.isPinOnRecipientDevice.collectAsStateWithLifecycle()
        PinValidationDialog(
            order = order,
            isOnRecipientDevice = onRecipientDevice,
            onConfirmDelivery = { pin, fotoUri ->
                viewModel.validarPinYCompletarEntrega(
                    orderId = order.id,
                    pinIngresado = pin,
                    fotoEntregaUri = fotoUri,
                    onSuccess = { viewModel.closePinValidation() },
                    onError = { /* Error manejado dentro del flujo o mensaje de usuario */ }
                )
            },
            onDismiss = { viewModel.closePinValidation() }
        )
    }

    // Diálogo de Foto de Condición en Origen (Roadie)
    activeProofConditionOrder?.let { order ->
        ProofOfConditionDialog(
            order = order,
            onPhotoConfirmed = { uri ->
                viewModel.registrarFotoRecoleccion(order.id, uri)
            },
            onDismiss = { viewModel.closeProofOfCondition() }
        )
    }

    // Diálogo de Confirmación con Enlace Único de Rastreo Web al Crear Mandado
    recentlyCreatedOrder?.let { order ->
        OrderCreatedConfirmationDialog(
            order = order,
            onDismiss = { viewModel.dismissRecentlyCreatedOrder() },
            onOpenWebTracking = {
                viewModel.dismissRecentlyCreatedOrder()
                viewModel.openWebTracking(order)
            }
        )
    }

    // Diálogo de Simulación de Rastreo Web Público (Sin App para WhatsApp)
    activeWebTrackingOrder?.let { order ->
        WebTrackingPreviewDialog(
            order = order,
            driverLat = driverLoc.lat,
            driverLng = driverLoc.lng,
            driverSpeedKmh = driverLoc.speedKmh,
            driverBearing = driverLoc.bearing,
            distanceRemainingKm = driverLoc.distanceRemainingKm,
            etaMinutes = driverLoc.etaMinutes,
            onDismiss = { viewModel.closeWebTrackingPreview() }
        )
    }
}

@Composable
private fun AcaBottomNav(
    currentMode: AppMode,
    clienteTab: ClienteTab,
    repartidorTab: RepartidorTab,
    onSelectClienteTab: (ClienteTab) -> Unit,
    onSelectRepartidorTab: (RepartidorTab) -> Unit
) {
    NavigationBar(
        containerColor = Color.White,
        tonalElevation = 8.dp,
        windowInsets = WindowInsets.navigationBars
    ) {
        if (currentMode == AppMode.CLIENTE) {
            NavigationBarItem(
                selected = clienteTab == ClienteTab.INICIO,
                onClick = { onSelectClienteTab(ClienteTab.INICIO) },
                icon = {
                    Icon(
                        if (clienteTab == ClienteTab.INICIO) Icons.Filled.Home else Icons.Outlined.Home,
                        contentDescription = "Inicio"
                    )
                },
                label = { Text("Inicio", fontSize = 10.5.sp) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = AcaAzulConfianza,
                    selectedTextColor = AcaAzulConfianza,
                    indicatorColor = AcaAzulContainer
                ),
                modifier = Modifier.testTag("nav_cliente_inicio")
            )
            NavigationBarItem(
                selected = clienteTab == ClienteTab.SOLICITAR,
                onClick = { onSelectClienteTab(ClienteTab.SOLICITAR) },
                icon = {
                    Icon(
                        if (clienteTab == ClienteTab.SOLICITAR) Icons.Filled.AddCircle else Icons.Outlined.AddCircleOutline,
                        contentDescription = "Solicitar"
                    )
                },
                label = { Text("Solicitar", fontSize = 10.5.sp) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = AcaAzulConfianza,
                    selectedTextColor = AcaAzulConfianza,
                    indicatorColor = AcaAzulContainer
                ),
                modifier = Modifier.testTag("nav_cliente_solicitar")
            )
            NavigationBarItem(
                selected = clienteTab == ClienteTab.MIS_MANDADOS,
                onClick = { onSelectClienteTab(ClienteTab.MIS_MANDADOS) },
                icon = {
                    Icon(
                        if (clienteTab == ClienteTab.MIS_MANDADOS) Icons.Filled.LocalShipping else Icons.Outlined.LocalShipping,
                        contentDescription = "Mis Mandados"
                    )
                },
                label = { Text("Mandados", fontSize = 10.5.sp) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = AcaAzulConfianza,
                    selectedTextColor = AcaAzulConfianza,
                    indicatorColor = AcaAzulContainer
                ),
                modifier = Modifier.testTag("nav_cliente_mandados")
            )
            NavigationBarItem(
                selected = clienteTab == ClienteTab.ZONAS_TARIFAS,
                onClick = { onSelectClienteTab(ClienteTab.ZONAS_TARIFAS) },
                icon = {
                    Icon(
                        if (clienteTab == ClienteTab.ZONAS_TARIFAS) Icons.Filled.Map else Icons.Outlined.Map,
                        contentDescription = "Zonas y Tarifas"
                    )
                },
                label = { Text("Tarifas", fontSize = 10.5.sp) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = AcaAzulConfianza,
                    selectedTextColor = AcaAzulConfianza,
                    indicatorColor = AcaAzulContainer
                ),
                modifier = Modifier.testTag("nav_cliente_tarifas")
            )
            NavigationBarItem(
                selected = clienteTab == ClienteTab.PERFIL,
                onClick = { onSelectClienteTab(ClienteTab.PERFIL) },
                icon = {
                    Icon(
                        if (clienteTab == ClienteTab.PERFIL) Icons.Filled.Person else Icons.Outlined.Person,
                        contentDescription = "Mi Perfil"
                    )
                },
                label = { Text("Perfil", fontSize = 10.5.sp) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = AcaAzulConfianza,
                    selectedTextColor = AcaAzulConfianza,
                    indicatorColor = AcaAzulContainer
                ),
                modifier = Modifier.testTag("nav_cliente_perfil")
            )
        } else {
            NavigationBarItem(
                selected = repartidorTab == RepartidorTab.DISPONIBLES,
                onClick = { onSelectRepartidorTab(RepartidorTab.DISPONIBLES) },
                icon = {
                    Icon(
                        if (repartidorTab == RepartidorTab.DISPONIBLES) Icons.Filled.Dashboard else Icons.Outlined.Dashboard,
                        contentDescription = "Dashboard Repartidor"
                    )
                },
                label = { Text("Dashboard", fontSize = 11.sp) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = AcaTurquesaDark,
                    selectedTextColor = AcaTurquesaDark,
                    indicatorColor = AcaTurquesaContainer
                )
            )
            NavigationBarItem(
                selected = repartidorTab == RepartidorTab.MI_RUTA,
                onClick = { onSelectRepartidorTab(RepartidorTab.MI_RUTA) },
                icon = {
                    Icon(
                        if (repartidorTab == RepartidorTab.MI_RUTA) Icons.Filled.Navigation else Icons.Outlined.Navigation,
                        contentDescription = "Mi Ruta"
                    )
                },
                label = { Text("Mi Ruta", fontSize = 11.sp) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = AcaTurquesaDark,
                    selectedTextColor = AcaTurquesaDark,
                    indicatorColor = AcaTurquesaContainer
                )
            )
            NavigationBarItem(
                selected = repartidorTab == RepartidorTab.BILLETERA_SUSCRIPCION,
                onClick = { onSelectRepartidorTab(RepartidorTab.BILLETERA_SUSCRIPCION) },
                icon = {
                    Icon(
                        if (repartidorTab == RepartidorTab.BILLETERA_SUSCRIPCION) Icons.Filled.Paid else Icons.Outlined.Paid,
                        contentDescription = "Billetera"
                    )
                },
                label = { Text("Billetera", fontSize = 11.sp) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = AcaTurquesaDark,
                    selectedTextColor = AcaTurquesaDark,
                    indicatorColor = AcaTurquesaContainer
                )
            )
            NavigationBarItem(
                selected = repartidorTab == RepartidorTab.PERFIL,
                onClick = { onSelectRepartidorTab(RepartidorTab.PERFIL) },
                icon = {
                    Icon(
                        if (repartidorTab == RepartidorTab.PERFIL) Icons.Filled.Person else Icons.Outlined.Person,
                        contentDescription = "Perfil Repartidor"
                    )
                },
                label = { Text("Perfil", fontSize = 11.sp) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = AcaTurquesaDark,
                    selectedTextColor = AcaTurquesaDark,
                    indicatorColor = AcaTurquesaContainer
                )
            )
        }
    }
}
