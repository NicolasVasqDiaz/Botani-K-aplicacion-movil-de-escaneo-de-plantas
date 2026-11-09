package com.botanik.app.ui.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

object NotificationHelper {

    const val CHANNEL_ID = "botanik_care_reminders"
    const val CHANNEL_NAME = "Recordatorios de cuidado"
    const val CHANNEL_DESC = "Avisos para regar y cuidar tus plantas de Mi Jardín"

    /** Crea el canal de notificación (llamar al iniciar la app). */
    fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = CHANNEL_DESC
            }
            val manager = context.getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    /** Muestra una notificación de recordatorio para una planta. */
    fun showCareReminder(
        context: Context,
        notificationId: Int,
        plantCommonName: String,
        plantScientificName: String
    ) {
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("🌱 Hora de cuidar tu $plantCommonName")
            .setContentText("Revisa el riego y los cuidados de tu planta")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("Tu $plantCommonName ($plantScientificName) puede necesitar agua hoy. Abre Botani-k para más detalles.")
            )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)

        try {
            NotificationManagerCompat.from(context).notify(notificationId, builder.build())
        } catch (e: SecurityException) {
            // El usuario no concedió POST_NOTIFICATIONS (Android 13+)
        }
    }
}