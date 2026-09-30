package com.example

import com.example.data.model.MetodoPago
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class PaymentSelectionTest {

    @Test
    fun testPaymentMethodToggleValues() {
        var currentSelection = MetodoPago.EFECTIVO
        assertEquals("EFECTIVO", currentSelection.id)

        // Toggle to Credit/Debit Card
        currentSelection = MetodoPago.TARJETA
        assertEquals("TARJETA", currentSelection.id)
        assertNotEquals(MetodoPago.EFECTIVO, currentSelection)

        // Toggle back to Cash Payment
        currentSelection = MetodoPago.EFECTIVO
        assertEquals("EFECTIVO", currentSelection.id)
    }

    @Test
    fun testPaymentMethodEnumLabels() {
        val card = MetodoPago.TARJETA
        val cash = MetodoPago.EFECTIVO

        assertEquals("TARJETA", card.id)
        assertEquals("EFECTIVO", cash.id)
    }
}
