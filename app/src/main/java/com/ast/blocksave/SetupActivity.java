package com.ast.blocksave;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import static com.ast.blocksave.NotificationsService.EVENING_NOTIFICATION_HOUR;
import static com.ast.blocksave.NotificationsService.MIDDAY_NOTIFICATION_HOUR;
import static com.ast.blocksave.NotificationsService.MORINNG_NOTIFICATION_HOUR;

public class SetupActivity extends AppCompatActivity {

    public static int BLOCKS_PER_DAY = 10;

    private float totalMoneyToSpend = 0.0F;
    private float currentMoneyToSpend = 0.0F;

    private long payDate = Calendar.getInstance().getTimeInMillis();
    private long setupDate = Calendar.getInstance().getTimeInMillis();

    private LinearLayout datePickerLayout;
    private LinearLayout dateContinueLayout;
    private LinearLayout enterAmountLayout;
    private LinearLayout blockWorthLayout;

    private GridLayout notificationLayout;
    private LinearLayout morningNotificationLayout;
    private LinearLayout middayNotification;
    private LinearLayout eveningNotificationLayout;
    private LinearLayout saveLayout;

    private DatePicker datePicker;
    private EditText budget;
    private TextView blockValueText;
    private TextView currency1;
    private Button saveButton;
    private Button continueButton;

    private String staticBlockPrice = "0.00";

    private CheckBox morningCheckbox;
    private CheckBox afternoonCheckbox;
    private CheckBox eveningCheckbox;

    private boolean morningNotification;
    private boolean afternoonNotification;
    private boolean eveningNotification;

