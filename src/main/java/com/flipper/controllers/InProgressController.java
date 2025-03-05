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
//import java.awt.BorderLayout;
//
//import javax.swing.SwingUtilities;
//
//import com.flipper.FlipperConfig;
//import com.flipper.helpers.GrandExchange;
//import com.flipper.helpers.Log;
//import com.flipper.helpers.Persistor;
//import com.flipper.helpers.UiUtilities;
//import com.flipper.models.Flip;
//import com.flipper.models.Transaction;
//import com.flipper.views.components.Pagination;
//import com.flipper.views.inprogress.InProgressPage;
//import com.flipper.views.inprogress.InProgressPanel;
//
//import lombok.Getter;
//import lombok.Setter;
//import net.runelite.api.ItemComposition;
//import net.runelite.client.callback.ClientThread;
//import net.runelite.client.game.ItemManager;
//import net.runelite.api.GrandExchangeOffer;
//import net.runelite.api.GrandExchangeOfferState;
//
//public class InProgressController {
//    @Getter
//    @Setter
//    private List<Transaction> inProgressTransactions = new ArrayList<Transaction>();
//    private List<Transaction> filteredInProgressTransactions = new ArrayList<Transaction>();
//    private InProgressPage inProgressPage;
//    private Consumer<UUID> removeInProgressTransactionConsumer;
//    private Pagination pagination;
//    private ItemManager itemManager;
//    private String searchText;
//    private Consumer<String> onSearchTextChangedCallback;
//    private ClientThread cThread;
//
//    public InProgressController(
//            ItemManager itemManager,
//            FlipperConfig config,
//            ClientThread cThread
//    ) throws IOException {
//        this.removeInProgressTransactionConsumer = id -> this.removeInProgressTransaction(id);
//        this.itemManager = itemManager;
//        this.onSearchTextChangedCallback = (searchText) -> this.onSearchTextChanged(searchText);
//        this.inProgressPage = new InProgressPage(this.onSearchTextChangedCallback);
//        this.cThread = cThread;
//
//        Consumer<Object> renderItemCallback = (Object transaction) -> {
//            InProgressPanel inProgressPanel = new InProgressPanel(
//                    (Transaction) transaction,
//                    itemManager,
//                    this.removeInProgressTransactionConsumer,
//                    config.isPromptDeleteMargin(), // Consider a separate config option
//                    cThread
//            );
//            this.inProgressPage.addInProgressPanel(inProgressPanel);
//        };
//        Runnable buildViewCallback = () -> this.buildView();
//        this.pagination = new Pagination(
//                renderItemCallback,
//                UiUtilities.ITEMS_PER_PAGE,
//                buildViewCallback
//        );
//        // this.loadInProgressTransactions(); // Removed load
//    }
//
//    public void onSearchTextChanged(String searchText) {
//        this.searchText = searchText;
//        this.pagination.resetPage();
//        this.buildView();
//    }
//
//    // No longer needed, as we remove only when the offer is EMPTY
//
//    public boolean removeInProgressTransaction(UUID transactionId) {
//        ListIterator<Transaction> inProgressTransactionsIterator = this.inProgressTransactions.listIterator();
//
//        while (inProgressTransactionsIterator.hasNext()) {
//            Transaction iterTransaction = inProgressTransactionsIterator.next();
//
//            if (iterTransaction.getId().equals(transactionId)) {
//                inProgressTransactionsIterator.remove();
//                this.buildView();
//                // this.saveInProgressTransactions(); // Removed Save
//                return true;
//            }
//        }
//
//        return false;
//    }
//
//
//    private boolean isRender(Transaction transaction) {
//        final ItemComposition[] itemComp = new ItemComposition[1];
//        cThread.invoke(() -> {
//            itemComp[0] = this.itemManager.getItemComposition(transaction.getItemId());
//        });
//        String itemName = itemComp[0].getName();
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
//    public InProgressPage getPage() {
//        return this.inProgressPage;
//    }
//
//    // private void loadInProgressTransactions() throws IOException { // Removed load
//    //     // Load *nothing* initially. The in-progress list starts empty.
//    //     this.inProgressTransactions = new ArrayList<>(); // Start with an empty list
//    //     this.filteredInProgressTransactions = new ArrayList<>(); // Start with empty filtered list
//    //     this.buildView();
//    // }
//
//    // public void saveInProgressTransactions() { // Removed Save
//
//    // }
//
//    public void filterList() {
//        if (this.searchText == "" || this.searchText == null) {
//            this.filteredInProgressTransactions = this.inProgressTransactions;
//        } else {
//            Iterator<Transaction> inProgressTransactionsIter = this.inProgressTransactions.iterator();
//            this.filteredInProgressTransactions = new ArrayList<Transaction>();
//            while (inProgressTransactionsIter.hasNext()) {
//                Transaction currentTransaction = inProgressTransactionsIter.next();
//                if (this.isRender(currentTransaction)) {
//                    filteredInProgressTransactions.add(currentTransaction);
//                }
//            }
//        }
//    }
//
//    public void buildView() {
//        SwingUtilities.invokeLater(() -> {
//            this.filterList();
//            this.inProgressPage.resetContainer();
//            this.inProgressPage.add(
//                    this.pagination.getComponent(this.filteredInProgressTransactions),
//                    BorderLayout.SOUTH
//            );
//            this.pagination.renderList(this.filteredInProgressTransactions);
//            this.inProgressPage.revalidate();
//        });
//    }
//
//    /**
//     * updates in progress offers. If the offer is now empty, the offer is removed
//     * otherwise, offer is either created or updated
//     *
//     * @param offer
//     * @param slot
//     */
//    public void updateOffer(GrandExchangeOffer offer, int slot) {
//        // Check to see if there is an existing transaction we can update
//        for (Transaction transaction : inProgressTransactions) {
//            // Update existing transaction if it matches
//            if (transaction.getSlot() == slot) {
//                transaction.updateTransaction(offer);
//                buildView(); // Update the view after modifying the transaction
//                return; // Exit after updating
//            }
//        }
//
//        // If we get here, it's a *new* in-progress transaction for this slot
//        Transaction newTransaction = GrandExchange.createTransactionFromOffer(offer, itemManager, slot);
//        inProgressTransactions.add(newTransaction);
//        buildView(); // Update the view after adding the transaction
//    }
//
//
//
//    /**
//     * Remove all offers from the in progress page
//     */
//    // public void removeEmptyOffers(int slot) { // Removed this method, it is not needed.
//    //     inProgressTransactions.removeIf(transaction -> transaction.getSlot() == slot);
//    //     //this.saveInProgressTransactions(); // We're not saving in-progress separately anymore
//    //     SwingUtilities.invokeLater(() -> this.buildView());
//    // }
//}