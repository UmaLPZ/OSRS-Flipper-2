package com.flipper2.helpers;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class Timestamps
{
	public static String format(Instant instant)
	{
		LocalDateTime localDateTime = instant.atZone(ZoneId.systemDefault()).toLocalDateTime();
		return Timestamps.format(localDateTime);
	}

	public static String format(LocalDateTime localDateTime)
	{
		return localDateTime.format(DateTimeFormatter.ofPattern("d MMM h:mm a"));
	}
}