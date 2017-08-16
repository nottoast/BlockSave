package com.ast.blocksave;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import java.util.Calendar;

public class HelpActivity extends AppCompatActivity {

    private LinearLayout helpTextLayout;
    private LinearLayout helpTextButtonLayout;
    private Button continueButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);
    }

    @Override
    protected void onResume() {
        super.onResume();
        setContentView(R.layout.activity_help);

        findHelpScreenElements();

        loadData();
        addListeners();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_help, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        /*
        if (id == R.id.action_dashboard) {

            saveData();

            Intent intent = new Intent(getApplicationContext(), DashboardActivity.class);
            startActivity(intent);
            return true;
        } else
        */
        if (id == R.id.action_setup) {

            saveData();

            Intent intent = new Intent(getApplicationContext(), SetupActivity.class);
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
        saveData();
        Intent intent = new Intent(getApplicationContext(), DashboardActivity.class);
        startActivity(intent);
    }

    private void findHelpScreenElements() {
        helpTextLayout = (LinearLayout) findViewById(R.id.helpTextLayout);
        helpTextButtonLayout = (LinearLayout) findViewById(R.id.helpTextButtonLayout);

        helpTextLayout.setElevation(DashboardActivity.ELEVATION_HEIGHT);
        helpTextButtonLayout.setElevation(DashboardActivity.ELEVATION_HEIGHT);

        continueButton = (Button) findViewById(R.id.continueButton1);
    }

    private void saveData() {
        SharedPreferences settings = getSharedPreferences("block_save_data", 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean("help_visited", true);
        editor.commit();
    }

    private void addListeners() {
        continueButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {

                    SharedPreferences settings = getSharedPreferences("block_save_data", 0);
                    SharedPreferences.Editor editor = settings.edit();
                    editor.putBoolean("help_visited", true);
                    editor.commit();

                    Intent intent = new Intent(getApplicationContext(), SetupActivity.class);
                    startActivity(intent);
                }
                return false;
            }
        });
    }

    private void loadData() {
        SharedPreferences preferences = getSharedPreferences("block_save_data", 0);
        boolean helpVisited = preferences.getBoolean("help_visited", false);
        if(helpVisited) {
            Intent intent = new Intent(getApplicationContext(), SetupActivity.class);
            startActivity(intent);
        }
    }
}
