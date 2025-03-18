package com.flipper.helpers;

import java.math.BigInteger;
import java.text.NumberFormat;

public class Numbers {
    public static String numberWithCommas(int number) {
        return NumberFormat.getIntegerInstance().format(number);
    }

    public static String numberWithCommas(String number) {
        if (number == "0") {
            return "0";
        }

        BigInteger numberAsBigInt = new BigInteger(number);
        return String.format("%,d", numberAsBigInt);
    }

    /**
     * Shortens a number for display, adding "K", "M", or "B" suffixes as appropriate,
     * rounding to one decimal place, and omitting the decimal if it's .0.
     * Correctly handles negative numbers.
     *
     * @param number The number to shorten.
     * @return The shortened number string.
     */
    public static String toShortNumber(int number) {
        if (number > -100000 && number < 100000) {
            // Numbers less than 100,000 and greater than -100,000 are not shortened
            return numberWithCommas(number);
        }

        // Store the sign, then work with the absolute value.
        String sign = number < 0 ? "-" : "";
        number = Math.abs(number);

        double shortNumber;
        String suffix;

        if (number < 1000000) {
            shortNumber = number / 1000.0;
            suffix = "K";
        } else if (number < 1000000000) {
            shortNumber = number / 1000000.0;
            suffix = "M";
        } else {
            shortNumber = number / 1000000000.0;
            suffix = "B";
        }

        shortNumber = Math.round(shortNumber * 10.0) / 10.0;

        if (shortNumber == (int) shortNumber) {
            return String.format("%s%d%s", sign, (int) shortNumber, suffix); // Add sign
        } else {
            return String.format("%s%.1f%s", sign, shortNumber, suffix); // Add sign
        }
    }
}