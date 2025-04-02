package com.flipper2.controllers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.UUID;
import java.util.function.Consumer;

import javax.swing.SwingUtilities;

import com.flipper2.helpers.GrandExchange;
import com.flipper2.helpers.Log;
import com.flipper2.helpers.Persistor;
import com.flipper2.helpers.UiUtilities;
import com.flipper2.models.Flip;
import com.flipper2.models.Transaction;
import com.flipper2.views.components.Pagination;
import com.flipper2.views.flips.FlipPage;
import com.flipper2.views.flips.FlipPanel;
import com.flipper2.FlipperConfig;

import lombok.Getter;
import lombok.Setter;

import java.awt.BorderLayout;

import net.runelite.api.ItemComposition;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.game.ItemManager;

public class FlipsController {
    @Getter
    @Setter
    private List<Flip> flips = new ArrayList<Flip>();
    private List<Flip> filteredFlips = new ArrayList<Flip>();
    private FlipPage flipPage;
    private Consumer<UUID> removeFlipConsumer;
    private Runnable refreshFlipsRunnable;
    private String totalProfit = "0";
    private Pagination pagination;
    private String searchText;
    private ItemManager itemManager;
    private Consumer<String> onSearchTextChangedCallback;
    private boolean isTrackingFlips = true;
    private ClientThread cThread;

    public FlipsController(ItemManager itemManager, FlipperConfig config, ClientThread cThread) throws IOException {
        this.itemManager = itemManager;
        this.cThread = cThread;
        this.removeFlipConsumer = id -> this.removeFlip(id);
        this.refreshFlipsRunnable = () -> this.loadFlips();
        this.onSearchTextChangedCallback = (searchText) -> this.onSearchTextChanged(searchText);


        this.flipPage = new FlipPage(
                refreshFlipsRunnable,
                this.onSearchTextChangedCallback,
                this::toggleIsTrackingFlips,
                this.isTrackingFlips
        );


        Consumer<Object> renderItemCallback = (Object flip) -> {
            FlipPanel flipPanel = new FlipPanel(
                    (Flip) flip,
                    itemManager,
                    this.removeFlipConsumer,
                    config.isPromptDeleteFlip()
            );
            this.flipPage.addFlipPanel(flipPanel);
        };

        Runnable buildViewCallback = () -> this.buildView();

        this.pagination = new Pagination(renderItemCallback, UiUtilities.ITEMS_PER_PAGE, buildViewCallback);
        this.loadFlips();
    }

    private void toggleIsTrackingFlips() {
        this.isTrackingFlips = !this.isTrackingFlips;
    }

    public void onSearchTextChanged(String searchText) {
        this.searchText = searchText;
        this.pagination.resetPage();
        this.buildView();
    }

    public void addFlip(Flip flip) {
        if (this.isTrackingFlips) {
            this.flips.add(0, flip);
            this.totalProfit = calculateTotalProfit(flips);
            Persistor.saveFlips(this.flips);
            getFlipNamesAndBuild();
        }
    }


    public void removeFlip(UUID flipId) {
        Iterator<Flip> flipsIter = this.flips.iterator();
        while (flipsIter.hasNext()) {
            Flip flip = flipsIter.next();
            if (flip.getFlipId().equals(flipId)) {
                flipsIter.remove();
                this.totalProfit = calculateTotalProfit(flips);
                Persistor.saveFlips(this.flips);
                this.buildView();
                return;
            }
        }
    }

    public FlipPage getPage() {
        return this.flipPage;
    }

    public void loadFlips() {
        try {
            this.flips = Persistor.loadFlips();
            this.totalProfit = calculateTotalProfit(flips);
            this.filteredFlips = new ArrayList<>(this.flips);
            getFlipNamesAndBuild();
        } catch (IOException e) {
            Log.info("Failed to load flips from file: " + e.getMessage());
            this.flips = new ArrayList<>();
            this.filteredFlips = new ArrayList<>();
        }
    }
    public void getFlipNamesAndBuild(){
        cThread.invoke(() -> {
            for (Flip flip : flips) {
                if (flip.getItemName() == null) {
                    flip.setItemName(itemManager.getItemComposition(flip.getItemId()).getName());
                }
            }
            this.buildView();
        });
    }


    private String calculateTotalProfit(List<Flip> flipList) {
        int total = 0;
        for (Flip flip : flipList) {
            total += flip.getTotalProfit();
        }
        return String.valueOf(total);
    }

    private void updateFlip(Transaction sell, Transaction buy, Flip flip) {
        flip.setSellPrice(sell.getFinPricePer());
        flip.setBuyPrice(buy.getFinPricePer());
        flip.setQuantity(sell.getQuantity());
        flip.setItemId(sell.getItemId());
        flip.setItemName(sell.getItemName());
        this.totalProfit = calculateTotalProfit(flips);
        Persistor.saveFlips(this.flips);
        getFlipNamesAndBuild();
    }

    private boolean isRender(Flip flip) {

        if (flip.getItemName() == null) {
            ItemComposition itemComp = itemManager.getItemComposition(flip.getItemId());
            flip.setItemName(itemComp.getName());
        }
        String itemName = flip.getItemName();

        if (this.searchText != null &&
                itemName.toLowerCase().contains(this.searchText.toLowerCase())) {
            return true;
        } else if (this.searchText != null && !this.searchText.isEmpty()) {
            return false;
        }

        return true;
    }

    /**
     * Potentially creates a flip if the sell is complete and has a corresponding
     * buy. This logic remains largely the same, but it now interacts with the local
     * lists instead of the API.
     */
    public Flip upsertFlip(Transaction sell, List<Transaction> buys) {
        ListIterator<Transaction> buysIterator = buys.listIterator(buys.size());


        if (sell.isFlipped()) {
            ListIterator<Flip> flipsIterator = flips.listIterator();
            while (flipsIterator.hasNext()) {
                Flip flip = flipsIterator.next();
                if (flip.getSellId().equals(sell.getId())) {

                    while (buysIterator.hasPrevious()) {
                        Transaction buy = buysIterator.previous();
                        if (buy.getId().equals(flip.getBuyId())) {
                            updateFlip(sell, buy, flip);
                            return flip;
                        }
                    }
                }
            }
        } else {

            if (this.isTrackingFlips) {
                while (buysIterator.hasPrevious()) {
                    Transaction buy = buysIterator.previous();
                    if (GrandExchange.checkIsSellAFlipOfBuy(sell, buy)) {
                        Flip flip = new Flip(buy, sell);
                        if (!flip.isMarginCheck()) {
                            this.addFlip(flip);
                            buy.setIsFlipped(true);
                            sell.setIsFlipped(true);
                        }
                        return flip;
                    }
                }
            }
        }

        return null;
    }

    public void filterList() {
        if (this.searchText == null || this.searchText.isEmpty()) {
            this.filteredFlips = new ArrayList<>(this.flips);
        } else {

            this.filteredFlips = new ArrayList<>();
            for (Flip flip : this.flips) {
                if (isRender(flip)) {
                    filteredFlips.add(flip);
                }
            }
        }
    }

    public void buildView() {

        SwingUtilities.invokeLater(() -> {
            this.filterList();
            this.flipPage.resetContainer(isTrackingFlips);
            this.flipPage.add(
                    this.pagination.getComponent(this.filteredFlips),
                    BorderLayout.SOUTH
            );
            this.pagination.renderFromBeginning(this.filteredFlips);
            this.flipPage.setTotalProfit(totalProfit);
            this.flipPage.revalidate();
            this.flipPage.repaint();
        });
    }

    public void saveTransactions() {
        Persistor.saveFlips(this.flips);
    }
}