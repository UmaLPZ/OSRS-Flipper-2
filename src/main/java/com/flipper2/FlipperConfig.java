package com.flipper2;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

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


}
