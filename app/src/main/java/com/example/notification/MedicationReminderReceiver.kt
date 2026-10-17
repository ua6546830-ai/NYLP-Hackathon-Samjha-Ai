package com.example.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R

class MedicationReminderReceiver : BroadcastReceiver() {

    companion object {
        const val CHANNEL_ID = "medication_dosage_reminders"
        const val CHANNEL_NAME = "Medication Dosage Reminders"
        const val EXTRA_MED_NAME = "extra_med_name"
        const val EXTRA_SLOT_NAME = "extra_slot_name"
        const val EXTRA_DOSAGE = "extra_dosage"
        const val EXTRA_FOOD_NOTE = "extra_food_note"
        const val EXTRA_NOTIFICATION_ID = "extra_notification_id"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val medName = intent.getStringExtra(EXTRA_MED_NAME) ?: "Prescribed Medication"
        val slotName = intent.getStringExtra(EXTRA_SLOT_NAME) ?: "Dosage Reminder"
        val dosage = intent.getStringExtra(EXTRA_DOSAGE) ?: "Take as prescribed"
        val foodNote = intent.getStringExtra(EXTRA_FOOD_NOTE) ?: "Take with water"
        val notificationId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, (System.currentTimeMillis() % 10000).toInt())

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Create Channel on Android O+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Daily notifications reminding patients to take medications based on extracted prescription instructions"
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        // Tap intent to open Samjha? Rx app
        val contentIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("💊 Medication Time: $medName")
            .setContentText("$slotName: $dosage • $foodNote")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("It's time to take your prescribed dose of $medName.\n\n• Dosage: $dosage\n• Timing: $slotName\n• Instructions: $foodNote\n\nPrescription directions verified via Samjha? Rx.")
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .build()

        notificationManager.notify(notificationId, notification)
    }
}
