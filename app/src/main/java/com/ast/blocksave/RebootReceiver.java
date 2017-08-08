package com.ast.blocksave;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import java.util.Calendar;

public class RebootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        Calendar calendar = Calendar.getInstance();

        calendar.set(Calendar.HOUR_OF_DAY, 7);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        scheduleNotification(context, getNotification(context, "7am BlockSave notification"), calendar);

        calendar.set(Calendar.HOUR_OF_DAY, 2);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        scheduleNotification(context, getNotification(context, "2pm BlockSave notification"), calendar);

        calendar.set(Calendar.HOUR_OF_DAY, 9);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        scheduleNotification(context, getNotification(context, "9pm BlockSave notification"), calendar);
    }

    private void scheduleNotification(Context context, Notification notification, Calendar calendar) {

        Intent notificationIntent = new Intent(context, NotificationsService.class);
        notificationIntent.putExtra(NotificationsService.NOTIFICATION_ID, 1);
        notificationIntent.putExtra(NotificationsService.NOTIFICATION, notification);

        PendingIntent pendingIntent = PendingIntent.getService(context, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        AlarmManager alarManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent);
    }

    private Notification getNotification(Context context, String content) {

        Notification.Builder builder = new Notification.Builder(context);
        builder.setContentTitle("Scheduled Notification");
        builder.setContentText(content);
        builder.setSmallIcon(R.mipmap.ic_launcher);

        return builder.build();
    }

}