package com.ast.blocksave;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
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
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.DecimalFormat;

public class DashboardActivity extends AppCompatActivity {

    private String AMOUNT_SPENT_TEMPLATE_TEXT = "Enter amount spent:   " + Utils.getCurrencySymbol() + " ";
    private String TODAYS_BLOCKS_POSITIVE = "Blocks left to spend today:";
    private String TODAYS_BLOCKS_NEGATIVE = "Blocks you have over spent:";

    private float totalMoneyToSpend = 0.0F;
    private float currentMoneyToSpend = 0.0F;
    private long nextPayDay = 0L;
    private long setupDay = 0L;

    private LinearLayout blockDisplayLayoutTop;
    private LinearLayout blockDisplayLayoutBottom;
    private TextView blocksToSpendToday;
    private TextView purchaseAmount;
    private TextView calculatedBlocks;
    private Button purchaseButton;
    private TextView underSpendOutput;
    private TextView overSpendOutput;
    private TextView tomorrowsBudgetOutput;
    private TextView amountSpentText;
    private TextView blocksToSpendText;

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
                switchToSettingsScreen();
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

    private void switchToSettingsScreen() {
        Intent intent = new Intent(getApplicationContext(), SetupActivity.class);
        startActivity(intent);
    }

    private void addListeners() {
        purchaseButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {

                    if(!purchaseAmount.getText().toString().isEmpty()) {
                        currentMoneyToSpend = currentMoneyToSpend - Float.valueOf(purchaseAmount.getText().toString());
                        saveData();
                        purchaseAmount.setText("");
                        calculateAndDisplayData();
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
        totalMoneyToSpend = preferences.getFloat("total_money_to_spend", 0.0F);
        currentMoneyToSpend = preferences.getFloat("current_money_to_spend", 0.0F);
        nextPayDay = preferences.getLong("next_pay_day", 0L);
        setupDay = preferences.getLong("setup_day", 0L);
        return preferences.getBoolean("help_visited", false);
    }

    private void saveData() {
        SharedPreferences settings = getSharedPreferences("block_save_data", 0);
        SharedPreferences.Editor editor = settings.edit();
        //editor.putFloat("total_money_to_spend", totalMoneyToSpend);
        editor.putFloat("current_money_to_spend", currentMoneyToSpend);
        //editor.putLong("next_pay_day", nextPayDay);
        editor.putBoolean("help_visited", true);
        editor.commit();
    }

    private void findDashboardScreenElements() {
        blockDisplayLayoutTop = (LinearLayout) findViewById(R.id.blockDisplayLayoutTop);
        blockDisplayLayoutBottom = (LinearLayout) findViewById(R.id.blockDisplayLayoutBottom);
        blocksToSpendToday = (TextView) findViewById(R.id.blocksToSpendToday);
        purchaseAmount = (TextView) findViewById(R.id.purchaseAmount);
        purchaseButton = (Button) findViewById(R.id.purchaseButton);
        calculatedBlocks = (TextView) findViewById(R.id.calculatedBlocks);
        underSpendOutput = (TextView) findViewById(R.id.underSpendOutput);
        overSpendOutput = (TextView) findViewById(R.id.overSpendOutput);
        tomorrowsBudgetOutput = (TextView) findViewById(R.id.tomorrowsBudgetOutput);
        amountSpentText = (TextView) findViewById(R.id.amountSpentText);
        amountSpentText.setText(AMOUNT_SPENT_TEMPLATE_TEXT);
        blocksToSpendText = (TextView) findViewById(R.id.blocksToSpendText);
    }

    private void displayBlocks(long numberOfBlocksToDisplay, long numberOfBlocksToHighlight) {

        //numberOfBlocksToDisplay = 22;
        //numberOfBlocksToHighlight = 0;

        blockDisplayLayoutTop.removeAllViews();
        blockDisplayLayoutBottom.removeAllViews();

        if(numberOfBlocksToDisplay > 13) {
            numberOfBlocksToDisplay = 14;
        }

        for (int i = 0; i < numberOfBlocksToDisplay; i++) {

            TextView textView = null;

            if ( (i % 2) == 0) {
                textView = new TextView(blockDisplayLayoutTop.getContext());
            } else {
                textView = new TextView(blockDisplayLayoutBottom.getContext());
            }

            textView.setText("  +  ");
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            params.setMargins(20, 20, 0, 0);
            Drawable drawable = null;
            textView.setPadding(13, 21, 13, 21);
            textView.setTextSize(17);

            if(i >= numberOfBlocksToDisplay - numberOfBlocksToHighlight) {
                drawable = ContextCompat.getDrawable(getApplicationContext(), R.drawable.block_style_2);
                if(i == 13 && numberOfBlocksToDisplay == 14) {
                    textView.setTextColor(Color.BLACK);
                } else {
                    textView.setTextColor(ContextCompat.getColor(this, R.color.block_colour_2));
                }
            } else {
                drawable = ContextCompat.getDrawable(getApplicationContext(), R.drawable.block_style_1);
                if(i == 13 && numberOfBlocksToDisplay == 14) {
                    textView.setTextColor(Color.BLACK);
                } else {
                    textView.setTextColor(ContextCompat.getColor(this, R.color.block_colour));
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

    private void calculateAndDisplayData() {


        int numberOfDaysUntilPayDay = Utils.getNumberOfDaysUntilPayDay(nextPayDay);
        double unformattedStaticBlockPrice = Utils.getStaticBlockPrice(totalMoneyToSpend, numberOfDaysUntilPayDay);

        DecimalFormat formatter = new DecimalFormat("##.00");
        double staticBlockPrice = Double.valueOf(formatter.format(unformattedStaticBlockPrice));

        Long blocksToDeduct = 0L;
        if (!purchaseAmount.getText().toString().isEmpty()) {
            blocksToDeduct = Utils.getBlocksToDeduct(totalMoneyToSpend, currentMoneyToSpend, Double.valueOf(purchaseAmount.getText().toString()), staticBlockPrice, setupDay, nextPayDay);
            calculatedBlocks.setText(blocksToDeduct + "");
        } else {
            calculatedBlocks.setText("0");
        }

        long blocksToDisplay = Utils.getBlocksToDisplay(totalMoneyToSpend, currentMoneyToSpend, setupDay, nextPayDay, staticBlockPrice);
        long blocksToShow = blocksToDisplay;
        if (blocksToDisplay < 0) {
            blocksToDisplay = blocksToDisplay * -1;
            blocksToSpendText.setText(TODAYS_BLOCKS_NEGATIVE);
            blocksToSpendToday.setTextColor(ContextCompat.getColor(this, android.R.color.holo_red_dark));
            blocksToSpendToday.setText(blocksToDisplay + "");
        } else if (blocksToDisplay > 0) {
            blocksToSpendText.setText(TODAYS_BLOCKS_POSITIVE);
            blocksToSpendToday.setTextColor(ContextCompat.getColor(this, android.R.color.holo_green_dark));
            blocksToSpendToday.setText(blocksToDisplay + "");
        } else {
            blocksToSpendText.setText(TODAYS_BLOCKS_POSITIVE);
            blocksToSpendToday.setTextColor(ContextCompat.getColor(this, android.R.color.darker_gray));
            blocksToSpendToday.setText("None");
        }

        int underSpend = Utils.getUnderSpendValue(setupDay, nextPayDay, totalMoneyToSpend, currentMoneyToSpend);
        if(underSpend < 10) {
            if(underSpend == 1) {
                underSpendOutput.setText(" " + underSpend + " block");
            } else {
                underSpendOutput.setText(" " + underSpend + " blocks");
            }
        } else {
            underSpendOutput.setText(underSpend + " blocks");
        }

        int overSpend = Utils.getOverSpendValue(setupDay, nextPayDay, totalMoneyToSpend, currentMoneyToSpend);
        if(overSpend < 10) {
            if(overSpend == 1) {
                overSpendOutput.setText(" " + overSpend + " block");
            } else {
                overSpendOutput.setText(" " + overSpend + " blocks");
            }
        } else {
            overSpendOutput.setText(overSpend + " blocks");
        }

        long tomorrowsBudget = Utils.getTomorrowBlocksToDisplay(setupDay, nextPayDay, totalMoneyToSpend, currentMoneyToSpend, staticBlockPrice);
        if(tomorrowsBudget < 10) {
            if(overSpend == 1) {
                tomorrowsBudgetOutput.setText(" " + tomorrowsBudget + " block");
            } else {
                tomorrowsBudgetOutput.setText(" " + tomorrowsBudget + " blocks");
            }
        } else {
            tomorrowsBudgetOutput.setText(tomorrowsBudget + " blocks");
        }

        if(tomorrowsBudget < 1) {
            switchToSettingsScreen();
        }

        displayBlocks(blocksToShow, blocksToDeduct);

    }


}
