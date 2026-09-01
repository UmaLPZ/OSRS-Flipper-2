package com.flipper2.helpers;

import com.flipper2.models.Flip;
import com.flipper2.models.Transaction;
import net.runelite.api.GrandExchangeOfferState;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Handles legacy data migrations and historical flip repairs.
 */
public class DataMigrator
{
	private static Instant extractLegacyInstant(JsonObject obj, String field)
	{
		if (!obj.has(field) || obj.get(field).isJsonNull())
		{
			return null;
		}
		JsonElement element = obj.get(field);
		if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isString())
		{
			try
			{
				java.util.Date legacyDate = new Gson().fromJson(element, java.util.Date.class);
				obj.remove(field);
				return legacyDate != null ? legacyDate.toInstant() : null;
			}
			catch (Exception e)
			{
				obj.remove(field);
				return null;
			}
		}
		return null;
	}

	/**
	 * Migrates raw legacy flip JSON array into a v2 Flip list.
	 */
	public static List<Flip> migrateLegacyFlips(JsonArray rawFlips)
	{
		List<Flip> flips = new ArrayList<>();

		for (JsonElement element : rawFlips)
		{
			JsonObject obj = element.getAsJsonObject();

			Instant legacyCreatedAt = extractLegacyInstant(obj, "createdAt");
			Instant legacyUpdatedAt = extractLegacyInstant(obj, "updatedAt");

			Flip flip = Persistor.gson.fromJson(obj, Flip.class);

			if (legacyCreatedAt != null)
			{
				flip.setCreatedAt(legacyCreatedAt);
			}
			if (legacyUpdatedAt != null)
			{
				flip.setUpdatedAt(legacyUpdatedAt);
			}
			if (flip.getCreatedAt() == null)
			{
				flip.setCreatedAt(Instant.now());
			}
			if (flip.getUpdatedAt() == null)
			{
				flip.setUpdatedAt(flip.getCreatedAt());
			}
			int taxPerItem = GrandExchange.calculateTaxPerItem(flip.getItemId(), flip.getSellPrice(), flip.getCreatedAt());
			flip.setTax(taxPerItem * flip.getQuantity());
			flip.setTaxPerItem(taxPerItem);

			flip.setTotalBuy((long) flip.getBuyPrice() * flip.getQuantity());
			flip.setTotalSell((long) flip.getSellPrice() * flip.getQuantity());
			flip.setTotalProfit(flip.getTotalSell() - flip.getTotalBuy() - flip.getTax());
			flip.setProfitPerItem(flip.getQuantity() > 0 ? (int) (flip.getTotalProfit() / flip.getQuantity()) : 0);
			flip.setMarginCheck(flip.getQuantity() == 1 && flip.getBuyPrice() >= flip.getSellPrice());

			flips.add(flip);
		}
		return flips;
	}

	/**
	 * Migrates raw legacy transaction JSON array into a v2 Transaction list.
	 * Explicitly derives the true flippedQuantity and isFlipped state from the migrated flips list.
	 */
	public static List<Transaction> migrateLegacyTransactions(JsonArray raw, List<Flip> flips, boolean isBuy)
	{
		List<Transaction> transactions = new ArrayList<>();
		for (JsonElement element : raw)
		{
			JsonObject obj = element.getAsJsonObject();
			if (obj.has("quantity"))
			{
				obj.addProperty("finQuantity", obj.get("quantity").getAsInt());
			}
			if (obj.has("totalQuantity"))
			{
				obj.addProperty("initQuantity", obj.get("totalQuantity").getAsInt());
			}
			Transaction transaction = Persistor.gson.fromJson(obj, Transaction.class);

			int flippedSum = 0;
			for (Flip flip : flips)
			{
				UUID linkedId = isBuy ? flip.getBuyId() : flip.getSellId();
				if (linkedId != null && linkedId.equals(transaction.getId()))
				{
					flippedSum += flip.getQuantity();
				}
			}
			transaction.setFlippedQuantity(flippedSum);
			boolean completedFull = transaction.getFinQuantity() == transaction.getInitQuantity();
			if (transaction.isBuy())
			{
				transaction.setCurrentState(completedFull ? GrandExchangeOfferState.BOUGHT : GrandExchangeOfferState.CANCELLED_BUY);
			}
			else
			{
				transaction.setCurrentState(completedFull ? GrandExchangeOfferState.SOLD : GrandExchangeOfferState.CANCELLED_SELL);
			}

			if (transaction.getCompletedTime() == null && transaction.isComplete())
			{
				transaction.setCompletedTime(transaction.getCreatedTime());
			}

			if (!transaction.isBuy())
			{
				transaction.setInitTaxPer(GrandExchange.calculateTaxPerItem(transaction.getItemId(), transaction.getInitPricePer(), transaction.getCreatedTime()));
				transaction.setInitTax(transaction.getInitTaxPer() * transaction.getInitQuantity());

				transaction.setFinTaxPer(GrandExchange.calculateTaxPerItem(transaction.getItemId(), transaction.getFinPricePer(), transaction.getCreatedTime()));
				transaction.setFinTax(transaction.getFinTaxPer() * transaction.getFinQuantity());
			}

			transaction.setInitTotal(((long) transaction.getInitPricePer() * transaction.getInitQuantity()) - transaction.getInitTax());
			transaction.setFinTotal(((long) transaction.getFinPricePer() * transaction.getFinQuantity()) - transaction.getFinTax());

			transactions.add(transaction);
		}

		Collections.reverse(transactions);

		return transactions;
	}

	/**
	 * Rebuilds the completed list of flips chronologically using a FIFO matching strategy.
	 */
	public static List<Flip> repairFlips(List<Transaction> allBuys, List<Transaction> allSells, List<Flip> oldFlips)
	{
		Map<String, List<Flip>> oldFlipsMap = new HashMap<>();
		for (Flip f : oldFlips)
		{
			String uniqueKey = f.getBuyId().toString() + "_" + f.getSellId().toString();
			oldFlipsMap.computeIfAbsent(uniqueKey, k -> new ArrayList<>()).add(f);
		}
		List<Flip> repairedFlips = new ArrayList<>();

		for (Transaction b : allBuys)
		{
			b.setFlippedQuantity(0);
		}
		for (Transaction s : allSells)
		{
			s.setFlippedQuantity(0);
		}

		List<Transaction> sortedSellsForMatching = new ArrayList<>(allSells);
		sortedSellsForMatching.sort((a, b) -> a.getCreatedTime().compareTo(b.getCreatedTime()));

		for (Transaction sell : sortedSellsForMatching)
		{
			if (sell.isBuy())
			{
				continue;
			}
			int remainingToFlip = sell.getFinQuantity();
			for (int i = 0; i < allBuys.size() && remainingToFlip > 0; i++)
			{
				Transaction buy = allBuys.get(i);
				String uniqueKey = buy.getId().toString() + "_" + sell.getId().toString();
				List<Flip> historicalMatches = oldFlipsMap.get(uniqueKey);
				boolean isHistoricalFlip = historicalMatches != null && !historicalMatches.isEmpty();

				if (isHistoricalFlip || GrandExchange.checkIsSellAFlipOfBuy(sell, buy))
				{
					int availableInBuy = buy.getFinQuantity() - buy.getFlippedQuantity();
					int amountToTake = Math.min(remainingToFlip, availableInBuy);

					if (amountToTake > 0)
					{
						Flip flip = new Flip(buy, sell, amountToTake);

						if (isHistoricalFlip)
						{
							Flip old = historicalMatches.remove(0);
							flip.setFlipId(old.getFlipId());

							long timeDifferenceSeconds = Math.abs(old.getCreatedAt().getEpochSecond() - sell.getCreatedTime().getEpochSecond());
							if (timeDifferenceSeconds <= 86400)
							{
								flip.setCreatedAt(old.getCreatedAt());
								flip.setUpdatedAt(old.getUpdatedAt());
							}
							else
							{
								flip.setCreatedAt(sell.getCreatedTime());
								flip.setUpdatedAt(sell.getCreatedTime());
							}
						}

						repairedFlips.add(0, flip);

						buy.setFlippedQuantity(buy.getFlippedQuantity() + amountToTake);
						sell.setFlippedQuantity(sell.getFlippedQuantity() + amountToTake);

						remainingToFlip -= amountToTake;
					}
				}
			}
		}
		return repairedFlips;
	}
}