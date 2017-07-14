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
import android.widget.EditText;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class SetupActivity extends AppCompatActivity {

    public static int BLOCKS_PER_DAY = 10;
    private String BLOCK_VALUE_TEMPLATE_TEXT_GBP = "A block is worth  " + Utils.getCurrencySymbol() + " ";

    private float totalMoneyToSpend = 0.0F;
    private long nextPayDay = Calendar.getInstance().getTimeInMillis();
    private long setupDate = Calendar.getInstance().getTimeInMillis();

    private EditText payDay;
    private EditText budget;
    private TextView blockValueText;
    private TextView currency1;
    private Button saveButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);
    }

    @Override
    protected void onResume() {
        super.onResume();
        setContentView(R.layout.activity_setup);
        loadData();
        findSetupScreenElements();
        calculateAndDisplayData();
        addListeners();
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

    private void addListeners() {
        saveButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                    Date date = null;
                    try {
                        date = sdf.parse(payDay.getText().toString());
                    } catch (ParseException e) {
                    }
                    if (date != null) {
                        nextPayDay = date.getTime();
                    }
                    try {
                        totalMoneyToSpend = Float.parseFloat(budget.getText().toString());
                    } catch (Exception ex) {
                    }
                    saveData();
                    calculateAndDisplayData();

                    Intent intent = new Intent(getApplicationContext(), DashboardActivity.class);
                    startActivity(intent);
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

        payDay.setOnKeyListener(new View.OnKeyListener() {
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

    private void findSetupScreenElements() {
        payDay = (EditText) findViewById(R.id.payDay);
        payDay.setText(new SimpleDateFormat("dd/MM/yyyy").format(new Date(nextPayDay)));
        budget = (EditText) findViewById(R.id.budget);
        budget.setText("" + totalMoneyToSpend);
        saveButton = (Button) findViewById(R.id.saveButton);
        blockValueText = (TextView) findViewById(R.id.blockValueText);
        currency1 = (TextView) findViewById(R.id.currency1);
        currency1.setText(Utils.getCurrencySymbol());
    }

    private void loadData() {
        SharedPreferences preferences = getSharedPreferences("block_save_data", 0);
        totalMoneyToSpend = preferences.getFloat("total_money_to_spend", 0.0F);
        nextPayDay = preferences.getLong("next_pay_day", 0L);
        setupDate = preferences.getLong("setup_day", 0L);
    }

    private void saveData() {
        setupDate = Calendar.getInstance().getTimeInMillis();
        SharedPreferences settings = getSharedPreferences("block_save_data", 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putFloat("total_money_to_spend", totalMoneyToSpend);
        editor.putFloat("current_money_to_spend", totalMoneyToSpend);
        editor.putLong("next_pay_day", nextPayDay);
        editor.putLong("setup_day", setupDate);
        editor.putBoolean("help_visited", true);
        editor.commit();
    }

    private void calculateAndDisplayData() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        try {
            nextPayDay = sdf.parse(payDay.getText().toString()).getTime();
            DecimalFormat formatter = new DecimalFormat("##.00");
            int daysDifference = Utils.getDaysDifference(setupDate, nextPayDay);
            String staticBlockPrice = formatter.format(Utils.getStaticBlockPrice(totalMoneyToSpend, daysDifference));
            if(staticBlockPrice == null || staticBlockPrice.equals("") || staticBlockPrice.equals("-.00")) {
                staticBlockPrice = "0.00";
            }
            blockValueText.setText(BLOCK_VALUE_TEMPLATE_TEXT_GBP + staticBlockPrice);
        } catch(Exception ex) {

        }
    }
}
