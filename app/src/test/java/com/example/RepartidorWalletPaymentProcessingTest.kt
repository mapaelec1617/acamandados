package com.example

import com.example.data.db.CalificacionDao
import com.example.data.db.MandadoDao
import com.example.data.db.NotificationDao
import com.example.data.db.RepartidorDao
import com.example.data.model.DepositoTarjetaLog
import com.example.data.model.Mandado
import com.example.data.model.MandadoOrder
import com.example.data.model.NotificationItem
import com.example.data.model.RepartidorProfile
import com.example.data.model.TransaccionBilletera
import com.example.data.repository.MandadosRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class RepartidorWalletPaymentProcessingTest {

    private lateinit var fakeMandadoDao: FakeMandadoDao
    private lateinit var fakeRepartidorDao: FakeRepartidorDao
    private lateinit var fakeCalificacionDao: FakeCalificacionDao
    private lateinit var fakeNotificationDao: FakeNotificationDao
    private lateinit var repository: MandadosRepository

    @Before
    fun setup() {
        fakeMandadoDao = FakeMandadoDao()
        fakeRepartidorDao = FakeRepartidorDao()
        fakeCalificacionDao = FakeCalificacionDao()
        fakeNotificationDao = FakeNotificationDao()

        repository = MandadosRepository(
            fakeMandadoDao,
            fakeRepartidorDao,
            fakeCalificacionDao,
            fakeNotificationDao,
            firestoreRepository = null
        )
    }

    @Test
    fun testProcessCardPaymentGeneratesSpeiDepositAndMarksOrder() = runBlocking {
        val order = MandadoOrder(
            id = 101L,
            folio = "ACA-2001",
            tipo = "EXPRESS",
            titulo = "Documentos urgentes notaría",
            descripcion = "Sobre legal",
            puntoRecogida = "Notaría 4",
            zonaRecogida = "Costera",
            puntoEntrega = "Banco Santander",
            zonaEntrega = "Costera",
            tarifaEnvio = 80.0,
            costoProductosEstimado = 20.0,
            metodoPago = "TARJETA",
            estado = "ENTREGADO",
            clienteNombre = "Lic. Garza",
            clienteTelefono = "7441239876",
            repartidorId = 1L,
            repartidorNombre = "Carlos Mendoza",
            pagoDepositadoAlRepartidor = false
        )
        fakeMandadoDao.insertOrder(order)

        val result = repository.procesarPagoMandado(101L)
        assertTrue("Processing payment should succeed", result.isSuccess)

        val depositLog = result.getOrNull()
        assertNotNull("Deposit log should be created for TARJETA", depositLog)
        assertEquals("ACA-2001", depositLog?.orderFolio)
        assertEquals(100.0, depositLog?.monto ?: 0.0, 0.001)
        assertEquals("BBVA México", depositLog?.bancoDestino)
        assertTrue("SPEI tracking key should start with STP", depositLog?.claveRastreoSpei?.startsWith("STP") == true)

        val updatedOrder = fakeMandadoDao.getOrderById(101L)
        assertNotNull(updatedOrder)
        assertTrue("Order must be marked as pagoDepositadoAlRepartidor", updatedOrder!!.pagoDepositadoAlRepartidor)
        assertNotNull("Order must have claveRastreoSpei populated", updatedOrder.claveRastreoSpei)

        // Verify driver notification was sent
        val notif = fakeNotificationDao.notifications.find { it.orderId == 101L && it.tipo == "SPEI" }
        assertNotNull("SPEI deposit notification should be sent to driver", notif)
        assertEquals("REPARTIDOR", notif?.paraRol)
    }

    @Test
    fun testProcessCashPaymentConfirmsReceiptWithoutSpei() = runBlocking {
        val order = MandadoOrder(
            id = 102L,
            folio = "ACA-2002",
            tipo = "COMIDA",
            titulo = "Cena Tacos",
            descripcion = "Tacos al pastor",
            puntoRecogida = "Taquería",
            zonaRecogida = "Progreso",
            puntoEntrega = "Hornos",
            zonaEntrega = "Hornos",
            tarifaEnvio = 50.0,
            costoProductosEstimado = 150.0,
            metodoPago = "EFECTIVO",
            estado = "ENTREGADO",
            clienteNombre = "Juan",
            clienteTelefono = "7441112233",
            repartidorId = 1L,
            pagoDepositadoAlRepartidor = false
        )
        fakeMandadoDao.insertOrder(order)

        val result = repository.procesarPagoMandado(102L)
        assertTrue(result.isSuccess)

        // For cash, no bank deposit log is created, but the order is marked settled
        assertNull(result.getOrNull())

        val updated = fakeMandadoDao.getOrderById(102L)
        assertTrue(updated!!.pagoDepositadoAlRepartidor)

        val notif = fakeNotificationDao.notifications.find { it.orderId == 102L }
        assertNotNull("Cash confirmation notification should be created", notif)
        assertTrue(notif!!.titulo.contains("Efectivo") || notif.mensaje.contains("cobro"))
    }

    @Test
    fun testCannotProcessNonDeliveredOrderOrAlreadyPaidOrder() = runBlocking {
        val inTransitOrder = MandadoOrder(
            id = 103L,
            folio = "ACA-2003",
            tipo = "EXPRESS",
            titulo = "En camino",
            descripcion = "En camino",
            puntoRecogida = "A",
            zonaRecogida = "A",
            puntoEntrega = "B",
            zonaEntrega = "B",
            tarifaEnvio = 60.0,
            metodoPago = "TARJETA",
            estado = "EN_CAMINO",
            clienteNombre = "Cliente",
            clienteTelefono = "7440000000",
            pagoDepositadoAlRepartidor = false
        )
        fakeMandadoDao.insertOrder(inTransitOrder)

        val inTransitResult = repository.procesarPagoMandado(103L)
        assertTrue("Cannot process payment for an order that is not yet delivered", inTransitResult.isFailure)

        val alreadyPaidOrder = inTransitOrder.copy(
            id = 104L,
            folio = "ACA-2004",
            estado = "ENTREGADO",
            pagoDepositadoAlRepartidor = true
        )
        fakeMandadoDao.insertOrder(alreadyPaidOrder)

        val alreadyPaidResult = repository.procesarPagoMandado(104L)
        assertTrue("Cannot process an order that is already paid", alreadyPaidResult.isFailure)
    }

    @Test
    fun testProcessAllPendingPayoutsInBatch() = runBlocking {
        val order1 = MandadoOrder(
            id = 201L,
            folio = "ACA-3001",
            tipo = "EXPRESS",
            titulo = "O1",
            descripcion = "D1",
            puntoRecogida = "A",
            zonaRecogida = "A",
            puntoEntrega = "B",
            zonaEntrega = "B",
            tarifaEnvio = 70.0,
            metodoPago = "TARJETA",
            estado = "ENTREGADO",
            clienteNombre = "C1",
            clienteTelefono = "7440001",
            repartidorId = 1L,
            pagoDepositadoAlRepartidor = false
        )
        val order2 = MandadoOrder(
            id = 202L,
            folio = "ACA-3002",
            tipo = "EXPRESS",
            titulo = "O2",
            descripcion = "D2",
            puntoRecogida = "A",
            zonaRecogida = "A",
            puntoEntrega = "B",
            zonaEntrega = "B",
            tarifaEnvio = 85.0,
            metodoPago = "TARJETA",
            estado = "ENTREGADO",
            clienteNombre = "C2",
            clienteTelefono = "7440002",
            repartidorId = 1L,
            pagoDepositadoAlRepartidor = false
        )
        fakeMandadoDao.insertOrder(order1)
        fakeMandadoDao.insertOrder(order2)

        val batchResult = repository.procesarTodosLosPagosPendientes(1L)
        assertTrue(batchResult.isSuccess)
        assertEquals("Both orders should be processed", 2, batchResult.getOrDefault(0))

        assertTrue(fakeMandadoDao.getOrderById(201L)!!.pagoDepositadoAlRepartidor)
        assertTrue(fakeMandadoDao.getOrderById(202L)!!.pagoDepositadoAlRepartidor)
        assertEquals(2, fakeRepartidorDao.depositos.size)
    }

    @Test
    fun testCompletedOrderPaymentCreatesWalletIncomeTransaction() = runBlocking {
        val order = MandadoOrder(
            id = 301L,
            folio = "ACA-4001",
            tipo = "EXPRESS",
            titulo = "Medicinas Farmacia del Ahorro",
            descripcion = "Antibióticos y suero",
            puntoRecogida = "Farmacia del Ahorro Costera",
            zonaRecogida = "Costera",
            puntoEntrega = "Condominios Las Torres",
            zonaEntrega = "Costera",
            tarifaEnvio = 85.0,
            costoProductosEstimado = 150.0,
            metodoPago = "TARJETA",
            estado = "ENTREGADO",
            clienteNombre = "María Elena",
            clienteTelefono = "7449988776",
            repartidorId = 1L,
            repartidorNombre = "Carlos Mendoza",
            pagoDepositadoAlRepartidor = false
        )
        fakeMandadoDao.insertOrder(order)

        val result = repository.procesarPagoMandado(301L)
        assertTrue("Payment processing must succeed", result.isSuccess)

        // Verificar que se registró una TransaccionBilletera de tipo INGRESO
        val transacciones = fakeRepartidorDao.transacciones
        val ingresoTrans = transacciones.find { it.orderFolio == "ACA-4001" }
        assertNotNull("Should create an income transaction in wallet", ingresoTrans)
        assertEquals("INGRESO", ingresoTrans?.tipo)
        assertTrue("isIngreso helper must be true", ingresoTrans?.isIngreso == true)
        assertEquals(235.0, ingresoTrans?.monto ?: 0.0, 0.001) // 85 envío + 150 productos
        assertEquals("TARJETA", ingresoTrans?.metodoPago)
        assertNotNull("Should contain SPEI tracking key", ingresoTrans?.claveRastreoSpei)
        assertEquals("BBVA México", ingresoTrans?.bancoDestino)
        assertEquals("1234", ingresoTrans?.clabeTerminacion)
        assertEquals("LIQUIDADO", ingresoTrans?.estado)
    }

    @Test
    fun testSolicitarRetiroCreatesRetiroTransactionWithSpei() = runBlocking {
        val result = repository.solicitarRetiro(monto = 350.0, motivo = "Retiro parcial de ganancias")
        assertTrue("Withdrawal request must succeed", result.isSuccess)

        val transaccion = result.getOrNull()
        assertNotNull("Retiro transaction must not be null", transaccion)
        assertEquals("RETIRO", transaccion?.tipo)
        assertTrue("isRetiro helper must be true", transaccion?.isRetiro == true)
        assertEquals(350.0, transaccion?.monto ?: 0.0, 0.001)
        assertEquals("SPEI", transaccion?.metodoPago)
        assertTrue("Tracking key must start with RET", transaccion?.claveRastreoSpei?.startsWith("RET") == true)
        assertEquals("BBVA México", transaccion?.bancoDestino)
        assertEquals("1234", transaccion?.clabeTerminacion)
        assertEquals("LIQUIDADO", transaccion?.estado)

        // Verificar que está presente en la lista del DAO
        val transList = fakeRepartidorDao.transacciones
        assertTrue("Transaction must be stored in DAO", transList.contains(transaccion))

        // Verificar notificación enviada al repartidor
        val notif = fakeNotificationDao.notifications.find { it.tipo == "SPEI" && it.titulo.contains("Retiro") }
        assertNotNull("Notification must be sent for withdrawal", notif)
        assertEquals("REPARTIDOR", notif?.paraRol)
    }

    @Test
    fun testSolicitarRetiroFailsWithZeroOrNegativeAmount() = runBlocking {
        val zeroResult = repository.solicitarRetiro(0.0)
        assertTrue("Zero amount withdrawal must fail", zeroResult.isFailure)

        val negativeResult = repository.solicitarRetiro(-100.0)
        assertTrue("Negative amount withdrawal must fail", negativeResult.isFailure)
    }

    @Test
    fun testTransactionHistoryTracksBothIncomesAndWithdrawals() = runBlocking {
        // Registrar un ingreso por mandado en efectivo
        val cashOrder = MandadoOrder(
            id = 501L,
            folio = "ACA-5001",
            tipo = "SUPERMERCADO",
            titulo = "Despensa Express",
            descripcion = "Víveres",
            puntoRecogida = "Soriana Costera",
            zonaRecogida = "Costera",
            puntoEntrega = "Fracc. Magallanes",
            zonaEntrega = "Costera",
            tarifaEnvio = 90.0,
            costoProductosEstimado = 0.0,
            metodoPago = "EFECTIVO",
            estado = "ENTREGADO",
            clienteNombre = "Roberto",
            clienteTelefono = "7443322110",
            repartidorId = 1L,
            pagoDepositadoAlRepartidor = false
        )
        fakeMandadoDao.insertOrder(cashOrder)
        repository.procesarPagoMandado(501L)

        // Registrar un retiro
        repository.solicitarRetiro(monto = 90.0)

        val allTransactions = fakeRepartidorDao.transacciones
        val ingresos = allTransactions.filter { it.tipo == "INGRESO" }
        val retiros = allTransactions.filter { it.tipo == "RETIRO" }

        assertTrue("Should have at least 1 income transaction", ingresos.isNotEmpty())
        assertTrue("Should have at least 1 withdrawal transaction", retiros.isNotEmpty())
        assertEquals("ACA-5001", ingresos.first().orderFolio)
        assertEquals("EFECTIVO", ingresos.first().metodoPago)
        assertEquals(90.0, retiros.first().monto, 0.001)
    }

    // Fakes for testing
    private class FakeMandadoDao : MandadoDao {
        val orders = mutableListOf<MandadoOrder>()
        val mandados = mutableListOf<Mandado>()

        // Mandado CRUD
        override suspend fun insert(mandado: Mandado): Long {
            val id = if (mandado.id == 0L) (mandados.size + 1).toLong() else mandado.id
            val toSave = mandado.copy(id = id)
            mandados.removeAll { it.id == id }
            mandados.add(toSave)
            return id
        }

        override suspend fun insertMandado(mandado: Mandado): Long = insert(mandado)

        override suspend fun insertAll(mandados: List<Mandado>): List<Long> {
            return mandados.map { insert(it) }
        }

        override fun getAllMandados(): Flow<List<Mandado>> = flowOf(mandados)
        override fun getAll(): Flow<List<Mandado>> = flowOf(mandados)

        override suspend fun getMandadoById(id: Long): Mandado? = mandados.find { it.id == id }
        override fun getMandadoByIdFlow(id: Long): Flow<Mandado?> = flowOf(mandados.find { it.id == id })
        override fun getMandadosByStatus(status: String): Flow<List<Mandado>> =
            flowOf(mandados.filter { it.status == status })

        override suspend fun update(mandado: Mandado) {
            mandados.removeAll { it.id == mandado.id }
            mandados.add(mandado)
        }

        override suspend fun updateMandado(mandado: Mandado) = update(mandado)

        override suspend fun updateStatus(id: Long, status: String) {
            val existing = mandados.find { it.id == id }
            if (existing != null) {
                mandados.removeAll { it.id == id }
                mandados.add(existing.copy(status = status))
            }
        }

        override suspend fun delete(mandado: Mandado) {
            mandados.removeAll { it.id == mandado.id }
        }

        override suspend fun deleteMandado(mandado: Mandado) = delete(mandado)

        override suspend fun deleteMandadoById(id: Long) {
            mandados.removeAll { it.id == id }
        }

        override suspend fun deleteAllMandados() {
            mandados.clear()
        }

        // MandadoOrder operations
        override fun getAllOrders(): Flow<List<MandadoOrder>> = flowOf(orders)
        override fun getAvailableOrders(): Flow<List<MandadoOrder>> = flowOf(orders.filter { it.estado == "SOLICITADO" })
        override fun getActiveOrders(): Flow<List<MandadoOrder>> = flowOf(orders.filter { it.estado in listOf("SOLICITADO", "ACEPTADO", "EN_CAMINO") })
        override fun getCurrentActiveDriverOrder(driverId: Long): Flow<MandadoOrder?> = flowOf(orders.find { it.repartidorId == driverId && it.estado in listOf("ACEPTADO", "EN_CAMINO") })
        override fun getDriverCompletedOrders(driverId: Long): Flow<List<MandadoOrder>> = flowOf(orders.filter { it.repartidorId == driverId && it.estado == "ENTREGADO" })
        override suspend fun getOrderById(orderId: Long): MandadoOrder? = orders.find { it.id == orderId }

        override suspend fun insertOrder(order: MandadoOrder): Long {
            orders.removeAll { it.id == order.id }
            orders.add(order)
            return order.id
        }

        override suspend fun updateOrder(order: MandadoOrder) {
            orders.removeAll { it.id == order.id }
            orders.add(order)
        }

        override suspend fun deleteOrderById(orderId: Long) {
            orders.removeAll { it.id == orderId }
        }
    }

    private class FakeRepartidorDao : RepartidorDao {
        var profile: RepartidorProfile? = RepartidorProfile(
            id = 1L,
            nombre = "Carlos Mendoza",
            telefono = "7441234567",
            clabeInterbancaria = "012180015678901234",
            banco = "BBVA México"
        )
        val depositos = mutableListOf<DepositoTarjetaLog>()
        val transacciones = mutableListOf<TransaccionBilletera>()

        override fun getProfileFlow(): Flow<RepartidorProfile?> = flowOf(profile)
        override suspend fun getProfileSync(): RepartidorProfile? = profile
        override suspend fun insertOrUpdateProfile(profile: RepartidorProfile) {
            this.profile = profile
        }
        override fun getDepositosFlow(): Flow<List<DepositoTarjetaLog>> = flowOf(depositos)
        override suspend fun insertDeposito(deposito: DepositoTarjetaLog): Long {
            depositos.add(deposito)
            return depositos.size.toLong()
        }

        override fun getTransaccionesFlow(): Flow<List<TransaccionBilletera>> = flowOf(transacciones)
        override suspend fun insertTransaccion(transaccion: TransaccionBilletera): Long {
            transacciones.add(0, transaccion)
            return transacciones.size.toLong()
        }
        override suspend fun insertTransacciones(transacciones: List<TransaccionBilletera>) {
            this.transacciones.addAll(0, transacciones)
        }
        override suspend fun getTransaccionesCount(): Int = transacciones.size
    }

    private class FakeCalificacionDao : CalificacionDao {
        override fun getAllCalificaciones() = flowOf(emptyList<com.example.data.model.CalificacionReview>())
        override fun getCalificacionesPara(nombre: String) = flowOf(emptyList<com.example.data.model.CalificacionReview>())
        override suspend fun getCalificacionForOrder(orderId: Long, tipo: String) = null
        override suspend fun insertCalificacion(calificacion: com.example.data.model.CalificacionReview): Long = 1L
    }

    private class FakeNotificationDao : NotificationDao {
        val notifications = mutableListOf<NotificationItem>()
        override fun getAllNotifications(): Flow<List<NotificationItem>> = flowOf(notifications)
        override fun getNotificationsForRole(role: String): Flow<List<NotificationItem>> = flowOf(notifications.filter { it.paraRol == role || it.paraRol == "TODOS" })
        override fun getUnreadCountFlow(): Flow<Int> = flowOf(notifications.count { !it.leida })
        override suspend fun insertNotification(notification: NotificationItem): Long {
            notifications.add(notification)
            return notifications.size.toLong()
        }
        override suspend fun markAsRead(id: Long) {}
        override suspend fun markAllAsRead() {}
        override suspend fun deleteNotification(id: Long) {
            notifications.removeAll { it.id == id }
        }
    }
}
