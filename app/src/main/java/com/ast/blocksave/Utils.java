package com.ast.blocksave;

import android.app.Application;
import android.content.Context;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Currency;
import java.util.Date;
import java.util.Locale;

import static com.ast.blocksave.SetupActivity.BLOCKS_PER_DAY;

public class Utils {

    public static void main(String[] args) {

        Float totalMoney = 150.0F;
        Float currentMoney = 145.0F;

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        Date payDate = null;
        Date setupDate = null;
        try {
            payDate = sdf.parse("31/07/2017");
            setupDate = sdf.parse("18/07/2017");
        } catch(Exception ex) {
        }

        int daysDifference = getDaysDifference(setupDate.getTime(), payDate.getTime());
        System.out.println("Total number of days: " + daysDifference);

        System.out.println("Number of days until pay day: " + getNumberOfDaysUntilPayDay(payDate.getTime()));

        double staticBlockPrice = getStaticBlockPrice(totalMoney, daysDifference);
        System.out.println("Static block price: "+staticBlockPrice);

        System.out.println(getBlocksToDisplay(totalMoney, currentMoney, setupDate.getTime(), payDate.getTime(), staticBlockPrice));
        System.out.println(getTomorrowBlocksToDisplay(setupDate.getTime(), payDate.getTime(), totalMoney, currentMoney, staticBlockPrice));
        System.out.println("------------------------------------------------------");

        System.out.println(getDayNumber(setupDate.getTime()));
        System.out.println("------------------------------------------------------");
        System.out.println(getUnderSpendValue(setupDate.getTime(), payDate.getTime(), totalMoney, currentMoney));
        System.out.println(getOverSpendValue(setupDate.getTime(), payDate.getTime(), totalMoney, currentMoney));
        System.out.println("------------------------------------------------------");
        System.out.println("Get blocks to deduct: " + getBlocksToDeduct(totalMoney, currentMoney, 5.00, staticBlockPrice, setupDate.getTime(), payDate.getTime()));

    }

    public static int getNumberOfDaysUntilPayDay(long nextPayDay) {
        double differenceDouble = nextPayDay - Calendar.getInstance().getTimeInMillis();
        Double daysDouble = differenceDouble / 86400000;
        daysDouble = Math.ceil(daysDouble);
        int days = daysDouble.intValue();
        return days + 1;
    }

    public static int getDayNumber(long setupDay) {
        double differenceDouble = Calendar.getInstance().getTimeInMillis() - setupDay;
        Double daysDouble = differenceDouble / 86400000;
        daysDouble = Math.ceil(daysDouble);
        int days = daysDouble.intValue();
        return days - 1;
    }

    public static int getDaysDifference(long setupDay, long nextPayDay) {
        double differenceDouble = nextPayDay - setupDay;
        Double daysDouble = differenceDouble / 86400000;
        daysDouble = Math.ceil(daysDouble);
        int days = daysDouble.intValue();
        return days + 1;
    }

    public static double getStaticBlockPrice(double totalMoneyToSpend, int daysDifference) {
        double staticBlockPrice = (totalMoneyToSpend / daysDifference) / BLOCKS_PER_DAY;
        return staticBlockPrice;
    }

    public static long getBlocksToDisplay(double totalMoneyToSpend, double currentMoneyToSpend, long setupDay, long payDate, double staticBlockPrice) {
        int daysPassed = getDayNumber(setupDay);
        double totalBlocksAvailableInteger = ((totalMoneyToSpend) / staticBlockPrice) - (daysPassed * BLOCKS_PER_DAY);
        double currentBlocksAvailableInteger = ((currentMoneyToSpend) / staticBlockPrice);

        if(totalBlocksAvailableInteger < currentBlocksAvailableInteger) {
            double spareBlocks = currentBlocksAvailableInteger - totalBlocksAvailableInteger;
            double spareBlocksPerDay = Math.floor(spareBlocks / getNumberOfDaysUntilPayDay(payDate));
            currentBlocksAvailableInteger = totalBlocksAvailableInteger;
            currentBlocksAvailableInteger = currentBlocksAvailableInteger + spareBlocksPerDay;
        }

        long blocksToDisplay = Math.round(BLOCKS_PER_DAY - (totalBlocksAvailableInteger - currentBlocksAvailableInteger));
        return blocksToDisplay;
    }

