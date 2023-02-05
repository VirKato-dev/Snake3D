package my.virkato.snake3d;
import java.util.Calendar;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class PrayerTimeNotification extends BroadcastReceiver {

    private static final int NOTIFICATION_ID = 1;
    private static final String NOTIFICATION_CHANNEL_ID = "prayer_time_channel";

    @Override
    public void onReceive(Context context, Intent intent) {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        // Проверка времени молитвы для различных молитв
        if (hour == 6 && minute == 30) {
            sendNotification(context, "Время зурр (утренней) молитвы");
        } else if (hour == 12 && minute == 45) {
            sendNotification(context, "Время зухр (дневной) молитвы");
        } else if (hour == 16 && minute == 30) {
            sendNotification(context, "Время магриб (вечерней) молитвы");
        } else if (hour == 19 && minute == 30) {
            sendNotification(context, "Время иша (ночной) молитвы");
        }
    }

    private void sendNotification(Context context, String message) {
        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

        Notification.Builder builder = new Notification.Builder(context)
                //.setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("Время молитвы")
                .setContentText(message)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }
}