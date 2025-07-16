package com.flipper2.helpers;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

import com.flipper2.helpers.TimestampFormatEnums;

public class Timestamps
{
	private static final DateTimeFormatter SIMPLE_DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("d MMM h:mm a");

	// Formats for the new configurable options
	private static final DateTimeFormatter COMPACT_DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("dd MMM HH:mm");
	private static final DateTimeFormatter TIME_ONLY_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss");
	public static String format(Timestamp timestamp) //for flip panel
	{
		LocalDateTime localDateTime = timestamp.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
		return Timestamps.format(localDateTime);
	}

	public static String format(Instant instant) //for transaction panel
	{
		LocalDateTime localDateTime = instant.atZone(ZoneId.systemDefault()).toLocalDateTime();
		return Timestamps.format(localDateTime);
	}

	public static String format(LocalDateTime localDateTime)
	{
		return localDateTime.format(DateTimeFormatter.ofPattern("d MMM h:mm a"));
	}

	// --- New Configurable Formatting Method ---
	public static String formatForPanel(Instant instant, TimestampFormatEnums formatEnum) {
		if (instant == null) return "---";
		if (formatEnum == null) formatEnum = TimestampFormatEnums.RELATIVE; // Default

		LocalDateTime ldt = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());

		switch (formatEnum) {
			case SIMPLE_DATE_TIME:
				return ldt.format(SIMPLE_DATE_TIME_FORMAT);
			case COMPACT_DATE_TIME:
				return ldt.format(COMPACT_DATE_TIME_FORMAT);
			case TIME_ONLY:
				return ldt.format(TIME_ONLY_FORMAT);
			case RELATIVE:
			default:
				long diffSeconds = ChronoUnit.SECONDS.between(instant, Instant.now());
				if (diffSeconds < 0) diffSeconds = 0;
				if (diffSeconds < 60) return diffSeconds + "s ago";
				if (diffSeconds < 3600) return (diffSeconds / 60) + "m ago";
				long hours = diffSeconds / 3600;
				long minutes = (diffSeconds % 3600) / 60;
				if (hours < 24) {
					return minutes > 0 ? hours + "h " + minutes + "m ago" : hours + "h ago";
				}
				return (diffSeconds / 86400) + "d ago";
		}
	}
}
