package com.ast.blocksave;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DecimalFormat;
import java.util.Calendar;

import static com.ast.blocksave.NotificationsService.EVENING_NOTIFICATION_HOUR;
import static com.ast.blocksave.NotificationsService.MIDDAY_NOTIFICATION_HOUR;
import static com.ast.blocksave.NotificationsService.MORINNG_NOTIFICATION_HOUR;
import static com.ast.blocksave.SetupActivity.BLOCKS_PER_DAY;

public class DashboardActivity extends AppCompatActivity {

    public static int ELEVATION_HEIGHT = 6;

    private String TODAYS_BLOCKS_POSITIVE = "Your budget for today:";
    private String TODAYS_BLOCKS_NEGATIVE = "You have overspent by:";
    private long BLOCK_DISPLAY_LIMIT = 14;
    private String OVER_SPEND_ZERO_TEXT = "Tomorrows budget is";
    private String OVER_SPEND_BLOCK_ZERO_TEXT = "  0 blocks";

    private float staticBlockPrice = 0.0F;
    private float currentMoneyToSpend = 0.0F;

    private long nextPayDay = 0L;
    private long setupDay = 0L;

    private long tomorrowBlockCount = 10L;
    private long blockCount = 10L;
    private long blockCountDay = 0L;
    private long todaysBlockTotal = 0L;

    private LinearLayout blockDisplayLayoutTop;
    private LinearLayout blockDisplayLayoutBottom;

    private LinearLayout overSpendLayout;
    private LinearLayout blocksOverLayout;

    private TextView overSpendTitleText1;
    private TextView overSpendTitleText2;

    private GridLayout topLevelGridLayout;
    private GridLayout dayCountLayout;
    private GridLayout blocksToSpendLayout;
    private GridLayout purchaseEntryGridLayout;
    private GridLayout tomorrowInfoLayout;

    private TextView blocksToSpendToday;
    private TextView purchaseAmount;
    private TextView calculatedBlocks;
    private Button purchaseButton;

    private TextView underSpendOutput;
    private TextView overSpendOutput;
    private TextView tomorrowsBudgetOutput;
    private TextView amountSpentText;
    private TextView blocksToSpendText;
    private TextView daysLeft;

    private boolean morningNotification;
    private boolean afternoonNotification;
    private boolean eveningNotification;

