package com.flipper2.models;

import java.sql.Timestamp;
import java.util.UUID;

import lombok.Data;
import com.flipper2.helpers.GrandExchange;

/**
 * Represents a buy and sell (flip) of an item
 */
@Data
public class Flip
{
	public UUID flipId;
	public int tax;
	public int taxPerItem;
	public UUID buyId;
	public UUID sellId;
	public int itemId;
	String itemName;
	public int quantity;
	public int buyPrice;
	public int sellPrice;
	public long totalBuy;
	public long totalSell;
	public int profitPerItem;
	public long totalProfit;
	public boolean isMarginCheck;
	private Timestamp updatedAt;
	private Timestamp createdAt;

	public Flip()
	{
	}

	public Flip(Transaction buy, Transaction sell)
	{
		this.flipId = UUID.randomUUID();
		this.buyId = buy.id;
		this.sellId = sell.id;
		this.itemId = sell.getItemId();
		this.itemName = sell.getItemName();
		this.quantity = sell.getFinQuantity();
		this.buyPrice = buy.getFinPricePer();
		this.sellPrice = sell.getFinPricePer();

		this.tax = sell.getFinTax();
		this.taxPerItem = this.quantity > 0 ? this.tax / this.quantity : 0;

		this.totalBuy = (long) this.buyPrice * this.quantity;
		this.totalSell = (long) this.sellPrice * this.quantity;
		this.totalProfit = this.totalSell - this.totalBuy - this.tax;
		this.profitPerItem = this.quantity > 0 ? (int) (this.totalProfit / this.quantity) : 0;

		this.isMarginCheck = this.quantity == 1 && this.buyPrice >= this.sellPrice;

		// Default to Sell time (used if this is a newly discovered "missed" flip)
		this.updatedAt = new Timestamp(sell.getCreatedTime().toEpochMilli());
		this.createdAt = new Timestamp(sell.getCreatedTime().toEpochMilli());
	}

	public String describeFlip()
	{
		return String.valueOf(quantity) + " " + this.itemName + "(s)";
	}

	public int getTax()
	{
		return this.taxPerItem;
	}

	public int getTotalTax()
	{
		return this.tax;
	}

	public long getTotalProfit()
	{
		return this.totalProfit;
	}

	public long getTotalBuy()
	{
		return this.totalBuy;
	}

	public long getTotalSell()
	{
		return this.totalSell;
	}
}