//// FlipsController.java
//package com.flipper.controllers;
//
//import java.io.IOException;
//import java.util.ArrayList;
//import java.util.Iterator;
//import java.util.List;
//import java.util.ListIterator;
//import java.util.UUID;
//import java.util.function.Consumer;
//
//import javax.swing.SwingUtilities;
//
//import com.flipper.helpers.GrandExchange;
//import com.flipper.helpers.Log;
//import com.flipper.helpers.Persistor;
//import com.flipper.helpers.UiUtilities;
//import com.flipper.models.Flip;
//import com.flipper.models.Transaction;
//import com.flipper.views.components.Pagination;
//import com.flipper.views.flips.FlipPage;
//import com.flipper.views.flips.FlipPanel;
//import com.flipper.FlipperConfig;
//
//import lombok.Getter;
//import lombok.Setter;
//
//import java.awt.BorderLayout;
//
//import net.runelite.api.ItemComposition;
//import net.runelite.client.callback.ClientThread;
//import net.runelite.client.game.ItemManager;
//
//public class FlipsController {
//    @Getter
//    @Setter
//    private List<Flip> flips = new ArrayList<Flip>();
//    private List<Flip> filteredFlips = new ArrayList<Flip>();
//    private FlipPage flipPage;
//    private Consumer<UUID> removeFlipConsumer;
//    private Runnable refreshFlipsRunnable;
//    private String totalProfit = "0";
//    private Pagination pagination;
//    private String searchText;
//    private ItemManager itemManager;
//    private Consumer<String> onSearchTextChangedCallback;
//    private boolean isTrackingFlips = true;
//    private ClientThread cThread;
//
//    public FlipsController(ItemManager itemManager, FlipperConfig config, ClientThread cThread) throws IOException {
//        this.flips = new ArrayList<Flip>();
//        this.removeFlipConsumer = id -> this.removeFlip(id);
//        this.refreshFlipsRunnable = () -> this.loadFlips();
//        this.itemManager = itemManager;
//        this.onSearchTextChangedCallback = (searchText) -> this.onSearchTextChanged(searchText);
//        Runnable toggleIsTrackingFlipsRunnable = () -> {
//            this.isTrackingFlips = !this.isTrackingFlips;
//        };
//        this.flipPage = new FlipPage(
//                refreshFlipsRunnable,
//                this.onSearchTextChangedCallback,
//                toggleIsTrackingFlipsRunnable,
//                this.isTrackingFlips
//        );
//        this.cThread = cThread;
//        Consumer<Object> renderItemCallback = (Object flip) -> {
//            FlipPanel flipPanel = new FlipPanel(
//                    (Flip) flip,
//                    itemManager,
//                    this.removeFlipConsumer,
//                    config.isPromptDeleteBuy()
//            );
//            this.flipPage.addFlipPanel(flipPanel);
//        };
//        Runnable buildViewCallback = () -> this.buildView();
//
//        this.pagination = new Pagination(renderItemCallback, UiUtilities.ITEMS_PER_PAGE, buildViewCallback);
//        this.loadFlips(); // Load flips from local storage
//    }
//
//    public void onSearchTextChanged(String searchText) {
//        this.searchText = searchText;
//        this.pagination.resetPage();
//        this.buildView();
//    }
//
//    public void addFlip(Flip flip) {
//        if (this.isTrackingFlips) {
//            this.flips.add(0, flip);
//            this.totalProfit = calculateTotalProfit(flips); // Recalculate total profit
//            Persistor.saveFlips(this.flips); // Save to local storage
//        }
//    }
//
//    public void removeFlip(UUID flipId) {
//        Iterator<Flip> flipsIter = this.flips.iterator();
//        while (flipsIter.hasNext()) {
//            Flip flip = flipsIter.next();
//            if (flip.getId().equals(flipId)) {
//                flipsIter.remove();
//                this.totalProfit = calculateTotalProfit(flips); // Recalculate
//                Persistor.saveFlips(this.flips); // Save to local storage
//                this.buildView();
//                return; // Exit after removing
//            }
//        }
//    }
//
//    public FlipPage getPage() {
//        return this.flipPage;
//    }
//
//    public void loadFlips() {
//        // Load flips from local storage
//        try {
//            this.flips = Persistor.loadFlips();
//            this.totalProfit = calculateTotalProfit(flips);
//            this.filteredFlips = this.flips;// Initialize filtered list only once
//            getFlipNamesAndBuild();
//        } catch (IOException e) {
//            Log.info("Failed to load flips from file");
//            this.flips = new ArrayList<>(); // Initialize as empty if load fails
//            this.filteredFlips = new ArrayList<>();
//        }
//    }
//    public void getFlipNamesAndBuild(){
//        cThread.invoke(() -> {
//            for (Flip flip : flips) {
//                if (flip.getItemName() == null) {
//                    flip.setItemName(itemManager.getItemComposition(flip.getItemId()).getName());
//                }
//            }
//            this.buildView();
//        });
//    }
//
//    private String calculateTotalProfit(List<Flip> flips) {
//        int totalProfit = 0;
//        for (Flip flip : flips) {
//            totalProfit += flip.getTotalProfit();
//        }
//        return String.valueOf(totalProfit);
//    }
//
//    private void updateFlip(Transaction sell, Transaction buy, Flip flip) {
//        flip.sellPrice = sell.getPricePer();
//        flip.buyPrice = buy.getPricePer();
//        flip.quantity = sell.getQuantity();
//        flip.itemId = sell.getItemId();
//        flip.setItemName(sell.getItemName());
//        this.totalProfit = calculateTotalProfit(flips); // Recalculate total
//        Persistor.saveFlips(this.flips); // Save to local storage
//        this.buildView();
//    }
//
//    private boolean isRender(Flip flip) {
//        cThread.invoke(() -> {
//            ItemComposition itemComp = this.itemManager.getItemComposition(flip.getItemId());
//            flip.setItemName(itemComp.getName());
//        });
//        String itemName = flip.getItemName();
//
//        if (
//                this.searchText != null &&
//                        itemName.toLowerCase().contains(this.searchText.toLowerCase())
//        ) {
//            return true;
//        } else if (this.searchText != null && this.searchText != "") {
//            return false;
//        }
//
//        return true;
//    }
//
//    /**
//     * Potentially creates a flip if the sell is complete and has a corresponding
//     * buy
//     *
//     * @param sell
//     * @param buys
//     */
//    public Flip upsertFlip(Transaction sell, List<Transaction> buys) {
//        ListIterator<Transaction> buysIterator = buys.listIterator(buys.size());
//        // If sell has already been flipped, look for it's corresponding buy and update
//        // the flip
//        if (sell.isFlipped()) {
//            ListIterator<Flip> flipsIterator = flips.listIterator();
//            while (flipsIterator.hasNext()) {
//                Flip flip = flipsIterator.next();
//                if (flip.getSellId().equals(sell.id)) {
//                    // Now find the corresponding buy
//                    while (buysIterator.hasPrevious()) {
//                        Transaction buy = buysIterator.previous();
//                        if (buy.id.equals(flip.getBuyId())) {
//                            updateFlip(sell, buy, flip);
//                            flipsIterator.set(flip);
//                            return flip;
//                        }
//                    }
//                }
//            }
//        } else {
//            // Attempt to match sell to a buy
//            if (this.isTrackingFlips) {
//                while (buysIterator.hasPrevious()) {
//                    Transaction buy = buysIterator.previous();
//                    if (GrandExchange.checkIsSellAFlipOfBuy(sell, buy)) {
//                        Flip flip = new Flip(buy, sell);
//                        if (!flip.isMarginCheck()) {
//                            this.addFlip(flip);
//                            buy.setIsFlipped(true);
//                            sell.setIsFlipped(true);
//                        }
//                        return flip;
//                    }
//                }
//            }
//        }
//
//        return null;
//    }
//
//    public void filterList() {
//        if (this.searchText == "" || this.searchText == null) {
//            this.filteredFlips = this.flips;
//        } else {
//            // Create filtered list
//            Iterator<Flip> flipsIter = this.flips.iterator();
//            this.filteredFlips = new ArrayList<Flip>();
//            while (flipsIter.hasNext()) {
//                Flip currentFlip = flipsIter.next();
//                if (this.isRender(currentFlip)) {
//                    filteredFlips.add(currentFlip);
//                }
//            }
//        }
//    }
//
//    public void buildView() {
//        SwingUtilities.invokeLater(() -> {
//            this.filterList();
//            this.flipPage.resetContainer(isTrackingFlips);
//            this.flipPage.add(
//                    this.pagination.getComponent(this.filteredFlips),
//                    BorderLayout.SOUTH
//            );
//            this.pagination.renderFromBeginning(
//                    this.filteredFlips
//            );
//            this.flipPage.setTotalProfit(totalProfit);
//            this.flipPage.revalidate();
//        });
//    }
//
//    public void saveTransactions() {
//        Persistor.saveFlips(this.flips);
//    }
//}