    private String currencySymbol;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
    }

    @Override
    protected void onResume() {
        super.onResume();
        setContentView(R.layout.activity_dashboard);

        boolean continueToLoadActivity = loadData();

        if(continueToLoadActivity) {

            findDashboardScreenElements();
            calculateAndDisplayData();
            addListeners();

            scheduleNotifications();

        } else {
            Intent intent = new Intent(getApplicationContext(), HelpActivity.class);
            startActivity(intent);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.action_setup) {

            Intent intent = new Intent(getApplicationContext(), SetupActivity.class);
            startActivity(intent);
            return true;

        } else if (id == R.id.action_help) {

            SharedPreferences settings = getSharedPreferences("block_save_data", 0);
            SharedPreferences.Editor editor = settings.edit();
            editor.putBoolean("help_visited", false);
            editor.commit();

            Intent intent = new Intent(getApplicationContext(), HelpActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.action_exit) {
            this.finishAffinity();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void switchToSettingsScreen() {
        Intent intent = new Intent(getApplicationContext(), SetupActivity.class);
        startActivity(intent);
    }

    private void addListeners() {
        purchaseButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {

                    if(!purchaseAmount.getText().toString().trim().isEmpty()
                            && !purchaseAmount.getText().toString().trim().equals(".")
                            && (currentMoneyToSpend - Float.valueOf(purchaseAmount.getText().toString().trim()) > 0.0)) {

                        AlertDialog.Builder builder = new AlertDialog.Builder(DashboardActivity.this);

                        String blocksToDeductString = calculatedBlocks.getText().toString();
                        if(!blocksToDeductString.equals("1")) {
                            blocksToDeductString = calculatedBlocks.getText().toString() + " blocks";
                        } else {
                            blocksToDeductString = calculatedBlocks.getText().toString() + " block";
                        }

                        builder.setMessage("Add purchase of "
                                + currencySymbol
                                + Utils.formatMonetaryValue(purchaseAmount.getText().toString()) + " ?"
                                + "\n\nThis will cost "
                                + blocksToDeductString + "\n");

                        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {

                                dialog.dismiss();

                                currentMoneyToSpend = currentMoneyToSpend - Float.valueOf(purchaseAmount.getText().toString());

                                saveData();
                                purchaseAmount.setText("");
                                calculateAndDisplayData();


                                int duration = Toast.LENGTH_SHORT;
                                Toast toast = Toast.makeText(getApplicationContext(), "Purchase added", duration);
                                toast.show();

                            }
                        });
                        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.dismiss();
                            }
                        });
                        AlertDialog alert = builder.create();
                        alert.show();

                    } else if(purchaseAmount.getText().toString().trim().isEmpty() || purchaseAmount.getText().toString().trim().equals(".")) {

                        int duration = Toast.LENGTH_SHORT;
                        Toast toast = Toast.makeText(getApplicationContext(), "Enter an amount to purchase", duration);
                        toast.show();

                    } else if(!purchaseAmount.getText().toString().trim().isEmpty()
                            && !purchaseAmount.getText().toString().trim().equals(".")
                            && (currentMoneyToSpend - Float.valueOf(purchaseAmount.getText().toString().trim()) <= 0.0)) {

                        int duration = Toast.LENGTH_SHORT;
                        Toast toast = Toast.makeText(getApplicationContext(), "You do not have enough money", duration);
                        toast.show();

                    }

                }
                return false;
            }
        });

        purchaseAmount.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View view, int keyCode, KeyEvent event) {
                calculateAndDisplayData();
                return false;
            }
        });
    }

    private boolean loadData() {

        SharedPreferences preferences = getSharedPreferences("block_save_data", 0);

        staticBlockPrice = preferences.getFloat("static_block_price", 0.0F);
        currentMoneyToSpend = preferences.getFloat("current_money_to_spend", 0.0F);
        nextPayDay = preferences.getLong("next_pay_day", 0L);
        setupDay = preferences.getLong("setup_day", 0L);

        morningNotification = preferences.getBoolean("morning_notification", false);
        afternoonNotification = preferences.getBoolean("afternoon_notification", false);
        eveningNotification = preferences.getBoolean("evening_notification", false);

        return preferences.getBoolean("help_visited", false);
    }

    private void saveData() {

        SharedPreferences settings = getSharedPreferences("block_save_data", 0);

        SharedPreferences.Editor editor = settings.edit();
        editor.putFloat("current_money_to_spend", currentMoneyToSpend);
        editor.putBoolean("help_visited", true);
        editor.putLong("tomorrow_block_count", tomorrowBlockCount);

        editor.commit();
    }

    private void saveTomorrowBlockCountData() {

        SharedPreferences settings = getSharedPreferences("block_save_data", 0);

        SharedPreferences.Editor editor = settings.edit();
        editor.putLong("tomorrow_block_count", tomorrowBlockCount);

        editor.commit();
    }

    private void loadTodaysBlocksData() {
        SharedPreferences preferences = getSharedPreferences("block_save_data", 0);
        todaysBlockTotal = preferences.getLong("todays_block_total", 0L);
        tomorrowBlockCount = preferences.getLong("tomorrow_block_count", 10L);
        blockCount = preferences.getLong("block_count", 10L);
        blockCountDay = preferences.getLong("block_count_day", 0L);
    }

    private void saveTodaysBlocksData() {
        SharedPreferences settings = getSharedPreferences("block_save_data", 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putLong("block_count", blockCount);
        editor.putLong("block_count_day", blockCountDay);
        editor.commit();
    }

    private void saveTodaysBlockTotal() {
        SharedPreferences settings = getSharedPreferences("block_save_data", 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putLong("todays_block_total", todaysBlockTotal);
        editor.commit();
    }

    private void findDashboardScreenElements() {

        currencySymbol = Utils.getCurrencySymbol(this);

        topLevelGridLayout = (GridLayout) findViewById(R.id.topLevelGridLayout);

        dayCountLayout = (GridLayout) findViewById(R.id.dayCountLayout);
        dayCountLayout.setElevation(ELEVATION_HEIGHT);
        blocksToSpendLayout = (GridLayout) findViewById(R.id.blocksToSpendLayout);
        blocksToSpendLayout.setElevation(ELEVATION_HEIGHT);
        purchaseEntryGridLayout = (GridLayout) findViewById(R.id.purchaseEntryGridLayout);
        purchaseEntryGridLayout.setElevation(ELEVATION_HEIGHT);
        tomorrowInfoLayout = (GridLayout) findViewById(R.id.tomorrowInfoLayout);
        tomorrowInfoLayout.setElevation(ELEVATION_HEIGHT);

        blocksOverLayout = (LinearLayout) findViewById(R.id.blocksOverLayout);
        overSpendLayout = (LinearLayout) findViewById(R.id.overSpendLayout);
        overSpendTitleText1 = (TextView) findViewById(R.id.overSpendTitleText1);
        overSpendTitleText2 = (TextView) findViewById(R.id.overSpendTitleText2);

        blockDisplayLayoutTop = (LinearLayout) findViewById(R.id.blockDisplayLayoutTop);
        blockDisplayLayoutBottom = (LinearLayout) findViewById(R.id.blockDisplayLayoutBottom);
        tomorrowInfoLayout = (GridLayout) findViewById(R.id.tomorrowInfoLayout);
        blocksToSpendToday = (TextView) findViewById(R.id.blocksToSpendToday);
        purchaseAmount = (TextView) findViewById(R.id.purchaseAmount);
        purchaseButton = (Button) findViewById(R.id.purchaseButton);
        calculatedBlocks = (TextView) findViewById(R.id.calculatedBlocks);
        underSpendOutput = (TextView) findViewById(R.id.underSpendOutput);
        overSpendOutput = (TextView) findViewById(R.id.overSpendOutput);
        tomorrowsBudgetOutput = (TextView) findViewById(R.id.tomorrowsBudgetOutput);
        amountSpentText = (TextView) findViewById(R.id.amountSpentText);
        amountSpentText.setText("Enter amount spent:   " + currencySymbol);
        blocksToSpendText = (TextView) findViewById(R.id.blocksToSpendText);
        daysLeft = (TextView) findViewById(R.id.daysLeft);
    }

    private void displayBlocks(long numberOfBlocksToDisplay, long numberOfBlocksToHighlight) {

        blockDisplayLayoutTop.removeAllViews();
        blockDisplayLayoutBottom.removeAllViews();

        long spareBlocks = numberOfBlocksToDisplay - BLOCK_DISPLAY_LIMIT;

        if(numberOfBlocksToDisplay > BLOCK_DISPLAY_LIMIT-1) {
            numberOfBlocksToDisplay = BLOCK_DISPLAY_LIMIT;
        }

        for (int i = 0; i < numberOfBlocksToDisplay; i++) {

            TextView textView = null;

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);

            if ( (i % 2) == 0) {
                textView = new TextView(blockDisplayLayoutTop.getContext());
                textView.setPadding(Utils.convertDpToPixels(2.5F, this.getApplicationContext()),
                        Utils.convertDpToPixels(7F, this.getApplicationContext()),
                        Utils.convertDpToPixels(2.5F, this.getApplicationContext()),
                        Utils.convertDpToPixels(8F, this.getApplicationContext()));
                params.setMargins(Utils.convertDpToPixels(8F, this.getApplicationContext()),
                        Utils.convertDpToPixels(6F, this.getApplicationContext()), 0, 0);
            } else {
                textView = new TextView(blockDisplayLayoutBottom.getContext());
                textView.setPadding(Utils.convertDpToPixels(2.5F, this.getApplicationContext()),
                        Utils.convertDpToPixels(7F, this.getApplicationContext()),
                        Utils.convertDpToPixels(2.5F, this.getApplicationContext()),
                        Utils.convertDpToPixels(8F, this.getApplicationContext()));
                params.setMargins(Utils.convertDpToPixels(8F, this.getApplicationContext()),
                        Utils.convertDpToPixels(2F, this.getApplicationContext()), 0, 0);
            }

            Typeface typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL);
            textView.setTypeface(typeface);

            Drawable drawable = null;

            textView.setTextSize(17);

            textView.setText(" x ");
            textView.setGravity(Gravity.CENTER_VERTICAL);
            textView.setElevation(ELEVATION_HEIGHT);

            if(i >= numberOfBlocksToDisplay - numberOfBlocksToHighlight) {
                drawable = ContextCompat.getDrawable(getApplicationContext(), R.drawable.block_style_2);
                textView.setTextColor(Color.DKGRAY);

                if(i >= BLOCK_DISPLAY_LIMIT - spareBlocks) {
                    textView.setTextColor(Color.DKGRAY);
                }
            } else {
                drawable = ContextCompat.getDrawable(getApplicationContext(), R.drawable.block_style_1);
                textView.setTextColor(ContextCompat.getColor(this, R.color.block_colour));

                if(i >= BLOCK_DISPLAY_LIMIT - spareBlocks) {
                    textView.setTextColor(Color.DKGRAY);
                    textView.setText(" 2 ");
                }
            }

            textView.setBackground(drawable);

            if ( (i % 2) == 0) {
                blockDisplayLayoutTop.addView(textView, params);
            } else {
                blockDisplayLayoutBottom.addView(textView, params);
            }
        }
    }

    private boolean calculateAndDisplayData() {

        int numberOfDaysUntilPayDay = Utils.getNumberOfDaysUntilPayDay(nextPayDay);

        if(numberOfDaysUntilPayDay < 1) {
            switchToSettingsScreen();
            return false;
        }

        loadTodaysBlocksData();

        if(!Utils.isTheSameDay(blockCountDay, Calendar.getInstance().getTimeInMillis())) {

            todaysBlockTotal = ((Double) Utils.getCurrentBlocksAvailable(currentMoneyToSpend, staticBlockPrice)).intValue();

            saveTodaysBlockTotal();

            if(tomorrowBlockCount <= 0L) {
                tomorrowBlockCount = BLOCKS_PER_DAY;
            }

            blockCount = Utils.getBlocksToDisplayRounded(currentMoneyToSpend, staticBlockPrice, tomorrowBlockCount, todaysBlockTotal);

            if(blockCount <= 0L) {
                blockCount = 1L;
            }

            blockCountDay = Calendar.getInstance().getTimeInMillis();

            saveTodaysBlocksData();
        }

        daysLeft.setText("Day " + Utils.getDayNumber(setupDay) + " of " + Utils.getDaysDifference(setupDay, nextPayDay));

        long blocksToDeduct = 0L;

        if (!purchaseAmount.getText().toString().trim().isEmpty() && !purchaseAmount.getText().toString().trim().equals(".")) {
            blocksToDeduct = Utils.getBlocksToDeduct(currentMoneyToSpend, Double.valueOf(purchaseAmount.getText().toString().trim()), staticBlockPrice, nextPayDay, blockCount, todaysBlockTotal);
            calculatedBlocks.setText(blocksToDeduct + "");
        } else {
            calculatedBlocks.setText("0");
        }

        String blocksToDisplayString = "";
        long blocksToDisplay = Utils.getBlocksToDisplayRounded(currentMoneyToSpend, staticBlockPrice, blockCount, todaysBlockTotal);
        long blocksToShow = blocksToDisplay;

        if (blocksToDisplay < 0) {

            blocksToDisplay = blocksToDisplay * -1;
            blocksToSpendText.setText(TODAYS_BLOCKS_NEGATIVE);
            blocksToSpendToday.setTextColor(ContextCompat.getColor(this, android.R.color.holo_red_dark));
            blocksToSpendToday.setText(blocksToDisplay + "");

            ViewGroup.LayoutParams layoutParams = blocksToSpendLayout.getLayoutParams();
            layoutParams.height = Utils.convertDpToPixels(47.0F, this.getApplicationContext());
            blocksToSpendLayout.setLayoutParams(layoutParams);

        } else if (blocksToDisplay > 0) {

            blocksToSpendText.setText(TODAYS_BLOCKS_POSITIVE);
            blocksToSpendToday.setTextColor(ContextCompat.getColor(this, android.R.color.holo_green_dark));
            if(blocksToDisplay > BLOCK_DISPLAY_LIMIT * 2) {
                blocksToDisplayString = (BLOCK_DISPLAY_LIMIT * 2) + "+";
            } else {
                blocksToDisplayString = blocksToDisplay+"";
            }
            blocksToSpendToday.setText(blocksToDisplayString);

        } else {

            blocksToSpendText.setText(TODAYS_BLOCKS_POSITIVE);
            blocksToSpendToday.setTextColor(ContextCompat.getColor(this, android.R.color.darker_gray));
            blocksToSpendToday.setText("None");

        }

        if(numberOfDaysUntilPayDay < 2) {

            topLevelGridLayout.removeView(tomorrowInfoLayout);

        } else {

            long underSpend = Utils.getUnderSpendValue(setupDay, nextPayDay, currentMoneyToSpend, staticBlockPrice);
            if (underSpend < 10) {
                if (underSpend == 1) {
                    underSpendOutput.setText("  " + underSpend + " block");
                } else {
                    underSpendOutput.setText("  " + underSpend + " blocks");
                }
            } else if (underSpend > 99) {
                underSpendOutput.setText("99+ blocks");
            } else {
                underSpendOutput.setText(" " + underSpend + " blocks");
            }

            int overSpend = Utils.getOverSpendValue(setupDay, nextPayDay, currentMoneyToSpend, staticBlockPrice);
            if (overSpend < 10) {
                if (overSpend == 1) {
                    overSpendOutput.setText("  " + overSpend + " block");
                } else {
                    overSpendOutput.setText("  " + overSpend + " blocks");
                }
            } else if (overSpend > 99) {
                overSpendOutput.setText("99+ blocks");
            } else {
                overSpendOutput.setText(" " + overSpend + " blocks");
            }

            tomorrowBlockCount = Utils.getBlockBudgetFromTomorrow(setupDay, nextPayDay, currentMoneyToSpend, staticBlockPrice);

            saveTomorrowBlockCountData();

            if(tomorrowBlockCount < 1) {

                tomorrowInfoLayout.removeView(blocksOverLayout);
                tomorrowInfoLayout.removeView(overSpendLayout);

                ViewGroup.LayoutParams layoutParams = tomorrowInfoLayout.getLayoutParams();
                layoutParams.height = Utils.convertDpToPixels(132.0F, this.getApplicationContext());
                tomorrowInfoLayout.setLayoutParams(layoutParams);

                overSpendTitleText1.setText(OVER_SPEND_ZERO_TEXT);
                overSpendTitleText2.setText(OVER_SPEND_BLOCK_ZERO_TEXT);

            } else {

                if (tomorrowBlockCount < 10) {
                    if (tomorrowBlockCount == 1) {
                        tomorrowsBudgetOutput.setText("  " + tomorrowBlockCount + " block");
                    } else {
                        tomorrowsBudgetOutput.setText("  " + tomorrowBlockCount + " blocks");
                    }
                } else if (tomorrowBlockCount > 99) {
                    tomorrowsBudgetOutput.setText("99+ blocks");
                } else {
                    tomorrowsBudgetOutput.setText(" " + tomorrowBlockCount + " blocks");
                }

            }

        }

        displayBlocks(blocksToShow, blocksToDeduct);

        return true;
    }

    private void scheduleNotifications() {

        if(morningNotification) {

            Calendar calendar = Calendar.getInstance();

            calendar.set(Calendar.HOUR_OF_DAY, MORINNG_NOTIFICATION_HOUR);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);

            if(calendar.getTimeInMillis() < Calendar.getInstance().getTimeInMillis()) {
                calendar.add(Calendar.DATE, 1);
            }

            scheduleNotification(calendar, MORINNG_NOTIFICATION_HOUR);
        } else {
            scheduleNotification(MORINNG_NOTIFICATION_HOUR);
        }

        if(afternoonNotification) {

            Calendar calendar = Calendar.getInstance();

            calendar.set(Calendar.HOUR_OF_DAY, MIDDAY_NOTIFICATION_HOUR);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);

            if(calendar.getTimeInMillis() < Calendar.getInstance().getTimeInMillis()) {
                calendar.add(Calendar.DATE, 1);
            }

            scheduleNotification(calendar, MIDDAY_NOTIFICATION_HOUR);
        } else {
            scheduleNotification(MIDDAY_NOTIFICATION_HOUR);
        }

        if(eveningNotification) {

            Calendar calendar = Calendar.getInstance();

            calendar.set(Calendar.HOUR_OF_DAY, EVENING_NOTIFICATION_HOUR);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);

            if(calendar.getTimeInMillis() < Calendar.getInstance().getTimeInMillis()) {
                calendar.add(Calendar.DATE, 1);
            }

            scheduleNotification(calendar, EVENING_NOTIFICATION_HOUR);
        } else {
            scheduleNotification(EVENING_NOTIFICATION_HOUR);
        }

    }

    private void scheduleNotification(int notificationId) {

        Intent notificationIntent = new Intent(this, NotificationsService.class);
        notificationIntent.putExtra(NotificationsService.NOTIFICATION_ID, notificationId);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, notificationId, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        alarmManager.cancel(pendingIntent);
    }

    private void scheduleNotification(Calendar calendar, int notificationId) {

        Intent notificationIntent = new Intent(this, NotificationsService.class);
        notificationIntent.putExtra(NotificationsService.NOTIFICATION_ID, notificationId);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, notificationId, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent);
    }

}
