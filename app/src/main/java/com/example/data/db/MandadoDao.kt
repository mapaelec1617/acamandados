package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.MandadoOrder
import kotlinx.coroutines.flow.Flow

@Dao
interface MandadoDao {
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
