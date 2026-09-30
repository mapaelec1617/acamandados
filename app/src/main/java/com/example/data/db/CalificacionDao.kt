package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.CalificacionReview
import kotlinx.coroutines.flow.Flow

@Dao
interface CalificacionDao {
    @Query("SELECT * FROM calificaciones ORDER BY fecha DESC")
    fun getAllCalificaciones(): Flow<List<CalificacionReview>>

    @Query("SELECT * FROM calificaciones WHERE destinatarioNombre = :nombre ORDER BY fecha DESC")
    fun getCalificacionesPara(nombre: String): Flow<List<CalificacionReview>>

    @Query("SELECT * FROM calificaciones WHERE orderId = :orderId AND tipo = :tipo LIMIT 1")
    suspend fun getCalificacionForOrder(orderId: Long, tipo: String): CalificacionReview?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCalificacion(calificacion: CalificacionReview): Long
}
