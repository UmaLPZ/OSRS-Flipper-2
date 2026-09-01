package com.flipper2.helpers;

import java.text.NumberFormat;

public class Numbers
{
	public static String numberWithCommas(long number)
	{
		return NumberFormat.getIntegerInstance().format(number);
	}

	/**
	 * Shortens a number for display, adding "K", "M", or "B" suffixes as appropriate,
	 * rounding to two decimal places, and omitting trailing zero decimals.
	 * Correctly handles negative numbers.
	 *
	 * @param number The number to shorten.
	 * @return The shortened number string.
	 */
	public static String toShortNumber(long number)
	{
		if (number > -100000 && number < 100000)
		{
			return numberWithCommas(number);
		}

		String sign = number < 0 ? "-" : "";
		number = Math.abs(number);

		double shortNumber;
		String suffix;

		if (number < 1000000)
		{
			shortNumber = Math.round((number / 1000.0) * 100.0) / 100.0;
			suffix = "K";
			if (shortNumber >= 1000)
			{
				shortNumber = shortNumber / 1000.0;
				suffix = "M";
			}
		}
		else if (number < 1000000000)
		{
			shortNumber = Math.round((number / 1000000.0) * 100.0) / 100.0;
			suffix = "M";
			if (shortNumber >= 1000)
			{
				shortNumber = shortNumber / 1000.0;
				suffix = "B";
			}
		}
		else
		{
			shortNumber = Math.round((number / 1000000000.0) * 100.0) / 100.0;
			suffix = "B";
		}

		shortNumber = Math.round(shortNumber * 100.0) / 100.0;

		String formatted = String.format("%.2f", shortNumber);
		if (formatted.contains("."))
		{
			formatted = formatted.replaceAll("0+$", "");
			formatted = formatted.replaceAll("\\.$", "");
		}
		return sign + formatted + suffix;
	}
}