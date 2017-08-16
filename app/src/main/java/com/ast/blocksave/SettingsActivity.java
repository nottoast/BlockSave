package com.ast.blocksave;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
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

public class SettingsActivity extends AppCompatActivity {

    private GridLayout notificationLayout;
    private LinearLayout morningNotificationLayout;
    private LinearLayout middayNotification;
    private LinearLayout eveningNotificationLayout;
    private LinearLayout saveLayout;

    private Button saveButton;

    private CheckBox morningCheckbox;
    private CheckBox afternoonCheckbox;
    private CheckBox eveningCheckbox;

    private boolean morningNotification;
    private boolean afternoonNotification;
    private boolean eveningNotification;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
    }

    @Override
    protected void onResume() {
        super.onResume();
        setContentView(R.layout.activity_settings);

        loadData();

        findTotalSetupScreenElements();

        addTotalSetupListeners();

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
        } else if (id == R.id.action_setup) {

            Intent intent = new Intent(getApplicationContext(), SetupActivity.class);
            startActivity(intent);

            return true;
        }
        /*
        else if (id == R.id.action_help) {

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
        */
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(getApplicationContext(), DashboardActivity.class);
        startActivity(intent);
    }

    private void addTotalSetupListeners() {

        saveButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                if (event.getAction() == KeyEvent.ACTION_DOWN) {

                    saveNotificationData();

                    Intent intent = new Intent(getApplicationContext(), DashboardActivity.class);
                    startActivity(intent);
                }

                return false;
            }
        });

    }


    private void findTotalSetupScreenElements() {

        notificationLayout = (GridLayout) findViewById(R.id.notificationLayout);
        morningNotificationLayout = (LinearLayout) findViewById(R.id.morningNotification);
        middayNotification = (LinearLayout) findViewById(R.id.middayNotification);
        eveningNotificationLayout = (LinearLayout) findViewById(R.id.eveningNotification);
        saveLayout = (LinearLayout) findViewById(R.id.saveLayout);

        notificationLayout.setElevation(DashboardActivity.ELEVATION_HEIGHT);
        morningNotificationLayout.setElevation(DashboardActivity.ELEVATION_HEIGHT);
        middayNotification.setElevation(DashboardActivity.ELEVATION_HEIGHT);
        eveningNotificationLayout.setElevation(DashboardActivity.ELEVATION_HEIGHT);
        saveLayout.setElevation(DashboardActivity.ELEVATION_HEIGHT);

        saveButton = (Button) findViewById(R.id.saveButton);

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

        morningNotification = preferences.getBoolean("morning_notification", false);
        afternoonNotification = preferences.getBoolean("afternoon_notification", false);
        eveningNotification = preferences.getBoolean("evening_notification", false);

    }

    private void saveNotificationData() {

        SharedPreferences settings = getSharedPreferences("block_save_data", 0);
        SharedPreferences.Editor editor = settings.edit();

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

}
