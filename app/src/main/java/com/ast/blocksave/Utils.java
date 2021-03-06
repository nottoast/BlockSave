package com.ast.blocksave;

import android.content.Context;
import android.content.res.Resources;
import android.telephony.TelephonyManager;
import android.util.TypedValue;
import android.widget.DatePicker;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Currency;
import java.util.Date;
import java.util.Locale;

import static com.ast.blocksave.SetupActivity.BLOCKS_PER_DAY;

public class Utils {

    private static long roundDate(long dateToRound) {

        SimpleDateFormat sdf = new SimpleDateFormat("dd/M/yyyy");
        String date = sdf.format(new Date(dateToRound));
        long dateToReturn = 0L;
        try {
            dateToReturn = sdf.parse(date).getTime();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return dateToReturn;
    }

    public static int getNumberOfDaysUntilPayDay(long nextPayDay) {

        double differenceDouble = roundDate(nextPayDay) - roundDate(Calendar.getInstance().getTimeInMillis());
        Double daysDouble = differenceDouble / 86400000;
        daysDouble = Math.ceil(daysDouble);
        int days = daysDouble.intValue();

        return days + 1;
    }

    public static int getDayNumber(long setupDay) {

        double differenceDouble = roundDate(Calendar.getInstance().getTimeInMillis()) - roundDate(setupDay);
        Double daysDouble = differenceDouble / 86400000;
        daysDouble = Math.ceil(daysDouble);
        int days = daysDouble.intValue();

        return days + 1;
    }

    public static int getDaysDifference(long setupDay, long nextPayDay) {

        double differenceDouble = roundDate(nextPayDay) - roundDate(setupDay);
        Double daysDouble = differenceDouble / 86400000;
        daysDouble = Math.ceil(daysDouble);
        int days = daysDouble.intValue();

        return days + 1;
    }

    public static float getStaticBlockPrice(double totalMoneyToSpend, int daysDifference) {

        double staticBlockPrice = (totalMoneyToSpend / daysDifference) / BLOCKS_PER_DAY;

        return Float.parseFloat(staticBlockPrice+"");
    }

    public static double getCurrentBlocksAvailable(float currentMoneyToSpend, float staticBlockPrice) {

        double currentBlocksAvailable = currentMoneyToSpend / staticBlockPrice;
        double currentBlocksAvailableRounded = Math.ceil((currentBlocksAvailable));

        return currentBlocksAvailableRounded;
    }

    public static long getBlocksToDisplayRounded(float currentMoneyToSpend, float staticBlockPrice, long todaysBlockBudget, long todaysTotalBlocks) {

        return Math.round(getBlocksToDisplay(currentMoneyToSpend, staticBlockPrice, todaysBlockBudget, todaysTotalBlocks));
    }

    public static double getBlocksToDisplay(float currentMoneyToSpend, float staticBlockPrice, long todaysBlockBudget, long todaysTotalBlocks) {

        double currentBlocksAvailableRounded = getCurrentBlocksAvailable(currentMoneyToSpend, staticBlockPrice);
        double blockDifference = todaysTotalBlocks - currentBlocksAvailableRounded;

        return todaysBlockBudget - blockDifference;
    }

    public static double getDoubleBlockBudgetFromTomorrow(long setupDay, long payDate, double currentMoneyToSpend, double staticBlockPrice) {

        int daysDifference = getDaysDifference(setupDay, payDate);
        int daysPassed = getDayNumber(setupDay);
        double currentBlocksAvailable = currentMoneyToSpend / staticBlockPrice;
        double daysToDivide = (daysDifference - (daysPassed));
        if(daysToDivide == 0) {
            return 0;
        }
        double tomorrowsBlocks = currentBlocksAvailable / daysToDivide;

        return tomorrowsBlocks;
    }

    public static long getBlockBudgetFromTomorrow(long setupDay, long payDate, double currentMoneyToSpend, double staticBlockPrice) {

        long tomorrowsBlocksRounded = ((Double)Math.floor(getDoubleBlockBudgetFromTomorrow(setupDay, payDate, currentMoneyToSpend, staticBlockPrice))).intValue();

        return tomorrowsBlocksRounded;
    }

    public static String getCurrencySymbol(Context context) {

        Currency currency = null;
        Locale locale = null;

        try {
            TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(context.TELEPHONY_SERVICE);
            String countryCode = telephonyManager.getNetworkCountryIso();
            locale = new Locale("en",countryCode.toUpperCase());
            if (locale != null) {
                currency = Currency.getInstance(locale);
                return currency.getSymbol();
            }
        } catch(Exception ex1) {
            try {
                locale = context.getResources().getConfiguration().locale;
                if (locale != null) {
                    currency = Currency.getInstance(locale);
                    return currency.getSymbol();
                }
            } catch(Exception ex2) {
                try {
                    currency = Currency.getInstance(Locale.getDefault());
                    return currency.getSymbol();
                } catch(Exception ex3) {
                    return "$";
                }
            }
        }

        return "$";
    }

    public static long getUnderSpendValue(long setupDay, long payDate, double currentMoneyToSpend, double staticBlockPrice) {

        double blockBudgetTomorrow = getDoubleBlockBudgetFromTomorrow(setupDay, payDate, currentMoneyToSpend, staticBlockPrice);
        double blockValueThreshold = Math.ceil(blockBudgetTomorrow + 0.00000001);
        double blockValueDifference = blockValueThreshold - blockBudgetTomorrow;
        int daysDifference = getDaysDifference(setupDay, payDate);
        int daysPassed = getDayNumber(setupDay);
        double blockValueDifferenceTotal = blockValueDifference * (daysDifference - daysPassed);
        Double blockSpendValue = Math.ceil(blockValueDifferenceTotal);

        return blockSpendValue.intValue();
    }

    public static int getOverSpendValue(long setupDay, long payDate, double currentMoneyToSpend, double staticBlockPrice) {

        double blockBudgetTomorrow = getDoubleBlockBudgetFromTomorrow(setupDay, payDate, currentMoneyToSpend, staticBlockPrice);
        double blockValueThreshold = Math.floor(blockBudgetTomorrow) - 0.00000001;
        double blockValueDifference = blockBudgetTomorrow - blockValueThreshold;
        int daysDifference = getDaysDifference(setupDay, payDate);
        int daysPassed = getDayNumber(setupDay);
        double blockValueDifferenceTotal = blockValueDifference * (daysDifference - daysPassed);
        Double blockSpendValue = Math.floor(blockValueDifferenceTotal) + 1;

        return blockSpendValue.intValue();
    }

    public static long getBlocksToDeduct(float currentMoneyToSpend, Double purchaseAmount, float staticBlockPrice, long payDate, long todaysBlockBudget, long todaysCurrentBlocks) {

        double currentDisplayedBlocks = getBlocksToDisplay(currentMoneyToSpend, staticBlockPrice, todaysBlockBudget, todaysCurrentBlocks);
        double newDisplayBlocks = getBlocksToDisplay((float)(currentMoneyToSpend - purchaseAmount), staticBlockPrice, todaysBlockBudget, todaysCurrentBlocks);

        long blocksToDeduct = Math.round(currentDisplayedBlocks - newDisplayBlocks);

        return blocksToDeduct;
    }

    public static java.util.Date getDateFromDatePicker(DatePicker datePicker) throws Exception {

        int day = datePicker.getDayOfMonth();
        int month = datePicker.getMonth();
        int year = datePicker.getYear();

        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, day);

        return calendar.getTime();
    }

