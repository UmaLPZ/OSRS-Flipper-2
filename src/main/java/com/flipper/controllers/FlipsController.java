package com.flipper.controllers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.UUID;
import java.util.function.Consumer;

import javax.swing.SwingUtilities;

import com.flipper.helpers.GrandExchange;
import com.flipper.helpers.Log;
import com.flipper.helpers.Persistor;
import com.flipper.helpers.UiUtilities;
import com.flipper.models.Flip;
import com.flipper.models.Transaction;
import com.flipper.views.components.Pagination;
import com.flipper.views.flips.FlipPage;
import com.flipper.views.flips.FlipPanel;
import com.flipper.FlipperConfig;

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
    private String totalProfit = "0"; // Keep totalProfit as String
    private Pagination pagination;
    private String searchText;
    private ItemManager itemManager;
    private Consumer<String> onSearchTextChangedCallback;
    private boolean isTrackingFlips = true; // Keep this for potential toggling later
    private ClientThread cThread;

    public FlipsController(ItemManager itemManager, FlipperConfig config, ClientThread cThread) throws IOException {
        this.itemManager = itemManager;
        this.cThread = cThread;
        this.removeFlipConsumer = id -> this.removeFlip(id);
        this.refreshFlipsRunnable = () -> this.loadFlips();
        this.onSearchTextChangedCallback = (searchText) -> this.onSearchTextChanged(searchText);

        // Initialize FlipPage, but we won't populate it until loadFlips()
        this.flipPage = new FlipPage(
                refreshFlipsRunnable,
                this.onSearchTextChangedCallback,
                this::toggleIsTrackingFlips, // Use method reference
                this.isTrackingFlips
        );

        // Consumer for rendering individual FlipPanels (used by Pagination)
        Consumer<Object> renderItemCallback = (Object flip) -> {
            FlipPanel flipPanel = new FlipPanel(
                    (Flip) flip,
                    itemManager,
                    this.removeFlipConsumer,
                    config.isPromptDeleteFlip() // Use the correct config option
            );
            this.flipPage.addFlipPanel(flipPanel);
        };

        Runnable buildViewCallback = () -> this.buildView();

        this.pagination = new Pagination(renderItemCallback, UiUtilities.ITEMS_PER_PAGE, buildViewCallback);
        this.loadFlips(); // Load flips from local storage
    }

    private void toggleIsTrackingFlips() {
        this.isTrackingFlips = !this.isTrackingFlips;
    }

    public void onSearchTextChanged(String searchText) {
        this.searchText = searchText;
        this.pagination.resetPage();
        this.buildView(); // Rebuild the view when search text changes
    }

    public void addFlip(Flip flip) {
        if (this.isTrackingFlips) {
            this.flips.add(0, flip); // Add to the *beginning* of the list
            this.totalProfit = calculateTotalProfit(flips); // Recalculate
            Persistor.saveFlips(this.flips); // Save to disk
            getFlipNamesAndBuild(); // Update the names and the view.
        }
    }


    public void removeFlip(UUID flipId) {
        Iterator<Flip> flipsIter = this.flips.iterator();
        while (flipsIter.hasNext()) {
            Flip flip = flipsIter.next();
            if (flip.getFlipId().equals(flipId)) { // Changed from getId()
                flipsIter.remove();
                this.totalProfit = calculateTotalProfit(flips); // Recalculate
                Persistor.saveFlips(this.flips); // Save to disk
                this.buildView();
                return; // Exit after removing (important for iterator safety)
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
            this.filteredFlips = new ArrayList<>(this.flips); // Initialize filtered list
            getFlipNamesAndBuild(); // Populate item names and build initial view
        } catch (IOException e) {
            Log.info("Failed to load flips from file: " + e.getMessage());
            this.flips = new ArrayList<>(); // Initialize as empty list on error
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

    // Helper method to calculate total profit (DRY principle)
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
        flip.setItemName(sell.getItemName()); // Update the item name
        this.totalProfit = calculateTotalProfit(flips); // Recalculate
        Persistor.saveFlips(this.flips); // Save changes
        getFlipNamesAndBuild(); // Refresh list and item names
    }

    private boolean isRender(Flip flip) {
        // Ensure item name is loaded.
        if (flip.getItemName() == null) {
            ItemComposition itemComp = itemManager.getItemComposition(flip.getItemId());
            flip.setItemName(itemComp.getName());
        }
        String itemName = flip.getItemName();

        if (this.searchText != null &&
                itemName.toLowerCase().contains(this.searchText.toLowerCase())) {
            return true;
        } else if (this.searchText != null && !this.searchText.isEmpty()) { // Use !isEmpty()
            return false;
        }

        return true; // Default to rendering if no search text
    }

    /**
     * Potentially creates a flip if the sell is complete and has a corresponding
     * buy. This logic remains largely the same, but it now interacts with the local
     * lists instead of the API.
     */
    public Flip upsertFlip(Transaction sell, List<Transaction> buys) {
        ListIterator<Transaction> buysIterator = buys.listIterator(buys.size());

        // If sell has already been flipped, look for its corresponding buy and update the flip
        if (sell.isFlipped()) {
            ListIterator<Flip> flipsIterator = flips.listIterator();
            while (flipsIterator.hasNext()) {
                Flip flip = flipsIterator.next();
                if (flip.getSellId().equals(sell.getId())) {
                    // Now find the corresponding buy
                    while (buysIterator.hasPrevious()) {
                        Transaction buy = buysIterator.previous();
                        if (buy.getId().equals(flip.getBuyId())) {
                            updateFlip(sell, buy, flip);
                            return flip; // Return the updated flip
                        }
                    }
                }
            }
        } else {
            // Attempt to match sell to a buy
            if (this.isTrackingFlips) { // Check if tracking is enabled
                while (buysIterator.hasPrevious()) {
                    Transaction buy = buysIterator.previous();
                    if (GrandExchange.checkIsSellAFlipOfBuy(sell, buy)) {
                        Flip flip = new Flip(buy, sell);
                        if (!flip.isMarginCheck()) {
                            this.addFlip(flip);
                            buy.setIsFlipped(true);
                            sell.setIsFlipped(true);
                        }
                        return flip; // Return the new flip
                    }
                }
            }
        }

        return null; // No flip created/updated
    }

    public void filterList() {
        if (this.searchText == null || this.searchText.isEmpty()) { // Use isEmpty()
            this.filteredFlips = new ArrayList<>(this.flips); // Reset to full list
        } else {
            // Create filtered list (more efficient to create a new list)
            this.filteredFlips = new ArrayList<>();
            for (Flip flip : this.flips) {
                if (isRender(flip)) {
                    filteredFlips.add(flip);
                }
            }
        }
    }

    public void buildView() {
        // Always use SwingUtilities.invokeLater for UI updates
        SwingUtilities.invokeLater(() -> {
            this.filterList(); // Filter based on search text
            this.flipPage.resetContainer(isTrackingFlips); // Reset the container
            this.flipPage.add(
                    this.pagination.getComponent(this.filteredFlips),
                    BorderLayout.SOUTH
            );
            this.pagination.renderFromBeginning(this.filteredFlips); // Render the flips
            this.flipPage.setTotalProfit(totalProfit); // Update total profit display
            this.flipPage.revalidate();
            this.flipPage.repaint();
        });
    }

    public void saveTransactions() {
        Persistor.saveFlips(this.flips);
    }
}