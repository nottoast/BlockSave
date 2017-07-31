package com.ast.blocksave;

import android.app.AlertDialog;
import android.content.DialogInterface;
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
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class SetupActivity extends AppCompatActivity {

    public static int BLOCKS_PER_DAY = 10;
    private String BLOCK_VALUE_TEMPLATE_TEXT_GBP = "A block is worth  " + Utils.getCurrencySymbol() + " ";

    private float totalMoneyToSpend = 0.0F;
    private float currentMoneyToSpend = 0.0F;

    private long payDate = Calendar.getInstance().getTimeInMillis();
    private long setupDate = Calendar.getInstance().getTimeInMillis();

    private DatePicker datePicker;
    private EditText budget;
    private TextView blockValueText;
    private TextView currency1;
    private Button saveButton;
    private Button continueButton;

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
                        builder.setMessage("Manage spending of "+Utils.getCurrencySymbol() + Utils.formatMonetaryValue(totalMoneyToSpend) + " for " + Utils.getDaysDifference(setupDate, payDate) + " days?");

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

        budget = (EditText) findViewById(R.id.budget);
        budget.setText(Utils.formatMonetaryValue(currentMoneyToSpend));
        saveButton = (Button) findViewById(R.id.saveButton);
        blockValueText = (TextView) findViewById(R.id.blockValueText);
        currency1 = (TextView) findViewById(R.id.currency1);
        currency1.setText(Utils.getCurrencySymbol());
    }

    private void loadData() {
        SharedPreferences preferences = getSharedPreferences("block_save_data", 0);
        totalMoneyToSpend = preferences.getFloat("total_money_to_spend", 0.0F);
        currentMoneyToSpend = preferences.getFloat("current_money_to_spend", 0.0F);
        payDate = preferences.getLong("next_pay_day", 0L);
        setupDate = preferences.getLong("setup_day", 0L);
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
        editor.commit();
    }

    private void calculateAndDisplayData() {
        //SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        try {
            payDate = Utils.getDateFromDatePicker(datePicker).getTime();
            DecimalFormat formatter = new DecimalFormat("##.00");
            int daysDifference = Utils.getDaysDifference(setupDate, payDate);
            String staticBlockPrice = formatter.format(Utils.getStaticBlockPrice(totalMoneyToSpend, daysDifference));
            if(staticBlockPrice == null || staticBlockPrice.equals("") || staticBlockPrice.equals("-.00")) {
                staticBlockPrice = "0.00";
            }
            if(Double.valueOf(staticBlockPrice) < 1.0) {
                staticBlockPrice = "0" + staticBlockPrice;
            }
            blockValueText.setText(BLOCK_VALUE_TEMPLATE_TEXT_GBP + staticBlockPrice);
        } catch(Exception ex) {

        }
    }
}
