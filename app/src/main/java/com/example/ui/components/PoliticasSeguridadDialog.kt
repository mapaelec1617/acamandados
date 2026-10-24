package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.*

/**
 * Diálogo de Políticas de Seguridad y Aviso de Privacidad Integral de AcaMandados Express,
 * cumpliendo con la Ley Federal de Protección de Datos Personales en Posesión de los Particulares (LFPDPPP)
 * y los estándares de Privacidad y Seguridad de Datos de Google Play Developer Program.
 */
@Composable
fun PoliticasSeguridadDialog(
    onDismiss: () -> Unit,
    onAccept: () -> Unit = onDismiss,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Seguridad", "Aviso Privacidad", "Términos")

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.88f)
                .testTag("dialog_politicas_seguridad")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Cabecera con Icono de Escudo de Seguridad
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(CircleShape)
                                .background(AcaAzulConfianza.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = null,
                                tint = AcaAzulConfianza,
                                modifier = Modifier.size(26.dp)
                            )
                        }

                        Column {
                            Text(
                                text = "Políticas de Seguridad",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = AcaAzulDark
                            )
                            Text(
                                text = "Protección & Autorización de Datos",
                                fontSize = 11.5.sp,
                                color = AcaGrisSubtle
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("btn_close_politicas")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cerrar",
                            tint = Color(0xFF64748B)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Selector de Pestañas
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color(0xFFF1F5F9),
                    contentColor = AcaAzulConfianza,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                ) {
                    tabs.forEachIndexed { index, title ->
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
                            modifier = Modifier.testTag("tab_politicas_$index")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Contenido Scrolleable según la pestaña
                val scrollState = rememberScrollState()
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(scrollState),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    when (selectedTab) {
                        0 -> TabSeguridadContent()
                        1 -> TabPrivacidadContent()
                        2 -> TabTerminosContent()
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Botones de Acción
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                            .testTag("btn_cerrar_politicas")
                    ) {
                        Text("Cerrar", color = AcaGrisEquilibrio, fontWeight = FontWeight.SemiBold)
                    }

                    Button(
                        onClick = onAccept,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AcaAzulConfianza),
                        modifier = Modifier
                            .weight(1.3f)
                            .height(46.dp)
                            .testTag("btn_aceptar_politicas")
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Entendido y Acepto", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun TabSeguridadContent() {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        PoliticaItemCard(
            icon = Icons.Default.Pin,
            title = "PIN de Validación Obligatorio (Uber Connect)",
            description = "Cada mandado genera un código PIN aleatorio de 4 dígitos. Para completar la entrega, el repartidor debe solicitar este PIN al receptor y validarlo en su app, garantizando que el paquete se entrega exactamente a la persona correcta.",
            color = AcaTurquesaDark
        )

        PoliticaItemCard(
            icon = Icons.Default.VerifiedUser,
            title = "Verificación de Repartidores Locales",
            description = "Todos los repartidores de AcaMandados pasan por un proceso de verificación que incluye identificación oficial (INE), licencia de conducir vigente, comprobante de domicilio en Acapulco y validación vehicular de su motocicleta.",
            color = AcaAzulConfianza
        )

        PoliticaItemCard(
            icon = Icons.Default.Payments,
            title = "Tope de Efectivo por Seguridad ($500 MXN)",
            description = "Para proteger la integridad física de repartidores y clientes en las diferentes zonas de Acapulco, el cobro de productos en efectivo está limitado a un máximo de $500 MXN por mandado.",
            color = Color(0xFF0D9488)
        )

        PoliticaItemCard(
            icon = Icons.Default.AddAPhoto,
            title = "Comprobante Fotográfico de Condición",
            description = "El repartidor puede capturar una foto del paquete tanto en la recolección como en la entrega (ej. paquete sellado, sobre cerrado) para dejar constancia del estado del envío y evitar cualquier disputa.",
            color = Color(0xFF7C3AED)
        )

        PoliticaItemCard(
            icon = Icons.Default.Lock,
            title = "Cifrado de Datos y Reglas de Nube",
            description = "Las comunicaciones se realizan mediante conexiones HTTPS/TLS cifradas con Firebase y Firestore Security Rules. Los enlaces de seguimiento web públicos utilizan tokens efímeros y no exponen credenciales de usuario.",
            color = Color(0xFFEA580C)
        )
    }
}

