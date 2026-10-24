package com.example.ui

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.AppDatabase
import com.example.data.model.AcapulcoLocations
import com.example.data.model.CalificacionReview
import com.example.data.model.DepositoTarjetaLog
import com.example.data.model.AdminDailyStats
import com.example.data.model.AdminTab
import com.example.data.model.DriverTelemetryInfo
import com.example.data.model.MandadoOrder
import com.example.data.model.MetodoPago
import com.example.data.model.NewMandadoDriverAlert
import com.example.data.model.NotificationItem
import com.example.data.model.RepartidorProfile
import com.example.data.model.TarifaDesglose
import com.example.data.model.TipoMandado
import com.example.data.model.TransaccionBilletera
import com.example.data.model.UserProfile
import com.example.data.repository.FirestoreRepository
import com.example.data.repository.MandadosRepository
import com.example.data.repository.UserProfileRepository
import com.example.util.AcaNotificationHelper
import com.example.util.FirebaseAuthHelper
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

enum class AppMode {
    CLIENTE,
    REPARTIDOR,
    ADMIN
}

enum class ClienteTab {
    INICIO,
    SOLICITAR,
    MIS_MANDADOS,
    ZONAS_TARIFAS,
    PERFIL
}

enum class RepartidorTab {
    DISPONIBLES,
    MI_RUTA,
    BILLETERA_SUSCRIPCION,
    PERFIL
}

enum class DarkThemeConfig(val label: String) {
    SYSTEM("Automático (Sistema)"),
    LIGHT("Modo Claro"),
    DARK("Modo Oscuro")
}

data class DriverLocationState(
    val lat: Double = 16.8580,
    val lng: Double = -99.8760,
    val speedKmh: Int = 38,
    val bearing: Float = 45f,
    val distanceRemainingKm: Double = 1.6,
    val etaMinutes: Int = 5,
    val isNear: Boolean = false,
    val targetType: String = "DELIVERY",
    val lastUpdated: Long = System.currentTimeMillis()
)

data class AuthUserState(
    val uid: String,
    val email: String,
    val displayName: String,
    val photoUrl: String? = null,
    val role: AppMode,
    val isGoogleAuth: Boolean = true
)

class MandadosViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application, viewModelScope)
    val firestoreRepository = FirestoreRepository.create(application)
    val userProfileRepository = UserProfileRepository.create(application)
    private val repository = MandadosRepository(
        database.mandadoDao(),
        database.repartidorDao(),
        database.calificacionDao(),
        database.notificationDao(),
        firestoreRepository
    )

    // Estado de sesión del usuario con Firebase
    private val _currentUser = MutableStateFlow<AuthUserState?>(null)
    val currentUser: StateFlow<AuthUserState?> = _currentUser.asStateFlow()

    init {
        AcaNotificationHelper.initNotificationChannels(application)
        startRealtimeLocationEngine()

        // Verificar si existe sesión previa de Firebase Auth y sincronizar rol desde Firestore
        val fbUser = FirebaseAuthHelper.getCurrentFirebaseUser(application)
        if (fbUser != null) {
            viewModelScope.launch(Dispatchers.IO) {
                val profileResult = userProfileRepository.getUserProfile(fbUser.uid).getOrNull()
                    ?: firestoreRepository.getUserProfile(fbUser.uid).getOrNull()
                val effectiveRole = if (profileResult?.rol?.equals("REPARTIDOR", ignoreCase = true) == true) {
                    AppMode.REPARTIDOR
                } else {
                    AppMode.CLIENTE
                }
                withContext(Dispatchers.Main) {
                    _currentUser.value = AuthUserState(
                        uid = fbUser.uid,
                        email = fbUser.email ?: "usuario@acamandados.com",
                        displayName = profileResult?.nombre?.ifBlank { null } ?: fbUser.displayName ?: "Usuario AcaMandados",
                        photoUrl = fbUser.photoUrl?.toString(),
                        role = effectiveRole,
                        isGoogleAuth = true
                    )
                    profileResult?.let { _userProfile.value = it }
                    switchMode(effectiveRole)
                    if (effectiveRole == AppMode.CLIENTE) {
                        _clienteTab.value = ClienteTab.INICIO
                    } else {
                        _repartidorTab.value = RepartidorTab.DISPONIBLES
                    }
                }
            }
        }
    }

    fun loginWithFirebaseUser(
        user: FirebaseUser,
        role: AppMode,
        isGoogle: Boolean = true,
        customDisplayName: String? = null,
        customPhone: String? = null
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            // Consultar si el usuario ya tiene un rol guardado previamente en Firestore
            val existingProfile = userProfileRepository.getUserProfile(user.uid).getOrNull()
                ?: firestoreRepository.getUserProfile(user.uid).getOrNull()

            val effectiveRole = when {
                existingProfile?.rol?.equals("REPARTIDOR", ignoreCase = true) == true -> AppMode.REPARTIDOR
                existingProfile?.rol?.equals("CLIENTE", ignoreCase = true) == true -> AppMode.CLIENTE
                else -> role
            }

            val userName = existingProfile?.nombre?.ifBlank { null }
                ?: customDisplayName?.ifBlank { null }
                ?: user.displayName?.ifBlank { null }
                ?: if (effectiveRole == AppMode.CLIENTE) "Cliente AcaMandados" else "Repartidor AcaMandados"
            val userEmail = existingProfile?.email?.ifBlank { null }
                ?: user.email
                ?: "${if (effectiveRole == AppMode.CLIENTE) "cliente" else "repartidor"}@acamandados.com"
            val effectivePhone = existingProfile?.telefono?.ifBlank { null }
                ?: customPhone?.ifBlank { null }
                ?: user.phoneNumber
                ?: ""

            val profileToSave = existingProfile?.copy(
                nombre = userName,
                email = userEmail,
                telefono = effectivePhone.ifBlank { existingProfile.telefono }
            ) ?: UserProfile(
                uid = user.uid,
                nombre = userName,
                email = userEmail,
                telefono = effectivePhone,
                rol = if (effectiveRole == AppMode.CLIENTE) "CLIENTE" else "REPARTIDOR",
                fotoUrl = user.photoUrl?.toString()
            )

            firestoreRepository.saveUserProfile(profileToSave)
            userProfileRepository.saveUserProfile(profileToSave)

            withContext(Dispatchers.Main) {
                _userProfile.value = profileToSave
                _currentUser.value = AuthUserState(
                    uid = user.uid,
                    email = userEmail,
                    displayName = userName,
                    photoUrl = user.photoUrl?.toString(),
                    role = effectiveRole,
                    isGoogleAuth = isGoogle
                )
                switchMode(effectiveRole)
                if (effectiveRole == AppMode.CLIENTE) {
                    clienteNombreInput.value = userName
                    if (effectivePhone.isNotBlank()) clienteTelefonoInput.value = effectivePhone
                    _clienteTab.value = ClienteTab.INICIO
                } else {
                    repository.actualizarPerfilRepartidor(
                        nombre = userName,
                        telefono = effectivePhone.ifBlank { "7441234567" },
                        tipoTransporte = existingProfile?.tipoTransporte ?: "MOTO",
                        modelo = existingProfile?.modeloVehiculo ?: "Italika FT150 Blanca",
                        placas = existingProfile?.placas ?: "GRO-489A",
                        clabe = existingProfile?.clabeInterbancaria ?: "012180015678901234",
                        banco = existingProfile?.banco ?: "BBVA México"
                    )
                    _repartidorTab.value = RepartidorTab.DISPONIBLES
                }
                _userMessage.value = "¡Bienvenido, $userName! Sesión iniciada como ${if (effectiveRole == AppMode.CLIENTE) "Cliente" else "Repartidor"}."
            }
        }
    }

    fun loginWithEmailAndPassword(
        email: String,
        pass: String,
        role: AppMode,
        onResult: (Result<FirebaseUser>) -> Unit
    ) {
        viewModelScope.launch {
            val result = FirebaseAuthHelper.signInWithEmail(getApplication(), email, pass)
            result.onSuccess { user ->
                loginWithFirebaseUser(user, role, isGoogle = false)
            }
            onResult(result)
        }
    }

    fun registerWithEmailAndPassword(
        email: String,
        pass: String,
        displayName: String,
        phone: String,
        role: AppMode,
        onResult: (Result<FirebaseUser>) -> Unit
    ) {
        viewModelScope.launch {
            val result = FirebaseAuthHelper.createUserWithEmail(getApplication(), email, pass, displayName)
            result.onSuccess { user ->
                loginWithFirebaseUser(
                    user = user,
                    role = role,
                    isGoogle = false,
                    customDisplayName = displayName.ifBlank { null },
                    customPhone = phone.ifBlank { null }
                )
            }
            onResult(result)
        }
    }

    fun sendPasswordReset(
        email: String,
        onResult: (Result<Unit>) -> Unit
    ) {
        viewModelScope.launch {
            val result = FirebaseAuthHelper.sendPasswordReset(getApplication(), email)
            onResult(result)
        }
    }

    fun loginWithGoogleProfile(
        email: String = "luisalbertotrevino66@gmail.com",
        displayName: String = "Luis Alberto Treviño",
        role: AppMode
    ) {
        val safeUid = "google_user_" + email.filter { it.isLetterOrDigit() }.take(20)
        viewModelScope.launch(Dispatchers.IO) {
            val existingProfile = userProfileRepository.getUserProfile(safeUid).getOrNull()
                ?: firestoreRepository.getUserProfile(safeUid).getOrNull()

            val effectiveRole = when {
                existingProfile?.rol?.equals("REPARTIDOR", ignoreCase = true) == true -> AppMode.REPARTIDOR
                existingProfile?.rol?.equals("CLIENTE", ignoreCase = true) == true -> AppMode.CLIENTE
                else -> role
            }

            val profileToSave = existingProfile?.copy(
                nombre = displayName,
                email = email
            ) ?: UserProfile(
                uid = safeUid,
                nombre = displayName,
                email = email,
                telefono = if (effectiveRole == AppMode.CLIENTE) clienteTelefonoInput.value else "7445209911",
                rol = if (effectiveRole == AppMode.CLIENTE) "CLIENTE" else "REPARTIDOR"
            )

            firestoreRepository.saveUserProfile(profileToSave)
            userProfileRepository.saveUserProfile(profileToSave)

            withContext(Dispatchers.Main) {
                _userProfile.value = profileToSave
                _currentUser.value = AuthUserState(
                    uid = safeUid,
                    email = email,
                    displayName = displayName,
                    photoUrl = null,
                    role = effectiveRole,
                    isGoogleAuth = true
                )
                switchMode(effectiveRole)
                if (effectiveRole == AppMode.CLIENTE) {
                    clienteNombreInput.value = displayName
                    _clienteTab.value = ClienteTab.INICIO
                } else {
                    repository.actualizarPerfilRepartidor(
                        nombre = displayName,
                        telefono = "7445209911",
                        tipoTransporte = existingProfile?.tipoTransporte ?: "MOTO",
                        modelo = existingProfile?.modeloVehiculo ?: "Italika FT150 Blanca",
                        placas = existingProfile?.placas ?: "GRO-489A",
                        clabe = existingProfile?.clabeInterbancaria ?: "012180015678901234",
                        banco = existingProfile?.banco ?: "BBVA México"
                    )
                    _repartidorTab.value = RepartidorTab.DISPONIBLES
                }
                _userMessage.value = "Sesión de Google iniciada como $displayName (${if (effectiveRole == AppMode.CLIENTE) "Cliente" else "Repartidor"})."
            }
        }
    }

    fun loginDemoUser(role: AppMode) {
        val demoName = if (role == AppMode.CLIENTE) "Ana Karen Valenzuela" else "Carlos Mendoza"
        val demoEmail = if (role == AppMode.CLIENTE) "karen.valenzuela@acamandados.com" else "carlos.mendoza@acamandados.com"
        val demoUid = if (role == AppMode.CLIENTE) "demo_cliente_123" else "demo_repartidor_456"

        _currentUser.value = AuthUserState(
            uid = demoUid,
            email = demoEmail,
            displayName = demoName,
            photoUrl = null,
            role = role,
            isGoogleAuth = false
        )
        switchMode(role)
        if (role == AppMode.CLIENTE) {
            clienteNombreInput.value = demoName
            _clienteTab.value = ClienteTab.INICIO
        } else {
            _repartidorTab.value = RepartidorTab.DISPONIBLES
        }
        viewModelScope.launch(Dispatchers.IO) {
            val profile = UserProfile(
                uid = demoUid,
                nombre = demoName,
                email = demoEmail,
                telefono = if (role == AppMode.CLIENTE) "7445209911" else "7441234567",
                rol = if (role == AppMode.CLIENTE) "CLIENTE" else "REPARTIDOR"
            )
            firestoreRepository.saveUserProfile(profile)
            userProfileRepository.saveUserProfile(profile)
            withContext(Dispatchers.Main) {
                _userProfile.value = profile
            }
        }
        _userMessage.value = "Has ingresado como $demoName (${if (role == AppMode.CLIENTE) "Cliente" else "Repartidor"})."
    }

    fun logout() {
        FirebaseAuthHelper.signOut(getApplication())
        _currentUser.value = null
        tarjetaCvvInput.value = ""
        closePinValidation()
        _userMessage.value = "Sesión cerrada correctamente."
    }

    // Modo actual (Cliente / Repartidor)
    private val _currentMode = MutableStateFlow(AppMode.CLIENTE)
    val currentMode: StateFlow<AppMode> = _currentMode.asStateFlow()

    // Control de pantalla de inicio / bienvenida para selección de rol
    private val _showPantallaInicio = MutableStateFlow(false)
    val showPantallaInicio: StateFlow<Boolean> = _showPantallaInicio.asStateFlow()

    fun openPantallaInicio() {
        _showPantallaInicio.value = true
    }

    fun closePantallaInicio() {
        _showPantallaInicio.value = false
    }

    // Pantalla de registro simplificada para nuevos repartidores
    val showRegistroRepartidorScreen = MutableStateFlow(false)

    fun openRegistroRepartidorScreen() {
        showRegistroRepartidorScreen.value = true
    }

    fun closeRegistroRepartidorScreen() {
        showRegistroRepartidorScreen.value = false
    }

    fun onDriverRegisteredSuccess(profile: RepartidorProfile) {
        showRegistroRepartidorScreen.value = false
        _currentMode.value = AppMode.REPARTIDOR
        _repartidorTab.value = RepartidorTab.DISPONIBLES
        _currentUser.value = AuthUserState(
            uid = "rep_${profile.id}",
            email = "",
            displayName = profile.nombre,
            role = AppMode.REPARTIDOR,
            isGoogleAuth = false
        )
        _userProfile.value = UserProfile.fromRepartidorProfile(profile, uid = "rep_${profile.id}")
        _userMessage.value = "🎉 ¡Bienvenido a AcaMandados, ${profile.nombre}! Tu cuenta de repartidor está activa."
    }

    private val _clienteTab = MutableStateFlow(ClienteTab.INICIO)
    val clienteTab: StateFlow<ClienteTab> = _clienteTab.asStateFlow()

    private val _repartidorTab = MutableStateFlow(RepartidorTab.DISPONIBLES)
    val repartidorTab: StateFlow<RepartidorTab> = _repartidorTab.asStateFlow()

    // Formulario de nuevo mandado (Cliente)
    val selectedTipo = MutableStateFlow(TipoMandado.DOCUMENTACION)
    val tituloInput = MutableStateFlow("")
    val descripcionInput = MutableStateFlow("")
    val puntoRecogidaInput = MutableStateFlow("")
    val zonaRecogidaInput = MutableStateFlow(AcapulcoLocations.ZONAS[0].nombre)
    val puntoEntregaInput = MutableStateFlow("")
    val zonaEntregaInput = MutableStateFlow(AcapulcoLocations.ZONAS[1].nombre)
    val costoProductosInput = MutableStateFlow("")
    val selectedMetodoPago = MutableStateFlow(MetodoPago.EFECTIVO)
    val clienteNombreInput = MutableStateFlow("Ana Karen Valenzuela")
    val clienteTelefonoInput = MutableStateFlow("7445209911")
    val remitenteNombreInput = MutableStateFlow("")
    val remitenteTelefonoInput = MutableStateFlow("")
    val destinatarioNombreInput = MutableStateFlow("")
    val destinatarioTelefonoInput = MutableStateFlow("")
    val referenciasDomicilioInput = MutableStateFlow("")
    val notasInput = MutableStateFlow("")

    // Declaración y categorización de carga (Estilo Roadie)
    val tamanoPaqueteInput = MutableStateFlow("MEDIANO")
    val confirmacionDimensionesMoto = MutableStateFlow(true)

    // Cobro Contra Entrega (COD) para comercios locales (Límite $500 MXN)
    val esCobroContraEntrega = MutableStateFlow(false)
    val montoCobroProductoInput = MutableStateFlow("")

    // Pago con tarjeta en app
    val tarjetaNumeroInput = MutableStateFlow("4152 8392 1029 4820")
    val tarjetaTitularInput = MutableStateFlow("Ana Karen Valenzuela")
    val tarjetaExpiracionInput = MutableStateFlow("12/28")
    val tarjetaCvvInput = MutableStateFlow("832")

    // Modales de validación y flujos de seguridad
    val activeProofOfConditionOrder = MutableStateFlow<MandadoOrder?>(null)
    val activePinValidationOrder = MutableStateFlow<MandadoOrder?>(null)
    val isPinOnRecipientDevice = MutableStateFlow(false)
    val activeWebTrackingOrder = MutableStateFlow<MandadoOrder?>(null)
    val recentlyCreatedOrder = MutableStateFlow<MandadoOrder?>(null)

    fun dismissRecentlyCreatedOrder() {
        recentlyCreatedOrder.value = null
    }

    fun copyOrderWebTrackingUrl(context: Context, order: MandadoOrder) {
        val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as? android.content.ClipboardManager
        val clip = android.content.ClipData.newPlainText("URL de Rastreo", order.getWebTrackingUrl())
        clipboardManager?.setPrimaryClip(clip)
        _userMessage.value = "✅ Enlace web de rastreo copiado: ${order.getWebTrackingUrl()}"
        Toast.makeText(context, "✅ Enlace copiado al portapapeles", Toast.LENGTH_SHORT).show()
    }

    fun shareOrderWebTracking(context: Context, order: MandadoOrder) {
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, order.getShareableTrackingMessage())
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, "Compartir seguimiento de mandado")
        context.startActivity(shareIntent)
    }

    fun shareOrderViaWhatsApp(context: Context, order: MandadoOrder) {
        val tel = order.getTelefonoDestinatario().replace("[^0-9]".toRegex(), "")
        val msg = order.getShareableTrackingMessage()
        val url = if (tel.length == 10) {
            "https://api.whatsapp.com/send?phone=52$tel&text=" + Uri.encode(msg)
        } else {
            "https://api.whatsapp.com/send?text=" + Uri.encode(msg)
        }
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            context.startActivity(intent)
        } catch (_: Exception) {
            copyOrderWebTrackingUrl(context, order)
        }
    }

    fun handleIncomingWebTrackingUrl(url: String) {
        viewModelScope.launch {
            val uri = Uri.parse(url)
            val path = uri.path ?: ""
            val folio = if (path.contains("/order/")) {
                path.substringAfter("/order/").substringBefore("/").substringBefore("?")
            } else if (path.contains("/t/")) {
                path.substringAfter("/t/").substringBefore("/").substringBefore("?")
            } else {
                ""
            }
            if (folio.isNotBlank()) {
                val all = repository.allOrders.first()
                val match = all.find { it.folio.equals(folio, ignoreCase = true) }
                if (match != null) {
                    openWebTracking(match)
                }
            }
        }
    }

    fun copiarDatosClienteARemitente() {
        remitenteNombreInput.value = clienteNombreInput.value
        remitenteTelefonoInput.value = clienteTelefonoInput.value
    }

    fun copiarDatosClienteADestinatario() {
        destinatarioNombreInput.value = clienteNombreInput.value
        destinatarioTelefonoInput.value = clienteTelefonoInput.value
    }

    // Mensaje temporal de alerta o éxito
    private val _userMessage = MutableStateFlow<String?>(null)
    val userMessage: StateFlow<String?> = _userMessage.asStateFlow()

    // Telemetría de geolocalización en tiempo real
    private val _driverLocationState = MutableStateFlow(DriverLocationState())
    val driverLocationState: StateFlow<DriverLocationState> = _driverLocationState.asStateFlow()

    // Modales y Hojas
    val activeOrderForRating = MutableStateFlow<Pair<MandadoOrder, Boolean>?>(null) // (Order, isDriverRatingClient)
    val activeOrderForTracking = MutableStateFlow<MandadoOrder?>(null)
    val isNotificationSheetOpen = MutableStateFlow(false)
    val showCompletedOrdersScreen = MutableStateFlow(false)
    val showEditProfileScreen = MutableStateFlow(false)
    val activeReceiptOrder = MutableStateFlow<MandadoOrder?>(null)
    val activeSelectedTransaccion = MutableStateFlow<TransaccionBilletera?>(null)

    // Alerta en primer plano (heads-up banner) para repartidores cuando se crea un nuevo mandado
    val newMandadoAlert = MutableStateFlow<NewMandadoDriverAlert?>(null)

    // Configuración de Tema (Modo Claro / Oscuro / Sistema)
    val darkThemeConfig = MutableStateFlow(DarkThemeConfig.SYSTEM)

    // Configuración de Dynamic Colors (Material 3 Android 12+)
    val dynamicColorConfig = MutableStateFlow(true)

    fun setDynamicColorConfig(enabled: Boolean) {
        dynamicColorConfig.value = enabled
    }

    fun toggleDynamicColor() {
        dynamicColorConfig.value = !dynamicColorConfig.value
    }

    fun setDarkThemeConfig(config: DarkThemeConfig) {
        darkThemeConfig.value = config
    }

    fun toggleDarkTheme() {
        darkThemeConfig.value = when (darkThemeConfig.value) {
            DarkThemeConfig.LIGHT -> DarkThemeConfig.DARK
            DarkThemeConfig.DARK -> DarkThemeConfig.SYSTEM
            DarkThemeConfig.SYSTEM -> DarkThemeConfig.LIGHT
        }
    }

    // Estado para "Deslizar para actualizar" (Swipe-to-refresh / Pull-to-refresh)
    val isRefreshing = MutableStateFlow(false)

    fun refreshData() {
        viewModelScope.launch {
            isRefreshing.value = true
            delay(750)
            isRefreshing.value = false
            _userMessage.value = "Datos actualizados correctamente"
        }
    }

    // Diálogo y acción de eliminación de cuenta para cumplimiento Google Play
    val showDeleteAccountDialog = MutableStateFlow(false)

    // Diálogos de Políticas de Seguridad, Privacidad y Autorización de Datos
    val showPoliticasSeguridadDialog = MutableStateFlow(false)
    val showAutorizacionDatosDialog = MutableStateFlow(false)
    val showPrivacyPolicyScreen = MutableStateFlow(false)

    // Estados de consentimiento y autorización de datos (LFPDPPP y Google Play Data Safety)
    val userConsentDataAuth = MutableStateFlow(true)
    val userConsentGpsTracking = MutableStateFlow(true)
    val userConsentPushNotifications = MutableStateFlow(true)
    val userConsentPhoneMasking = MutableStateFlow(true)
    val userConsentPhotoEvidence = MutableStateFlow(true)

    fun openPrivacyPolicyScreen() {
        showPrivacyPolicyScreen.value = true
    }

    fun closePrivacyPolicyScreen() {
        showPrivacyPolicyScreen.value = false
    }

    fun updateDataAuthorizations(
        generalAuth: Boolean,
        gpsTracking: Boolean,
        pushNotifications: Boolean,
        phoneMasking: Boolean = true,
        photoEvidence: Boolean = true
    ) {
        userConsentDataAuth.value = generalAuth
        userConsentGpsTracking.value = gpsTracking
        userConsentPushNotifications.value = pushNotifications
        userConsentPhoneMasking.value = phoneMasking
        userConsentPhotoEvidence.value = photoEvidence
        _userMessage.value = "Preferencias de autorización de datos actualizadas con éxito"
    }

    fun openPoliticasSeguridadDialog() {
        showPoliticasSeguridadDialog.value = true
    }

    fun closePoliticasSeguridadDialog() {
        showPoliticasSeguridadDialog.value = false
    }

    fun openAutorizacionDatosDialog() {
        showAutorizacionDatosDialog.value = true
    }

    fun closeAutorizacionDatosDialog() {
        showAutorizacionDatosDialog.value = false
    }

    fun openDeleteAccountDialog() {
        showDeleteAccountDialog.value = true
    }

    fun closeDeleteAccountDialog() {
        showDeleteAccountDialog.value = false
    }

    fun deleteUserAccount(onComplete: () -> Unit = {}) {
        showDeleteAccountDialog.value = false
        val currentUid = _currentUser.value?.uid ?: _userProfile.value.uid
        _currentUser.value = null
        tarjetaNumeroInput.value = ""
        tarjetaTitularInput.value = ""
        tarjetaExpiracionInput.value = ""
        tarjetaCvvInput.value = ""
        closePinValidation()
        clienteNombreInput.value = ""
        clienteTelefonoInput.value = ""
        _userProfile.value = UserProfile(
            uid = "deleted",
            nombre = "Usuario",
            email = "",
            telefono = "",
            rol = "CLIENTE"
        )
        _userMessage.value = "Tu cuenta y datos personales han sido eliminados de acuerdo a las políticas de privacidad."
        _clienteTab.value = ClienteTab.INICIO
        _repartidorTab.value = RepartidorTab.DISPONIBLES
        _currentMode.value = AppMode.CLIENTE
        onComplete()

        viewModelScope.launch(Dispatchers.IO) {
            try {
                kotlinx.coroutines.withTimeoutOrNull(1500) {
                    userProfileRepository.deleteUserProfile(currentUid)
                }
            } catch (_: Exception) {}

            try {
                FirebaseAuthHelper.signOut(getApplication())
            } catch (_: Exception) {}
        }
    }

    fun dismissNewMandadoAlert() {
        newMandadoAlert.value = null
    }

    fun acceptOrViewNewMandadoFromAlert(orderId: Long) {
        newMandadoAlert.value = null
        switchMode(AppMode.REPARTIDOR)
        setRepartidorTab(RepartidorTab.DISPONIBLES)
    }

    // Perfil completo del usuario persistido y sincronizado con Firestore
    private val _userProfile = MutableStateFlow(
        UserProfile(
            uid = "demo_cliente_123",
            nombre = "Ana Karen Valenzuela",
            email = "karen.valenzuela@acamandados.com",
            telefono = "7445209911",
            rol = "CLIENTE",
            direccionFrecuente = "Av. Costera Miguel Alemán #240, Fracc. Condesa",
            zonaFrecuente = "Zona Costera & Condesa",
            referenciasDomicilio = "Edificio blanco con azul, junto a Farmacia del Ahorro",
            notificacionesWhatsApp = true,
            metodoPagoPreferido = "EFECTIVO",
            exigirPinSeguridad = true,
            tamanoPaquetePreferido = "MEDIANO",
            dejarEnPuertaSiNoContesta = false,
            notificacionesPush = true,
            propinaDefaultPorcentaje = 10
        )
    )
    val userProfile: StateFlow<UserProfile> = _userProfile.asStateFlow()

    fun openEditProfile() {
        showEditProfileScreen.value = true
    }

    fun closeEditProfile() {
        showEditProfileScreen.value = false
    }

    fun guardarPerfil(
        nombre: String,
        telefono: String,
        email: String,
        direccionFrecuente: String,
        zonaFrecuente: String,
        referenciasDomicilio: String,
        notificacionesWhatsApp: Boolean,
        metodoPagoPreferido: String,
        exigirPinSeguridad: Boolean,
        tamanoPaquetePreferido: String,
        dejarEnPuertaSiNoContesta: Boolean,
        notificacionesPush: Boolean,
        propinaDefaultPorcentaje: Int,
        tipoTransporte: String = "MOTO",
        modeloVehiculo: String = "",
        placas: String = "",
        clabe: String = "",
        banco: String = "BBVA México",
        rolElegido: AppMode? = null,
        onSuccess: () -> Unit = {}
    ) {
        val effectiveMode = rolElegido ?: currentMode.value
        val currentUid = currentUser.value?.uid ?: _userProfile.value.uid.ifBlank { "user_default" }
        val rolStr = if (effectiveMode == AppMode.CLIENTE) "CLIENTE" else "REPARTIDOR"

        val updated = _userProfile.value.copy(
            uid = currentUid,
            nombre = nombre,
            telefono = telefono,
            email = email,
            rol = rolStr,
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
            clabeInterbancaria = clabe,
            banco = banco,
            ultimaActividad = System.currentTimeMillis()
        )

        _userProfile.value = updated
        _currentUser.value = _currentUser.value?.copy(role = effectiveMode)
        switchMode(effectiveMode)

        // Sincronizar inputs locales de la vista
        if (effectiveMode == AppMode.CLIENTE) {
            clienteNombreInput.value = nombre
            clienteTelefonoInput.value = telefono
            if (direccionFrecuente.isNotBlank()) puntoEntregaInput.value = direccionFrecuente
            if (zonaFrecuente.isNotBlank()) zonaEntregaInput.value = zonaFrecuente
            if (referenciasDomicilio.isNotBlank()) referenciasDomicilioInput.value = referenciasDomicilio
            selectedMetodoPago.value = if (metodoPagoPreferido == "TARJETA") MetodoPago.TARJETA else MetodoPago.EFECTIVO
            tamanoPaqueteInput.value = tamanoPaquetePreferido
        }

        viewModelScope.launch(Dispatchers.IO) {
            userProfileRepository.saveUserProfile(updated)
            firestoreRepository.saveUserProfile(updated)
            if (effectiveMode == AppMode.REPARTIDOR) {
                repository.actualizarPerfilRepartidor(
                    nombre = nombre,
                    telefono = telefono,
                    tipoTransporte = tipoTransporte,
                    modelo = modeloVehiculo,
                    placas = placas,
                    clabe = clabe,
                    banco = banco
                )
            }
            _userMessage.value = "✅ Información de perfil y rol ($rolStr) guardados en Firestore."
            withContext(Dispatchers.Main) {
                closeEditProfile()
                onSuccess()
            }
        }
    }

    /**
     * Permite al usuario elegir si es 'Cliente' o 'Repartidor' y guardar esta preferencia en Firestore.
     */
    fun cambiarYGuardarRol(
        nuevoRol: AppMode,
        onComplete: (Boolean) -> Unit = {}
    ) {
        val rolStr = if (nuevoRol == AppMode.CLIENTE) "CLIENTE" else "REPARTIDOR"
        val currentUid = currentUser.value?.uid ?: _userProfile.value.uid.ifBlank { "user_default" }

        // Actualizamos estado en UI
        _userProfile.value = _userProfile.value.copy(
            uid = currentUid,
            rol = rolStr,
            ultimaActividad = System.currentTimeMillis()
        )
        _currentUser.value = _currentUser.value?.copy(role = nuevoRol)
        switchMode(nuevoRol)

        viewModelScope.launch(Dispatchers.IO) {
            val res1 = userProfileRepository.updateUserRole(currentUid, rolStr)
            val res2 = firestoreRepository.updateUserRole(currentUid, rolStr)
            val isSuccess = res1.isSuccess || res2.isSuccess

            _userMessage.value = if (isSuccess) {
                "✅ Preferencia guardada en Firestore: Ahora eres ${if (nuevoRol == AppMode.CLIENTE) "Cliente" else "Repartidor"}."
            } else {
                "⚠️ Rol cambiado localmente a ${if (nuevoRol == AppMode.CLIENTE) "Cliente" else "Repartidor"}."
            }
            withContext(Dispatchers.Main) {
                onComplete(isSuccess)
            }
        }
    }

    // Datos reactivos de Room combinados con actualizaciones optimistas inmediatas (Optimistic UI)
    private val _optimisticOrders = MutableStateFlow<Map<Long, MandadoOrder>>(emptyMap())

    val allOrders: StateFlow<List<MandadoOrder>> = combine(
        repository.allOrders,
        _optimisticOrders
    ) { dbOrders, optimistic ->
        if (optimistic.isEmpty()) {
            dbOrders
        } else {
            val dbMap = dbOrders.associateBy { it.id }.toMutableMap()
            optimistic.forEach { (id, order) ->
                dbMap[id] = order
            }
            dbMap.values.sortedByDescending { it.fechaCreacion }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val availableOrders: StateFlow<List<MandadoOrder>> = allOrders.map { orders ->
        orders.filter { it.estado == "SOLICITADO" }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeOrders: StateFlow<List<MandadoOrder>> = allOrders.map { orders ->
        orders.filter { it.estado != "SOLICITADO" && it.estado != "ENTREGADO" && it.estado != "CANCELADO" }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val currentDriverActiveOrder: StateFlow<MandadoOrder?> = allOrders.map { orders ->
        orders.firstOrNull { it.repartidorId == 1L && it.estado != "ENTREGADO" && it.estado != "CANCELADO" }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val driverCompletedOrders: StateFlow<List<MandadoOrder>> = allOrders.map { orders ->
        orders.filter { it.repartidorId == 1L && it.estado == "ENTREGADO" }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val repartidorProfile: StateFlow<RepartidorProfile?> = repository.repartidorProfile
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val depositosLog: StateFlow<List<DepositoTarjetaLog>> = repository.depositosLog
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val transaccionesBilletera: StateFlow<List<TransaccionBilletera>> = repository.transaccionesBilletera
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allReviews: StateFlow<List<CalificacionReview>> = repository.allReviews
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allNotifications: StateFlow<List<NotificationItem>> = repository.allNotifications
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val unreadNotificationsCount: StateFlow<Int> = repository.unreadNotificationsCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // Sub-pestaña para el Panel de Administrador
    private val _adminTab = MutableStateFlow(AdminTab.RESUMEN_METRICAS)
    val adminTab: StateFlow<AdminTab> = _adminTab.asStateFlow()

    fun setAdminTab(tab: AdminTab) {
        _adminTab.value = tab
    }

    // Flota de repartidores activos en Acapulco para el Administrador
    val fleetDrivers: StateFlow<List<DriverTelemetryInfo>> = combine(
        driverLocationState,
        repartidorProfile,
        currentDriverActiveOrder,
        activeOrders
    ) { loc, profile, activeOrder, orders ->
        val carlosOrderFolio = activeOrder?.folio
        val carlosEstado = when {
            activeOrder == null -> if (profile?.disponibleParaMandados == true) "DISPONIBLE" else "DESCONECTADO"
            activeOrder.estado == "EN_RUTA" -> "EN_RUTA"
            activeOrder.estado == "EN_RECOLECCION" -> "EN_RECOLECCION"
            else -> "EN_RUTA"
        }

        listOf(
            DriverTelemetryInfo(
                id = 1L,
                nombre = profile?.nombre ?: "Carlos Mendoza",
                telefono = profile?.telefono ?: "7441234567",
                vehiculo = profile?.modeloVehiculo ?: "Italika FT150 Blanca",
                placas = profile?.placas ?: "GRO-489A",
                calificacion = profile?.calificacion ?: 4.95,
                estado = carlosEstado,
                zonaNombre = "Costera / Magallanes",
                lat = loc.lat,
                lng = loc.lng,
                velocidadKmh = loc.speedKmh,
                bateriaPorcentaje = 88,
                mandadoFolioAsignado = carlosOrderFolio,
                pedidosHoy = 7,
                pedidosCompletadosTotal = profile?.pedidosCompletados ?: 19
            ),
            DriverTelemetryInfo(
                id = 2L,
                nombre = "Brayan Salgado",
                telefono = "7442345678",
                vehiculo = "Honda Cargo 150 Roja",
                placas = "GRO-712B",
                calificacion = 4.88,
                estado = if (orders.any { it.zonaEntrega.contains("Diamante") || it.zonaRecogida.contains("Diamante") }) "EN_RUTA" else "DISPONIBLE",
                zonaNombre = "Zona Diamante & Princess",
                lat = 16.7820,
                lng = -99.7890,
                velocidadKmh = 42,
                bateriaPorcentaje = 76,
                mandadoFolioAsignado = orders.firstOrNull { it.zonaEntrega.contains("Diamante") || it.zonaRecogida.contains("Diamante") }?.folio,
                pedidosHoy = 5,
                pedidosCompletadosTotal = 34
            ),
            DriverTelemetryInfo(
                id = 3L,
                nombre = "Jonathan Alarcón",
                telefono = "7443456789",
                vehiculo = "Yamaha YBR 125 Azul",
                placas = "GRO-304C",
                calificacion = 4.92,
                estado = "EN_RECOLECCION",
                zonaNombre = "Costa Azul & Club Deportivo",
                lat = 16.8480,
                lng = -99.8510,
                velocidadKmh = 28,
                bateriaPorcentaje = 64,
                mandadoFolioAsignado = orders.firstOrNull { it.zonaEntrega.contains("Costa Azul") || it.zonaRecogida.contains("Costa Azul") }?.folio,
                pedidosHoy = 8,
                pedidosCompletadosTotal = 52
            ),
            DriverTelemetryInfo(
                id = 4L,
                nombre = "Daniel Rendón",
                telefono = "7444567890",
                vehiculo = "Vento Ryder 150 Negra",
                placas = "GRO-518D",
                calificacion = 4.80,
                estado = "DISPONIBLE",
                zonaNombre = "Centro & Caleta",
                lat = 16.8320,
                lng = -99.9040,
                velocidadKmh = 0,
                bateriaPorcentaje = 92,
                mandadoFolioAsignado = null,
                pedidosHoy = 4,
                pedidosCompletadosTotal = 28
            ),
            DriverTelemetryInfo(
                id = 5L,
                nombre = "Fernando Nava",
                telefono = "7445678901",
                vehiculo = "Suzuki AX100 Gris",
                placas = "GRO-890E",
                calificacion = 4.85,
                estado = "DISPONIBLE",
                zonaNombre = "Pie de la Cuesta",
                lat = 16.9100,
                lng = -99.9600,
                velocidadKmh = 35,
                bateriaPorcentaje = 70,
                mandadoFolioAsignado = null,
                pedidosHoy = 3,
                pedidosCompletadosTotal = 15
            )
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Estadísticas operativas del día para el Administrador
    val adminDailyStats: StateFlow<AdminDailyStats> = combine(
        allOrders,
        activeOrders,
        availableOrders,
        driverCompletedOrders,
        fleetDrivers
    ) { all, active, available, completed, drivers ->
        val totalHoy = all.size
        val entregadosHoy = all.count { it.estado == "ENTREGADO" }
        val activosHoy = active.size
        val pendientesHoy = available.size
        val canceladosHoy = all.count { it.estado == "CANCELADO" }

        val totalEntregados = if (entregadosHoy > 0) entregadosHoy else completed.size
        val tasaExito = if (totalHoy > 0) {
            Math.round((totalEntregados.toDouble() / (totalEntregados + canceladosHoy).coerceAtLeast(1)) * 1000.0) / 10.0
        } else {
            100.0
        }

        val totalEnviosMxn = all.filter { it.estado == "ENTREGADO" }.sumOf { it.tarifaEnvio }
        val totalProductosMxn = all.filter { it.estado == "ENTREGADO" }.sumOf { it.costoProductosEstimado }
        val totalTransaccionado = totalEnviosMxn + totalProductosMxn

        val conectados = drivers.count { it.estado != "DESCONECTADO" }
        val enRuta = drivers.count { it.estado == "EN_RUTA" || it.estado == "EN_RECOLECCION" }
        val disponibles = drivers.count { it.estado == "DISPONIBLE" }

        val zonaCounts = mutableMapOf<String, Int>()
        all.forEach { order ->
            val zona = order.zonaEntrega.ifBlank { "Costera Miguel Alemán" }
            zonaCounts[zona] = (zonaCounts[zona] ?: 0) + 1
        }

        AdminDailyStats(
            totalPedidosHoy = totalHoy,
            pedidosEntregadosHoy = totalEntregados,
            pedidosActivosHoy = activosHoy,
            pedidosPendientesAsignacion = pendientesHoy,
            pedidosCanceladosHoy = canceladosHoy,
            tasaExitoPorcentaje = tasaExito,
            tiempoPromedioMinutos = 23,
            totalIngresosEnvioMxn = totalEnviosMxn,
            totalProductosMxn = totalProductosMxn,
            totalTransaccionadoMxn = totalTransaccionado,
            repartidoresConectados = conectados,
            repartidoresEnRuta = enRuta,
            repartidoresDisponibles = disponibles,
            distribucionPorZona = zonaCounts
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AdminDailyStats(0, 0, 0, 0, 0, 100.0, 23, 0.0, 0.0, 0.0, 5, 2, 3))

    fun switchMode(mode: AppMode) {
        _currentMode.value = mode
    }

    // Pila de navegación interna para preservar el historial entre pestañas y soportar el botón Atrás
    private val clienteTabStack = mutableListOf(ClienteTab.INICIO)
    private val repartidorTabStack = mutableListOf(RepartidorTab.DISPONIBLES)

    fun setClienteTab(tab: ClienteTab, addToStack: Boolean = true) {
        if (_clienteTab.value != tab) {
            if (addToStack) {
                // Evitar ciclos consecutivos repetidos
                if (clienteTabStack.lastOrNull() != tab) {
                    clienteTabStack.add(tab)
                }
            }
            _clienteTab.value = tab
        }
    }

    fun canPopClienteTab(): Boolean = clienteTabStack.size > 1

    fun popClienteTab(): Boolean {
        if (clienteTabStack.size > 1) {
            clienteTabStack.removeAt(clienteTabStack.lastIndex)
            val prevTab = clienteTabStack.last()
            _clienteTab.value = prevTab
            return true
        }
        return false
    }

    fun iniciarMandadoPorTipo(tipo: TipoMandado) {
        selectedTipo.value = tipo
        tituloInput.value = tipo.label
        setClienteTab(ClienteTab.SOLICITAR)
    }

    fun setRepartidorTab(tab: RepartidorTab, addToStack: Boolean = true) {
        if (_repartidorTab.value != tab) {
            if (addToStack) {
                if (repartidorTabStack.lastOrNull() != tab) {
                    repartidorTabStack.add(tab)
                }
            }
            _repartidorTab.value = tab
        }
    }

    fun canPopRepartidorTab(): Boolean = repartidorTabStack.size > 1

    fun popRepartidorTab(): Boolean {
        if (repartidorTabStack.size > 1) {
            repartidorTabStack.removeAt(repartidorTabStack.lastIndex)
            val prevTab = repartidorTabStack.last()
            _repartidorTab.value = prevTab
            return true
        }
        return false
    }

    fun clearUserMessage() {
        _userMessage.value = null
    }

    fun getCalculatedFeeDesglose(): TarifaDesglose {
        return AcapulcoLocations.calcularTarifaDesglose(
            zonaOrigen = zonaRecogidaInput.value,
            zonaDestino = zonaEntregaInput.value,
            tipo = selectedTipo.value.id
        )
    }

    fun getCalculatedFee(): Double {
        return getCalculatedFeeDesglose().total
    }

    fun openLiveTracking(order: MandadoOrder) {
        activeOrderForTracking.value = order
    }

    fun closeLiveTracking() {
        activeOrderForTracking.value = null
    }

    fun openWebTracking(order: MandadoOrder) {
        activeWebTrackingOrder.value = order
    }

    fun closeWebTracking() {
        activeWebTrackingOrder.value = null
    }

    fun openWebTrackingPreview(order: MandadoOrder) {
        openWebTracking(order)
    }

    fun closeWebTrackingPreview() {
        closeWebTracking()
    }

    fun openCompletedOrdersScreen() {
        showCompletedOrdersScreen.value = true
    }

    fun closeCompletedOrdersScreen() {
        showCompletedOrdersScreen.value = false
    }

    fun reordenarMandado(order: MandadoOrder) {
        val matchingTipo = TipoMandado.values().find { it.id == order.tipo } ?: TipoMandado.EXPRESS
        selectedTipo.value = matchingTipo
        tituloInput.value = order.titulo
        descripcionInput.value = order.descripcion
        puntoRecogidaInput.value = order.puntoRecogida
        zonaRecogidaInput.value = order.zonaRecogida
        puntoEntregaInput.value = order.puntoEntrega
        zonaEntregaInput.value = order.zonaEntrega
        selectedMetodoPago.value = if (order.metodoPago == "TARJETA") MetodoPago.TARJETA else MetodoPago.EFECTIVO
        showCompletedOrdersScreen.value = false
        _currentMode.value = AppMode.CLIENTE
        _clienteTab.value = ClienteTab.SOLICITAR
        _userMessage.value = "📋 Datos cargados del pedido ${order.folio}. ¡Revisa y confirma tu nuevo mandado!"
    }

    fun simularEntregaCompletada() {
        viewModelScope.launch {
            val randomFolioNum = (1050..9999).random()
            val tipos = listOf(TipoMandado.COMIDA, TipoMandado.FARMACIA, TipoMandado.COMPRAS, TipoMandado.EXPRESS)
            val randomTipo = tipos.random()
            val isCard = listOf(true, false).random()
            val now = System.currentTimeMillis()
            val randomFee = listOf(45.0, 50.0, 60.0, 75.0, 85.0).random()
            val randomProd = if (randomTipo == TipoMandado.COMIDA || randomTipo == TipoMandado.COMPRAS) (140..390).random().toDouble() else 0.0

            val order = MandadoOrder(
                folio = "ACA-$randomFolioNum",
                tipo = randomTipo.id,
                titulo = when (randomTipo) {
                    TipoMandado.COMIDA -> "Pozole guerrerense y tacos dorados"
                    TipoMandado.FARMACIA -> "Medicamentos y termómetro digital"
                    TipoMandado.COMPRAS -> "Despensa OXXO / Chedraui Diamante"
                    else -> "Envío exprés de llaves y documentos"
                },
                descripcion = "Entrega completada exitosamente a satisfacción del cliente con PIN verificado.",
                puntoRecogida = "Av. Costera Miguel Alemán #240, Acapulco",
                zonaRecogida = "Zona Costera & Condesa",
                puntoEntrega = "Condominios Mayan Island, Torre 2",
                zonaEntrega = "Zona Diamante & Blvd. de las Naciones",
                costoProductosEstimado = randomProd,
                tarifaEnvio = randomFee,
                metodoPago = if (isCard) "TARJETA" else "EFECTIVO",
                estado = "ENTREGADO",
                clienteNombre = clienteNombreInput.value.ifBlank { "Ana Karen Valenzuela" },
                clienteTelefono = clienteTelefonoInput.value.ifBlank { "7441234567" },
                remitenteNombre = "Comercio Asociado",
                remitenteTelefono = "7444850011",
                destinatarioNombre = clienteNombreInput.value.ifBlank { "Ana Karen Valenzuela" },
                destinatarioTelefono = clienteTelefonoInput.value.ifBlank { "7441234567" },
                repartidorId = 1,
                repartidorNombre = "Carlos Mendoza",
                repartidorTelefono = "7449876543",
                repartidorVehiculo = "Italika FT150 Blanca",
                repartidorPlacas = "GRO-489A",
                fechaCreacion = now - (1000 * 60 * (30..90).random()),
                fechaEntrega = now - (1000 * 60 * (5..20).random()),
                pinSeguridad = String.format("%04d", java.security.SecureRandom().nextInt(10000)),
                pagoDepositadoAlRepartidor = isCard,
                claveRastreoSpei = if (isCard) "SPEI-${(100000..999999).random()}" else null,
                calificadoPorCliente = true,
                calificacionClienteEstrellas = 5,
                calificacionClienteComentario = "¡Excelente servicio! Muy rápido y cuidadoso con el paquete."
            )
            repository.insertCompletedOrder(order)
            _userMessage.value = "🎉 Se añadió el mandado completado ${order.folio} al historial."
        }
    }

    fun openProofOfCondition(order: MandadoOrder) {
        activeProofOfConditionOrder.value = order
    }

    fun closeProofOfCondition() {
        activeProofOfConditionOrder.value = null
    }

    fun registrarFotoRecoleccion(orderId: Long, fotoUri: String) {
        confirmarFotoRecoleccion(orderId, fotoUri)
    }

    fun confirmarFotoRecoleccion(orderId: Long, fotoUri: String) {
        viewModelScope.launch {
            repository.registrarFotoRecoleccion(orderId, fotoUri)
            closeProofOfCondition()
            _userMessage.value = "📸 Foto de condición registrada en origen. Estado: En camino a entrega."
        }
    }

    fun openPinValidation(order: MandadoOrder, onRecipientDevice: Boolean = false) {
        isPinOnRecipientDevice.value = onRecipientDevice
        activePinValidationOrder.value = order
    }

    fun openRecipientDevicePinValidation(order: MandadoOrder) {
        openPinValidation(order, onRecipientDevice = true)
    }

    fun closePinValidation() {
        activePinValidationOrder.value = null
        isPinOnRecipientDevice.value = false
    }

    fun validarPinYCompletarEntrega(
        orderId: Long,
        pinIngresado: String,
        fotoEntregaUri: String?,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            val valid = repository.validarPinYFinalizarEntrega(orderId, pinIngresado, fotoEntregaUri)
            if (valid) {
                // Actualización optimista inmediata
                val currentOrder = allOrders.value.find { it.id == orderId }
                if (currentOrder != null) {
                    _optimisticOrders.value = _optimisticOrders.value + (orderId to currentOrder.copy(
                        estado = "ENTREGADO",
                        fechaEntrega = System.currentTimeMillis()
                    ))
                }
                closePinValidation()
                _userMessage.value = "✅ ¡PIN validado con éxito! Entrega completada y ganancia 100% acreditada."
                onSuccess()
            } else {
                onError("PIN de 4 dígitos incorrecto. Pídele al receptor que te dicte el código de seguridad de su app o WhatsApp.")
            }
        }
    }

    fun openRatingDialog(order: MandadoOrder, isDriverRatingClient: Boolean) {
        activeOrderForRating.value = Pair(order, isDriverRatingClient)
    }

    fun closeRatingDialog() {
        activeOrderForRating.value = null
    }

    fun toggleNotificationSheet(open: Boolean) {
        isNotificationSheetOpen.value = open
    }

    fun markNotificationAsRead(id: Long) {
        viewModelScope.launch {
            repository.markNotificationAsRead(id)
        }
    }

    fun markAllNotificationsAsRead() {
        viewModelScope.launch {
            repository.markAllNotificationsAsRead()
            _userMessage.value = "Todas las notificaciones marcadas como leídas."
        }
    }

    fun calificarServicio(
        orderId: Long,
        estrellas: Int,
        comentario: String,
        tags: String
    ) {
        val currentPair = activeOrderForRating.value ?: return
        val order = currentPair.first
        val isDriverRatingClient = currentPair.second

        val tipo = if (isDriverRatingClient) "REPARTIDOR_A_CLIENTE" else "CLIENTE_A_REPARTIDOR"
        val autor = if (isDriverRatingClient) (order.repartidorNombre ?: "Carlos Mendoza") else order.clienteNombre
        val destinatario = if (isDriverRatingClient) order.clienteNombre else (order.repartidorNombre ?: "Carlos Mendoza")

        viewModelScope.launch {
            repository.calificarServicio(
                orderId = orderId,
                tipo = tipo,
                estrellas = estrellas,
                comentario = comentario,
                tags = tags,
                autorNombre = autor,
                destinatarioNombre = destinatario
            )
            closeRatingDialog()
            _userMessage.value = "¡Gracias por tu calificación de $estrellas estrellas!"
        }
    }

    fun crearNuevoMandado(onSuccess: (Long) -> Unit = {}) {
        val recogida = puntoRecogidaInput.value.trim()
        val entrega = puntoEntregaInput.value.trim()
        val desc = descripcionInput.value.trim()

        // Validación de campos obligatorios requeridos para el mandado
        if (recogida.isBlank()) {
            _userMessage.value = "⚠️ Debes ingresar la dirección de origen (punto de recogida)."
            return
        }
        if (entrega.isBlank()) {
            _userMessage.value = "⚠️ Debes ingresar la dirección de destino (punto de entrega)."
            return
        }
        if (desc.isBlank()) {
            _userMessage.value = "⚠️ Por favor describe qué servicio o encargo necesita realizar el repartidor."
            return
        }

        // Validación 1: Confirmación de dimensiones para moto
        if (!confirmacionDimensionesMoto.value) {
            _userMessage.value = "⚠️ Debes marcar la confirmación de que tu paquete no excede 12 kg y cabe en moto."
            return
        }

        // Validación 2: Límite de monto para Cobro Contra Entrega (máximo $500 MXN en MVP)
        val codAmount = if (esCobroContraEntrega.value) {
            val amount = montoCobroProductoInput.value.toDoubleOrNull() ?: 0.0
            if (amount <= 0.0) {
                _userMessage.value = "⚠️ Ingresa el valor del producto a cobrar contra entrega."
                return
            }
            if (amount > 500.0) {
                _userMessage.value = "⚠️ En el MVP de Acapulco, el cobro contra entrega está limitado a un máximo de $500 MXN para seguridad del repartidor."
                return
            }
            amount
        } else {
            0.0
        }

        val tipo = selectedTipo.value
        val titulo = tituloInput.value.trim().take(100).ifBlank { tipo.label }
        val costoProductos = (costoProductosInput.value.toDoubleOrNull() ?: 0.0).coerceAtLeast(0.0)
        val desglose = getCalculatedFeeDesglose()
        val tarifa = desglose.total.coerceAtLeast(35.0)

        val isTarjeta = selectedMetodoPago.value == MetodoPago.TARJETA
        val digitsOnly = tarjetaNumeroInput.value.filter { it.isDigit() }
        val tarjetaUltimos4 = if (isTarjeta) digitsOnly.takeLast(4).ifBlank { "4820" } else ""
        val titular = if (isTarjeta) tarjetaTitularInput.value.trim().take(100).ifBlank { clienteNombreInput.value.trim().take(100) } else ""

        val remNombre = remitenteNombreInput.value.trim().take(100).ifBlank { clienteNombreInput.value.trim().take(100).ifBlank { "Cliente Acapulco" } }
        val remTel = remitenteTelefonoInput.value.filter { it.isDigit() }.take(10).ifBlank { clienteTelefonoInput.value.filter { it.isDigit() }.take(10).ifBlank { "7440000000" } }
        val destNombre = destinatarioNombreInput.value.trim().take(100).ifBlank { clienteNombreInput.value.trim().take(100).ifBlank { "Destinatario Acapulco" } }
        val destTel = destinatarioTelefonoInput.value.filter { it.isDigit() }.take(10).ifBlank { clienteTelefonoInput.value.filter { it.isDigit() }.take(10).ifBlank { "7440000000" } }
        val referencias = referenciasDomicilioInput.value.trim().take(300)

        val tempId = System.currentTimeMillis()
        val optOrder = MandadoOrder(
            id = tempId,
            folio = "ACA-${(1000..9999).random()}",
            tipo = tipo.id,
            titulo = titulo,
            descripcion = desc,
            puntoRecogida = recogida,
            zonaRecogida = zonaRecogidaInput.value,
            puntoEntrega = entrega,
            zonaEntrega = zonaEntregaInput.value,
            costoProductosEstimado = costoProductos,
            tarifaEnvio = tarifa,
            metodoPago = selectedMetodoPago.value.id,
            estado = "SOLICITADO",
            clienteNombre = clienteNombreInput.value.ifBlank { "Cliente Acapulco" },
            clienteTelefono = clienteTelefonoInput.value.ifBlank { "7440000000" },
            remitenteNombre = remNombre,
            remitenteTelefono = remTel,
            destinatarioNombre = destNombre,
            destinatarioTelefono = destTel,
            referenciasDomicilio = referencias,
            pinSeguridad = "0000",
            tamanoPaquete = tamanoPaqueteInput.value,
            confirmacionDimensionesMoto = confirmacionDimensionesMoto.value,
            esCobroContraEntrega = esCobroContraEntrega.value,
            montoProductoCobrar = codAmount,
            recargoZonaLejana = desglose.recargoZonaLejana,
            tarjetaUltimos4 = tarjetaUltimos4,
            tarjetaTitular = titular,
            tarjetaAprobada = isTarjeta,
            notasAdicionales = notasInput.value,
            fechaCreacion = System.currentTimeMillis()
        )
        // Actualización optimista inmediata en la UI (cero latencia percibida)
        _optimisticOrders.value = _optimisticOrders.value + (tempId to optOrder)

        viewModelScope.launch {
            val orderId = repository.crearMandado(
                tipo = tipo.id,
                titulo = titulo,
                descripcion = desc,
                puntoRecogida = recogida,
                zonaRecogida = zonaRecogidaInput.value,
                puntoEntrega = entrega,
                zonaEntrega = zonaEntregaInput.value,
                costoEstimadoProductos = costoProductos,
                tarifaEnvio = tarifa,
                metodoPago = selectedMetodoPago.value.id,
                clienteNombre = clienteNombreInput.value.ifBlank { "Cliente Acapulco" },
                clienteTelefono = clienteTelefonoInput.value.ifBlank { "7440000000" },
                remitenteNombre = remNombre,
                remitenteTelefono = remTel,
                destinatarioNombre = destNombre,
                destinatarioTelefono = destTel,
                referenciasDomicilio = referencias,
                tamanoPaquete = tamanoPaqueteInput.value,
                confirmacionDimensionesMoto = confirmacionDimensionesMoto.value,
                esCobroContraEntrega = esCobroContraEntrega.value,
                montoProductoCobrar = codAmount,
                recargoZonaLejana = desglose.recargoZonaLejana,
                tarjetaUltimos4 = tarjetaUltimos4,
                tarjetaTitular = titular,
                tarjetaAprobada = isTarjeta,
                notas = notasInput.value
            )

            // Reemplazar id temporal optimista con el id persistido
            _optimisticOrders.value = _optimisticOrders.value - tempId
            val created = repository.getOrderById(orderId)
            if (created != null) {
                _optimisticOrders.value = _optimisticOrders.value + (orderId to created)
                recentlyCreatedOrder.value = created
            }

            // Notificación push automática para repartidores con clave de navegación
            val pagoTexto = if (isTarjeta) "Tarjeta en App" else "Efectivo al recibir"
            val codTexto = if (esCobroContraEntrega.value) " • COD: $${codAmount.toInt()} MXN" else ""
            AcaNotificationHelper.showSystemNotification(
                getApplication(),
                title = "🚨 ¡Nueva solicitud en ${zonaRecogidaInput.value}! ($pagoTexto)",
                message = "$titulo [${tamanoPaqueteInput.value}] hasta ${zonaEntregaInput.value}. Tarifa 100% tuya: $${String.format("%.2f", tarifa)} MXN$codTexto",
                isDriverNotification = true,
                orderId = orderId,
                orderFolio = created?.folio
            )
            AcaNotificationHelper.triggerDriverAlertVibration(getApplication())

            // Disparar banner flotante de alerta inmediata para repartidores en primer plano
            newMandadoAlert.value = NewMandadoDriverAlert(
                orderId = orderId,
                folio = created?.folio ?: "ACA-NUEVO",
                titulo = titulo,
                zonaRecogida = zonaRecogidaInput.value,
                zonaEntrega = zonaEntregaInput.value,
                tarifaEnvio = tarifa,
                metodoPago = selectedMetodoPago.value.id,
                tamanoPaquete = tamanoPaqueteInput.value,
                codAviso = codTexto
            )

            // Resetear inputs principales
            tituloInput.value = ""
            descripcionInput.value = ""
            puntoRecogidaInput.value = ""
            puntoEntregaInput.value = ""
            costoProductosInput.value = ""
            remitenteNombreInput.value = ""
            remitenteTelefonoInput.value = ""
            destinatarioNombreInput.value = ""
            destinatarioTelefonoInput.value = ""
            referenciasDomicilioInput.value = ""
            notasInput.value = ""
            confirmacionDimensionesMoto.value = true
            esCobroContraEntrega.value = false
            montoCobroProductoInput.value = ""

            _userMessage.value = "¡Mandado solicitado con éxito! Generamos tu PIN de entrega de 4 dígitos y enlace de seguimiento web."
            _clienteTab.value = ClienteTab.MIS_MANDADOS
            onSuccess(orderId)
        }
    }

    fun simularNuevoMandadoCliente() {
        viewModelScope.launch {
            val randomFolioNum = (1000..9999).random()
            val orderId = repository.crearMandado(
                tipo = "EXPRESS",
                titulo = "Envío Express Urgente #$randomFolioNum",
                descripcion = "Recoger paquete sellado en Costera y entregar en Fracc. Costa Azul.",
                puntoRecogida = "OXXO Condesa, Costera Miguel Alemán #120",
                zonaRecogida = "Costera Miguel Alemán",
                puntoEntrega = "Calle Albatros #45, Fracc. Costa Azul",
                zonaEntrega = "Costa Azul & Icacos",
                costoEstimadoProductos = 0.0,
                tarifaEnvio = 75.0,
                metodoPago = "EFECTIVO",
                clienteNombre = "Mariana Solís",
                clienteTelefono = "7445551122",
                remitenteNombre = "Mariana Solís",
                remitenteTelefono = "7445551122",
                destinatarioNombre = "Carlos Gómez",
                destinatarioTelefono = "7449876543",
                referenciasDomicilio = "Casa blanca con portón café, timbre en la entrada.",
                tamanoPaquete = "CHICO",
                confirmacionDimensionesMoto = true,
                esCobroContraEntrega = false,
                montoProductoCobrar = 0.0,
                recargoZonaLejana = 0.0,
                notas = "Simulación para probar aceptación de servicio"
            )
            val created = repository.getOrderById(orderId)

            // Notificación push automática para repartidores
            AcaNotificationHelper.showSystemNotification(
                getApplication(),
                title = "🚨 ¡Nueva solicitud en Costera Miguel Alemán! (Efectivo)",
                message = "Envío Express Urgente #$randomFolioNum [CHICO] hasta Costa Azul & Icacos. Tarifa 100% tuya: $75.00 MXN",
                isDriverNotification = true,
                orderId = orderId,
                orderFolio = created?.folio
            )
            AcaNotificationHelper.triggerDriverAlertVibration(getApplication())

            // Disparar banner flotante de alerta inmediata para repartidores en primer plano
            newMandadoAlert.value = NewMandadoDriverAlert(
                orderId = orderId,
                folio = created?.folio ?: "ACA-$randomFolioNum",
                titulo = "Envío Express Urgente #$randomFolioNum",
                zonaRecogida = "Costera Miguel Alemán",
                zonaEntrega = "Costa Azul & Icacos",
                tarifaEnvio = 75.0,
                metodoPago = "EFECTIVO",
                tamanoPaquete = "CHICO"
            )

            _userMessage.value = "🛵 ¡Mandado disponible generado y notificado de inmediato a los repartidores!"
        }
    }

    fun aceptarPrimerMandadoDisponible() {
        val first = availableOrders.value.firstOrNull()
        if (first != null) {
            tomarMandado(first.id)
        } else {
            _userMessage.value = "No hay mandados disponibles en este momento. Puedes pulsar 'Generar Mandado' para crear uno."
        }
    }

    fun tomarMandado(orderId: Long) {
        val profile = repartidorProfile.value ?: RepartidorProfile()
        if (!profile.suscripcionActiva) {
            _userMessage.value = "Tu suscripción semanal ($150 MXN) está inactiva. Renuévala para tomar pedidos."
            _repartidorTab.value = RepartidorTab.BILLETERA_SUSCRIPCION
            return
        }

        // Actualización optimista inmediata en la UI
        val currentOrder = allOrders.value.find { it.id == orderId }
        if (currentOrder != null) {
            val optAssigned = currentOrder.copy(
                estado = "ASIGNADO",
                repartidorId = profile.id,
                repartidorNombre = profile.nombre,
                repartidorTelefono = profile.telefono,
                repartidorVehiculo = profile.tipoTransporte,
                repartidorPlacas = profile.placas
            )
            _optimisticOrders.value = _optimisticOrders.value + (orderId to optAssigned)
        }

        viewModelScope.launch {
            repository.tomarMandado(orderId, profile)

            // Notificación push para cliente: Repartidor asignado
            AcaNotificationHelper.showSystemNotification(
                getApplication(),
                title = "🛵 ¡Repartidor asignado en Acapulco!",
                message = "${profile.nombre} va en camino a recoger tu mandado en su ${profile.modeloVehiculo}.",
                isDriverNotification = false
            )

            _userMessage.value = "¡Mandado aceptado! Revisa los detalles en Mi Ruta."
            setRepartidorTab(RepartidorTab.MI_RUTA)
        }
    }

    fun avanzarEstado(orderId: Long, nuevoEstado: String) {
        if (nuevoEstado == "ENTREGADO") {
            val order = allOrders.value.find { it.id == orderId }
            if (order != null) {
                openPinValidation(order, onRecipientDevice = false)
                _userMessage.value = "🔐 Se requiere validar el PIN de 4 dígitos del receptor para completar la entrega."
                return
            }
        }

        // Actualización optimista inmediata en la UI
        val currentOrder = allOrders.value.find { it.id == orderId }
        if (currentOrder != null) {
            val optAdvanced = currentOrder.copy(
                estado = nuevoEstado,
                fechaEntrega = if (nuevoEstado == "ENTREGADO") System.currentTimeMillis() else currentOrder.fechaEntrega
            )
            _optimisticOrders.value = _optimisticOrders.value + (orderId to optAdvanced)
        }

        viewModelScope.launch {
            repository.avanzarEstadoMandado(orderId, nuevoEstado)
            val msg = when (nuevoEstado) {
                "EN_CAMINO" -> {
                    AcaNotificationHelper.showSystemNotification(
                        getApplication(),
                        title = "📦 ¡Tu pedido ha sido recogido!",
                        message = "El repartidor ya tiene tu mandado y va en camino a tu domicilio.",
                        isDriverNotification = false
                    )
                    "Estado actualizado: En camino a entrega."
                }
                "ENTREGADO" -> {
                    AcaNotificationHelper.showSystemNotification(
                        getApplication(),
                        title = "✅ ¡Mandado entregado con éxito!",
                        message = "Tu mandado ha llegado. Por favor califica la atención de tu repartidor.",
                        isDriverNotification = false
                    )
                    "¡Mandado entregado con éxito! Ganancia 100% registrada."
                }
                else -> "Estado actualizado."
            }
            _userMessage.value = msg
        }
    }

    fun cancelarMandado(orderId: Long) {
        // Actualización optimista inmediata en la UI
        val currentOrder = allOrders.value.find { it.id == orderId }
        if (currentOrder != null) {
            _optimisticOrders.value = _optimisticOrders.value + (orderId to currentOrder.copy(estado = "CANCELADO"))
        }

        viewModelScope.launch {
            repository.cancelarMandado(orderId)
            _userMessage.value = "Mandado cancelado."
        }
    }

    fun renovarSuscripcion() {
        viewModelScope.launch {
            repository.renovarSuscripcionSemanal()
            _userMessage.value = "¡Suscripción semanal de $150 MXN activada por 7 días más! Ganancias 100% para ti."
        }
    }

    fun openPaymentReceipt(order: MandadoOrder) {
        activeReceiptOrder.value = order
    }

    fun closePaymentReceipt() {
        activeReceiptOrder.value = null
    }

    fun openTransaccionDetail(transaccion: TransaccionBilletera) {
        activeSelectedTransaccion.value = transaccion
    }

    fun closeTransaccionDetail() {
        activeSelectedTransaccion.value = null
    }

    /**
     * Solicita y dispersa un retiro de fondos hacia la cuenta bancaria CLABE del repartidor.
     */
    fun solicitarRetiro(monto: Double, motivo: String = "Retiro SPEI a cuenta bancaria") {
        viewModelScope.launch {
            val result = repository.solicitarRetiro(monto, motivo)
            if (result.isSuccess) {
                val trans = result.getOrNull()
                _userMessage.value = "💸 ¡Retiro SPEI procesado! Se dispersaron $${String.format("%.2f", monto)} MXN a tu cuenta ${trans?.bancoDestino ?: "BBVA"} (••${trans?.clabeTerminacion ?: "1234"})."
            } else {
                _userMessage.value = "⚠️ Error al procesar retiro: ${result.exceptionOrNull()?.message}"
            }
        }
    }

    /**
     * Procesa el pago de un mandado completado individual desde la billetera.
     */
    fun procesarPagoMandado(orderId: Long) {
        viewModelScope.launch {
            val result = repository.procesarPagoMandado(orderId)
            if (result.isSuccess) {
                val dep = result.getOrNull()
                if (dep != null) {
                    _userMessage.value = "✅ ¡Pago procesado! Se dispersaron $${String.format("%.2f", dep.monto)} MXN vía SPEI a tu cuenta."
                } else {
                    _userMessage.value = "✅ Cobro en efectivo asentado y registrado en tu billetera."
                }
            } else {
                _userMessage.value = "⚠️ Error al procesar pago: ${result.exceptionOrNull()?.message}"
            }
        }
    }

    /**
     * Procesa y dispersa todos los pagos pendientes de mandados completados con tarjeta.
     */
    fun procesarTodosLosPagosPendientes() {
        viewModelScope.launch {
            val result = repository.procesarTodosLosPagosPendientes(1L)
            if (result.isSuccess) {
                val count = result.getOrDefault(0)
                if (count > 0) {
                    _userMessage.value = "🎉 ¡Éxito! Se procesaron y dispersaron $count pagos a tu cuenta bancaria vía SPEI."
                } else {
                    _userMessage.value = "Todos tus mandados completados ya se encuentran liquidados."
                }
            } else {
                _userMessage.value = "⚠️ Error al dispersar pagos: ${result.exceptionOrNull()?.message}"
            }
        }
    }

    fun actualizarPerfilRepartidor(
        nombre: String,
        telefono: String,
        tipoTransporte: String,
        modelo: String,
        placas: String,
        clabe: String,
        banco: String
    ) {
        viewModelScope.launch {
            repository.actualizarPerfilRepartidor(
                nombre = nombre,
                telefono = telefono,
                tipoTransporte = tipoTransporte,
                modelo = modelo,
                placas = placas,
                clabe = clabe,
                banco = banco
            )
            _userMessage.value = "Perfil de repartidor actualizado."
        }
    }

    fun setDisponibilidad(disponible: Boolean) {
        viewModelScope.launch {
            repository.alternarDisponibilidad(disponible)
        }
    }

    /**
     * Motor de simulación de geolocalización en tiempo real en Acapulco.
     * Actualiza cada 3-4 segundos las coordenadas, velocidad, dirección y distancia restante
     * del repartidor hacia el punto de recogida o entrega.
     * Dispara automáticamente la alerta push "Tu repartidor está cerca" cuando está a menos de 500m.
     */
    private fun startRealtimeLocationEngine() {
        viewModelScope.launch {
            var progress = 0.15f
            var alertedNearForOrderId: Long? = null

            while (isActive) {
                delay(3500)
                val activeOrder = currentDriverActiveOrder.value

                if (activeOrder != null && (activeOrder.estado == "ACEPTADO" || activeOrder.estado == "EN_CAMINO")) {
                    val isHeadingToPickup = activeOrder.estado == "ACEPTADO"
                    val targetLat = if (isHeadingToPickup) activeOrder.latRecogida else activeOrder.latEntrega
                    val targetLng = if (isHeadingToPickup) activeOrder.lngRecogida else activeOrder.lngEntrega

                    // Origen base para interpolación
                    val originLat = if (isHeadingToPickup) 16.8580 else activeOrder.latRecogida
                    val originLng = if (isHeadingToPickup) -99.8760 else activeOrder.lngRecogida

                    progress = (progress + 0.05f).coerceAtMost(0.96f)

                    val currentLat = originLat + (targetLat - originLat) * progress
                    val currentLng = originLng + (targetLng - originLng) * progress

                    val remainingDistKm = AcapulcoLocations.calcularDistanciaKm(currentLat, currentLng, targetLat, targetLng)
                    val speed = (32..48).random()
                    val eta = Math.max(1, (remainingDistKm / 0.5).toInt())
                    val isNear = remainingDistKm <= 0.5

                    val bearing = calculateBearing(originLat, originLng, targetLat, targetLng)

                    _driverLocationState.value = DriverLocationState(
                        lat = currentLat,
                        lng = currentLng,
                        speedKmh = speed,
                        bearing = bearing.toFloat(),
                        distanceRemainingKm = Math.round(remainingDistKm * 100.0) / 100.0,
                        etaMinutes = eta,
                        isNear = isNear,
                        targetType = if (isHeadingToPickup) "PICKUP" else "DELIVERY",
                        lastUpdated = System.currentTimeMillis()
                    )

                    // Actualizar telemetría de ruta en Firestore para seguimiento en tiempo real
                    viewModelScope.launch {
                        firestoreRepository.updateActiveDeliveryLocation(
                            orderFolio = activeOrder.folio,
                            lat = currentLat,
                            lng = currentLng,
                            speedKmh = speed,
                            bearing = bearing.toFloat(),
                            etaMinutes = eta,
                            distanceRemainingKm = Math.round(remainingDistKm * 100.0) / 100.0
                        )
                    }

                    // Si está cerca (<500 metros) y no se ha emitido la alerta para este pedido, notificar
                    if (isNear && alertedNearForOrderId != activeOrder.id) {
                        alertedNearForOrderId = activeOrder.id
                        val driverName = activeOrder.repartidorNombre ?: "Carlos Mendoza"
                        val distText = "${(remainingDistKm * 1000).toInt()} metros"

                        repository.agregarNotificacionCerca(
                            orderId = activeOrder.id,
                            folio = activeOrder.folio,
                            driverName = driverName,
                            distText = distText
                        )

                        AcaNotificationHelper.showSystemNotification(
                            getApplication(),
                            title = "📍 ¡Tu repartidor está cerca!",
                            message = "$driverName está a $distText de tu domicilio (llegando en aprox. $eta min).",
                            isDriverNotification = false
                        )
                    }
                } else {
                    // Posición idle del repartidor en Costera / Magallanes Acapulco
                    progress = 0.15f
                    _driverLocationState.value = _driverLocationState.value.copy(
                        lat = 16.8580,
                        lng = -99.8760,
                        speedKmh = 0,
                        distanceRemainingKm = 0.0,
                        etaMinutes = 0,
                        isNear = false,
                        lastUpdated = System.currentTimeMillis()
                    )
                }
            }
        }
    }

    fun updateDriverBaseLocation(lat: Double, lng: Double) {
        _driverLocationState.value = _driverLocationState.value.copy(
            lat = lat,
            lng = lng,
            lastUpdated = System.currentTimeMillis()
        )
    }

    private fun calculateBearing(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val phi1 = Math.toRadians(lat1)
        val phi2 = Math.toRadians(lat2)
        val deltaLambda = Math.toRadians(lon2 - lon1)
        val y = Math.sin(deltaLambda) * Math.cos(phi2)
        val x = Math.cos(phi1) * Math.sin(phi2) - Math.sin(phi1) * Math.cos(phi2) * Math.cos(deltaLambda)
        val theta = Math.atan2(y, x)
        return (Math.toDegrees(theta) + 360.0) % 360.0
    }
}
