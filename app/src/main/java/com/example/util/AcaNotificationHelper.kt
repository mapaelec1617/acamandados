package com.example.util

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.MainActivity
import com.example.R

object AcaNotificationHelper {
    const val CHANNEL_CLIENTE_ID = "acamandados_cliente_channel"
    const val CHANNEL_REPARTIDOR_ID = "acamandados_repartidor_channel"

    private var channelsCreated = false

    fun initNotificationChannels(context: Context) {
        if (channelsCreated || Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            ?: return

        // Canal para clientes (estado de pedidos, repartidor cerca, entrega)
        val clienteChannel = NotificationChannel(
            CHANNEL_CLIENTE_ID,
            "Estado de Mandados (Clientes)",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Alertas de seguimiento en tiempo real: pedido recogido, repartidor cerca y entregado."
            enableVibration(true)
            vibrationPattern = longArrayOf(0, 250, 150, 250)
        }

        // Canal para repartidores (nuevas solicitudes de mandados con distancia)
        val repartidorChannel = NotificationChannel(
            CHANNEL_REPARTIDOR_ID,
            "Nuevas Solicitudes (Repartidores)",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Alertas sonoras de nuevos mandados locales en Acapulco con distancia y tarifa."
            enableVibration(true)
            vibrationPattern = longArrayOf(0, 300, 200, 300)
        }

        notificationManager.createNotificationChannel(clienteChannel)
        notificationManager.createNotificationChannel(repartidorChannel)
        channelsCreated = true
    }

    fun showSystemNotification(
        context: Context,
        title: String,
        message: String,
        isDriverNotification: Boolean = false,
        notificationId: Int = (1000..9999).random()
    ) {
        initNotificationChannels(context)

        // Verificar permisos en Android 13+ (TIRAMISU)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val hasPermission = ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!hasPermission) {
                // Si aún no está otorgado el permiso en tiempo de ejecución, omitimos la notificación del sistema
                return
            }
        }

        val channelId = if (isDriverNotification) CHANNEL_REPARTIDOR_ID else CHANNEL_CLIENTE_ID

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        try {
            NotificationManagerCompat.from(context).notify(notificationId, builder.build())
        } catch (_: SecurityException) {
            // Manejo seguro si el permiso fue revocado
        }
    }
}
