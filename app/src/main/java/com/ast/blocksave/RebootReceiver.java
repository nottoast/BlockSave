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
        //Calendar calendar = this.getMyCalendar();
        //this.scheduleAlarms(context, calendar);
        System.out.println("Test");
    }

    /*
    private Calendar getMyCalendar() {
        // get your calendar object
        return new Calendar.getInstance();
    }
    */

    private void scheduleAlarms(Context context, Calendar calendar) {
        /*

        Intent i = new Intent(context, ScheduledService.class);
        i.putExtra(ALARM_ID, 1);
        i.putExtra(NOTIFICATION_ID, 1);
        */

        Intent notificationIntent = new Intent(context, NotificationsService.class);
        notificationIntent.putExtra(NotificationsService.NOTIFICATION_ID, 1);
        notificationIntent.putExtra(NotificationsService.NOTIFICATION, getNotification(context, "BlockSave Notification"));

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