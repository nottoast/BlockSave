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
        System.out.println("------------------------------------------------------");
        System.out.println(getUnderSpendValue(setupDate.getTime(), payDate.getTime(), totalMoney, currentMoney));
        System.out.println(getOverSpendValue(setupDate.getTime(), payDate.getTime(), totalMoney, currentMoney));
        System.out.println("------------------------------------------------------");

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

    public static long getBlocksToDisplay(double totalMoneyToSpend, double currentMoneyToSpend, long setupDay, double staticBlockPrice) {
        int daysPassed = getDayNumber(setupDay);
        double totalBlocksAvailableInteger = ((totalMoneyToSpend) / staticBlockPrice) - (daysPassed * BLOCKS_PER_DAY);
        double currentBlocksAvailableInteger = ((currentMoneyToSpend) / staticBlockPrice) - (daysPassed * BLOCKS_PER_DAY);
        long blocksToDisplay = Math.round(BLOCKS_PER_DAY - (totalBlocksAvailableInteger - currentBlocksAvailableInteger));
        return blocksToDisplay;
    }

    public static long getTomorrowBlocksToDisplay(long setupDay, long payDate, double currentMoneyToSpend,  double staticBlockPrice) {
        int daysPassed = getDayNumber(setupDay);
        double currentBlocksAvailable = ((currentMoneyToSpend) / staticBlockPrice);
        double currentBlocksAvailableAdjusted = currentBlocksAvailable - ((daysPassed + 1) * BLOCKS_PER_DAY);
        double numberOfDaysUntilPayDay = getNumberOfDaysUntilPayDay(payDate);
        double blocksToDisplay = currentBlocksAvailableAdjusted / (numberOfDaysUntilPayDay - 1);
        return ((Double) Math.ceil(blocksToDisplay - 0.35)).intValue();
    }

    public static String getCurrencySymbol() {
        return "$";
    }

    public static int getUnderSpendValue(long setupDay, long nextPayDate, double totalMoneyToSpend, double currentMoneyToSpend) {
        int daysDifference = getDaysDifference(setupDay, nextPayDate);
        double staticBlockPrice = getStaticBlockPrice(totalMoneyToSpend, daysDifference);
        int daysPassed = getDayNumber(setupDay);
        double currentBlocksAvailable = currentMoneyToSpend / staticBlockPrice;
        double currentBlocksAvailableAdjusted = currentBlocksAvailable - (BLOCKS_PER_DAY * daysPassed);
        double tomorrowsBlocks = Math.ceil((currentBlocksAvailableAdjusted) / (daysDifference - (daysPassed + 1)));
        int underSpendValue = -1;
        //double tomorrowsBlocksAdjusted = Math.ceil((currentBlocksAvailableAdjusted) / (daysDifference - (daysPassed + 1)));
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

        //double tomorrowsBlocksAdjusted = Math.ceil((currentBlocksAvailableAdjusted) / (daysDifference - (daysPassed + 1)));
        while(true) {
            overSpendValue++;
            double calculatedAdjustment = (currentBlocksAvailableAdjusted - overSpendValue) / (daysDifference - (daysPassed + 1));
            //tomorrowsBlocksAdjusted = Math.ceil(calculatedAdjustment);

            if(tomorrowsBlocks - calculatedAdjustment > 1.0) {
                break;
            }

            if (overSpendValue > 98) {
                break;
            }
        }
        return overSpendValue;
    }

    public static long getBlocksToDeduct(float totalMoneyToSpend, float currentMoneyToSpend, Double purchaseAmount, Double staticBlockPrice, long setupDay) {
        double currentDisplayedBlocks = getBlocksToDisplay(totalMoneyToSpend, currentMoneyToSpend, setupDay, staticBlockPrice);
        double newDisplayBlocks = getBlocksToDisplay(totalMoneyToSpend, currentMoneyToSpend - purchaseAmount, setupDay, staticBlockPrice);
        return Math.round(currentDisplayedBlocks - newDisplayBlocks);
    }
}
