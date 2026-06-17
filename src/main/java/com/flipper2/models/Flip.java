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
	public UUID buyId;
	public UUID sellId;
	public int itemId;
	String itemName;
	public int quantity;
	public int buyPrice;
	public int sellPrice;
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
		this.quantity = sell.getQuantity();
		this.buyPrice = buy.getFinPricePer();
		this.sellPrice = sell.getFinPricePer();
		this.tax = sell.getTax();

		this.updatedAt = new Timestamp(System.currentTimeMillis());
		this.createdAt = new Timestamp(System.currentTimeMillis());
	}

	/**
	 * We know a flip is a margin check when only 1 is bought and it's bought for a
	 * greater to or equal price than sold for
	 */
	public boolean isMarginCheck()
	{
		return quantity == 1 && buyPrice >= sellPrice;
	}

	public String describeFlip()
	{
		return String.valueOf(quantity) + " " + this.itemName + "(s)";
	}

	/**
	 * We only concern ourselves with the amount sold (ignore extra bought and kept)
	 *
	 * @return profit of flip
	 */

	/**
	 * The GE floors tax per item.
	 *
	 * @return tax per item of flip
	 */
	public int getTax()
	{
		return this.quantity > 0 ? this.tax / this.quantity : 0;
	}

	/**
	 * Gets the total tax of the sale
	 *
	 * @return total tax of sale
	 */
	public int getTotalTax()
	{
		return this.tax;
	}

	public int getTotalProfit()
	{
		return (sellPrice - buyPrice) * quantity - this.tax;
	}

	public int getTotalBuy()
	{
		return buyPrice * quantity;
	}

	public int getTotalSell()
	{
		return sellPrice * quantity;
	}
}