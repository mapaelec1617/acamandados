package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Contactless
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.MetodoPago
import com.example.ui.theme.AcaAzulConfianza
import com.example.ui.theme.AcaAzulContainer
import com.example.ui.theme.AcaAzulDark
import com.example.ui.theme.AcaGrisEquilibrio
import com.example.ui.theme.AcaGrisSubtle
import com.example.ui.theme.AcaSurfaceVariant
import com.example.ui.theme.AcaTurquesa
import com.example.ui.theme.AcaTurquesaContainer
import com.example.ui.theme.AcaTurquesaDark
import com.example.ui.theme.AcaTurquesaLight

/**
 * Componente modular de selección de método de pago para la pantalla de solicitud de mandado.
 * Incluye dos botones conmutables (toggleable):
 * 1. 'Credit/Debit Card' (Tarjeta de Crédito / Débito)
 * 2. 'Cash Payment' (Pago en Efectivo)
 *
 * El estado captura la selección ([MetodoPago.TARJETA] o [MetodoPago.EFECTIVO])
 * para asociarse al registro del pedido en base de datos.
 */
@Composable
fun PaymentSelectionComponent(
    selectedMetodo: MetodoPago,
    onMetodoSelected: (MetodoPago) -> Unit,
    modifier: Modifier = Modifier,
    tarjetaNumero: String = "",
    tarjetaExpiracion: String = "",
    tarjetaCvv: String = "",
    tarjetaTitular: String = "",
    clienteNombre: String = "",
    onTarjetaNumeroChange: ((String) -> Unit)? = null,
    onTarjetaExpiracionChange: ((String) -> Unit)? = null,
    onTarjetaCvvChange: ((String) -> Unit)? = null,
    totalTarifa: Double? = null,
    isZeroCashRestricted: Boolean = false
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(16.dp))
            .testTag("payment_selection_component")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Cabecera con indicador de estado actual
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "5. Método de Pago del Envío",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = AcaAzulDark
                    )
                    Text(
                        text = "Selecciona cómo se liquidará el viaje",
                        fontSize = 11.5.sp,
                        color = AcaGrisSubtle
                    )
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (selectedMetodo == MetodoPago.TARJETA) AcaAzulContainer else AcaTurquesaContainer
                ) {
                    Text(
                        text = if (selectedMetodo == MetodoPago.TARJETA) "💳 TARJETA DIGITAL" else "💵 PAGO EFECTIVO",
                        color = if (selectedMetodo == MetodoPago.TARJETA) AcaAzulConfianza else AcaTurquesaDark,
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            // Dos botones toggleables: 'Credit/Debit Card' y 'Cash Payment'
            PaymentToggleButtons(
                selectedMetodo = selectedMetodo,
                onMetodoSelected = onMetodoSelected,
                isZeroCashRestricted = isZeroCashRestricted,
                modifier = Modifier.fillMaxWidth()
            )

            // Detalle expandible si seleccionó 'Credit/Debit Card'
            AnimatedVisibility(visible = selectedMetodo == MetodoPago.TARJETA) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    // Tarjeta Virtual Visual Preview
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = AcaAzulDark),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("virtual_card_preview")
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CreditCard,
                                        contentDescription = null,
                                        tint = AcaTurquesaLight,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "AcaMandados Pay Digital",
                                        color = AcaTurquesaLight,
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Icon(
                                    imageVector = Icons.Default.Contactless,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Text(
                                text = tarjetaNumero.ifBlank { "•••• •••• •••• 4820" },
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 2.sp
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "TITULAR",
                                        color = Color.White.copy(alpha = 0.6f),
                                        fontSize = 8.5.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = tarjetaTitular.ifBlank { clienteNombre.ifBlank { "CLIENTE ACAMANDADOS" } },
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "VENCE",
                                        color = Color.White.copy(alpha = 0.6f),
                                        fontSize = 8.5.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = tarjetaExpiracion.ifBlank { "12/28" },
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }

                    if (onTarjetaNumeroChange != null) {
                        OutlinedTextField(
                            value = tarjetaNumero,
                            onValueChange = { input ->
                                if (input.length <= 19) {
                                    onTarjetaNumeroChange(input.filter { it.isDigit() || it == ' ' })
                                }
                            },
                            label = { Text("Número de Tarjeta (16 dígitos)") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_tarjeta_numero")
                        )
                    }

                    if (onTarjetaExpiracionChange != null && onTarjetaCvvChange != null) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = tarjetaExpiracion,
                                onValueChange = { input ->
                                    if (input.length <= 5) {
                                        onTarjetaExpiracionChange(input)
                                    }
                                },
                                label = { Text("MM/AA") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("input_tarjeta_expiracion")
                            )
                            OutlinedTextField(
                                value = tarjetaCvv,
                                onValueChange = { input ->
                                    if (input.length <= 4 && input.all { it.isDigit() }) {
                                        onTarjetaCvvChange(input)
                                    }
                                },
                                label = { Text("CVV") },
                                singleLine = true,
                                visualTransformation = PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("input_tarjeta_cvv")
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFFEFF6FF),
                        border = BorderStroke(1.dp, Color(0xFFBFDBFE)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.VerifiedUser,
                                contentDescription = null,
                                tint = AcaAzulConfianza,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "El cobro se procesa digitalmente en la app. El repartidor recibe su pago íntegro por transferencia bancaria y NO te cobrará efectivo al entregar.",
                                fontSize = 11.sp,
                                color = AcaAzulDark,
                                lineHeight = 14.5.sp
                            )
                        }
                    }
                }
            }

            // Detalle si seleccionó 'Cash Payment'
            AnimatedVisibility(visible = selectedMetodo == MetodoPago.EFECTIVO) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFFF0FDF4),
                    border = BorderStroke(1.dp, Color(0xFFBBF7D0)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp)
                        .testTag("cash_payment_details")
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = Color(0xFF15803D),
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = if (totalTarifa != null) {
                                "Pagarás $${String.format("%.2f", totalTarifa)} MXN en efectivo directamente al repartidor al momento de recibir el paquete en mano."
                            } else {
                                "Pagarás el total en efectivo directamente al repartidor al momento de recibir tu paquete en mano."
                            },
                            fontSize = 11.sp,
                            color = Color(0xFF14532D),
                            lineHeight = 14.5.sp
                        )
                    }
                }
            }
        }
    }
}

