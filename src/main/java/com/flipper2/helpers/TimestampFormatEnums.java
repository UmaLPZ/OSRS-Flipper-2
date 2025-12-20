
package com.flipper2.helpers;

public enum TimestampFormatEnums
{
	RELATIVE("Relative (e.g. 5m ago)"),
	SIMPLE_DATE_TIME_12("12H Date/Time (e.g. 1 Jan 2:30 PM)"),
	SIMPLE_DATE_TIME_24("24H Date/Time (e.g. 1 Jan 14:30)"),
	TIME_ONLY_12("12H Time Only (e.g. 2:30 PM)"),
	TIME_ONLY_24("24H Time Only (e.g. 14:30)");
	private final String displayName;

	TimestampFormatEnums(String displayName)
	{
		this.displayName = displayName;
	}

	@Override
	public String toString()
	{
		return displayName;
	}
}