package com.flipper2;

import com.flipper2.helpers.TimestampFormatEnums;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;


@ConfigGroup("Flipper2")
public interface FlipperConfig extends Config
{
	@ConfigItem(
		keyName = "isPromptDeleteBuy",
		name = "Delete Buy Prompt",
		description = "Shows confirmation prompt before deleting buy"
	)
	default boolean isPromptDeleteBuy()
	{
		return true;
	}

	@ConfigItem(
		keyName = "isPromptDeleteSell",
		name = "Delete Sell Prompt",
		description = "Shows confirmation prompt before deleting sell"
	)
	default boolean isPromptDeleteSell()
	{
		return true;
	}


	@ConfigItem(
		keyName = "isPromptDeleteFlip",
		name = "Delete Flip Prompt",
		description = "Shows confirmation prompt before deleting flip"
	)
	default boolean isPromptDeleteFlip()
	{
		return true;
	}

	@ConfigSection(
		name = "In-Progress Panel Timestamps",
		description = "Settings for the timestamp display on in-progress offer panels",
		position = 10
	)
	String timestampSection = "timestampSection";

	@ConfigItem(
		keyName = "isLastUpdate",
		name = "Show Last Update",
		description = "Displays a timestamp of the last time an offer was updated",
		position = 1,
		section = timestampSection
	)
	default boolean isLastUpdate()
	{
		return true;
	}

	@ConfigItem(
		keyName = "inProgressTimestampFormat", // YOUR SPECIFIED KEY NAME
		name = "Timestamp Format",
		description = "Choose the format for the last update timestamp.",
		position = 2,
		section = timestampSection
	)
	default TimestampFormatEnums inProgressTimestampFormat() { // YOUR SPECIFIED METHOD NAME, returns TimestampFormatEnums
		return TimestampFormatEnums.SIMPLE_DATE_TIME; // Default format
	}


}
