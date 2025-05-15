package com.flipper2.models;

import lombok.Data;
import net.runelite.api.GrandExchangeOffer;
import net.runelite.api.GrandExchangeOfferState;

import java.time.Instant;
import java.util.UUID;

import com.flipper2.helpers.GrandExchange;

/**
 * Represents either a buy or sell of an item(s) on the GE
 */
@Data
public class Transaction
{
	public static final double TAX_RATE = 0.01;
	public static final int MAX_TAX = 5000000;

	public final UUID id;
	private int quantity;
	private int totalQuantity;
	private int itemId;
	private int finPricePer;
	private int initPricePer;
	private int slot;
	private String itemName;
	private boolean isBuy;
	private boolean isComplete;
	private boolean isFlipped;
	private boolean isAlched;
	private Instant completedTime;
	private Instant createdTime;
	private boolean hasCancelledOnce = false;
	private GrandExchangeOfferState currentState;

	public Transaction(
		int quantity,
		int totalQuantity,
		int itemId,
		int finPricePer,
		int initPricePer,
		int slot,
		String itemName,
		boolean isBuy,
		boolean isComplete
	)
	{
		id = UUID.randomUUID();
		this.quantity = quantity;
		this.totalQuantity = totalQuantity;
		this.itemId = itemId;
		this.finPricePer = finPricePer;
		this.initPricePer = initPricePer;
		this.slot = slot;
		this.itemName = itemName;
		this.isBuy = isBuy;
		this.isComplete = isComplete;
		this.createdTime = Instant.now();
		this.isFlipped = false;
		this.hasCancelledOnce = false;
	}

	public Transaction updateTransaction(GrandExchangeOffer offer)
	{
		this.quantity = offer.getQuantitySold();
		this.currentState = offer.getState();

		if (offer.getQuantitySold() > 0)
		{
			this.finPricePer = offer.getSpent() / offer.getQuantitySold();
		}
		else
		{
			this.finPricePer = 0;
		}

		boolean isCancelState = GrandExchange.checkIsCancelState(offer.getState());

		if (!isCancelState || (this.hasCancelledOnce && isCancelState))
		{
			this.isComplete = GrandExchange.checkIsComplete(offer.getState());
		}

		if (isCancelState)
		{
			this.hasCancelledOnce = true;
		}

		if (this.isComplete)
		{
			completedTime = Instant.now();
		}
		return this;
	}

	public String describeTransaction()
	{
		return String.valueOf(this.quantity) + " " + this.itemName + "(s)";
	}

	public void setIsFlipped(boolean isFlipped)
	{
		this.isFlipped = isFlipped;
	}


	public int getTax()
	{
		if (this.isBuy)
		{
			return 0;
		}

		int tax = (int) Math.floor(this.finPricePer * TAX_RATE);
		return Math.min(tax, MAX_TAX);
	}

	public int getTotalTax()
	{
		return getTax() * this.quantity;
	}
}