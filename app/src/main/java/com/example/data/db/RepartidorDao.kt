package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.DepositoTarjetaLog
import com.example.data.model.RepartidorProfile
import kotlinx.coroutines.flow.Flow

@Dao
interface RepartidorDao {
    @Query("SELECT * FROM repartidor_profile WHERE id = 1")
    fun getProfileFlow(): Flow<RepartidorProfile?>

    @Query("SELECT * FROM repartidor_profile WHERE id = 1")
    suspend fun getProfileSync(): RepartidorProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateProfile(profile: RepartidorProfile)

    @Query("SELECT * FROM deposito_tarjeta_log ORDER BY fecha DESC")
    fun getDepositosFlow(): Flow<List<DepositoTarjetaLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDeposito(deposito: DepositoTarjetaLog): Long
}
