package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.db.AppDatabase
import com.example.data.db.MandadoDao
import com.example.data.model.Mandado
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class MandadoDaoCrudTest {

    private lateinit var db: AppDatabase
    private lateinit var mandadoDao: MandadoDao

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        mandadoDao = db.mandadoDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun testInsertAndGetMandadoById() = runBlocking {
        val mandado = Mandado(
            description = "Comprar medicinas en Farmacia del Ahorro",
            location = "Costera Miguel Alemán, Acapulco",
            status = "PENDING",
            price = 65.0
        )
        val generatedId = mandadoDao.insertMandado(mandado)
        assertTrue(generatedId > 0)

        val retrieved = mandadoDao.getMandadoById(generatedId)
        assertNotNull(retrieved)
        assertEquals("Comprar medicinas en Farmacia del Ahorro", retrieved?.description)
        assertEquals("Costera Miguel Alemán, Acapulco", retrieved?.location)
        assertEquals("PENDING", retrieved?.status)
        assertEquals(65.0, retrieved?.price ?: 0.0, 0.001)
    }

    @Test
    fun testGetAllMandadosFlow() = runBlocking {
        val m1 = Mandado(description = "Entrega de documentos", location = "Centro", status = "PENDING", price = 45.0)
        val m2 = Mandado(description = "Comida rápida", location = "Diamante", status = "DELIVERED", price = 80.0)

        mandadoDao.insert(m1)
        mandadoDao.insert(m2)

        val allList = mandadoDao.getAllMandados().first()
        assertEquals(2, allList.size)
    }

    @Test
    fun testGetMandadosByStatus() = runBlocking {
        val m1 = Mandado(description = "Pedido 1", location = "Caleta", status = "PENDING", price = 50.0)
        val m2 = Mandado(description = "Pedido 2", location = "Progreso", status = "IN_PROGRESS", price = 70.0)
        val m3 = Mandado(description = "Pedido 3", location = "Costa Azul", status = "PENDING", price = 60.0)

        mandadoDao.insert(m1)
        mandadoDao.insert(m2)
        mandadoDao.insert(m3)

        val pending = mandadoDao.getMandadosByStatus("PENDING").first()
        assertEquals(2, pending.size)

        val inProgress = mandadoDao.getMandadosByStatus("IN_PROGRESS").first()
        assertEquals(1, inProgress.size)
        assertEquals("Pedido 2", inProgress.first().description)
    }

    @Test
    fun testUpdateMandado() = runBlocking {
        val mandado = Mandado(description = "Despensa", location = "Renacimiento", status = "PENDING", price = 90.0)
        val id = mandadoDao.insert(mandado)

        val inserted = mandadoDao.getMandadoById(id)
        assertNotNull(inserted)

        val updated = inserted!!.copy(status = "DELIVERED", price = 95.0)
        mandadoDao.updateMandado(updated)

        val afterUpdate = mandadoDao.getMandadoById(id)
        assertEquals("DELIVERED", afterUpdate?.status)
        assertEquals(95.0, afterUpdate?.price ?: 0.0, 0.001)
    }

    @Test
    fun testDeleteMandado() = runBlocking {
        val mandado = Mandado(description = "Flores", location = "Hornos", status = "PENDING", price = 40.0)
        val id = mandadoDao.insert(mandado)

        val retrieved = mandadoDao.getMandadoById(id)
        assertNotNull(retrieved)

        mandadoDao.delete(retrieved!!)
        val afterDelete = mandadoDao.getMandadoById(id)
        assertNull(afterDelete)
    }

    @Test
    fun testDeleteMandadoById() = runBlocking {
        val mandado = Mandado(description = "Paquete ropa", location = "Condesa", status = "PENDING", price = 55.0)
        val id = mandadoDao.insert(mandado)

        mandadoDao.deleteMandadoById(id)
        val afterDelete = mandadoDao.getMandadoById(id)
        assertNull(afterDelete)
    }
}