/**
 * Barra de dos botones conmutables (toggleable buttons) para la selección de método de pago:
 * - 'Credit/Debit Card'
 * - 'Cash Payment'
 */
@Composable
fun PaymentToggleButtons(
    selectedMetodo: MetodoPago,
    onMetodoSelected: (MetodoPago) -> Unit,
    modifier: Modifier = Modifier,
    isZeroCashRestricted: Boolean = false,
    onDarkBackground: Boolean = false
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        val isCard = selectedMetodo == MetodoPago.TARJETA
        val cardBgColor by animateColorAsState(
            targetValue = if (onDarkBackground) {
                if (isCard) Color(0xFF1E3A8A) else Color.White.copy(alpha = 0.10f)
            } else {
                if (isCard) Color(0xFFEEF2FF) else AcaSurfaceVariant
            },
            animationSpec = tween(200),
            label = "card_bg"
        )
        val cardBorderColor by animateColorAsState(
            targetValue = if (onDarkBackground) {
                if (isCard) Color(0xFF93C5FD) else Color.White.copy(alpha = 0.25f)
            } else {
                if (isCard) AcaAzulConfianza else Color(0xFFCBD5E1)
            },
            animationSpec = tween(200),
            label = "card_border"
        )

        // Botón 1: 'Credit/Debit Card'
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = cardBgColor,
            border = BorderStroke(if (isCard) 2.dp else 1.dp, cardBorderColor),
            modifier = Modifier
                .weight(1f)
                .height(84.dp)
                .clip(RoundedCornerShape(14.dp))
                .clickable(
                    role = Role.RadioButton,
                    onClick = { onMetodoSelected(MetodoPago.TARJETA) }
                )
                .testTag("toggle_credit_debit_card"),
            tonalElevation = if (isCard) 3.dp else 0.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CreditCard,
                        contentDescription = "Credit/Debit Card",
                        tint = if (onDarkBackground) {
                            if (isCard) Color(0xFF93C5FD) else Color.White.copy(alpha = 0.7f)
                        } else {
                            if (isCard) AcaAzulConfianza else AcaGrisSubtle
                        },
                        modifier = Modifier.size(22.dp)
                    )
                    Icon(
                        imageVector = if (isCard) Icons.Default.RadioButtonChecked else Icons.Default.RadioButtonUnchecked,
                        contentDescription = null,
                        tint = if (onDarkBackground) {
                            if (isCard) Color(0xFF93C5FD) else Color.White.copy(alpha = 0.7f)
                        } else {
                            if (isCard) AcaAzulConfianza else AcaGrisSubtle
                        },
                        modifier = Modifier.size(18.dp)
                    )
                }

                Column {
                    Text(
                        text = "Credit/Debit Card",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (onDarkBackground) Color.White else if (isCard) AcaAzulDark else AcaGrisEquilibrio
                    )
                    Text(
                        text = "Tarjeta Débito/Crédito",
                        fontSize = 10.sp,
                        color = if (onDarkBackground) {
                            if (isCard) Color(0xFF93C5FD) else Color.White.copy(alpha = 0.7f)
                        } else {
                            if (isCard) AcaAzulConfianza else AcaGrisSubtle
                        }
                    )
                }
            }
        }

        val isCash = selectedMetodo == MetodoPago.EFECTIVO
        val cashBgColor by animateColorAsState(
            targetValue = if (onDarkBackground) {
                if (isCash) Color(0xFF065F46) else Color.White.copy(alpha = 0.10f)
            } else {
                if (isCash) Color(0xFFE6FFFA) else AcaSurfaceVariant
            },
            animationSpec = tween(200),
            label = "cash_bg"
        )
        val cashBorderColor by animateColorAsState(
            targetValue = if (onDarkBackground) {
                if (isCash) Color(0xFF6EE7B7) else Color.White.copy(alpha = 0.25f)
            } else {
                if (isCash) AcaTurquesaDark else Color(0xFFCBD5E1)
            },
            animationSpec = tween(200),
            label = "cash_border"
        )

        // Botón 2: 'Cash Payment'
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = if (isZeroCashRestricted) {
                if (onDarkBackground) Color.White.copy(alpha = 0.05f) else Color(0xFFF1F5F9)
            } else cashBgColor,
            border = BorderStroke(
                width = if (isCash) 2.dp else 1.dp,
                color = if (isZeroCashRestricted) Color.Gray.copy(alpha = 0.3f) else cashBorderColor
            ),
            modifier = Modifier
                .weight(1f)
                .height(84.dp)
                .clip(RoundedCornerShape(14.dp))
                .clickable(
                    enabled = !isZeroCashRestricted,
                    role = Role.RadioButton,
                    onClick = { onMetodoSelected(MetodoPago.EFECTIVO) }
                )
                .testTag("toggle_cash_payment"),
            tonalElevation = if (isCash) 3.dp else 0.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Payments,
                        contentDescription = "Cash Payment",
                        tint = if (isZeroCashRestricted) {
                            Color.Gray
                        } else if (onDarkBackground) {
                            if (isCash) Color(0xFF6EE7B7) else Color.White.copy(alpha = 0.7f)
                        } else {
                            if (isCash) AcaTurquesaDark else AcaGrisSubtle
                        },
                        modifier = Modifier.size(22.dp)
                    )
                    Icon(
                        imageVector = if (isCash) Icons.Default.RadioButtonChecked else Icons.Default.RadioButtonUnchecked,
                        contentDescription = null,
                        tint = if (onDarkBackground) {
                            if (isCash) Color(0xFF6EE7B7) else Color.White.copy(alpha = 0.7f)
                        } else {
                            if (isCash) AcaTurquesaDark else AcaGrisSubtle
                        },
                        modifier = Modifier.size(18.dp)
                    )
                }

                Column {
                    Text(
                        text = "Cash Payment",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (isZeroCashRestricted) Color.Gray else if (onDarkBackground) Color.White else if (isCash) AcaAzulDark else AcaGrisEquilibrio
                    )
                    Text(
                        text = if (isZeroCashRestricted) "Deshabilitado (Seguridad)" else "Pago en Efectivo",
                        fontSize = 10.sp,
                        color = if (onDarkBackground) {
                            if (isCash) Color(0xFF6EE7B7) else Color.White.copy(alpha = 0.7f)
                        } else {
                            if (isCash) AcaTurquesaDark else AcaGrisSubtle
                        }
                    )
                }
            }
        }
    }
}
