
package com.flipper2.helpers;

public enum TimestampFormatEnums {
	RELATIVE("Relative (e.g., 5m ago)"),
	SIMPLE_DATE_TIME("Date & Time (e.g., 01 Jan 2:30 PM)"),
	COMPACT_DATE_TIME("Compact Date & Time (e.g., 01 Jan 14:30)"),
	TIME_ONLY("Time Only (e.g., 14:30:05)");

	private final String displayName;

	TimestampFormatEnums(String displayName) {
		this.displayName = displayName;
	}

	@Override
	public String toString() {
		return displayName;
	}
}