//package com.flipper.controllers;
//
//import java.io.IOException;
//import java.util.ListIterator;
//
//import com.flipper.FlipperConfig;
//import com.flipper.helpers.GrandExchange;
//import com.flipper.helpers.Persistor;
//import net.runelite.client.game.ItemManager;
//import net.runelite.client.callback.ClientThread;
//
//import net.runelite.api.GrandExchangeOffer;
//import javax.swing.SwingUtilities;
//
//import com.flipper.models.Transaction;
//
//public class SellsController extends TransactionController {
//
//    public SellsController(ItemManager itemManager, FlipperConfig config, ClientThread cThread) throws IOException {
//        super("Sell", itemManager, config.isPromptDeleteSell(), cThread); // Pass parameters to super
//    }
//
//    @Override
//    public void loadTransactions() throws IOException {
//        this.transactions = Persistor.loadSells();
//        this.filteredTransactions = this.transactions;
//        this.buildView(); // Rebuild view after loading
//    }
//
//    @Override
//    public void saveTransactions() {
//        Persistor.saveSells(this.transactions);
//    }
//}