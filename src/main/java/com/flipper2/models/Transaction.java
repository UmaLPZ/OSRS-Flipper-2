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
	private int finQuantity;
	private int initQuantity;
	private int itemId;
	private int finPricePer;
	private int initPricePer;
	private int slot;
	private String itemName;
	private boolean isBuy;
	private boolean isComplete;
	private boolean isFlipped;
	private int finTax;
	private int initTax;
	private long initTotal;
	private long finTotal;
	private Instant completedTime;
	private Instant createdTime;
	private boolean hasCancelledOnce = false;
	private GrandExchangeOfferState currentState;

	public Transaction(
		int finQuantity,
		int initQuantity,
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
		this.finQuantity = finQuantity;
		this.initQuantity = initQuantity;
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

		this.initTotal = (long) this.initPricePer * this.initQuantity;
		this.finTotal = (long) this.finPricePer * this.finQuantity;

		if (!this.isBuy)
		{
			int taxPerInit = GrandExchange.calculateTaxPerItem(this.itemId, this.initPricePer, this.createdTime);
			this.initTax = taxPerInit * this.initQuantity;

			int taxPerFin = GrandExchange.calculateTaxPerItem(this.itemId, this.finPricePer, this.createdTime);
			this.finTax = taxPerFin * this.finQuantity;
		}
		else
		{
			this.initTax = 0;
			this.finTax = 0;
		}
	}

	public Transaction updateTransaction(GrandExchangeOffer offer)
	{
		this.finQuantity = offer.getQuantitySold();
		this.currentState = offer.getState();

		if (offer.getQuantitySold() > 0)
		{
			this.finPricePer = offer.getSpent() / offer.getQuantitySold();
		}
		else
		{
			this.finPricePer = 0;
		}

		this.finTotal = (long) this.finPricePer * this.finQuantity;

		if (!this.isBuy)
		{
			int taxPerFin = GrandExchange.calculateTaxPerItem(this.itemId, this.finPricePer, this.createdTime);
			this.finTax = taxPerFin * this.finQuantity;
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
		return String.valueOf(this.finQuantity) + " " + this.itemName + "(s)";
	}

	public void setIsFlipped(boolean isFlipped)
	{
		this.isFlipped = isFlipped;
	}

	public int calculateTax(int pricePer)
	{
		return GrandExchange.calculateTaxPerItem(this.itemId, pricePer, this.createdTime);
	}
}