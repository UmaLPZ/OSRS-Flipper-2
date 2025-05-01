package com.flipper2.controllers;

import java.io.IOException;

import com.flipper2.FlipperConfig;
import com.flipper2.helpers.Persistor;

import net.runelite.client.callback.ClientThread;
import net.runelite.client.game.ItemManager;

public class BuysController extends TransactionController
{


	public BuysController(ItemManager itemManager, FlipperConfig config, ClientThread clientThread) throws IOException
	{
		super("Buy", itemManager, config.isPromptDeleteBuy(), clientThread);
	}


	@Override
	public void loadTransactions() throws IOException
	{
		this.transactions = Persistor.loadBuys();
		this.filteredTransactions = this.transactions;
		this.buildView();
	}

	@Override
	public void saveTransactions()
	{
		Persistor.saveBuys(this.transactions);
	}
}