    public static long getTomorrowBlocksToDisplay(long setupDay, long payDate, double totalMoneyToSpend, double currentMoneyToSpend,  double staticBlockPrice) {
        int daysDifference = getDaysDifference(setupDay, payDate);
        int daysPassed = getDayNumber(setupDay);
        double currentBlocksAvailable = currentMoneyToSpend / staticBlockPrice;
        double currentBlocksAvailableAdjusted = currentBlocksAvailable - (BLOCKS_PER_DAY * daysPassed);
        long tomorrowsBlocks = Math.round((currentBlocksAvailableAdjusted) / (daysDifference - (daysPassed + 1)));
        return tomorrowsBlocks;
    }

    public static String getCurrencySymbol() {
        Currency currency = Currency.getInstance(Locale.getDefault());
        return currency.getSymbol();
    }

    public static int getUnderSpendValue(long setupDay, long nextPayDate, double totalMoneyToSpend, double currentMoneyToSpend) {
        int daysDifference = getDaysDifference(setupDay, nextPayDate);
        double staticBlockPrice = getStaticBlockPrice(totalMoneyToSpend, daysDifference);
        int daysPassed = getDayNumber(setupDay);
        double currentBlocksAvailable = currentMoneyToSpend / staticBlockPrice;
        double currentBlocksAvailableAdjusted = currentBlocksAvailable - (BLOCKS_PER_DAY * daysPassed);
        double tomorrowsBlocks = Math.ceil((currentBlocksAvailableAdjusted) / (daysDifference - (daysPassed + 1)));
        int underSpendValue = -1;
        while(true) {
            underSpendValue++;
            double calculatedAdjustment = (currentBlocksAvailableAdjusted + underSpendValue) / (daysDifference - (daysPassed + 1));
            if(calculatedAdjustment - tomorrowsBlocks > 1.0) {
                break;
            }
            if (underSpendValue > 98) {
                break;
            }
        }
        return underSpendValue;
    }

    public static int getOverSpendValue(long setupDay, long nextPayDate, double totalMoneyToSpend, double currentMoneyToSpend) {

        int daysDifference = getDaysDifference(setupDay, nextPayDate);
        double staticBlockPrice = getStaticBlockPrice(totalMoneyToSpend, daysDifference);
        int daysPassed = getDayNumber(setupDay);
        double currentBlocksAvailable = currentMoneyToSpend / staticBlockPrice;
        double currentBlocksAvailableAdjusted = currentBlocksAvailable - (BLOCKS_PER_DAY * daysPassed);
        double tomorrowsBlocks = Math.ceil((currentBlocksAvailableAdjusted) / (daysDifference - (daysPassed + 1)));
        int overSpendValue = -1;
        while(true) {
            overSpendValue++;
            double calculatedAdjustment = (currentBlocksAvailableAdjusted - overSpendValue) / (daysDifference - (daysPassed + 1));
            if(tomorrowsBlocks - calculatedAdjustment > 1.0) {
                break;
            }
            if (overSpendValue > 98) {
                break;
            }
        }
        return overSpendValue;
    }

    public static long getBlocksToDeduct(float totalMoneyToSpend, float currentMoneyToSpend, Double purchaseAmount, Double staticBlockPrice, long setupDay, long payDate) {
        double currentDisplayedBlocks = getBlocksToDisplay(totalMoneyToSpend, currentMoneyToSpend, setupDay, payDate, staticBlockPrice);
        double newDisplayBlocks = getBlocksToDisplay(totalMoneyToSpend, currentMoneyToSpend - purchaseAmount, setupDay, payDate, staticBlockPrice);
        return Math.round(currentDisplayedBlocks - newDisplayBlocks);
    }
}
