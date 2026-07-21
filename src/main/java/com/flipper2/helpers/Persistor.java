package com.flipper2.helpers;

import com.flipper2.models.Flip;
import com.flipper2.models.Transaction;

import net.runelite.api.GrandExchangeOfferState;
import net.runelite.client.RuneLite;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Read/Writes information to json file for storage
 */
public class Persistor
{
	public static Gson gson;
	public static final File PARENT_DIRECTORY = new File(RuneLite.RUNELITE_DIR, "flipper2");
	public static File directory;
	public static final String SELLS_JSON_FILE = "flipper2-sells.json";
	public static final String BUYS_JSON_FILE = "flipper2-buys.json";
	public static final String FLIPS_JSON_FILE = "flipper2-flips.json";
	private static final int FILE_VERSION = 2;

	private static class ParsedFile
	{
		JsonArray data;
		boolean isLegacyFormat;
	}

	public static void setUp() throws IOException
	{
		directory = PARENT_DIRECTORY;
		createDirectory(PARENT_DIRECTORY);
		createRequiredFiles();
	}

	/**
	 * Creates the required json files
	 */
	private static void createRequiredFiles() throws IOException
	{
		generateFileIfDoesNotExist(SELLS_JSON_FILE);
		generateFileIfDoesNotExist(BUYS_JSON_FILE);
		generateFileIfDoesNotExist(FLIPS_JSON_FILE);
	}

	private static void generateFileIfDoesNotExist(String filename) throws IOException
	{
		File file = new File(directory, filename);
		if (!file.exists())
		{
			if (!file.createNewFile())
			{
				Log.info("Failed to generate file " + file.getPath());
			}
		}
	}

	private static void createDirectory(File directory) throws IOException
	{
		if (!directory.exists())
		{
			Log.info("Creating flipper2 directory");
			if (!directory.mkdir())
			{
				throw new IOException("unable to create parent directory!");
			}
		}
	}

	public static void saveJson(List<?> list, String filename) throws IOException
	{
		File file = new File(directory, filename);
		JsonObject root = new JsonObject();
		root.addProperty("version", FILE_VERSION);
		root.add("data", gson.toJsonTree(list));
		Files.write(file.toPath(), gson.toJson(root).getBytes());
	}

	private static String getFileContent(String filename) throws IOException
	{
		Path filePath = Paths.get(directory.getAbsolutePath(), filename);
		byte[] fileBytes = Files.readAllBytes(filePath);
		return new String(fileBytes);
	}

	private static ParsedFile readFile(String filename) throws IOException
	{
		ParsedFile result = new ParsedFile();
		String jsonString = getFileContent(filename);

		if (jsonString == null || jsonString.trim().isEmpty())
		{
			result.data = new JsonArray();
			result.isLegacyFormat = false;
			return result;
		}

		JsonElement root = gson.fromJson(jsonString, JsonElement.class);

		if (root == null || root.isJsonNull())
		{
			result.data = new JsonArray();
			result.isLegacyFormat = false;
		}
		else if (root.isJsonArray())
		{
			result.data = root.getAsJsonArray();
			result.isLegacyFormat = true;
		}
		else
		{
			JsonObject rootObj = root.getAsJsonObject();
			result.data = rootObj.has("data") ? rootObj.get("data").getAsJsonArray() : new JsonArray();
			result.isLegacyFormat = false;
		}

		return result;
	}

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

	public static boolean saveBuys(List<Transaction> buys)
	{
		try
		{
			saveJson(buys, BUYS_JSON_FILE);
			return true;
		}
		catch (Exception error)
		{
			Log.info("Failed to save buys " + error.toString());
			return false;
		}
	}

	public static boolean saveSells(List<Transaction> sells)
	{
		try
		{
			saveJson(sells, SELLS_JSON_FILE);
			return true;
		}
		catch (Exception error)
		{
			Log.info("Failed to save sells " + error.toString());
			return false;
		}
	}

	public static List<Transaction> loadBuys() throws IOException
	{
		return loadTransactions(BUYS_JSON_FILE);
	}

	public static List<Transaction> loadSells() throws IOException
	{
		return loadTransactions(SELLS_JSON_FILE);
	}

	private static List<Transaction> loadTransactions(String filename) throws IOException
	{
		ParsedFile parsed = readFile(filename);
		List<Transaction> transactions = new ArrayList<>();

		for (JsonElement element : parsed.data)
		{
			JsonObject obj = element.getAsJsonObject();

			if (parsed.isLegacyFormat)
			{
				if (obj.has("quantity"))
				{
					obj.addProperty("finQuantity", obj.get("quantity").getAsInt());
				}
				if (obj.has("totalQuantity"))
				{
					obj.addProperty("initQuantity", obj.get("totalQuantity").getAsInt());
				}
			}

			Transaction transaction = gson.fromJson(obj, Transaction.class);

			if (parsed.isLegacyFormat)
			{

				if (obj.has("isFlipped") && obj.get("isFlipped").getAsBoolean() && !obj.has("flippedQuantity"))
				{
					transaction.setFlippedQuantity(transaction.getFinQuantity());
				}

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
			}

			transactions.add(transaction);
		}

		if (parsed.isLegacyFormat)
		{
			Collections.reverse(transactions);
		}

		return transactions;
	}

	public static boolean saveFlips(List<Flip> flips)
	{
		try
		{
			saveJson(flips, FLIPS_JSON_FILE);
			return true;
		}
		catch (IOException e)
		{
			Log.info("Failed to save flips: " + e.getMessage());
			return false;
		}
	}

	public static List<Flip> loadFlips() throws IOException
	{
		ParsedFile parsed = readFile(FLIPS_JSON_FILE);
		List<Flip> flips = new ArrayList<>();

		for (JsonElement element : parsed.data)
		{
			JsonObject obj = element.getAsJsonObject();
			Flip flip;

			if (parsed.isLegacyFormat)
			{
				Instant legacyCreatedAt = extractLegacyInstant(obj, "createdAt");
				Instant legacyUpdatedAt = extractLegacyInstant(obj, "updatedAt");

				flip = gson.fromJson(obj, Flip.class);

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
			}
			else
			{
				flip = gson.fromJson(obj, Flip.class);

				if (flip.getCreatedAt() == null)
				{
					flip.setCreatedAt(Instant.now());
				}
				if (flip.getUpdatedAt() == null)
				{
					flip.setUpdatedAt(flip.getCreatedAt());
				}
			}

			flips.add(flip);
		}

		return flips;
	}
}