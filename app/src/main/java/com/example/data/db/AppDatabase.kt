package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.CalificacionReview
import com.example.data.model.DepositoTarjetaLog
import com.example.data.model.Mandado
import com.example.data.model.MandadoOrder
import com.example.data.model.NotificationItem
import com.example.data.model.RepartidorProfile
import com.example.data.model.TransaccionBilletera
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        MandadoOrder::class,
        Mandado::class,
        RepartidorProfile::class,
        DepositoTarjetaLog::class,
        CalificacionReview::class,
        NotificationItem::class,
        TransaccionBilletera::class
    ],
    version = 6,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun mandadoDao(): MandadoDao
    abstract fun repartidorDao(): RepartidorDao
    abstract fun calificacionDao(): CalificacionDao
    abstract fun notificationDao(): NotificationDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "acamandados_db"
                )
                .fallbackToDestructiveMigration()
                .addCallback(DatabaseCallback(scope))
                .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        prepopulateDatabase(
                            database.mandadoDao(),
                            database.repartidorDao(),
                            database.calificacionDao(),
                            database.notificationDao()
                        )
                    }
                }
            }

            private suspend fun prepopulateDatabase(
                mandadoDao: MandadoDao,
                repartidorDao: RepartidorDao,
                calificacionDao: CalificacionDao,
                notificationDao: NotificationDao
            ) {
                // Perfil de repartidor inicial
                repartidorDao.insertOrUpdateProfile(
                    RepartidorProfile(
                        id = 1,
                        nombre = "Carlos Mendoza",
                        telefono = "7441234567",
                        tipoTransporte = "MOTO",
                        modeloVehiculo = "Italika FT150 Blanca",
                        placas = "GRO-489A",
                        suscripcionActiva = true,
                        suscripcionMontoSemanal = 150.0,
                        suscripcionFechaFin = System.currentTimeMillis() + (6 * 24 * 60 * 60 * 1000L),
                        clabeInterbancaria = "012180015678901234",
                        banco = "BBVA México",
                        disponibleParaMandados = true,
                        pedidosCompletados = 24,
                        calificacion = 4.96
                    )
                )

                // Depósito SPEI previo como muestra de cobro tarjeta recibido el mismo día
                repartidorDao.insertDeposito(
                    DepositoTarjetaLog(
                        orderFolio = "ACA-1018",
                        monto = 110.0,
                        fecha = System.currentTimeMillis() - 3600000 * 4,
                        bancoDestino = "BBVA México",
                        clabeTerminacion = "1234",
                        claveRastreoSpei = "STP20260922004921",
                        estado = "DEPOSITADO_HOY"
                    )
                )

                val now = System.currentTimeMillis()

                // Transacciones iniciales en el historial de la billetera (ingresos de mandados y retiros)
                repartidorDao.insertTransacciones(
                    listOf(
                        TransaccionBilletera(
                            tipo = "INGRESO",
                            concepto = "Ingreso Mandado ACA-1037",
                            detalle = "Entrega Express • Cobrado en efectivo en mano",
                            monto = 90.0,
                            fecha = now - 1000 * 60 * 135,
                            orderFolio = "ACA-1037",
                            metodoPago = "EFECTIVO",
                            estado = "LIQUIDADO"
                        ),
                        TransaccionBilletera(
                            tipo = "INGRESO",
                            concepto = "Ingreso Mandado ACA-1018",
                            detalle = "Comida Beto Godoy • Dispersión bancaria SPEI",
                            monto = 110.0,
                            fecha = now - 3600000 * 4,
                            orderFolio = "ACA-1018",
                            metodoPago = "TARJETA",
                            bancoDestino = "BBVA México",
                            clabeTerminacion = "1234",
                            claveRastreoSpei = "STP20260922004921",
                            estado = "LIQUIDADO"
                        ),
                        TransaccionBilletera(
                            tipo = "RETIRO",
                            concepto = "Retiro SPEI a Cuenta Bancaria",
                            detalle = "Dispersión de saldo acumulado a BBVA México •••• 1234",
                            monto = 250.0,
                            fecha = now - 3600000 * 8,
                            metodoPago = "SPEI",
                            bancoDestino = "BBVA México",
                            clabeTerminacion = "1234",
                            claveRastreoSpei = "RET20260922119022",
                            estado = "LIQUIDADO"
                        ),
                        TransaccionBilletera(
                            tipo = "INGRESO",
                            concepto = "Ingreso Mandado ACA-1002",
                            detalle = "Súper Chedraui Diamante • Tarifa flete y compra",
                            monto = 240.0,
                            fecha = now - 3600000 * 20,
                            orderFolio = "ACA-1002",
                            metodoPago = "TARJETA",
                            bancoDestino = "BBVA México",
                            clabeTerminacion = "1234",
                            claveRastreoSpei = "STP20260921008819",
                            estado = "LIQUIDADO"
                        )
                    )
                )

                // Pedidos de prueba locales en Acapulco
                val sampleOrders = listOf(
                    MandadoOrder(
                        folio = "ACA-1045",
                        tipo = "FARMACIA",
                        titulo = "Compra de antibiótico y suero",
                        descripcion = "Comprar Amoxicilina 500mg y 2 electrolit de coco en Farmacia Guadalajara de Magallanes.",
                        puntoRecogida = "Farmacia Guadalajara, Av. Wilfrido Massieu 23",
                        zonaRecogida = "Magallanes & Galerías Acapulco",
                        puntoEntrega = "Condominio La Palapa, Depto 402, Costera",
                        zonaEntrega = "Costera Miguel Alemán",
                        costoProductosEstimado = 280.0,
                        tarifaEnvio = 65.0,
                        metodoPago = "EFECTIVO",
                        estado = "SOLICITADO",
                        clienteNombre = "Mariana Solís",
                        clienteTelefono = "7445551122",
                        remitenteNombre = "Farmacia Guadalajara (Cajero)",
                        remitenteTelefono = "7444851234",
                        destinatarioNombre = "Mariana Solís (Enferma)",
                        destinatarioTelefono = "7445551122",
                        referenciasDomicilio = "Torre A, piso 4 depto 402. Avisar en caseta de seguridad con el oficial Gómez.",
                        pinSeguridad = "3819",
                        tamanoPaquete = "CHICO",
                        confirmacionDimensionesMoto = true,
                        esCobroContraEntrega = false,
                        montoProductoCobrar = 0.0,
                        recargoZonaLejana = 0.0,
                        fechaCreacion = now - 1000 * 60 * 12,
                        notasAdicionales = "Dejar en caseta de seguridad si no bajo en 2 minutos."
                    ),
                    MandadoOrder(
                        folio = "ACA-1046",
                        tipo = "DOCUMENTACION",
                        titulo = "Entrega de escrituras notariales",
                        descripcion = "Recoger sobre cerrado con contrato en Notaría Pública #4 y entregar en oficinas corporativas.",
                        puntoRecogida = "Notaría Pública #4, Av. Cuauhtémoc 120",
                        zonaRecogida = "Colonia Progreso & Cuauhtémoc",
                        puntoEntrega = "Torre Marina, Blvd. de las Naciones 800",
                        zonaEntrega = "Zona Diamante & Blvd. de las Naciones",
                        costoProductosEstimado = 0.0,
                        tarifaEnvio = 160.0, // 135 base + 25 recargo cruce zona lejana
                        metodoPago = "TARJETA",
                        estado = "SOLICITADO",
                        clienteNombre = "Lic. Roberto Garza",
                        clienteTelefono = "7443339988",
                        remitenteNombre = "Lic. Roberto Garza (Notaría)",
                        remitenteTelefono = "7443339988",
                        destinatarioNombre = "Ing. Armando Leyva (Recepción)",
                        destinatarioTelefono = "7441887766",
                        referenciasDomicilio = "Edificio de cristal azul junto a Plaza Las Palmas, piso 3 oficina 301.",
                        pinSeguridad = "7241",
                        tamanoPaquete = "CHICO",
                        confirmacionDimensionesMoto = true,
                        esCobroContraEntrega = false,
                        montoProductoCobrar = 0.0,
                        recargoZonaLejana = 25.0,
                        tarjetaUltimos4 = "4820",
                        tarjetaTitular = "Roberto Garza",
                        tarjetaAprobada = true,
                        fechaCreacion = now - 1000 * 60 * 25,
                        notasAdicionales = "Urgente, sobre sellado y frágil. Pagado con tarjeta en la app."
                    ),
                    MandadoOrder(
                        folio = "ACA-1044",
                        tipo = "COMIDA",
                        titulo = "Pozole Verde y relleno guerrerense (COD Comercio)",
                        descripcion = "Recoger orden familiar en Pozolería Los Cedros para entrega familiar y cobrar el costo del producto al cliente.",
                        puntoRecogida = "Pozolería Los Cedros, Calle Sonora 14",
                        zonaRecogida = "Colonia Progreso & Cuauhtémoc",
                        puntoEntrega = "Fracc. Costa Azul, Calle Albatros #18",
                        zonaEntrega = "Costa Azul & Club Deportivo",
                        costoProductosEstimado = 360.0,
                        tarifaEnvio = 75.0,
                        metodoPago = "EFECTIVO",
                        estado = "ACEPTADO",
                        clienteNombre = "Doña Lupe (Pozolería Los Cedros)",
                        clienteTelefono = "7444835678",
                        remitenteNombre = "Doña Lupe (Pozolería)",
                        remitenteTelefono = "7444835678",
                        destinatarioNombre = "Sra. Teresa Salgado",
                        destinatarioTelefono = "7449992211",
                        referenciasDomicilio = "Casa blanca de dos pisos con portón de herrería negra, frente al parque de Costa Azul.",
                        pinSeguridad = "5529",
                        tamanoPaquete = "MEDIANO",
                        confirmacionDimensionesMoto = true,
                        esCobroContraEntrega = true,
                        montoProductoCobrar = 360.0,
                        recargoZonaLejana = 0.0,
                        repartidorId = 1,
                        repartidorNombre = "Carlos Mendoza",
                        repartidorTelefono = "7441234567",
                        repartidorVehiculo = "Italika FT150 Blanca",
                        repartidorPlacas = "GRO-489A",
                        fechaCreacion = now - 1000 * 60 * 40,
                        notasAdicionales = "Va bien empaquetado en mochila térmica."
                    ),
                    MandadoOrder(
                        folio = "ACA-1040",
                        tipo = "COMPRAS",
                        titulo = "Encargo de Chedraui & OXXO",
                        descripcion = "Agua purificada garrafón, hielo y botanas.",
                        puntoRecogida = "Chedraui Diamante, Blvd de las Naciones",
                        zonaRecogida = "Zona Diamante & Blvd. de las Naciones",
                        puntoEntrega = "Condominios Mayan Island, Torre 2",
                        zonaEntrega = "Zona Diamante & Blvd. de las Naciones",
                        costoProductosEstimado = 190.0,
                        tarifaEnvio = 50.0,
                        metodoPago = "EFECTIVO",
                        estado = "ENTREGADO",
                        clienteNombre = "Patricia Domínguez",
                        clienteTelefono = "7444445566",
                        remitenteNombre = "Chedraui Mostrador",
                        remitenteTelefono = "7444621000",
                        destinatarioNombre = "Patricia Domínguez",
                        destinatarioTelefono = "7444445566",
                        referenciasDomicilio = "Torre 2 depto 501. Avisar al cel al llegar a la pluma principal.",
                        repartidorId = 1,
                        repartidorNombre = "Carlos Mendoza",
                        repartidorTelefono = "7441234567",
                        repartidorVehiculo = "Italika FT150 Blanca",
                        repartidorPlacas = "GRO-489A",
                        fechaCreacion = now - 1000 * 60 * 180,
                        fechaEntrega = now - 1000 * 60 * 135,
                        pagoDepositadoAlRepartidor = true,
                        claveRastreoSpei = "STP202609291040"
                    ),
                    MandadoOrder(
                        folio = "ACA-1038",
                        tipo = "FARMACIA",
                        titulo = "Medicamentos & Termómetro Digital",
                        descripcion = "Recoger receta surtida en Farmacia del Ahorro y entregar con cuidado en recepción.",
                        puntoRecogida = "Farmacia del Ahorro, Av. Costera 240",
                        zonaRecogida = "Costera Miguel Alemán",
                        puntoEntrega = "Hotel Fiesta Americana, Costera 97",
                        zonaEntrega = "Costera Miguel Alemán",
                        costoProductosEstimado = 145.0,
                        tarifaEnvio = 85.0,
                        metodoPago = "TARJETA",
                        estado = "ENTREGADO",
                        clienteNombre = "Dra. Marcela Estrada",
                        clienteTelefono = "7449871122",
                        remitenteNombre = "Farmacia del Ahorro",
                        remitenteTelefono = "7444840011",
                        destinatarioNombre = "Dra. Marcela Estrada",
                        destinatarioTelefono = "7449871122",
                        referenciasDomicilio = "Entregar en concierge / lobby con número de habitación 714.",
                        pinSeguridad = "3819",
                        tarjetaUltimos4 = "4820",
                        tarjetaTitular = "Marcela Estrada",
                        tarjetaAprobada = true,
                        repartidorId = 1,
                        repartidorNombre = "Carlos Mendoza",
                        repartidorTelefono = "7441234567",
                        repartidorVehiculo = "Italika FT150 Blanca",
                        repartidorPlacas = "GRO-489A",
                        fechaCreacion = now - 1000 * 60 * 90,
                        fechaEntrega = now - 1000 * 60 * 50,
                        pagoDepositadoAlRepartidor = false
                    )
                )

                for (order in sampleOrders) {
                    mandadoDao.insertOrder(order)
                }

                // Reseñas de muestra
                calificacionDao.insertCalificacion(
                    CalificacionReview(
                        orderId = 4,
                        orderFolio = "ACA-1040",
                        tipo = "CLIENTE_A_REPARTIDOR",
                        estrellas = 5,
                        comentario = "¡Excelente servicio! Carlos trajo el hielo bien congelado y en menos de 20 minutos hasta Mayan Island. Muy educado.",
                        tags = "Puntual ⚡, Cuidó el pedido 📦, Amable 😊",
                        autorNombre = "Patricia Domínguez",
                        destinatarioNombre = "Carlos Mendoza",
                        fecha = now - 1000 * 60 * 120
                    )
                )

                calificacionDao.insertCalificacion(
                    CalificacionReview(
                        orderId = 4,
                        orderFolio = "ACA-1040",
                        tipo = "REPARTIDOR_A_CLIENTE",
                        estrellas = 5,
                        comentario = "Excelente cliente, dio indicaciones exactas para entrar a la caseta del condominio y el pago en efectivo fue exacto.",
                        tags = "Pago puntual 💵, Indicaciones claras 🗺️",
                        autorNombre = "Carlos Mendoza",
                        destinatarioNombre = "Patricia Domínguez",
                        fecha = now - 1000 * 60 * 115
                    )
                )

                calificacionDao.insertCalificacion(
                    CalificacionReview(
                        orderId = 1,
                        orderFolio = "ACA-1018",
                        tipo = "CLIENTE_A_REPARTIDOR",
                        estrellas = 5,
                        comentario = "Me salvó con la receta médica de noche en Farmacia del Ahorro. Muy confiable y rápido en Costera.",
                        tags = "Súper rápido 🚀, Muy confiable 🔒",
                        autorNombre = "Dr. Alejandro Cuevas",
                        destinatarioNombre = "Carlos Mendoza",
                        fecha = now - 3600000 * 5
                    )
                )

                // Notificaciones iniciales de muestra
                notificationDao.insertNotification(
                    NotificationItem(
                        titulo = "🚨 Nueva solicitud en Progreso",
                        mensaje = "Entrega de documentos a Zona Diamante (a ~2.1 km de ti). Tarifa 100% para ti: $135 MXN",
                        tipo = "NUEVA_SOLICITUD",
                        orderFolio = "ACA-1046",
                        distanciaAprox = "2.1 km",
                        fecha = now - 1000 * 60 * 25,
                        leida = false,
                        paraRol = "REPARTIDOR"
                    )
                )

                notificationDao.insertNotification(
                    NotificationItem(
                        titulo = "🛵 Tu repartidor está cerca",
                        mensaje = "Carlos Mendoza está a 450 metros de tu domicilio en Fracc. Costa Azul. Prepárate para recibir.",
                        tipo = "CERCA",
                        orderFolio = "ACA-1044",
                        distanciaAprox = "450 m",
                        fecha = now - 1000 * 60 * 8,
                        leida = false,
                        paraRol = "CLIENTE"
                    )
                )

                notificationDao.insertNotification(
                    NotificationItem(
                        titulo = "💳 Depósito SPEI recibido",
                        mensaje = "Se depositaron $110.00 MXN a tu cuenta BBVA del pedido ACA-1018.",
                        tipo = "SPEI",
                        orderFolio = "ACA-1018",
                        fecha = now - 3600000 * 4,
                        leida = true,
                        paraRol = "REPARTIDOR"
                    )
                )
            }
        }
    }
}
