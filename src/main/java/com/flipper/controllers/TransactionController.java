//package com.flipper.controllers;
//
//import java.io.IOException;
//import java.util.ArrayList;
//import java.util.Iterator;
//import java.util.List;
//import java.util.ListIterator;
//import java.util.UUID;
//import java.util.function.Consumer;
//import java.awt.BorderLayout;
//
//import javax.swing.JButton;
//import javax.swing.SwingUtilities;
//
//import com.flipper.helpers.UiUtilities;
//import com.flipper.models.Transaction;
//import com.flipper.views.transactions.TransactionPanel;
//import com.google.common.base.Supplier;
//import com.flipper.views.transactions.TransactionPage;
//import com.flipper.views.components.Pagination;
//import com.flipper.helpers.Log;
//
//import lombok.Getter;
//import lombok.Setter;
//import net.runelite.client.game.ItemManager;
//import net.runelite.client.callback.ClientThread;
//
//public abstract class TransactionController { // Remains abstract
//    @Getter
//    @Setter
//    protected List<Transaction> transactions = new ArrayList<Transaction>();
//    protected List<Transaction> filteredTransactions = new ArrayList<Transaction>();
//    protected TransactionPage transactionPage;
//    protected ItemManager itemManager;
//    protected Pagination pagination;
//    protected Consumer<UUID> removeTransactionConsumer;
//    protected JButton extraComponent;
//    protected boolean isPrompt;
//    protected String searchText;
//    protected Consumer<String> onSearchTextChangedCallback;
//    protected ClientThread cThread; // Keep ClientThread here
//
//    // Constructor (no more controller dependencies)
//    public TransactionController(String name, ItemManager itemManager, boolean isPrompt, ClientThread cThread) throws IOException {
//        this.isPrompt = isPrompt;
//        this.itemManager = itemManager;
//        this.cThread = cThread; // Store ClientThread
//        this.removeTransactionConsumer = id -> this.removeTransaction(id);
//
//        // The rest of the constructor remains largely the same,
//        // but we're *not* injecting other controllers.
//        Supplier<JButton> renderExtraComponentSupplier = () -> {
//            return renderExtraComponent();
//        };
//        Consumer<Transaction> extraComponentPressedConsumer = (transaction) -> {
//            this.extraComponentPressed(transaction);
//        };
//        Consumer<Object> renderItemCallback = (Object sell) -> {
//            TransactionPanel transactionPanel = new TransactionPanel(
//                    name,
//                    (Transaction) sell,
//                    itemManager,
//                    this.removeTransactionConsumer,
//                    isPrompt,
//                    renderExtraComponentSupplier,
//                    extraComponentPressedConsumer,
//                    cThread // Pass cThread to TransactionPanel
//            );
//            this.transactionPage.addTransactionPanel(transactionPanel);
//        };
//        Runnable buildViewCallback = () -> this.buildView();
//        this.pagination = new Pagination(
//                renderItemCallback,
//                UiUtilities.ITEMS_PER_PAGE,
//                buildViewCallback
//        );
//        this.onSearchTextChangedCallback = (searchText) -> this.onSearchTextChanged(searchText);
//        this.transactionPage = new TransactionPage(this.onSearchTextChangedCallback);
//        this.loadTransactions(); // Load initial transactions
//    }
//
//    // Keep existing methods (addTransaction, removeTransaction, etc.)
//    public void onSearchTextChanged(String searchText) {
//        this.searchText = searchText;
//        this.pagination.resetPage();
//        this.buildView();
//    }
//
//    public void addTransaction(Transaction transaction) {
//        this.transactions.add(transaction);
//        this.buildView(); // Always rebuild the view after adding
//    }
//    public void removeTransaction(UUID id) {
//        ListIterator<Transaction> transactionIter = this.transactions.listIterator(this.transactions.size());
//        while (transactionIter.hasPrevious()) {
//            Transaction transaction = transactionIter.previous();
//            if (transaction.id.equals(id)) {
//                transactionIter.remove();
//                this.buildView(); // Always rebuild the view after removing
//                return;
//            }
//        }
//    }
//
//    public void extraComponentPressed(Transaction transaction) {};
//
//    public JButton renderExtraComponent() {
//        return null;
//    }
//
//    public abstract void loadTransactions() throws IOException; // Remains abstract
//
//    public abstract void saveTransactions(); // Remains abstract
//
//    public TransactionPage getPage() {
//        return this.transactionPage;
//    }
//
//    private boolean isRender(Transaction transaction) {
//        String itemName = transaction.getItemName();
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
//    public void filterList() {
//        if (this.searchText == "" || this.searchText == null) {
//            this.filteredTransactions = this.transactions;
//        } else {
//            // Create filtered list
//            Iterator<Transaction> transactionIter = this.transactions.iterator();
//            this.filteredTransactions = new ArrayList<Transaction>();
//            while (transactionIter.hasNext()) {
//                Transaction currentTransaction = transactionIter.next();
//                if (this.isRender(currentTransaction)) {
//                    filteredTransactions.add(currentTransaction);
//                }
//            }
//        }
//    }
//    public void buildView() {
//        SwingUtilities.invokeLater(() -> {
//            this.filterList();
//            this.transactionPage.resetContainer();
//            this.transactionPage.add(
//                    this.pagination.getComponent(this.filteredTransactions),
//                    BorderLayout.SOUTH
//            );
//            this.pagination.renderList(this.filteredTransactions);
//            this.transactionPage.revalidate();
//        });
//    }
//    // Remove updateTransaction (it's no longer needed in the base class)
//}