package com.ast.blocksave;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import java.text.DecimalFormat;
import java.util.Calendar;

public class NotificationsService extends BroadcastReceiver {

    public static int MORINNG_NOTIFICATION_HOUR = 7;
    public static int MIDDAY_NOTIFICATION_HOUR = 14;
    public static int EVENING_NOTIFICATION_HOUR = 21;

    public static String NOTIFICATION_ID = "notification-id";

    private float totalMoneyToSpend;
    private float currentMoneyToSpend;
    private long nextPayDay;
    private double staticBlockPrice;
    private long blockCount;
    private long blockCountDay;

    @Override
    public void onReceive(Context context, Intent intent) {

        NotificationManager notificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);

        int id = intent.getIntExtra(NOTIFICATION_ID, 0);

        loadData(context);

        int numberOfDaysUntilPayDay = Utils.getNumberOfDaysUntilPayDay(nextPayDay);

        double unformattedStaticBlockPrice = Utils.getStaticBlockPrice(totalMoneyToSpend, numberOfDaysUntilPayDay);

        DecimalFormat formatter = new DecimalFormat("#.00");
        staticBlockPrice = Double.valueOf(formatter.format(unformattedStaticBlockPrice));

        long blocksToDisplay = Utils.getBlocksToDisplayRounded(currentMoneyToSpend, nextPayDay, staticBlockPrice, blockCount);

        Notification.Builder builder = new Notification.Builder(context);
        builder.setSmallIcon(R.drawable.ic_monetization_on_black_24dp);

        String contentTitle = "";
        if(id == MORINNG_NOTIFICATION_HOUR) {
            contentTitle = "Morning BlockSave reminder";
        } else if(id == MIDDAY_NOTIFICATION_HOUR) {
            contentTitle = "Afternoon BlockSave reminder";
        } else if(id == EVENING_NOTIFICATION_HOUR) {
            contentTitle = "Evening BlockSave reminder";
        }
        builder.setContentTitle(contentTitle);

        String contentText = "";
        if(!Utils.isTheSameDay(blockCountDay, Calendar.getInstance().getTimeInMillis())) {
            contentText = "Please open BlockSave to see what today's budget is";
        } else if(blocksToDisplay > 1) {
            contentText = "You have " + blocksToDisplay + " blocks left to spend today";
        } else if(blocksToDisplay == 1) {
            contentText = "You have " + blocksToDisplay + " block left to spend today";
        } else if(blocksToDisplay == 0) {
            contentText = "You have no blocks left to spend today";
        } else if(blocksToDisplay < 0) {
            contentText = "You have overspent by " + (blocksToDisplay*-1) + " blocks";
        }
        builder.setContentText(contentText);

        PendingIntent contentIntent = PendingIntent.getActivity(context, 0,
                new Intent(context, DashboardActivity.class), PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(contentIntent);
        builder.setAutoCancel(true);

        notificationManager.notify(id, builder.build());
    }

    private void loadData(Context context) {

        SharedPreferences preferences = context.getSharedPreferences("block_save_data", 0);

        totalMoneyToSpend = preferences.getFloat("total_money_to_spend", 0.0F);
        currentMoneyToSpend = preferences.getFloat("current_money_to_spend", 0.0F);
        nextPayDay = preferences.getLong("next_pay_day", 0L);
        blockCount = preferences.getLong("block_count", 0L);
        blockCountDay = preferences.getLong("block_count_day", 0L);

    }

}