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
	private int tax;
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
		this.tax = 0;
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

		if (!this.isBuy)
		{
			this.tax = GrandExchange.calculateTotalTax(this.itemId, this.finPricePer, this.quantity, this.createdTime);
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

	public int calculateTax(int pricePer)
	{
		return GrandExchange.calculateTotalTax(this.itemId, pricePer, 1, this.createdTime);
	}
}