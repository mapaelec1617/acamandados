package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.AppDatabase
import com.example.data.model.RepartidorProfile
import com.example.data.model.UserProfile
import com.example.data.repository.UserProfileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

/**
 * Estado reactivo del formulario simplificado de registro de repartidores.
 */
data class RegistroRepartidorFormState(
    val nombre: String = "",
    val telefono: String = "",
    val tipoTransporte: String = "MOTO", // MOTO, MOTONETA, AUTO, BICI
    val modeloVehiculo: String = "",
    val placas: String = "",
    val zonaAcapulcoPreferida: String = "Costera Miguel Alemán",
    val terminosAceptados: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isSuccess: Boolean = false
)

/**
 * ViewModel que gestiona el estado y validación del formulario simplificado
 * para nuevos repartidores de AcaMandados.
 */
class RegistroRepartidorViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application, viewModelScope)
    private val repartidorDao = database.repartidorDao()
    private val userProfileRepository = UserProfileRepository.create(application)

    private val _formState = MutableStateFlow(RegistroRepartidorFormState())
    val formState: StateFlow<RegistroRepartidorFormState> = _formState.asStateFlow()

    fun updateNombre(nombre: String) {
        val sanitized = nombre.take(60)
        _formState.value = _formState.value.copy(
            nombre = sanitized,
            errorMessage = null
        )
    }

    fun updateTelefono(telefono: String) {
        val digitsOnly = telefono.filter { it.isDigit() }.take(10)
        _formState.value = _formState.value.copy(
            telefono = digitsOnly,
            errorMessage = null
        )
    }

    fun updateTipoTransporte(tipo: String) {
        _formState.value = _formState.value.copy(
            tipoTransporte = tipo,
            errorMessage = null
        )
    }

    fun updateModeloVehiculo(modelo: String) {
        val sanitized = modelo.take(60)
        _formState.value = _formState.value.copy(
            modeloVehiculo = sanitized,
            errorMessage = null
        )
    }

    fun updatePlacas(placas: String) {
        val sanitized = placas.uppercase().filter { it.isLetterOrDigit() || it == '-' || it == ' ' }.take(12)
        _formState.value = _formState.value.copy(
            placas = sanitized,
            errorMessage = null
        )
    }

    fun updateZonaPreferida(zona: String) {
        _formState.value = _formState.value.copy(
            zonaAcapulcoPreferida = zona,
            errorMessage = null
        )
    }

    fun updateTerminosAceptados(aceptados: Boolean) {
        _formState.value = _formState.value.copy(
            terminosAceptados = aceptados,
            errorMessage = null
        )
    }

    fun validarFormulario(): String? {
        val state = _formState.value
        val nombre = state.nombre.trim()
        if (nombre.length < 3) {
            return "Por favor ingresa tu nombre completo (mínimo 3 caracteres)."
        }
        val telefono = state.telefono.trim()
        if (telefono.length != 10) {
            return "El número de teléfono debe contener exactamente 10 dígitos (Ej. 744 123 4567)."
        }
        val modelo = state.modeloVehiculo.trim()
        if (modelo.isBlank()) {
            return "Ingresa la marca y modelo de tu vehículo (Ej. Italika FT150 / Nissan Versa)."
        }
        if (state.tipoTransporte != "BICI" && state.placas.trim().isBlank()) {
            return "Ingresa el número de placas de tu vehículo."
        }
        if (!state.terminosAceptados) {
            return "Debes autorizar el tratamiento seguro de datos y aceptar las políticas de seguridad."
        }
        return null
    }

    fun registrarRepartidor(onSuccess: (RepartidorProfile) -> Unit) {
        val error = validarFormulario()
        if (error != null) {
            _formState.value = _formState.value.copy(errorMessage = error)
            return
        }

        val state = _formState.value
        _formState.value = _formState.value.copy(isLoading = true, errorMessage = null)

        viewModelScope.launch {
            try {
                val profile = RepartidorProfile(
                    id = 1L,
                    nombre = state.nombre.trim(),
                    telefono = state.telefono.trim(),
                    tipoTransporte = state.tipoTransporte,
                    modeloVehiculo = state.modeloVehiculo.trim(),
                    placas = if (state.tipoTransporte == "BICI") "BICI-LOCAL" else state.placas.trim().uppercase(),
                    suscripcionActiva = true,
                    suscripcionMontoSemanal = 150.0,
                    disponibleParaMandados = true,
                    pedidosCompletados = 0,
                    calificacion = 5.0
                )

                // Persistir en base de datos local
                repartidorDao.insertOrUpdateProfile(profile)

                // Guardar también en UserProfileRepository (Firestore / local) en segundo plano si está disponible
                launch {
                    try {
                        val userProfile = UserProfile.fromRepartidorProfile(profile, uid = "rep_${System.currentTimeMillis()}")
                            .copy(zonaFrecuente = state.zonaAcapulcoPreferida)
                        withTimeoutOrNull(1500L) {
                            userProfileRepository.saveUserProfile(userProfile)
                        }
                    } catch (_: Throwable) {
                        // Modo offline o entorno de prueba
                    }
                }

                _formState.value = _formState.value.copy(
                    isLoading = false,
                    isSuccess = true,
                    errorMessage = null
                )

                onSuccess(profile)
            } catch (e: Exception) {
                _formState.value = _formState.value.copy(
                    isLoading = false,
                    errorMessage = "Error al completar el registro: ${e.message ?: "Intenta nuevamente"}"
                )
            }
        }
    }
}
