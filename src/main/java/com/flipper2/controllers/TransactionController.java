
package com.flipper2.controllers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.UUID;
import java.util.function.Consumer;
import java.awt.BorderLayout;

import javax.swing.JButton;
import javax.swing.SwingUtilities;

import com.flipper2.helpers.GrandExchange;
import com.flipper2.helpers.UiUtilities;
import com.flipper2.models.Transaction;
import com.flipper2.views.transactions.TransactionPanel;
import com.google.common.base.Supplier;
import com.flipper2.views.transactions.TransactionPage;
import com.flipper2.views.components.Pagination;

import lombok.Getter;
import lombok.Setter;
import net.runelite.api.GrandExchangeOffer;
import net.runelite.client.game.ItemManager;
import net.runelite.client.callback.ClientThread;

public class TransactionController {
    @Getter
    @Setter
    protected List<Transaction> transactions = new ArrayList<Transaction>();
    protected List<Transaction> filteredTransactions = new ArrayList<Transaction>();
    protected TransactionPage transactionPage;
    protected ItemManager itemManager;
    protected Pagination pagination;
    protected Consumer<UUID> removeTransactionConsumer;
    protected JButton extraComponent;
    protected boolean isPrompt;
    protected String searchText;
    protected Consumer<String> onSearchTextChangedCallback;
    protected ClientThread clientThread;

    public TransactionController(String name, ItemManager itemManager, boolean isPrompt, ClientThread clientThread) throws IOException {
        this.isPrompt = isPrompt;
        this.itemManager = itemManager;
        this.clientThread = clientThread;
        this.removeTransactionConsumer = id -> this.removeTransaction(id);

        Supplier<JButton> renderExtraComponentSupplier = () -> {
            return renderExtraComponent();
        };
        Consumer<Transaction> extraComponentPressedConsumer = (transaction) -> {
            this.extraComponentPressed(transaction);
        };
        Consumer<Object> renderItemCallback = (Object sell) -> {
            TransactionPanel transactionPanel = new TransactionPanel(
                    name,
                    (Transaction) sell,
                    itemManager,
                    this.removeTransactionConsumer,
                    isPrompt
            );
            this.transactionPage.addTransactionPanel(transactionPanel);
        };
        Runnable buildViewCallback = () -> this.buildView();
        this.pagination = new Pagination(
                renderItemCallback,
                UiUtilities.ITEMS_PER_PAGE,
                buildViewCallback
        );
        this.onSearchTextChangedCallback = (searchText) -> this.onSearchTextChanged(searchText);
        this.transactionPage = new TransactionPage(this.onSearchTextChangedCallback);
        this.loadTransactions();
    }

    public void onSearchTextChanged(String searchText) {
        this.searchText = searchText;
        this.pagination.resetPage();
        this.buildView();
    }

    public void addTransaction(Transaction transaction) {
        this.transactions.add(transaction);
        this.buildView();
    }

    public Transaction upsertTransaction(GrandExchangeOffer offer, int slot) {

        ListIterator<Transaction> transactionsIter = transactions.listIterator(transactions.size());
        while (transactionsIter.hasPrevious()) {
            Transaction transaction = transactionsIter.previous();

            if (GrandExchange.checkIsOfferPartOfTransaction(transaction, offer, slot)) {
                Transaction updatedTransaction = transaction.updateTransaction(offer);
                transactionsIter.set(updatedTransaction);
                this.buildView();
                return updatedTransaction;
            }
        }


        Transaction newTransaction = GrandExchange.createTransactionFromOffer(offer, itemManager, slot);
        this.addTransaction(newTransaction);
        return newTransaction;
    }


    public void removeTransaction(UUID id) {
        ListIterator<Transaction> transactionIter = this.transactions.listIterator(this.transactions.size());
        while (transactionIter.hasPrevious()) {
            Transaction transaction = transactionIter.previous();
            if (transaction.id.equals(id)) {
                transactionIter.remove();
                this.buildView();
                return;
            }
        }
    }
    public void extraComponentPressed(Transaction transaction) {};

    public JButton renderExtraComponent() {
        return null;
    }

    public void loadTransactions() throws IOException {};

    public void saveTransactions() {};

    public TransactionPage getPage() {
        return this.transactionPage;
    }

    private boolean isRender(Transaction transaction) {
        String itemName = transaction.getItemName();

        if (
                this.searchText != null &&
                        itemName.toLowerCase().contains(this.searchText.toLowerCase())
        ) {
            return true;
        } else if (this.searchText != null && this.searchText != "") {
            return false;
        }

        return true;
    }

    public void filterList() {
        if (this.searchText == "" || this.searchText == null) {
            this.filteredTransactions = this.transactions;
        } else {

            Iterator<Transaction> transactionIter = this.transactions.iterator();
            this.filteredTransactions = new ArrayList<Transaction>();
            while (transactionIter.hasNext()) {
                Transaction currentTransaction = transactionIter.next();
                if (this.isRender(currentTransaction)) {
                    filteredTransactions.add(currentTransaction);
                }
            }
        }
    }

    public void buildView() {
        SwingUtilities.invokeLater(() -> {
            this.filterList();
            this.transactionPage.resetContainer();
            this.transactionPage.add(
                    this.pagination.getComponent(this.filteredTransactions),
                    BorderLayout.SOUTH
            );
            this.pagination.renderList(this.filteredTransactions);
        });
    }
}