@Composable
private fun TabPrivacidadContent() {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color(0xFFF8FAFC),
            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Aviso de Privacidad Integral",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = AcaAzulDark
                )
                Text(
                    text = "AcaMandados Express, con domicilio en Acapulco, Guerrero, México, es el responsable del tratamiento y protección de sus datos personales conforme a la Ley Federal de Protección de Datos Personales en Posesión de los Particulares (LFPDPPP) y las políticas de Privacidad de Google Play.",
                    fontSize = 11.5.sp,
                    color = AcaGrisEquilibrio,
                    lineHeight = 16.sp
                )
            }
        }

        PoliticaItemCard(
            icon = Icons.Default.LocationOn,
            title = "Datos de Geolocalización en Tiempo Real",
            description = "Recopilamos datos de ubicación en tiempo real mientras haya un mandado activo para trazar rutas óptimas, calcular tarifas exactas en Acapulco y permitir el seguimiento en vivo al cliente y receptor. Esta ubicación no se comparte con terceros publicitarios.",
            color = Color(0xFF0284C7)
        )

        PoliticaItemCard(
            icon = Icons.Default.ContactPhone,
            title = "Datos de Contacto e Identificación",
            description = "Tu nombre, teléfono y dirección de entrega se utilizan exclusivamente para la coordinación del mandado. El repartidor asignado únicamente tiene acceso temporal a estos datos durante la vigencia del servicio.",
            color = Color(0xFF059669)
        )

        PoliticaItemCard(
            icon = Icons.Default.DeleteOutline,
            title = "Derechos ARCO y Eliminación de Cuenta",
            description = "En cualquier momento puedes ejercer tus derechos de Acceso, Rectificación, Cancelación y Oposición (ARCO). Asimismo, puedes solicitar la eliminación permanente de tu cuenta y todos tus datos personales directamente desde la sección de Perfil de la app.",
            color = Color(0xFFDC2626)
        )

        PoliticaItemCard(
            icon = Icons.Default.Share,
            title = "No Venta de Datos Personales",
            description = "AcaMandados NO vende, renta ni comercializa tus datos personales con terceras empresas para prospección comercial, marketing o publicidad no solicitada.",
            color = Color(0xFFD97706)
        )
    }
}

@Composable
private fun TabTerminosContent() {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        PoliticaItemCard(
            icon = Icons.Default.Rule,
            title = "Naturaleza del Servicio de Mandados",
            description = "AcaMandados Express es una plataforma tecnológica que conecta usuarios que solicitan mandados o transporte de paquetes con repartidores independientes certificados en motocicleta en Acapulco.",
            color = AcaAzulConfianza
        )

        PoliticaItemCard(
            icon = Icons.Default.Block,
            title = "Artículos Prohibidos",
            description = "Queda estrictamente prohibido solicitar el transporte de sustancias ilícitas, armas, materiales peligrosos, objetos de valor desproporcionado no declarados o cualquier artículo que infrinja las leyes mexicanas.",
            color = Color(0xFFB91C1C)
        )

        PoliticaItemCard(
            icon = Icons.Default.AccountBalanceWallet,
            title = "Tarifas, Pagos y Propinas",
            description = "Las tarifas base y por kilómetro se calculan transparentemente antes de confirmar la solicitud. Los pagos con tarjeta y transferencias SPEI son procesados de forma segura sin almacenar números de tarjeta en texto plano.",
            color = Color(0xFF047857)
        )

        PoliticaItemCard(
            icon = Icons.Default.SupportAgent,
            title = "Aclaraciones y Atención Local",
            description = "Cuentas con atención directa de nuestro equipo local en Acapulco vía WhatsApp y teléfono para resolver incidencias de entrega, reprogramaciones o devoluciones de cobros no aplicables.",
            color = AcaTurquesaDark
        )
    }
}

@Composable
private fun PoliticaItemCard(
    icon: ImageVector,
    title: String,
    description: String,
    color: Color
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(20.dp)
                )
            }

            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = AcaAzulDark
                )
                Text(
                    text = description,
                    fontSize = 11.5.sp,
                    color = AcaGrisEquilibrio,
                    lineHeight = 15.5.sp
                )
            }
        }
    }
}
