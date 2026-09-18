package com.flipper2.helpers;

import com.flipper2.models.Flip;
import com.flipper2.models.Transaction;

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

		if (parsed.isLegacyFormat)
		{
			List<Flip> flips = loadFlips();
			boolean isBuy = filename.equals(BUYS_JSON_FILE);

			return DataMigrator.migrateLegacyTransactions(parsed.data, flips, isBuy);
		}

		List<Transaction> transactions = new ArrayList<>();
		for (JsonElement element : parsed.data)
		{
			JsonObject obj = element.getAsJsonObject();
			Transaction transaction = gson.fromJson(obj, Transaction.class);
			transactions.add(transaction);
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

		if (parsed.isLegacyFormat)
		{
			return DataMigrator.migrateLegacyFlips(parsed.data);
		}

		List<Flip> flips = new ArrayList<>();
		for (JsonElement element : parsed.data)
		{
			JsonObject obj = element.getAsJsonObject();
			Flip flip = gson.fromJson(obj, Flip.class);

			if (flip.getCreatedAt() == null)
			{
				flip.setCreatedAt(Instant.now());
			}
			if (flip.getUpdatedAt() == null)
			{
				flip.setUpdatedAt(flip.getCreatedAt());
			}
			flips.add(flip);
		}
		return flips;
	}
}