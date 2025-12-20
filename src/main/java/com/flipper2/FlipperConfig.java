package com.flipper2;

import com.flipper2.helpers.TimestampFormatEnums;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;


@ConfigGroup("Flipper2")
public interface FlipperConfig extends Config
{
	@ConfigSection(
		name = "Delete Confirmation Prompts",
		description = "Show or skip the prompt before deleting an item. Deleted buys/sells/flip cannot be recovered. Recommended to keep these enabled",
		position = 1
	)
	String deletepromptSection = "deletepromptSection";

	@ConfigItem(
		keyName = "isPromptDeleteBuy",
		name = "Show Delete Buy Prompt",
		description = "Shows confirmation prompt before deleting buy",
		position = 1,
		section = deletepromptSection
	)
	default boolean isPromptDeleteBuy()
	{
		return true;
	}

	@ConfigItem(
		keyName = "isPromptDeleteSell",
		name = "Show Delete Sell Prompt",
		description = "Shows confirmation prompt before deleting sell",
		position = 2,
		section = deletepromptSection
	)
	default boolean isPromptDeleteSell()
	{
		return true;
	}

	@ConfigItem(
		keyName = "isPromptDeleteFlip",
		name = "Show Delete Flip Prompt",
		description = "Shows confirmation prompt before deleting flip",
		position = 3,
		section = deletepromptSection
	)
	default boolean isPromptDeleteFlip()
	{
		return true;
	}

	@ConfigSection(
		name = "In-Progress Panel Timestamps",
		description = "Settings for the timestamp display on in-progress offer panels",
		position = 2
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
		keyName = "inProgressTimestampFormat",
		name = "Timestamp Format",
		description = "Choose the format for the last update timestamp.",
		position = 2,
		section = timestampSection
	)
	default TimestampFormatEnums inProgressTimestampFormat()
	{
		return TimestampFormatEnums.SIMPLE_DATE_TIME_12;
	}
}