package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.NotificationItem
import kotlinx.coroutines.flow.Flow

@Dao
interface NotificationDao {
    @Query("SELECT * FROM app_notifications ORDER BY fecha DESC")
    fun getAllNotifications(): Flow<List<NotificationItem>>

    @Query("SELECT * FROM app_notifications WHERE paraRol IN (:role, 'TODOS') ORDER BY fecha DESC")
    fun getNotificationsForRole(role: String): Flow<List<NotificationItem>>

    @Query("SELECT COUNT(*) FROM app_notifications WHERE leida = 0")
    fun getUnreadCountFlow(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: NotificationItem): Long

    @Query("UPDATE app_notifications SET leida = 1 WHERE id = :id")
    suspend fun markAsRead(id: Long)

    @Query("UPDATE app_notifications SET leida = 1")
    suspend fun markAllAsRead()

    @Query("DELETE FROM app_notifications WHERE id = :id")
    suspend fun deleteNotification(id: Long)
}
