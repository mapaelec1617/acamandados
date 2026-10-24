package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.*

/**
 * Diálogo de confirmación para eliminación definitiva de cuenta de usuario,
 * cumpliendo con los lineamientos de privacidad de Google Play Developer Policy.
 */
@Composable
fun EliminarCuentaDialog(
    onDismiss: () -> Unit,
    onConfirmDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var confirmedCheckbox by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            border = BorderStroke(1.5.dp, Color(0xFFEF4444).copy(alpha = 0.4f)),
            modifier = modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("dialog_eliminar_cuenta")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFEE2E2)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteForever,
                        contentDescription = "Eliminar cuenta",
                        tint = Color(0xFFDC2626),
                        modifier = Modifier.size(32.dp)
                    )
                }

                Text(
                    text = "¿Eliminar cuenta y datos personales?",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF991B1B),
                    textAlign = TextAlign.Center
                )

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFFEF2F2),
                    border = BorderStroke(1.dp, Color(0xFFFECACA)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.WarningAmber,
                                contentDescription = null,
                                tint = Color(0xFFB91C1C),
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "Acción irreversible",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = Color(0xFF991B1B)
                            )
                        }
                        Text(
                            text = "De acuerdo a las políticas de Google Play y privacidad, al confirmar se eliminarán permanentemente de nuestros servidores:",
                            fontSize = 11.5.sp,
                            color = Color(0xFF7F1D1D),
                            lineHeight = 15.sp
                        )
                        Text(
                            text = "• Tu perfil, nombre, teléfono y correo electrónico\n• Direcciones y domicilios frecuentes guardados\n• Métodos de pago vinculados y sesiones activas\n• Calificaciones y telemetría registrada",
                            fontSize = 11.sp,
                            color = Color(0xFF991B1B),
                            lineHeight = 15.sp,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Checkbox(
                        checked = confirmedCheckbox,
                        onCheckedChange = { confirmedCheckbox = it },
                        colors = CheckboxDefaults.colors(checkedColor = Color(0xFFDC2626)),
                        modifier = Modifier.testTag("checkbox_confirm_delete_account")
                    )
                    Text(
                        text = "Entiendo que perderé acceso y todos mis datos serán eliminados permanentemente.",
                        fontSize = 11.5.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 15.sp,
                        modifier = Modifier.weight(1f)
                    )
                }

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onConfirmDelete,
                        enabled = confirmedCheckbox,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFDC2626),
                            disabledContainerColor = Color(0xFFFCA5A5)
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("btn_confirm_delete_account")
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteForever,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Eliminar mi cuenta definitivamente",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = Color.White
                        )
                    }

                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("btn_cancel_delete_account")
                    ) {
                        Text(
                            text = "Cancelar",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }
}
