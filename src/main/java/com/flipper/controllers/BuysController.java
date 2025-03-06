//package com.flipper.controllers;
//
//import java.io.IOException;
//// import java.util.function.Consumer; // Removed unused import
//
//// import javax.swing.ImageIcon; // Removed unused import
//// import javax.swing.JButton; // Removed unused import
//
//import com.flipper.FlipperConfig;
//import com.flipper.helpers.Persistor;
//// import com.flipper.helpers.UiUtilities; // Removed unused import
//import com.flipper.models.Transaction;
//
//import net.runelite.client.game.ItemManager;
//// import net.runelite.client.util.ImageUtil; // Removed unused import
//import net.runelite.client.callback.ClientThread; // Import ClientThread
//
//
//public class BuysController extends TransactionController {
//    // Removed highAlchCallback
//
//    public BuysController(ItemManager itemManager, FlipperConfig config, ClientThread clientThread) throws IOException { // Added ClientThread
//        super(itemManager, config.isPromptDeleteBuy(), clientThread); // Removed "Buy" string, added clientThread
//    }
//
//
//    @Override
//    public void loadTransactions() throws IOException {
//        this.transactions = Persistor.loadBuys();
//        this.filteredTransactions = this.transactions;
//        this.buildView();
//    }
//
//    @Override
//    public void saveTransactions() {
//        Persistor.saveBuys(this.transactions);
//    }
//}