package com.flipper2.helpers;

import com.flipper2.models.Flip;
import com.flipper2.models.Transaction;

import net.runelite.client.RuneLite;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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

	public static void setUp(String directoryPath) throws IOException
	{
		directory = new File(directoryPath);
		createDirectory(directory);
		createRequiredFiles();
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
		final String json = gson.toJson(list);
		Files.write(file.toPath(), json.getBytes());
	}

	private static String getFileContent(String filename) throws IOException
	{
		Path filePath = Paths.get(directory.getAbsolutePath(), filename);
		byte[] fileBytes = Files.readAllBytes(filePath);
		return new String(fileBytes);
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
		String jsonString = getFileContent(BUYS_JSON_FILE);
		Type type = new TypeToken<List<Transaction>>()
		{
		}.getType();
		List<Transaction> buys = gson.fromJson(jsonString, type);
		if (buys == null)
		{
			return new ArrayList<Transaction>();
		}
		return buys;
	}

	public static List<Transaction> loadSells() throws IOException
	{
		String jsonString = getFileContent(SELLS_JSON_FILE);
		Type type = new TypeToken<List<Transaction>>()
		{
		}.getType();
		List<Transaction> sells = gson.fromJson(jsonString, type);
		if (sells == null)
		{
			return new ArrayList<Transaction>();
		}
		return sells;
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
		String jsonString = getFileContent(FLIPS_JSON_FILE);
		Type type = new TypeToken<List<Flip>>()
		{
		}.getType();
		List<Flip> flips = gson.fromJson(jsonString, type);
		if (flips == null)
		{
			return new ArrayList<>();
		}
		return flips;
	}
}