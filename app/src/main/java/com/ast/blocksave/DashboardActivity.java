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
import android.widget.TextView;

import java.text.DecimalFormat;

public class DashboardActivity extends AppCompatActivity {

    private String AMOUNT_SPENT_TEMPLATE_TEXT = "Enter amount spent:   " + Utils.getCurrencySymbol() + " ";

    private double totalMoneyToSpend = 0.0;
    private double currentMoneyToSpend = 0.0;
    private long nextPayDay = 0L;
    private long setupDay = 0L;

    private LinearLayout blockDisplayLayout;
    private TextView blocksToSpendToday;
    private TextView purchaseAmount;
    private TextView calculatedBlocks;
    private Button purchaseButton;
    private TextView underSpendOutput;
    private TextView overSpendOutput;
    private TextView tomorrowsBudgetOutput;
    private TextView amountSpentText;

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
            if (Utils.getDaysDifference(setupDay, nextPayDay) < 1 || totalMoneyToSpend == 0.0) {
                Intent intent = new Intent(getApplicationContext(), SetupActivity.class);
                startActivity(intent);
            }
            findDashboardScreenElements();
            calculateAndDisplayData();
            addListeners();
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

    private void addListeners() {
        purchaseButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    currentMoneyToSpend = currentMoneyToSpend - Double.valueOf(purchaseAmount.getText().toString());
                    saveData();
                    //purchaseAmount.setText("0.00");
                    purchaseAmount.setText("");
                    calculateAndDisplayData();
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
        totalMoneyToSpend = Double.valueOf(preferences.getString("total_money_to_spend", "0"));
        currentMoneyToSpend = Double.valueOf(preferences.getString("current_money_to_spend", "0"));
        nextPayDay = preferences.getLong("next_pay_day", 0L);
        setupDay = preferences.getLong("setup_day", 0L);
        return preferences.getBoolean("help_visited", false);
    }

    private void saveData() {
        SharedPreferences settings = getSharedPreferences("block_save_data", 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("total_money_to_spend", totalMoneyToSpend + "");
        editor.putString("current_money_to_spend", currentMoneyToSpend + "");
        editor.putLong("next_pay_day", nextPayDay);
        editor.putBoolean("help_visited", true);
        editor.commit();
    }

    private void findDashboardScreenElements() {
        blockDisplayLayout = (LinearLayout) findViewById(R.id.blockDisplayLayout);
        blocksToSpendToday = (TextView) findViewById(R.id.blocksToSpendToday);
        purchaseAmount = (TextView) findViewById(R.id.purchaseAmount);
        purchaseButton = (Button) findViewById(R.id.purchaseButton);
        calculatedBlocks = (TextView) findViewById(R.id.calculatedBlocks);
        underSpendOutput = (TextView) findViewById(R.id.underSpendOutput);
        overSpendOutput = (TextView) findViewById(R.id.overSpendOutput);
        tomorrowsBudgetOutput = (TextView) findViewById(R.id.tomorrowsBudgetOutput);
        amountSpentText = (TextView) findViewById(R.id.amountSpentText);
        amountSpentText.setText(AMOUNT_SPENT_TEMPLATE_TEXT);
    }

    private void calculateAndDisplayData() {

        DecimalFormat formatter = new DecimalFormat("##.00");
        int numberOfDaysUntilPayDay = Utils.getNumberOfDaysUntilPayDay(nextPayDay);
        double staticBlockPrice = Double.valueOf(formatter.format(Utils.getStaticBlockPrice(totalMoneyToSpend, numberOfDaysUntilPayDay)));
        if (!purchaseAmount.getText().toString().isEmpty()) {
            Double blocksToDeduct = Double.valueOf(purchaseAmount.getText().toString()) / staticBlockPrice;
            Double blocksToDeductCeil = Math.ceil(blocksToDeduct);
            calculatedBlocks.setText(blocksToDeductCeil.intValue() + "");
        } else {
            calculatedBlocks.setText("0");
        }

        int blocksToDisplay = Utils.getBlocksToDisplay(totalMoneyToSpend, currentMoneyToSpend, setupDay, staticBlockPrice);
        if (blocksToDisplay < 0) {
            blocksToDisplay = 0;
        }
        blocksToSpendToday.setText(blocksToDisplay + "");

        int underSpend = Utils.getUnderSpendValue(setupDay, nextPayDay, totalMoneyToSpend, currentMoneyToSpend);
        underSpendOutput.setText(underSpend + " blocks");
        int overSpend = Utils.getOverSpendValue(setupDay, nextPayDay, totalMoneyToSpend, currentMoneyToSpend);
        overSpendOutput.setText(overSpend + " blocks");
        int tomorrowsBudget = Utils.getTomorrowBlocksToDisplay(setupDay, nextPayDay, currentMoneyToSpend, staticBlockPrice);
        tomorrowsBudgetOutput.setText(tomorrowsBudget + " blocks");

    }


}