    public static boolean isTheSameDay(long blockCountDay, long currentTime) {
        if(roundDate(blockCountDay) == roundDate(currentTime)) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean isTwoDaysOrMoreBefore(long blockCountDay, long currentTime) {

        long currentTimeToCompare = roundDate(currentTime);
        long blockCountDayToCompare = roundDate(blockCountDay);

        long dateDifference = currentTimeToCompare - blockCountDayToCompare;

        if(dateDifference >= 172800000) {
            return true;
        } else {
            return false;
        }
    }

    public static String formatMonetaryValue(String value) {

        if(!value.isEmpty()) {
            try {
                if (value.contains(".")) {
                    String[] valueSplit = value.split("\\.");
                    if (valueSplit[valueSplit.length - 1].length() == 1) {
                        value = value + "0";
                    }
                } else if (value.contains(",")) {
                    String[] valueSplit = value.split(",");
                    if (valueSplit[valueSplit.length - 1].length() == 1) {
                        value = value + "0";
                    }
                }
            } catch(Exception ex) {
            }
        }

        return value;
    }

    public static String formatMonetaryValue(float floatValue) {

        DecimalFormat formatter = new DecimalFormat("0.00");
        String value = formatter.format(floatValue);

        return formatMonetaryValue(value);
    }

    public static int convertDpToPixels(float dp, Context context){
        Resources resources = context.getResources();
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dp,
                resources.getDisplayMetrics()
        );
    }


}
