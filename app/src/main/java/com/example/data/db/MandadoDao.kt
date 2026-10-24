package com.example.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.Mandado
import com.example.data.model.MandadoOrder
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object (DAO) interface for the Mandado entity to enable CRUD operations
 * for managing deliveries, as well as preserving existing MandadoOrder operations.
 */
@Dao
interface MandadoDao {

    // ==========================================
    // CRUD Operations for Mandado Entity
    // ==========================================

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(mandado: Mandado): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMandado(mandado: Mandado): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(mandados: List<Mandado>): List<Long>

    @Query("SELECT * FROM mandados ORDER BY id DESC")
    fun getAllMandados(): Flow<List<Mandado>>

    @Query("SELECT * FROM mandados ORDER BY id DESC")
    fun getAll(): Flow<List<Mandado>>

    @Query("SELECT * FROM mandados WHERE id = :id")
    suspend fun getMandadoById(id: Long): Mandado?

    @Query("SELECT * FROM mandados WHERE id = :id")
    fun getMandadoByIdFlow(id: Long): Flow<Mandado?>

    @Query("SELECT * FROM mandados WHERE status = :status ORDER BY id DESC")
    fun getMandadosByStatus(status: String): Flow<List<Mandado>>

    @Update
    suspend fun update(mandado: Mandado)

    @Update
    suspend fun updateMandado(mandado: Mandado)

    @Query("UPDATE mandados SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: Long, status: String)

    @Delete
    suspend fun delete(mandado: Mandado)

    @Delete
    suspend fun deleteMandado(mandado: Mandado)

    @Query("DELETE FROM mandados WHERE id = :id")
    suspend fun deleteMandadoById(id: Long)

    @Query("DELETE FROM mandados")
    suspend fun deleteAllMandados()

    // ==========================================
    // Operations for MandadoOrder Entity
    // ==========================================

    @Query("SELECT * FROM mandado_orders ORDER BY fechaCreacion DESC")
    fun getAllOrders(): Flow<List<MandadoOrder>>

    @Query("SELECT * FROM mandado_orders WHERE estado = 'SOLICITADO' ORDER BY fechaCreacion DESC")
    fun getAvailableOrders(): Flow<List<MandadoOrder>>

    @Query("SELECT * FROM mandado_orders WHERE estado IN ('SOLICITADO', 'ACEPTADO', 'EN_CAMINO') ORDER BY fechaCreacion DESC")
    fun getActiveOrders(): Flow<List<MandadoOrder>>

    @Query("SELECT * FROM mandado_orders WHERE repartidorId = :driverId AND estado IN ('ACEPTADO', 'EN_CAMINO') ORDER BY fechaCreacion DESC LIMIT 1")
    fun getCurrentActiveDriverOrder(driverId: Long): Flow<MandadoOrder?>

    @Query("SELECT * FROM mandado_orders WHERE repartidorId = :driverId AND estado = 'ENTREGADO' ORDER BY fechaEntrega DESC")
    fun getDriverCompletedOrders(driverId: Long): Flow<List<MandadoOrder>>

    @Query("SELECT * FROM mandado_orders WHERE id = :orderId")
    suspend fun getOrderById(orderId: Long): MandadoOrder?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: MandadoOrder): Long

    @Update
    suspend fun updateOrder(order: MandadoOrder)

    @Query("DELETE FROM mandado_orders WHERE id = :orderId")
    suspend fun deleteOrderById(orderId: Long)
}
