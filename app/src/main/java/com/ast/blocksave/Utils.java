package com.ast.blocksave;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import static com.ast.blocksave.SetupActivity.BLOCKS_PER_DAY;

public class Utils {

    public static void main(String[] args) {

        Double totalMoney = 300.0;
        Double currentMoney = 296.35;

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        Date payDate = null;
        Date setupDate = null;
        try {
            payDate = sdf.parse("31/07/2017");
            setupDate = sdf.parse("11/07/2017");
        } catch(Exception ex) {
        }
        int daysDifference = getDaysDifference(setupDate.getTime(), payDate.getTime());
        System.out.println(daysDifference);
        double staticBlockPrice = getStaticBlockPrice(totalMoney, daysDifference);
        System.out.println(staticBlockPrice);
        System.out.println(getBlocksToDisplay(totalMoney, currentMoney, setupDate.getTime(), staticBlockPrice));
        System.out.println(getTomorrowBlocksToDisplay(setupDate.getTime(), payDate.getTime(), currentMoney, staticBlockPrice));
        System.out.println("------------------------------------------------------");
        System.out.println(getNumberOfDaysUntilPayDay(payDate.getTime()));
        System.out.println(getDayNumber(setupDate.getTime()));
        System.out.println("-----------------------------------------------------");
        System.out.println(getUnderSpendValue(setupDate.getTime(), payDate.getTime(), totalMoney, currentMoney));
        System.out.println(getOverSpendValue(setupDate.getTime(), payDate.getTime(), totalMoney, currentMoney));
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

    public static int getBlocksToDisplay(double totalMoneyToSpend, double currentMoneyToSpend, long setupDay, double staticBlockPrice) {
        int daysPassed = getDayNumber(setupDay);
        Integer totalBlocksAvailableInteger = Integer.valueOf(((Double) ((totalMoneyToSpend) / staticBlockPrice)).intValue()) - (daysPassed * BLOCKS_PER_DAY);
        Integer currentBlocksAvailableInteger = Integer.valueOf(((Double) ((currentMoneyToSpend) / staticBlockPrice)).intValue()) - (daysPassed * BLOCKS_PER_DAY);
        Integer blocksToDisplay = BLOCKS_PER_DAY - (totalBlocksAvailableInteger - currentBlocksAvailableInteger);
        return blocksToDisplay;
    }

    public static int getTomorrowBlocksToDisplay(long setupDay, long payDate, double currentMoneyToSpend,  double staticBlockPrice) {
        int daysPassed = getDayNumber(setupDay);
        Integer currentBlocksAvailableInteger = Integer.valueOf(((Double) ((currentMoneyToSpend) / staticBlockPrice)).intValue()) - ((daysPassed - 1) * BLOCKS_PER_DAY);
        Integer blocksToDisplay = (currentBlocksAvailableInteger / getNumberOfDaysUntilPayDay(payDate));
        return blocksToDisplay;
    }

    public static String getCurrencySymbol() {
        return "$";
    }

    public static int getUnderSpendValue(long setupDay, long nextPayDate, double totalMoneyToSpend, double currentMoneyToSpend) {
        int daysDifference = getDaysDifference(setupDay, nextPayDate);
        double staticBlockPrice = getStaticBlockPrice(totalMoneyToSpend, daysDifference);
        int daysPassed = getDayNumber(setupDay);
        Integer currentBlocksAvailableInteger = Integer.valueOf(((Double) Math.ceil((currentMoneyToSpend) / staticBlockPrice)).intValue()) - (BLOCKS_PER_DAY * daysPassed);
        int todaysBlocks = getTomorrowBlocksToDisplay(setupDay, nextPayDate, currentMoneyToSpend, staticBlockPrice);
        int underSpendValue = 1;
        int tomorrowsBlocks = todaysBlocks;
        while(todaysBlocks == tomorrowsBlocks) {
            underSpendValue++;
            tomorrowsBlocks = (currentBlocksAvailableInteger + underSpendValue) / (daysDifference - (daysPassed + 1));
            if (underSpendValue > 9999) {
                break;
            }
        }
        return underSpendValue;
    }

    public static int getOverSpendValue(long setupDay, long nextPayDate, double totalMoneyToSpend, double currentMoneyToSpend) {
        int daysDifference = getDaysDifference(setupDay, nextPayDate);
        double staticBlockPrice = getStaticBlockPrice(totalMoneyToSpend, daysDifference);
        int daysPassed = getDayNumber(setupDay);
        Integer currentBlocksAvailableInteger = Integer.valueOf(((Double) ((currentMoneyToSpend) / staticBlockPrice)).intValue()) - (BLOCKS_PER_DAY * daysPassed);
        int todaysBlocks = getTomorrowBlocksToDisplay(setupDay, nextPayDate, currentMoneyToSpend, staticBlockPrice);
        int overSpendValue = 1;
        int tomorrowsBlocks = todaysBlocks;
        while(todaysBlocks == tomorrowsBlocks) {
            overSpendValue++;
            tomorrowsBlocks = (currentBlocksAvailableInteger - overSpendValue) / (daysDifference - (daysPassed + 1));
            if (overSpendValue > 9999) {
                break;
            }
        }
        return overSpendValue;
    }
}
