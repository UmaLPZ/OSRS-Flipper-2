package com.flipper2.helpers;

import com.flipper2.models.Transaction;

import net.runelite.api.GrandExchangeOffer;
import net.runelite.api.GrandExchangeOfferState;
import net.runelite.api.ItemComposition;
import net.runelite.client.game.ItemManager;

import java.time.Instant;

/**
 * Handles GrandExchange events
 */
public class GrandExchange
{
	public static final double TAX_RATE = 0.02;
	public static final double OLD_TAX_RATE = 0.01;
	public static final int MAX_TAX = 5000000;
	public static final long TAX_CHANGE_EPOCH = 1748514600L;

	public static boolean checkIsBuy(GrandExchangeOfferState state)
	{
		return state == GrandExchangeOfferState.BOUGHT ||
			state == GrandExchangeOfferState.CANCELLED_BUY ||
			state == GrandExchangeOfferState.BUYING;
	}

	public static boolean checkIsActive(GrandExchangeOfferState state)
	{
		return state == GrandExchangeOfferState.BUYING ||
			state == GrandExchangeOfferState.SELLING;
	}

	public static boolean checkIsComplete(GrandExchangeOfferState state)
	{
		return state == GrandExchangeOfferState.BOUGHT ||
			state == GrandExchangeOfferState.SOLD ||
			state == GrandExchangeOfferState.CANCELLED_SELL ||
			state == GrandExchangeOfferState.CANCELLED_BUY;
	}

	public static boolean checkIsBoughtSold(GrandExchangeOfferState state)
	{
		return state == GrandExchangeOfferState.BOUGHT ||
			state == GrandExchangeOfferState.SOLD;
	}

	public static boolean checkIsCancelState(GrandExchangeOfferState state)
	{
		return state == GrandExchangeOfferState.CANCELLED_BUY ||
			state == GrandExchangeOfferState.CANCELLED_SELL;
	}

	/**
	 * Potentially creates a transaction based on the GrandExchange event
	 *
	 * @param offer
	 * @param itemManager
	 * @param slot
	 * @return null or newly created transaction
	 */
	public static Transaction createTransactionFromOffer(GrandExchangeOffer offer, ItemManager itemManager, int slot)
	{
		GrandExchangeOfferState state = offer.getState();
		boolean isBuy = checkIsBuy(state);
		boolean isComplete = checkIsComplete(state);

		ItemComposition itemComposition = itemManager.getItemComposition(offer.getItemId());
		return new Transaction(
			offer.getQuantitySold(),
			offer.getTotalQuantity(),
			offer.getItemId(),
			offer.getSpent() / (offer.getQuantitySold() > 0 ? offer.getQuantitySold() : 1),
			offer.getPrice(),
			slot,
			itemComposition.getName(),
			isBuy,
			isComplete
		);
	}

	public static boolean checkIsOfferPartOfTransaction(Transaction transaction, GrandExchangeOffer offer, int slot)
	{
		return
			(!transaction.isComplete() || (transaction.isComplete() && GrandExchange.checkIsComplete(offer.getState()))) &&
				transaction.getSlot() == slot &&
				transaction.getItemId() == offer.getItemId() &&
				transaction.getInitQuantity() == offer.getTotalQuantity();
	}

	public static boolean checkIsSellAFlipOfBuy(Transaction sell, Transaction buy)
	{
		boolean isSameItem = sell.getItemId() == buy.getItemId();
		boolean hasTransactionsBeenFlipped = sell.isFlipped() && buy.isFlipped();
		return isSameItem && !hasTransactionsBeenFlipped;
	}

	public static int calculateTaxPerItem(int itemId, int pricePer, Instant time)
	{
		if (itemId == 13190)
		{
			return 0;
		}

		double applicableRate = time.getEpochSecond() <= TAX_CHANGE_EPOCH
			? OLD_TAX_RATE
			: TAX_RATE;

		int taxPerItem = (int) Math.floor(pricePer * applicableRate);
		return Math.min(taxPerItem, MAX_TAX);
	}
}