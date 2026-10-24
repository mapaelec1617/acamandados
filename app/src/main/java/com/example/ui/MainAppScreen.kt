package com.example.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.data.model.AdminTab
import com.example.ui.components.AcaTopBar
import com.example.ui.components.AutorizacionDatosDialog
import com.example.ui.components.CalificarServicioDialog
import com.example.ui.components.EliminarCuentaDialog
import com.example.ui.components.LiveTrackingSheet
import com.example.ui.components.NewMandadoHeadsUpBanner
import com.example.ui.components.NotificationCenterSheet
import com.example.ui.components.OrderCreatedConfirmationDialog
import com.example.ui.components.PinValidationDialog
import com.example.ui.components.PoliticasSeguridadDialog
import com.example.ui.components.ProofOfConditionDialog
import com.example.ui.components.WebTrackingPreviewDialog
import com.example.ui.navigation.AcaDestinations
import com.example.ui.screens.*
import com.example.ui.theme.*

/**
 * Pantalla raíz de la aplicación implementada con Jetpack Navigation Compose para
 * gestionar las rutas nativamente, garantizar la integridad del botón 'Atrás'
 * de Android y preservar el estado del ViewModel sin pérdidas de datos.
 */
@Composable
fun MainAppScreen(
    viewModel: MandadosViewModel
) {
    val context = LocalContext.current
    val navController = rememberNavController()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val userMessage by viewModel.userMessage.collectAsStateWithLifecycle()

    val showPrivacyPolicy by viewModel.showPrivacyPolicyScreen.collectAsStateWithLifecycle()
    val showRegistroRepartidor by viewModel.showRegistroRepartidorScreen.collectAsStateWithLifecycle()
    val showCompletedOrders by viewModel.showCompletedOrdersScreen.collectAsStateWithLifecycle()
    val showEditProfile by viewModel.showEditProfileScreen.collectAsStateWithLifecycle()
    val showPantallaInicio by viewModel.showPantallaInicio.collectAsStateWithLifecycle()

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

    // Sincronización reactiva con Navigation Compose cuando se abren pantallas desde el ViewModel
    LaunchedEffect(showPrivacyPolicy) {
        if (showPrivacyPolicy && navController.currentDestination?.route != AcaDestinations.PRIVACY_POLICY) {
            navController.navigate(AcaDestinations.PRIVACY_POLICY)
        }
    }
    LaunchedEffect(showRegistroRepartidor) {
        if (showRegistroRepartidor && navController.currentDestination?.route != AcaDestinations.REGISTRO_REPARTIDOR) {
            navController.navigate(AcaDestinations.REGISTRO_REPARTIDOR)
        }
    }
    LaunchedEffect(showCompletedOrders) {
        if (showCompletedOrders && navController.currentDestination?.route != AcaDestinations.HISTORIAL_ORDENES) {
            navController.navigate(AcaDestinations.HISTORIAL_ORDENES)
        }
    }
    LaunchedEffect(showEditProfile) {
        if (showEditProfile && navController.currentDestination?.route != AcaDestinations.EDITAR_PERFIL) {
            navController.navigate(AcaDestinations.EDITAR_PERFIL)
        }
    }
    LaunchedEffect(showPantallaInicio) {
        if (showPantallaInicio && navController.currentDestination?.route != AcaDestinations.INICIO) {
            navController.navigate(AcaDestinations.INICIO)
        }
    }

    // Reaccionar a cambios de sesión (ej. Cierre de sesión o Login)
    LaunchedEffect(currentUser) {
        if (currentUser == null) {
            val currentRoute = navController.currentDestination?.route
            if (currentRoute != null && currentRoute != AcaDestinations.INICIO && currentRoute != AcaDestinations.LOGIN) {
                navController.navigate(AcaDestinations.INICIO) {
                    popUpTo(0) { inclusive = true }
                }
            }
        }
    }

    // Mantener los flags sincronizados cuando el usuario retrocede con el botón atrás nativo
    DisposableEffect(navController) {
        val listener = NavController.OnDestinationChangedListener { _, destination, _ ->
            when (destination.route) {
                AcaDestinations.DASHBOARD -> {
                    viewModel.closePrivacyPolicyScreen()
                    viewModel.closeRegistroRepartidorScreen()
                    viewModel.closeCompletedOrdersScreen()
                    viewModel.closeEditProfile()
                    viewModel.closePantallaInicio()
                }
                AcaDestinations.INICIO -> {
                    viewModel.closePrivacyPolicyScreen()
                    viewModel.closeRegistroRepartidorScreen()
                    viewModel.closeCompletedOrdersScreen()
                    viewModel.closeEditProfile()
                }
            }
        }
        navController.addOnDestinationChangedListener(listener)
        onDispose {
            navController.removeOnDestinationChangedListener(listener)
        }
    }

    val startDestination = if (currentUser == null) AcaDestinations.INICIO else AcaDestinations.DASHBOARD

    Box(modifier = Modifier.fillMaxSize()) {
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.fillMaxSize(),
            enterTransition = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Start,
                    animationSpec = tween(300, easing = FastOutSlowInEasing)
                ) + fadeIn(animationSpec = tween(300))
            },
            exitTransition = {
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Start,
                    animationSpec = tween(300, easing = FastOutSlowInEasing)
                ) + fadeOut(animationSpec = tween(300))
            },
            popEnterTransition = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.End,
                    animationSpec = tween(300, easing = FastOutSlowInEasing)
                ) + fadeIn(animationSpec = tween(300))
            },
            popExitTransition = {
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.End,
                    animationSpec = tween(300, easing = FastOutSlowInEasing)
                ) + fadeOut(animationSpec = tween(300))
            }
        ) {
            composable(AcaDestinations.INICIO) {
                PantallaInicioScreen(
                    viewModel = viewModel,
                    onSelectSolicitarMandado = {
                        navController.navigate(AcaDestinations.LOGIN)
                    },
                    onSelectRegistrarseRepartidor = {
                        navController.navigate(AcaDestinations.REGISTRO_REPARTIDOR)
                    },
                    onIniciarSesion = {
                        navController.navigate(AcaDestinations.LOGIN)
                    },
                    onQuickDemoCliente = {
                        viewModel.loginDemoUser(AppMode.CLIENTE)
                        viewModel.setClienteTab(ClienteTab.SOLICITAR)
                        navController.navigate(AcaDestinations.DASHBOARD) {
                            popUpTo(AcaDestinations.INICIO) { inclusive = false }
                        }
                    },
                    onQuickDemoRepartidor = {
                        viewModel.loginDemoUser(AppMode.REPARTIDOR)
                        viewModel.setRepartidorTab(RepartidorTab.DISPONIBLES)
                        navController.navigate(AcaDestinations.DASHBOARD) {
                            popUpTo(AcaDestinations.INICIO) { inclusive = false }
                        }
                    },
                    onBack = if (currentUser != null) {
                        { navController.popBackStack() }
                    } else null,
                    modifier = Modifier.fillMaxSize()
                )
            }

            composable(AcaDestinations.LOGIN) {
                LoginScreen(
                    viewModel = viewModel,
                    onBackToInicio = {
                        navController.popBackStack()
                    }
                )
            }

            composable(AcaDestinations.REGISTRO_REPARTIDOR) {
                RegistroRepartidorScreen(
                    onRegistroExitoso = { profile ->
                        viewModel.onDriverRegisteredSuccess(profile)
                        navController.navigate(AcaDestinations.DASHBOARD) {
                            popUpTo(AcaDestinations.INICIO) { inclusive = false }
                        }
                    },
                    onBack = {
                        viewModel.closeRegistroRepartidorScreen()
                        navController.popBackStack()
                    },
                    onOpenPoliticas = {
                        navController.navigate(AcaDestinations.PRIVACY_POLICY)
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }

            composable(AcaDestinations.PRIVACY_POLICY) {
                PrivacyPolicyScreen(
                    viewModel = viewModel,
                    onBack = {
                        viewModel.closePrivacyPolicyScreen()
                        navController.popBackStack()
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }

            composable(AcaDestinations.HISTORIAL_ORDENES) {
                HistorialOrdenesScreen(
                    viewModel = viewModel,
                    onBack = {
                        viewModel.closeCompletedOrdersScreen()
                        navController.popBackStack()
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }

            composable(AcaDestinations.EDITAR_PERFIL) {
                EditarPerfilScreen(
                    viewModel = viewModel,
                    onBack = {
                        viewModel.closeEditProfile()
                        navController.popBackStack()
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }

            composable(AcaDestinations.DASHBOARD) {
                DashboardScreen(
                    viewModel = viewModel,
                    navController = navController,
                    snackbarHostState = snackbarHostState,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 24.dp)
        )
    }
}

/**
 * Contenedor del panel principal (Dashboard) para Clientes, Repartidores y Administrador.
 * Gestiona el botón Atrás nativo de Android: primero descarta modales activos y
 * luego devuelve a la pestaña principal antes de salir.
 */
@Composable
fun DashboardScreen(
    viewModel: MandadosViewModel,
    navController: NavHostController,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier
) {
    val currentMode by viewModel.currentMode.collectAsStateWithLifecycle()
    val clienteTab by viewModel.clienteTab.collectAsStateWithLifecycle()
    val repartidorTab by viewModel.repartidorTab.collectAsStateWithLifecycle()
    val adminTab by viewModel.adminTab.collectAsStateWithLifecycle()
    val darkThemeConfig by viewModel.darkThemeConfig.collectAsStateWithLifecycle()
    val newMandadoAlert by viewModel.newMandadoAlert.collectAsStateWithLifecycle()

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
    val showDeleteAccountDialog by viewModel.showDeleteAccountDialog.collectAsStateWithLifecycle()
    val showPoliticasSeguridadDialog by viewModel.showPoliticasSeguridadDialog.collectAsStateWithLifecycle()
    val showAutorizacionDatosDialog by viewModel.showAutorizacionDatosDialog.collectAsStateWithLifecycle()
    val userConsentDataAuth by viewModel.userConsentDataAuth.collectAsStateWithLifecycle()
    val userConsentGpsTracking by viewModel.userConsentGpsTracking.collectAsStateWithLifecycle()
    val userConsentPushNotifications by viewModel.userConsentPushNotifications.collectAsStateWithLifecycle()

    // 1. Manejo del botón atrás para cerrar modales y hojas activas
    val hasActiveModal = isNotificationSheetOpen ||
            activeOrderForTracking != null ||
            activeOrderForRating != null ||
            activePinOrder != null ||
            activeProofConditionOrder != null ||
            recentlyCreatedOrder != null ||
            activeWebTrackingOrder != null ||
            showDeleteAccountDialog ||
            showPoliticasSeguridadDialog ||
            showAutorizacionDatosDialog

    BackHandler(enabled = hasActiveModal) {
        if (isNotificationSheetOpen) {
            viewModel.toggleNotificationSheet(false)
        } else if (activeOrderForTracking != null) {
            viewModel.closeLiveTracking()
        } else if (activeOrderForRating != null) {
            viewModel.closeRatingDialog()
        } else if (activePinOrder != null) {
            viewModel.closePinValidation()
        } else if (activeProofConditionOrder != null) {
            viewModel.closeProofOfCondition()
        } else if (recentlyCreatedOrder != null) {
            viewModel.dismissRecentlyCreatedOrder()
        } else if (activeWebTrackingOrder != null) {
            viewModel.closeWebTrackingPreview()
        } else if (showDeleteAccountDialog) {
            viewModel.closeDeleteAccountDialog()
        } else if (showPoliticasSeguridadDialog) {
            viewModel.closePoliticasSeguridadDialog()
        } else if (showAutorizacionDatosDialog) {
            viewModel.closeAutorizacionDatosDialog()
        }
    }

    // 2. Manejo del botón atrás respetando la pila compartida de pestañas
    val canPopTab = when (currentMode) {
        AppMode.CLIENTE -> viewModel.canPopClienteTab()
        AppMode.REPARTIDOR -> viewModel.canPopRepartidorTab()
        AppMode.ADMIN -> adminTab != AdminTab.RESUMEN_METRICAS
    }

    BackHandler(enabled = !hasActiveModal && canPopTab) {
        when (currentMode) {
            AppMode.CLIENTE -> viewModel.popClienteTab()
            AppMode.REPARTIDOR -> viewModel.popRepartidorTab()
            AppMode.ADMIN -> viewModel.setAdminTab(AdminTab.RESUMEN_METRICAS)
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            AcaTopBar(
                currentMode = currentMode,
                unreadNotificationsCount = unreadCount,
                darkThemeConfig = darkThemeConfig,
                onToggleDarkTheme = { viewModel.toggleDarkTheme() },
                onOpenNotifications = { viewModel.toggleNotificationSheet(true) },
                onOpenHistorial = { navController.navigate(AcaDestinations.HISTORIAL_ORDENES) },
                onOpenInicio = { navController.navigate(AcaDestinations.INICIO) },
                onToggleMode = { viewModel.switchMode(it) }
            )
        },
        bottomBar = {
            AcaBottomNav(
                currentMode = currentMode,
                clienteTab = clienteTab,
                repartidorTab = repartidorTab,
                adminTab = adminTab,
                onSelectClienteTab = { viewModel.setClienteTab(it) },
                onSelectRepartidorTab = { viewModel.setRepartidorTab(it) },
                onSelectAdminTab = { viewModel.setAdminTab(it) }
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Banner Heads-up flotante para avisar de inmediato a repartidores
            NewMandadoHeadsUpBanner(
                alert = newMandadoAlert,
                onAcceptOrView = { orderId ->
                    viewModel.acceptOrViewNewMandadoFromAlert(orderId)
                },
                onDismiss = { viewModel.dismissNewMandadoAlert() }
            )

            Crossfade(
                targetState = currentMode,
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                label = "modeTransition"
            ) { mode ->
                when (mode) {
                    AppMode.CLIENTE -> {
                        AnimatedContent(
                            targetState = clienteTab,
                            transitionSpec = {
                                if (targetState.ordinal > initialState.ordinal) {
                                    (slideInHorizontally(animationSpec = tween(280, easing = FastOutSlowInEasing)) { width -> width / 3 } + fadeIn(tween(280))) togetherWith
                                            (slideOutOfContainer(towards = AnimatedContentTransitionScope.SlideDirection.Start, animationSpec = tween(280, easing = FastOutSlowInEasing)) + fadeOut(tween(280)))
                                } else {
                                    (slideInHorizontally(animationSpec = tween(280, easing = FastOutSlowInEasing)) { width -> -width / 3 } + fadeIn(tween(280))) togetherWith
                                            (slideOutOfContainer(towards = AnimatedContentTransitionScope.SlideDirection.End, animationSpec = tween(280, easing = FastOutSlowInEasing)) + fadeOut(tween(280)))
                                }
                            },
                            label = "clienteTabTransition"
                        ) { tab ->
                            when (tab) {
                                ClienteTab.INICIO -> ClienteInicioScreen(viewModel = viewModel)
                                ClienteTab.SOLICITAR -> ClienteSolicitarScreen(viewModel = viewModel)
                                ClienteTab.MIS_MANDADOS -> ClienteMisMandadosScreen(viewModel = viewModel)
                                ClienteTab.ZONAS_TARIFAS -> ClienteZonasScreen()
                                ClienteTab.PERFIL -> PerfilScreen(viewModel = viewModel, isDriver = false)
                            }
                        }
                    }
                    AppMode.REPARTIDOR -> {
                        AnimatedContent(
                            targetState = repartidorTab,
                            transitionSpec = {
                                if (targetState.ordinal > initialState.ordinal) {
                                    (slideInHorizontally(animationSpec = tween(280, easing = FastOutSlowInEasing)) { width -> width / 3 } + fadeIn(tween(280))) togetherWith
                                            (slideOutOfContainer(towards = AnimatedContentTransitionScope.SlideDirection.Start, animationSpec = tween(280, easing = FastOutSlowInEasing)) + fadeOut(tween(280)))
                                } else {
                                    (slideInHorizontally(animationSpec = tween(280, easing = FastOutSlowInEasing)) { width -> -width / 3 } + fadeIn(tween(280))) togetherWith
                                            (slideOutOfContainer(towards = AnimatedContentTransitionScope.SlideDirection.End, animationSpec = tween(280, easing = FastOutSlowInEasing)) + fadeOut(tween(280)))
                                }
                            },
                            label = "repartidorTabTransition"
                        ) { tab ->
                            when (tab) {
                                RepartidorTab.DISPONIBLES -> RepartidorDashboardScreen(viewModel = viewModel)
                                RepartidorTab.MI_RUTA -> RepartidorRutaScreen(viewModel = viewModel)
                                RepartidorTab.BILLETERA_SUSCRIPCION -> RepartidorBilleteraScreen(viewModel = viewModel)
                                RepartidorTab.PERFIL -> PerfilScreen(viewModel = viewModel, isDriver = true)
                            }
                        }
                    }
                    AppMode.ADMIN -> {
                        AdminDashboardScreen(viewModel = viewModel)
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
                if (notification.tipo == "NUEVA_SOLICITUD" || notification.tipo == "NUEVO_MANDADO") {
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

    // Diálogo de Validación de Seguridad mediante PIN
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

    // Diálogo de Foto de Condición en Origen
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

    // Diálogo de Eliminación de Cuenta y Datos
    if (showDeleteAccountDialog) {
        EliminarCuentaDialog(
            onDismiss = { viewModel.closeDeleteAccountDialog() },
            onConfirmDelete = { viewModel.deleteUserAccount() }
        )
    }

    // Diálogo Integral de Políticas de Seguridad y Privacidad
    if (showPoliticasSeguridadDialog) {
        PoliticasSeguridadDialog(
            onDismiss = { viewModel.closePoliticasSeguridadDialog() },
            onAccept = {
                viewModel.userConsentDataAuth.value = true
                viewModel.closePoliticasSeguridadDialog()
            }
        )
    }

    // Diálogo de Gestión de Autorización y Consentimiento de Datos Personales
    if (showAutorizacionDatosDialog) {
        AutorizacionDatosDialog(
            userConsentDataAuth = userConsentDataAuth,
            onConsentDataAuthChange = { viewModel.userConsentDataAuth.value = it },
            userConsentGpsTracking = userConsentGpsTracking,
            onConsentGpsTrackingChange = { viewModel.userConsentGpsTracking.value = it },
            userConsentPushNotifications = userConsentPushNotifications,
            onConsentPushNotificationsChange = { viewModel.userConsentPushNotifications.value = it },
            onDismiss = { viewModel.closeAutorizacionDatosDialog() },
            onOpenPoliticasFull = {
                viewModel.closeAutorizacionDatosDialog()
                viewModel.openPoliticasSeguridadDialog()
            }
        )
    }
}

@Composable
private fun AcaBottomNav(
    currentMode: AppMode,
    clienteTab: ClienteTab,
    repartidorTab: RepartidorTab,
    adminTab: AdminTab,
    onSelectClienteTab: (ClienteTab) -> Unit,
    onSelectRepartidorTab: (RepartidorTab) -> Unit,
    onSelectAdminTab: (AdminTab) -> Unit
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp,
        windowInsets = WindowInsets.navigationBars
    ) {
        when (currentMode) {
            AppMode.CLIENTE -> {
                NavigationBarItem(
                    selected = clienteTab == ClienteTab.INICIO,
                    onClick = { onSelectClienteTab(ClienteTab.INICIO) },
                    icon = {
                        Icon(
                            if (clienteTab == ClienteTab.INICIO) Icons.Filled.Home else Icons.Outlined.Home,
                            contentDescription = "Inicio"
                        )
                    },
                    label = { Text("Inicio", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) },
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
                    label = { Text("Solicitar", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) },
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
                    label = { Text("Mandados", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) },
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
                    label = { Text("Tarifas", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) },
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
                    label = { Text("Perfil", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = AcaAzulConfianza,
                        selectedTextColor = AcaAzulConfianza,
                        indicatorColor = AcaAzulContainer
                    ),
                    modifier = Modifier.testTag("nav_cliente_perfil")
                )
            }
            AppMode.REPARTIDOR -> {
                NavigationBarItem(
                    selected = repartidorTab == RepartidorTab.DISPONIBLES,
                    onClick = { onSelectRepartidorTab(RepartidorTab.DISPONIBLES) },
                    icon = {
                        Icon(
                            if (repartidorTab == RepartidorTab.DISPONIBLES) Icons.Filled.Dashboard else Icons.Outlined.Dashboard,
                            contentDescription = "Dashboard Repartidor"
                        )
                    },
                    label = { Text("Dashboard", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) },
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
                    label = { Text("Mi Ruta", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) },
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
                    label = { Text("Billetera", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) },
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
                    label = { Text("Perfil", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = AcaTurquesaDark,
                        selectedTextColor = AcaTurquesaDark,
                        indicatorColor = AcaTurquesaContainer
                    )
                )
            }
            AppMode.ADMIN -> {
                NavigationBarItem(
                    selected = adminTab == AdminTab.RESUMEN_METRICAS,
                    onClick = { onSelectAdminTab(AdminTab.RESUMEN_METRICAS) },
                    icon = {
                        Icon(
                            if (adminTab == AdminTab.RESUMEN_METRICAS) Icons.Filled.Assessment else Icons.Outlined.Assessment,
                            contentDescription = "Resumen del Día"
                        )
                    },
                    label = { Text("Resumen", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFFD97706),
                        selectedTextColor = Color(0xFFD97706),
                        indicatorColor = Color(0xFFFEF3C7)
                    ),
                    modifier = Modifier.testTag("nav_admin_resumen")
                )
                NavigationBarItem(
                    selected = adminTab == AdminTab.MANDADOS_ACTIVOS,
                    onClick = { onSelectAdminTab(AdminTab.MANDADOS_ACTIVOS) },
                    icon = {
                        Icon(
                            if (adminTab == AdminTab.MANDADOS_ACTIVOS) Icons.Filled.LocalShipping else Icons.Outlined.LocalShipping,
                            contentDescription = "Mandados Activos"
                        )
                    },
                    label = { Text("Activos", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFFD97706),
                        selectedTextColor = Color(0xFFD97706),
                        indicatorColor = Color(0xFFFEF3C7)
                    ),
                    modifier = Modifier.testTag("nav_admin_mandados")
                )
                NavigationBarItem(
                    selected = adminTab == AdminTab.UBICACION_REPARTIDORES,
                    onClick = { onSelectAdminTab(AdminTab.UBICACION_REPARTIDORES) },
                    icon = {
                        Icon(
                            if (adminTab == AdminTab.UBICACION_REPARTIDORES) Icons.Filled.TwoWheeler else Icons.Outlined.TwoWheeler,
                            contentDescription = "Ubicación Repartidores"
                        )
                    },
                    label = { Text("Repartidores", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFFD97706),
                        selectedTextColor = Color(0xFFD97706),
                        indicatorColor = Color(0xFFFEF3C7)
                    ),
                    modifier = Modifier.testTag("nav_admin_repartidores")
                )
            }
        }
    }
}
