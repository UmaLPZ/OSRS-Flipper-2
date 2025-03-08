package com.flipper.controllers;

import java.io.IOException;

import com.flipper.FlipperConfig;
import com.flipper.helpers.Persistor;
import net.runelite.client.game.ItemManager;
import net.runelite.client.callback.ClientThread; // Import ClientThread

public class SellsController extends TransactionController {
    public SellsController(ItemManager itemManager, FlipperConfig config, ClientThread clientThread) throws IOException {
        super("Sell", itemManager, config.isPromptDeleteSell(), clientThread); // Pass clientThread
    }

    @Override
    public void loadTransactions() throws IOException {
        this.transactions = Persistor.loadSells();
        this.filteredTransactions = this.transactions;
        this.buildView();
    }

    @Override
    public void saveTransactions() {
        Persistor.saveSells(this.transactions);
    }
}