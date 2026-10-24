package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.AcapulcoLocations
import com.example.ui.AppMode
import com.example.ui.ClienteTab
import com.example.ui.MandadosViewModel
import com.example.ui.RepartidorTab
import com.example.ui.theme.*

/**
 * Pantalla de Inicio / Bienvenida para AcaMandados.
 * Permite seleccionar de forma clara y destacada entre:
 * 1. "Solicitar un mandado" (Cliente: Comida de la Costera, compras, paquetería express)
 * 2. "Registrarse como repartidor" (Repartidor: Ganancias del 100% sin comisiones abusivas)
 */
@Composable
fun PantallaInicioScreen(
    viewModel: MandadosViewModel,
    modifier: Modifier = Modifier,
    onSelectSolicitarMandado: () -> Unit = {},
    onSelectRegistrarseRepartidor: () -> Unit = {},
    onIniciarSesion: () -> Unit = {},
    onQuickDemoCliente: () -> Unit = {},
    onQuickDemoRepartidor: () -> Unit = {},
    onBack: (() -> Unit)? = null
) {
    if (onBack != null) {
        BackHandler { onBack() }
    }

    val scrollState = rememberScrollState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        AcaAzulDark,
                        AcaAzulConfianza,
                        Color(0xFF0B132B)
                    )
                )
            )
            .testTag("screen_pantalla_inicio")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(scrollState)
                .padding(horizontal = 18.dp, vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            // Botón opcional de regreso si se abre dentro de la app
            if (onBack != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("btn_volver_pantalla_inicio")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cerrar",
                            tint = Color.White
                        )
                    }
                    Text(
                        text = "Volver a mi panel",
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // =========================================================================
            // 1. HEADER DE IDENTIDAD Y MARCA ACAMANDADOS
            // =========================================================================
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 6.dp, bottom = 16.dp)
            ) {
                // Contenedor de Logo con brillo suave
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = Color.White,
                    shadowElevation = 10.dp,
                    modifier = Modifier.size(82.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.padding(10.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.acamandados_logo),
                            contentDescription = "Logo Oficial AcaMandados",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Fit
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Aca",
                        color = Color.White,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = (-0.5).sp
                    )
                    Text(
                        text = "Mandados",
                        color = AcaTurquesaLight,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = (-0.5).sp
                    )
                }

                Text(
                    text = "Acapulco, Gro. • De persona a persona",
                    color = AcaTurquesaContainer,
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Envíos, compras y entregas express locales con 100% de ganancia para repartidores.",
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 12.5.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Badges rápidos de confianza
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    InicioMiniBadge(icon = Icons.Default.LocationOn, text = "100% Acapulco")
                    InicioMiniBadge(icon = Icons.Default.Bolt, text = "Entregas Rápidas")
                    InicioMiniBadge(icon = Icons.Default.VerifiedUser, text = "PIN Seguro")
                }
            }

            // =========================================================================
            // 2. HERO BANNER VISUAL CON COSTERA DE ACAPULCO
            // =========================================================================
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = AcaAzulDark),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                border = BorderStroke(1.dp, AcaTurquesa.copy(alpha = 0.35f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .testTag("banner_hero_acamandados")
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Image(
                        painter = painterResource(id = R.drawable.acamandados_hero),
                        contentDescription = "Repartidor AcaMandados en Acapulco",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        Color(0xFF0A192F).copy(alpha = 0.94f),
                                        Color(0xFF0F172A).copy(alpha = 0.80f),
                                        Color.Transparent
                                    )
                                )
                            )
                    )
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = AcaNaranjaAcento,
                            modifier = Modifier.padding(bottom = 6.dp)
                        ) {
                            Text(
                                text = "RED LOCAL EN LA BAHÍA",
                                color = Color.White,
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                        Text(
                            text = "¡Lo que necesites hasta tu puerta!",
                            color = Color.White,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            lineHeight = 22.sp
                        )
                        Text(
                            text = "Conectamos toda la bahía: Costera, Diamante, Tradicional y Renacimiento.",
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = 11.5.sp,
                            lineHeight = 15.sp,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // =========================================================================
            // 3. SELECCIÓN PRINCIPAL: SOLICITAR UN MANDADO vs REGISTRARSE COMO REPARTIDOR
            // =========================================================================
            Text(
                text = "¿CÓMO DESEAS USAR ACAMANDADOS HOY?",
                color = AcaTurquesaLight,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp)
            )

            // -------------------------------------------------------------------------
            // OPCIÓN 1: SOLICITAR UN MANDADO (CLIENTE)
            // -------------------------------------------------------------------------
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                border = BorderStroke(1.5.dp, AcaTurquesa.copy(alpha = 0.7f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("option_solicitar_mandado")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = AcaTurquesa.copy(alpha = 0.18f),
                            border = BorderStroke(1.5.dp, AcaTurquesa),
                            modifier = Modifier.size(52.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.ShoppingBag,
                                    contentDescription = "Solicitar Mandado",
                                    tint = AcaTurquesaLight,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = AcaTurquesa.copy(alpha = 0.25f),
                                modifier = Modifier.padding(bottom = 3.dp)
                            ) {
                                Text(
                                    text = "CLIENTES Y NEGOCIOS",
                                    color = AcaTurquesaLight,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                            Text(
                                text = "Solicitar un Mandado",
                                color = Color.White,
                                fontSize = 19.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Text(
                        text = "Pide comida de la Costera, despensa de súper, medicinas de farmacia, envío de llaves o paquetería express directa.",
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )

                    // Beneficios clave del cliente
                    Column(
                        verticalArrangement = Arrangement.spacedBy(5.dp),
                        modifier = Modifier.padding(vertical = 2.dp)
                    ) {
                        BeneficioRow(icon = Icons.Default.MyLocation, text = "Rastreo GPS en vivo en el mapa de Acapulco")
                        BeneficioRow(icon = Icons.Default.Pin, text = "Entrega validada con código PIN de seguridad de 4 dígitos")
                        BeneficioRow(icon = Icons.Default.Payments, text = "Tarifas justas y transparentes desde $35 MXN")
                    }

                    // Botón de Acción Principal para Solicitar
                    Button(
                        onClick = onSelectSolicitarMandado,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AcaTurquesa,
                            contentColor = Color(0xFF0F172A)
                        ),
                        shape = RoundedCornerShape(14.dp),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("btn_solicitar_mandado_action")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "Solicitar Mandado Ahora",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    // Botón secundario de acceso de prueba instantáneo para cliente
                    OutlinedButton(
                        onClick = onQuickDemoCliente,
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.25f)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp)
                            .testTag("btn_demo_cliente_inicio")
                    ) {
                        Icon(
                            imageVector = Icons.Default.FlashOn,
                            contentDescription = null,
                            tint = AcaTurquesaLight,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Acceso Rápido como Cliente (Demo)",
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // -------------------------------------------------------------------------
            // OPCIÓN 2: REGISTRARSE COMO REPARTIDOR
            // -------------------------------------------------------------------------
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                border = BorderStroke(1.5.dp, Color(0xFF38BDF8).copy(alpha = 0.7f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("option_registrarse_repartidor")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFF38BDF8).copy(alpha = 0.18f),
                            border = BorderStroke(1.5.dp, Color(0xFF38BDF8)),
                            modifier = Modifier.size(52.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.TwoWheeler,
                                    contentDescription = "Registrarse como Repartidor",
                                    tint = Color(0xFF38BDF8),
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFF38BDF8).copy(alpha = 0.25f),
                                modifier = Modifier.padding(bottom = 3.dp)
                            ) {
                                Text(
                                    text = "100% DE LA TARIFA DIRECTA",
                                    color = Color(0xFF38BDF8),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                            Text(
                                text = "Registrarse como Repartidor",
                                color = Color.White,
                                fontSize = 19.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Text(
                        text = "Sé tu propio jefe en Acapulco en moto, auto o bici. ¡Gana el 100% de la tarifa de cada viaje sin comisiones abusivas por entrega!",
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )

                    // Beneficios clave del repartidor
                    Column(
                        verticalArrangement = Arrangement.spacedBy(5.dp),
                        modifier = Modifier.padding(vertical = 2.dp)
                    ) {
                        BeneficioRow(icon = Icons.Default.MonetizationOn, text = "0% comisión por viaje: la tarifa del cliente es 100% tuya")
                        BeneficioRow(icon = Icons.Default.Schedule, text = "Flexibilidad total: trabaja los días y horas que elijas")
                        BeneficioRow(icon = Icons.Default.NotificationsActive, text = "Alertas en tiempo real con Heads-up de nuevos mandados")
                    }

                    // Botón de Acción Principal para Repartidor
                    Button(
                        onClick = onSelectRegistrarseRepartidor,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF0284C7),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(14.dp),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("btn_registrarse_repartidor_action")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "Quiero ser Repartidor",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    // Botón secundario de acceso de prueba instantáneo para repartidor
                    OutlinedButton(
                        onClick = onQuickDemoRepartidor,
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.25f)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp)
                            .testTag("btn_demo_repartidor_inicio")
                    ) {
                        Icon(
                            imageVector = Icons.Default.FlashOn,
                            contentDescription = null,
                            tint = Color(0xFF38BDF8),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Acceso Rápido como Repartidor (Demo)",
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // =========================================================================
            // 4. ¿YA TIENES CUENTA? ACCESO A INICIAR SESIÓN
            // =========================================================================
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.White.copy(alpha = 0.08f),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "¿Ya tienes cuenta registrada?",
                            color = Color.White,
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Inicia sesión con Google o Correo",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 11.5.sp
                        )
                    }

                    TextButton(
                        onClick = onIniciarSesion,
                        colors = ButtonDefaults.textButtonColors(contentColor = AcaTurquesaLight),
                        modifier = Modifier.testTag("btn_iniciar_sesion_inicio")
                    ) {
                        Text(
                            text = "Iniciar Sesión",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.5.sp
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // =========================================================================
            // 5. COBERTURA DE ZONAS EN ACAPULCO
            // =========================================================================
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "ZONAS DE COBERTURA ACTIVA EN ACAPULCO",
                    color = AcaTurquesaLight,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.8.sp
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AcapulcoLocations.ZONAS.take(5).forEach { zona ->
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFF1E293B),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.12f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Place,
                                    contentDescription = null,
                                    tint = AcaTurquesaLight,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = zona.nombre,
                                    color = Color.White,
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "$${when(zona.zonaTier) { 1 -> 35; 2 -> 45; 3 -> 50; 4 -> 85; else -> 60 }} MXN",
                                    color = AcaTurquesaLight,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // =========================================================================
            // 6. ENLACE A POLÍTICAS DE SEGURIDAD Y PRIVACIDAD
            // =========================================================================
            TextButton(
                onClick = { viewModel.openPoliticasSeguridadDialog() },
                modifier = Modifier.testTag("btn_politicas_inicio")
            ) {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = null,
                    tint = AcaTurquesaLight,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Políticas de Seguridad, Rastreo y Privacidad de Datos",
                    color = AcaTurquesaLight,
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // Pie de página de tecnología
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.5f),
                    modifier = Modifier.size(13.dp)
                )
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                    text = "AcaMandados • Acapulco, México • Seguro y Protegido",
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 10.sp
                )
            }
        }
    }
}

@Composable
private fun InicioMiniBadge(
    icon: ImageVector,
    text: String
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color.White.copy(alpha = 0.12f),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = AcaTurquesaLight,
                modifier = Modifier.size(12.dp)
            )
            Text(
                text = text,
                color = Color.White,
                fontSize = 10.5.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun BeneficioRow(
    icon: ImageVector,
    text: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = AcaTurquesaLight,
            modifier = Modifier.size(15.dp)
        )
        Text(
            text = text,
            color = Color.White.copy(alpha = 0.9f),
            fontSize = 12.sp,
            lineHeight = 16.sp
        )
    }
}
