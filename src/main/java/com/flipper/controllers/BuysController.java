//package com.flipper.controllers;
//
//import java.io.IOException;
//import java.util.ListIterator;
//
//import javax.swing.ImageIcon;
//import javax.swing.JButton;
//import javax.swing.SwingUtilities;
//
//import com.flipper.FlipperConfig;
//import com.flipper.helpers.GrandExchange;
//import com.flipper.helpers.Persistor;
//import com.flipper.helpers.UiUtilities;
//import com.flipper.models.Transaction;
//
//import net.runelite.client.game.ItemManager;
//import net.runelite.client.util.ImageUtil;
//import net.runelite.client.callback.ClientThread;
//import net.runelite.api.GrandExchangeOffer;
//
//public class BuysController extends TransactionController {
//
//    public BuysController(ItemManager itemManager, FlipperConfig config, ClientThread cThread) throws IOException {
//        super("Buy", itemManager, config.isPromptDeleteBuy(), cThread); // Pass parameters to super
//    }
//
//    @Override
//    public void loadTransactions() throws IOException {
//        this.transactions = Persistor.loadBuys();
//        this.filteredTransactions = this.transactions;
//        this.buildView(); // Rebuild view after loading
//    }
//
//    @Override
//    public void saveTransactions() {
//        Persistor.saveBuys(this.transactions);
//    }
//}