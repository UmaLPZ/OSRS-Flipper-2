package com.flipper.controller;

import java.io.IOException;
import java.util.List;
import java.util.ListIterator;

import com.flipper.model.Transaction;
import com.flipper.view.buys.BuysPanel;
import com.flipper.helpers.GrandExchange;
import com.flipper.helpers.TradePersister;

import lombok.Getter;
import lombok.Setter;
import net.runelite.api.GrandExchangeOffer;
import net.runelite.client.game.ItemManager;

/**
 * @todo weird edge cases where user logs in with complete GE trnasaction. Duplicates buy
 * (i.e. 6800 cannonballs duplicated)
 */
public class BuysController {
    @Getter
    @Setter
    private List<Transaction> buys;
    private BuysPanel buysPanel;
    private ItemManager itemManager;

    public BuysController(ItemManager itemManager) throws IOException {
        this.itemManager = itemManager;
        this.buysPanel = new BuysPanel(itemManager);
        this.loadBuys();
    }

    public void addBuy(Transaction buy) {
        this.buys.add(buy);
        this.buysPanel.rebuildPanel(buys);
    }

    public BuysPanel getPanel() {
        return this.buysPanel;
    }

    public void loadBuys() throws IOException {
        this.buys = TradePersister.loadBuys();
        this.buysPanel.rebuildPanel(this.buys);
    }

    public void saveBuys() {
        TradePersister.saveBuys(buys);
    }

    public void createBuy(GrandExchangeOffer offer) {
        // Check to see if there is an incomplete buy transaction we can full fill
        ListIterator<Transaction> buysIterator = buys.listIterator(buys.size());
        while (buysIterator.hasPrevious()) {
            Transaction buy = buysIterator.previous();
            // Incomplete buy transaction found, update it
            if (GrandExchange.checkIsOfferPartOfTransaction(buy, offer)) {
                buysIterator.set(buy.updateTransaction(offer));
                this.buysPanel.rebuildPanel(buys);
                return;
            }
        }

        // It's a new buy, create one
        Transaction newBuy = GrandExchange.createTransactionFromOffer(offer, itemManager);
        this.addBuy(newBuy);
    }
}