    private String currencySymbol = "$";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup_date);
    }

    @Override
    protected void onResume() {
        super.onResume();
        setContentView(R.layout.activity_setup_date);

        loadData();

        findDateSetupScreenElements();

        addDateSetupListeners();

        calculateAndDisplayData();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_settings, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_dashboard) {

            Intent intent = new Intent(getApplicationContext(), DashboardActivity.class);
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

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(getApplicationContext(), DashboardActivity.class);
        startActivity(intent);
    }

    private void addDateSetupListeners() {

        continueButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                if (event.getAction() == KeyEvent.ACTION_DOWN) {

                    Date date = null;
                    try {
                        date = Utils.getDateFromDatePicker(datePicker);
                    } catch (Exception e) {
                    }
                    if (date != null) {
                        payDate = date.getTime();
                    }

                    setContentView(R.layout.activity_setup_total);
                    findTotalSetupScreenElements();
                    addTotalSetupListeners();
                    calculateAndDisplayData();

                }

                return false;
            }
        });
    }

    private void addTotalSetupListeners() {

        saveButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {

                    try {
                        totalMoneyToSpend = Float.parseFloat(budget.getText().toString());
                    } catch (Exception ex) {
                    }

                    AlertDialog.Builder builder = new AlertDialog.Builder(SetupActivity.this);
                    try {
                        builder.setMessage("Manage spending of " + currencySymbol + Utils.formatMonetaryValue(totalMoneyToSpend)
                                + " for " + Utils.getDaysDifference(setupDate, payDate) + " days?"
                                + "\n\nA single block will be worth " + currencySymbol + staticBlockPrice + "\n");

                        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {

                                dialog.dismiss();

                                saveData();

                                Intent intent = new Intent(getApplicationContext(), DashboardActivity.class);
                                startActivity(intent);

                            }
                        });
                        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.dismiss();
                            }
                        });
                        AlertDialog alert = builder.create();
                        alert.show();

                    } catch (Exception e) {
                    }

                }

                return false;
            }
        });

        budget.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View view, int keyCode, KeyEvent event) {
                try {
                    totalMoneyToSpend = Float.parseFloat(budget.getText().toString());
                } catch (Exception ex) {
                }
                calculateAndDisplayData();
                return false;
            }
        });

        datePicker.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View view, int keyCode, KeyEvent event) {
                try {
                    totalMoneyToSpend = Float.parseFloat(budget.getText().toString());
                } catch (Exception ex) {
                }
                calculateAndDisplayData();
                return false;
            }
        });
    }

    private void findDateSetupScreenElements() {

        currencySymbol = Utils.getCurrencySymbol(this);

        datePickerLayout = (LinearLayout) findViewById(R.id.datePickerLayout);
        dateContinueLayout = (LinearLayout) findViewById(R.id.dateContinueLayout);

        datePickerLayout.setElevation(DashboardActivity.ELEVATION_HEIGHT);
        dateContinueLayout.setElevation(DashboardActivity.ELEVATION_HEIGHT);

        datePicker = (DatePicker) findViewById(R.id.datePicker);
        datePicker.setMinDate(Calendar.getInstance().getTimeInMillis() - 1000);

        if(payDate == 0L) {
            datePicker.updateDate(Calendar.getInstance().get(Calendar.YEAR),
                    Calendar.getInstance().get(Calendar.MONTH),
                    Calendar.getInstance().getActualMaximum(Calendar.DAY_OF_MONTH));
        } else {
            datePicker.updateDate(Integer.valueOf(new SimpleDateFormat("yyyy").format(new Date(payDate))),
                    Integer.valueOf(new SimpleDateFormat("MM").format(new Date(payDate)))-1,
                    Integer.valueOf(new SimpleDateFormat("dd").format(new Date(payDate))));
        }

        continueButton = (Button) findViewById(R.id.continueButton);
    }

    private void findTotalSetupScreenElements() {

        enterAmountLayout = (LinearLayout) findViewById(R.id.enterAmountLayout);
        blockWorthLayout = (LinearLayout) findViewById(R.id.blockWorthLayout);

        notificationLayout = (GridLayout) findViewById(R.id.notificationLayout);
        morningNotificationLayout = (LinearLayout) findViewById(R.id.morningNotification);
        middayNotification = (LinearLayout) findViewById(R.id.middayNotification);
        eveningNotificationLayout = (LinearLayout) findViewById(R.id.eveningNotification);
        saveLayout = (LinearLayout) findViewById(R.id.saveLayout);

        enterAmountLayout.setElevation(DashboardActivity.ELEVATION_HEIGHT);
        blockWorthLayout.setElevation(DashboardActivity.ELEVATION_HEIGHT);

        notificationLayout.setElevation(DashboardActivity.ELEVATION_HEIGHT);
        morningNotificationLayout.setElevation(DashboardActivity.ELEVATION_HEIGHT);
        middayNotification.setElevation(DashboardActivity.ELEVATION_HEIGHT);
        eveningNotificationLayout.setElevation(DashboardActivity.ELEVATION_HEIGHT);
        saveLayout.setElevation(DashboardActivity.ELEVATION_HEIGHT);

        budget = (EditText) findViewById(R.id.budget);
        budget.setText(Utils.formatMonetaryValue(currentMoneyToSpend));
        saveButton = (Button) findViewById(R.id.saveButton);
        blockValueText = (TextView) findViewById(R.id.blockValueText);
        currency1 = (TextView) findViewById(R.id.currency1);
        currency1.setText(currencySymbol);

        morningCheckbox = (CheckBox) findViewById(R.id.morningCheckbox);
        if(morningNotification) {
            morningCheckbox.setChecked(true);
        }
        afternoonCheckbox = (CheckBox) findViewById(R.id.afternoonCheckbox);
        if(afternoonNotification) {
            afternoonCheckbox.setChecked(true);
        }
        eveningCheckbox = (CheckBox) findViewById(R.id.eveningCheckbox);
        if(eveningNotification) {
            eveningCheckbox.setChecked(true);
        }
    }

    private void loadData() {

        SharedPreferences preferences = getSharedPreferences("block_save_data", 0);

        totalMoneyToSpend = preferences.getFloat("total_money_to_spend", 0.0F);
        currentMoneyToSpend = preferences.getFloat("current_money_to_spend", 0.0F);
        payDate = preferences.getLong("next_pay_day", 0L);
        setupDate = preferences.getLong("setup_day", Calendar.getInstance().getTimeInMillis());

        morningNotification = preferences.getBoolean("morning_notification", false);
        afternoonNotification = preferences.getBoolean("afternoon_notification", false);
        eveningNotification = preferences.getBoolean("evening_notification", false);

    }

    private void saveData() {
        setupDate = Calendar.getInstance().getTimeInMillis();
        SharedPreferences settings = getSharedPreferences("block_save_data", 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putFloat("total_money_to_spend", totalMoneyToSpend);
        editor.putFloat("current_money_to_spend", totalMoneyToSpend);
        editor.putLong("next_pay_day", payDate);
        editor.putLong("setup_day", setupDate);
        editor.putBoolean("help_visited", true);
        editor.putLong("block_count", BLOCKS_PER_DAY);
        editor.putLong("block_count_day", setupDate);

        if(morningCheckbox.isChecked()) {
            morningNotification = true;
            editor.putBoolean("morning_notification", true);
        } else {
            morningNotification = false;
            editor.putBoolean("morning_notification", false);
        }
        if(afternoonCheckbox.isChecked()) {
            afternoonNotification = true;
            editor.putBoolean("afternoon_notification", true);
        } else {
            afternoonNotification = false;
            editor.putBoolean("afternoon_notification", false);
        }
        if(eveningCheckbox.isChecked()) {
            eveningNotification = true;
            editor.putBoolean("evening_notification", true);
        } else {
            eveningNotification = false;
            editor.putBoolean("evening_notification", false);
        }

        editor.commit();
    }

    private void calculateAndDisplayData() {
        try {
            payDate = Utils.getDateFromDatePicker(datePicker).getTime();

            int daysDifference = Utils.getDaysDifference(setupDate, payDate);

            staticBlockPrice = Utils.formatMonetaryValue(Utils.getStaticBlockPrice(totalMoneyToSpend, daysDifference));
            if(staticBlockPrice == null || staticBlockPrice.equals("") || staticBlockPrice.equals("-.00")) {
                staticBlockPrice = "0.00";
            }
            blockValueText.setText("A block is worth  " + currencySymbol + staticBlockPrice);
        } catch(Exception ex) {

        }
    }

}
