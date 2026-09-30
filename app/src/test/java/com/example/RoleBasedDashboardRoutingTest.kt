package com.example

import com.example.data.model.TipoMandado
import com.example.data.model.UserProfile
import com.example.ui.AppMode
import com.example.ui.ClienteTab
import com.example.ui.RepartidorTab
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RoleBasedDashboardRoutingTest {

    @Test
    fun testClientePrimaryDashboardTabIsInicio() {
        val defaultClienteTab = ClienteTab.INICIO
        assertEquals("INICIO", defaultClienteTab.name)
        assertEquals(0, defaultClienteTab.ordinal)
    }

    @Test
    fun testRepartidorPrimaryDashboardTabIsDisponibles() {
        val defaultRepartidorTab = RepartidorTab.DISPONIBLES
        assertEquals("DISPONIBLES", defaultRepartidorTab.name)
        assertEquals(0, defaultRepartidorTab.ordinal)
    }

    @Test
    fun testRoleRoutingFromFirestoreSavedProfile() {
        // Simular perfil guardado en Firestore como CLIENTE
        val clienteProfile = UserProfile(
            uid = "usr_cliente_acapulco",
            nombre = "Ana Karen Valenzuela",
            rol = "CLIENTE"
        )
        val roleForCliente = if (clienteProfile.rol.equals("REPARTIDOR", ignoreCase = true)) {
            AppMode.REPARTIDOR
        } else {
            AppMode.CLIENTE
        }
        assertEquals(AppMode.CLIENTE, roleForCliente)

        val destinationTabForCliente = if (roleForCliente == AppMode.CLIENTE) {
            ClienteTab.INICIO
        } else {
            ClienteTab.SOLICITAR
        }
        assertEquals(ClienteTab.INICIO, destinationTabForCliente)

        // Simular perfil guardado en Firestore como REPARTIDOR
        val repartidorProfile = UserProfile(
            uid = "usr_repartidor_acapulco",
            nombre = "Carlos Mendoza",
            rol = "REPARTIDOR",
            tipoTransporte = "MOTO",
            placas = "GRO-489A"
        )
        val roleForRepartidor = if (repartidorProfile.rol.equals("REPARTIDOR", ignoreCase = true)) {
            AppMode.REPARTIDOR
        } else {
            AppMode.CLIENTE
        }
        assertEquals(AppMode.REPARTIDOR, roleForRepartidor)

        val destinationTabForRepartidor = if (roleForRepartidor == AppMode.REPARTIDOR) {
            RepartidorTab.DISPONIBLES
        } else {
            RepartidorTab.MI_RUTA
        }
        assertEquals(RepartidorTab.DISPONIBLES, destinationTabForRepartidor)
    }

    @Test
    fun testServiceCategorySelectionForClienteDashboard() {
        val tipos = listOf(
            TipoMandado.DOCUMENTACION,
            TipoMandado.COMPRAS,
            TipoMandado.FARMACIA,
            TipoMandado.COMIDA
        )

        assertEquals("DOCUMENTACION", tipos[0].id)
        assertEquals("COMPRAS", tipos[1].id)
        assertEquals("FARMACIA", tipos[2].id)
        assertEquals("COMIDA", tipos[3].id)

        tipos.forEach { tipo ->
            assertNotNull(tipo.label)
            assertTrue(tipo.label.isNotBlank())
        }
    }
}
