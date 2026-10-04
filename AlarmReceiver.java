package com.example.remindhub;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import androidx.core.app.NotificationCompat;

public class AlarmReceiver extends BroadcastReceiver {

    public static final String CHANNEL_PAYMENT = "payment_reminder_channel";
    public static final String CHANNEL_MEDICINE = "medicine_reminder_channel";
    public static final String CHANNEL_RENTAL = "rental_reminder_channel";

    @Override
    public void onReceive(Context context, Intent intent) {
        String type = intent.getStringExtra("REMINDER_TYPE");
        if (type == null) {
            type = "MEDICINE";
        }

        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager == null) return;

        if ("RENTAL".equals(type)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel channel = new NotificationChannel(
                        CHANNEL_RENTAL,
                        "Rental Reminders",
                        NotificationManager.IMPORTANCE_HIGH
                );
                manager.createNotificationChannel(channel);
            }

            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_RENTAL)
                    .setSmallIcon(android.R.drawable.ic_dialog_alert)
                    .setContentTitle("Rent & Rental Payment Reminder")
                    .setContentText("Rent Reminder: Check monthly house/furniture rent payments pending in RemindHub.")
                    .setStyle(new NotificationCompat.BigTextStyle()
                            .bigText("Rent Reminder: Check monthly house/furniture rent payments pending in RemindHub. Ensure dues are cleared on time!"))
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setAutoCancel(true);

            manager.notify(2003, builder.build());

        } else if ("PAYMENT".equals(type)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel channel = new NotificationChannel(
                        CHANNEL_PAYMENT,
                        "Payment Reminders",
                        NotificationManager.IMPORTANCE_DEFAULT
                );
                manager.createNotificationChannel(channel);
            }

            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_PAYMENT)
                    .setSmallIcon(android.R.drawable.ic_dialog_info)
                    .setContentTitle("Payment Reminder")
                    .setContentText("Check your pending payments in RemindHub.")
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setAutoCancel(true);

            manager.notify(2001, builder.build());

        } else {
            Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
            if (soundUri == null) {
                soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel channel = new NotificationChannel(
                        CHANNEL_MEDICINE,
                        "Medicine Alarms",
                        NotificationManager.IMPORTANCE_HIGH
                );
                AudioAttributes audioAttributes = new AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .build();
                channel.setSound(soundUri, audioAttributes);
                channel.enableVibration(true);
                channel.setVibrationPattern(new long[]{0, 500, 200, 500, 200, 500});
                manager.createNotificationChannel(channel);
            }

            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_MEDICINE)
                    .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
                    .setContentTitle("Medicine Alarm")
                    .setContentText("It's time to take your scheduled medicines!")
                    .setPriority(NotificationCompat.PRIORITY_MAX)
                    .setCategory(NotificationCompat.CATEGORY_ALARM)
                    .setSound(soundUri)
                    .setVibrate(new long[]{0, 500, 200, 500, 200, 500})
                    .setAutoCancel(true);

            manager.notify(2002, builder.build());
        }
    }
}