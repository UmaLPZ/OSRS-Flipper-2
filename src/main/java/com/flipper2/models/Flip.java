package com.flipper2.models;

import java.time.Instant;
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
	public UUID buyId;
	public UUID sellId;
	public int itemId;
	String itemName;
	public int quantity;
	public int buyPrice;
	public int sellPrice;
	public int tax;
	public int taxPerItem;
	public long totalBuy;
	public long totalSell;
	public long totalProfit;
	public int profitPerItem;
	public boolean isMarginCheck;
	private Instant createdAt;
	private Instant updatedAt;

	public Flip()
	{
	}

	public Flip(Transaction buy, Transaction sell, int flipQuantity)
	{
		this.flipId = UUID.randomUUID();
		this.buyId = buy.id;
		this.sellId = sell.id;
		this.itemId = sell.getItemId();
		this.itemName = sell.getItemName();

		this.quantity = flipQuantity;
		this.buyPrice = buy.getFinPricePer();
		this.sellPrice = sell.getFinPricePer();

		this.tax = sell.getFinTax();
		this.taxPerItem = this.quantity > 0 ? this.tax / this.quantity : 0;

		this.totalBuy = (long) this.buyPrice * this.quantity;
		this.totalSell = (long) this.sellPrice * this.quantity;
		this.totalProfit = this.totalSell - this.totalBuy - this.tax;
		this.profitPerItem = this.quantity > 0 ? (int) (this.totalProfit / this.quantity) : 0;

		this.isMarginCheck = this.quantity == 1 && this.buyPrice >= this.sellPrice;

		this.createdAt = sell.getCreatedTime();
		this.updatedAt = sell.getCreatedTime();
	}

	public String describeFlip()
	{
		return String.valueOf(quantity) + " " + this.itemName + "(s)";
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