package com.flipper2.models;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;
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
	private int itemId;
	private String itemName;
	private boolean isBuy;
	private boolean isComplete;
	private int slot;
	private int initQuantity;
	private int finQuantity;
	private int initPricePer;
	private int finPricePer;
	private int initTaxPer;
	private int initTax;
	private int finTaxPer;
	private int finTax;
	private long initTotal;
	private long finTotal;

	@Setter(AccessLevel.NONE)
	private boolean isFlipped;

	private int flippedQuantity;
	private boolean hasCancelledOnce = false;
	private GrandExchangeOfferState currentState;
	private Instant createdTime;
	private Instant completedTime;

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
		this.flippedQuantity = 0;
		this.hasCancelledOnce = false;

		this.initTaxPer = 0;
		this.initTax = 0;
		this.finTaxPer = 0;
		this.finTax = 0;

		if (!this.isBuy)
		{
			this.initTaxPer = GrandExchange.calculateTaxPerItem(this.itemId, this.initPricePer, this.createdTime);
			this.initTax = this.initTaxPer * this.initQuantity;

			this.finTaxPer = GrandExchange.calculateTaxPerItem(this.itemId, this.finPricePer, this.createdTime);
			this.finTax = this.finTaxPer * this.finQuantity;
		}

		this.initTotal = ((long) this.initPricePer * this.initQuantity) - this.initTax;
		this.finTotal = ((long) this.finPricePer * this.finQuantity) - this.finTax;
	}

	/**
	 * Hardened setter that guarantees the isFlipped state remains in sync
	 * with the flipped quantity.
	 */
	public void setFlippedQuantity(int flippedQuantity)
	{
		this.flippedQuantity = flippedQuantity;
		this.isFlipped = (this.flippedQuantity >= this.finQuantity);
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

		if (!this.isBuy)
		{
			this.finTaxPer = GrandExchange.calculateTaxPerItem(this.itemId, this.finPricePer, this.createdTime);
			this.finTax = this.finTaxPer * this.finQuantity;
		}

		this.finTotal = ((long) this.finPricePer * this.finQuantity) - this.finTax;

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
}