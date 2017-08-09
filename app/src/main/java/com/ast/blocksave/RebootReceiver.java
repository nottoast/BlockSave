package com.ast.blocksave;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import java.util.Calendar;

import static com.ast.blocksave.NotificationsService.EVENING_NOTIFICATION_HOUR;
import static com.ast.blocksave.NotificationsService.MIDDAY_NOTIFICATION_HOUR;
import static com.ast.blocksave.NotificationsService.MORINNG_NOTIFICATION_HOUR;

public class RebootReceiver extends BroadcastReceiver {

    private boolean morningNotification;
    private boolean afternoonNotification;
    private boolean eveningNotification;

    @Override
    public void onReceive(Context context, Intent intent) {

        loadData(context);

        if(morningNotification) {
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, MORINNG_NOTIFICATION_HOUR);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            scheduleNotification(context, calendar, MORINNG_NOTIFICATION_HOUR, true);
        } else {
            scheduleNotification(context, Calendar.getInstance(), MORINNG_NOTIFICATION_HOUR, false);
        }

        if(afternoonNotification) {
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, MIDDAY_NOTIFICATION_HOUR);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            scheduleNotification(context, calendar, MIDDAY_NOTIFICATION_HOUR, true);
        } else {
            scheduleNotification(context, Calendar.getInstance(), MIDDAY_NOTIFICATION_HOUR, false);
        }

        if(eveningNotification) {
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, EVENING_NOTIFICATION_HOUR);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            scheduleNotification(context, calendar, EVENING_NOTIFICATION_HOUR, true);
        } else {
            scheduleNotification(context, Calendar.getInstance(), EVENING_NOTIFICATION_HOUR, false);
        }
    }

    private void scheduleNotification(Context context, Calendar calendar, int notificationId, boolean run) {

        Intent notificationIntent = new Intent(context, NotificationsService.class);
        notificationIntent.putExtra(NotificationsService.NOTIFICATION_ID, notificationId);

        PendingIntent pendingIntent = PendingIntent.getService(context, notificationId, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        if(run) {
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent);
        } else {
            alarmManager.cancel(pendingIntent);
        }
    }

    private void loadData(Context context) {

        SharedPreferences preferences = context.getSharedPreferences("block_save_data", 0);

        morningNotification = preferences.getBoolean("morning_notification", false);
        afternoonNotification = preferences.getBoolean("afternoon_notification", false);
        eveningNotification = preferences.getBoolean("evening_notification", false);
    }

}