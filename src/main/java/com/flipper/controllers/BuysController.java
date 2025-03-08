package com.flipper.controllers;

import java.io.IOException;
//import java.util.function.Consumer; // Removed

//import javax.swing.ImageIcon; // Removed
//import javax.swing.JButton; // Removed

import com.flipper.FlipperConfig;
import com.flipper.helpers.Persistor;
//import com.flipper.helpers.UiUtilities; // Removed
//import com.flipper.models.Transaction; // Removed

import net.runelite.client.callback.ClientThread; // Import ClientThread
import net.runelite.client.game.ItemManager;
//import net.runelite.client.util.ImageUtil; // Removed

public class BuysController extends TransactionController {
    // Removed highAlchCallback

    public BuysController(ItemManager itemManager, FlipperConfig config, ClientThread clientThread) throws IOException {
        super("Buy", itemManager, config.isPromptDeleteBuy(), clientThread); // Pass clientThread to super
    }

    // Removed extraComponentPressed and renderExtraComponent

    @Override
    public void loadTransactions() throws IOException {
        this.transactions = Persistor.loadBuys();
        this.filteredTransactions = this.transactions;
        this.buildView();
    }

    @Override
    public void saveTransactions() {
        Persistor.saveBuys(this.transactions);
    }
}