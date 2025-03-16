package com.flipper.views.transactions;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.GridLayout;
import java.awt.Color;
import java.awt.Insets;
import java.awt.event.*;
import java.awt.Dimension;

import com.flipper.helpers.UiUtilities;
import com.flipper.helpers.Numbers;
import com.flipper.helpers.Timestamps;
import com.flipper.models.Transaction;
import com.flipper.views.components.DeleteButton;
import com.flipper.views.components.ItemHeader;

import net.runelite.client.game.ItemManager;
import net.runelite.client.ui.ColorScheme;

import java.util.UUID;
import java.util.function.Consumer;

public class TransactionPanel extends JPanel {
    private Transaction transaction;
    private JPanel container;
    private JPanel itemInfoContainer;

    public TransactionPanel(
            String name,
            Transaction transaction,
            ItemManager itemManager,
            Consumer<UUID> removeTransactionConsumer,
            boolean isPrompt
    ) {
        init(
                name,
                transaction,
                itemManager,
                removeTransactionConsumer,
                isPrompt
        );
    }

    private void init(
            String name,
            Transaction transaction,
            ItemManager itemManager,
            Consumer<UUID> removeTransactionConsumer,
            boolean isPrompt
    ) {
        this.transaction = transaction;
        setLayout(new BorderLayout());
        setBackground(ColorScheme.DARK_GRAY_COLOR);

        DeleteButton deleteTransactionButton = new DeleteButton((ActionEvent action) -> {
            String describeTransaction = transaction.describeTransaction();
            int input = isPrompt
                    ? JOptionPane.showConfirmDialog(
                    null,
                    "Delete " + name + " of " + describeTransaction + "?"
            )
                    : 0;
            if (input == 0) {
                removeTransactionConsumer.accept(transaction.getId());
                setVisible(false);
            }
        });

        container = new JPanel(new BorderLayout());
        container.setBackground(ColorScheme.DARK_GRAY_COLOR);

        JPanel itemHeader = new ItemHeader(
                transaction.getItemId(),
                transaction.getFinPricePer(),
                transaction.getItemName(),
                itemManager,
                false,
                deleteTransactionButton
        );
        container.add(itemHeader, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBackground(ColorScheme.DARK_GRAY_COLOR);

        JPanel titlePanel = new JPanel(new GridLayout(1, 2, 0, 0));
        titlePanel.setBackground(ColorScheme.DARK_GRAY_COLOR);

        JLabel initialLabel = new JLabel("Initial");
        initialLabel.setHorizontalAlignment(JLabel.CENTER);
        initialLabel.setForeground(ColorScheme.GRAND_EXCHANGE_PRICE);
        titlePanel.add(initialLabel);

        JLabel finalLabel = new JLabel("Final");
        finalLabel.setHorizontalAlignment(JLabel.CENTER);
        finalLabel.setForeground(ColorScheme.GRAND_EXCHANGE_PRICE);
        titlePanel.add(finalLabel);

        centerPanel.add(titlePanel, BorderLayout.NORTH);

        constructItemInfo();
        centerPanel.add(itemInfoContainer, BorderLayout.CENTER);

        container.add(centerPanel, BorderLayout.CENTER);

        JPanel datePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        datePanel.setBackground(ColorScheme.DARK_GRAY_COLOR);
        String dateString = "Date: " + Timestamps.format(transaction.getCreatedTime());
        JLabel dateLabel = new JLabel(dateString);
        dateLabel.setForeground(ColorScheme.GRAND_EXCHANGE_ALCH);
        datePanel.add(dateLabel);

        container.add(datePanel, BorderLayout.SOUTH);

        container.setBorder(UiUtilities.ITEM_INFO_BORDER);
        this.add(container, BorderLayout.NORTH);
        this.setBorder(new EmptyBorder(0, 5, 3, 5));
    }

    private JLabel newLeftLabel(String text) {
        JLabel newJLabel = new JLabel(text);
        newJLabel.setForeground(ColorScheme.GRAND_EXCHANGE_PRICE);
        newJLabel.setBorder(new EmptyBorder(2, 2, 2, 0));
        return newJLabel;
    }

    private JLabel newRightLabel(String value, Color fontColor) {
        JLabel newRightLabel = new JLabel(value);
        newRightLabel.setHorizontalAlignment(JLabel.RIGHT);
        newRightLabel.setForeground(fontColor);
        newRightLabel.setBorder(new EmptyBorder(2, 0, 2, 2));
        return newRightLabel;
    }

    private int calculateTax(int pricePer) {
        int tax = (int) Math.floor(pricePer * Transaction.TAX_RATE);
        return Math.min(tax, Transaction.MAX_TAX);
    }

    private void constructItemInfo() {
        itemInfoContainer = new JPanel(new GridLayout(1, 2, 0, 0));
        itemInfoContainer.setBackground(ColorScheme.DARK_GRAY_COLOR);

        // --- Column 1 (Initial) ---
        JPanel column1 = new JPanel(new BorderLayout());
        column1.setBackground(ColorScheme.DARK_GRAY_COLOR);

        JPanel contentPanel1 = new JPanel(new GridLayout(0, 1));
        contentPanel1.setBackground(ColorScheme.DARK_GRAY_COLOR);

        // Initial Quantity
        JPanel initQuantityPanel = new JPanel(new BorderLayout()); // Renamed
        initQuantityPanel.setBackground(ColorScheme.DARK_GRAY_COLOR);
        String quantityValueTextInit = Numbers.numberWithCommas(transaction.getTotalQuantity()); // Total quantity
        initQuantityPanel.add(newLeftLabel("Quantity:"), BorderLayout.WEST);
        initQuantityPanel.add(newRightLabel(quantityValueTextInit, ColorScheme.GRAND_EXCHANGE_ALCH), BorderLayout.EAST);
        contentPanel1.add(initQuantityPanel);

        // Init Price Per
        JPanel initPricePanel = new JPanel(new BorderLayout());
        initPricePanel.setBackground(ColorScheme.DARK_GRAY_COLOR);
        initPricePanel.add(newLeftLabel("Price Per:"), BorderLayout.WEST);
        initPricePanel.add(newRightLabel(Numbers.numberWithCommas(transaction.getInitPricePer()), ColorScheme.GRAND_EXCHANGE_ALCH), BorderLayout.EAST);
        contentPanel1.add(initPricePanel);

        // Tax Per (Conditional, for Sells)
        if (!transaction.isBuy()) {
            JPanel taxPanel = new JPanel(new BorderLayout());
            taxPanel.setBackground(ColorScheme.DARK_GRAY_COLOR);
            int initialTax = calculateTax(transaction.getInitPricePer());
            taxPanel.add(newLeftLabel("Tax Per:"), BorderLayout.WEST);
            taxPanel.add(newRightLabel(Numbers.numberWithCommas(initialTax), ColorScheme.PROGRESS_ERROR_COLOR), BorderLayout.EAST);
            contentPanel1.add(taxPanel);
        }

        // Init Total
        JPanel initTotalPanel = new JPanel(new BorderLayout());
        initTotalPanel.setBackground(ColorScheme.DARK_GRAY_COLOR);
        initTotalPanel.add(newLeftLabel("Total Value:"), BorderLayout.WEST);
        int initTotalValue = transaction.getInitPricePer() * transaction.getQuantity(); // Use transaction.getQuantity()
        initTotalPanel.add(newRightLabel(Numbers.numberWithCommas(initTotalValue), ColorScheme.GRAND_EXCHANGE_ALCH), BorderLayout.EAST);
        contentPanel1.add(initTotalPanel);

        column1.add(contentPanel1, BorderLayout.CENTER);

        // --- Column 2 (Final) ---
        JPanel column2 = new JPanel(new BorderLayout());
        column2.setBackground(ColorScheme.DARK_GRAY_COLOR);

        JPanel contentPanel2 = new JPanel(new GridLayout(0, 1));
        contentPanel2.setBackground(ColorScheme.DARK_GRAY_COLOR);

        // Final Quantity
        JPanel finQuantityPanel = new JPanel(new BorderLayout()); // Renamed
        finQuantityPanel.setBackground(ColorScheme.DARK_GRAY_COLOR);
        String quantityValueTextFin = Numbers.numberWithCommas(transaction.getQuantity()); // Sold quantity
        finQuantityPanel.add(newLeftLabel("Quantity:"), BorderLayout.WEST);
        finQuantityPanel.add(newRightLabel(quantityValueTextFin, ColorScheme.GRAND_EXCHANGE_ALCH), BorderLayout.EAST);
        contentPanel2.add(finQuantityPanel);

        // Fin Price Per
        JPanel finPricePanel = new JPanel(new BorderLayout());
        finPricePanel.setBackground(ColorScheme.DARK_GRAY_COLOR);
        finPricePanel.add(newLeftLabel("Price Per:"), BorderLayout.WEST);
        finPricePanel.add(newRightLabel(Numbers.numberWithCommas(transaction.getFinPricePer()), ColorScheme.GRAND_EXCHANGE_ALCH), BorderLayout.EAST);
        contentPanel2.add(finPricePanel);

        // Tax Per (Conditional, for Sells)
        if (!transaction.isBuy()) {
            JPanel taxPanel = new JPanel(new BorderLayout());
            taxPanel.setBackground(ColorScheme.DARK_GRAY_COLOR);
            int finalTax = calculateTax(transaction.getFinPricePer());
            taxPanel.add(newLeftLabel("Tax Per:"), BorderLayout.WEST);
            taxPanel.add(newRightLabel(Numbers.numberWithCommas(finalTax), ColorScheme.PROGRESS_ERROR_COLOR), BorderLayout.EAST);
            contentPanel2.add(taxPanel);
        }

        // Fin Total
        JPanel finTotalPanel = new JPanel(new BorderLayout());
        finTotalPanel.setBackground(ColorScheme.DARK_GRAY_COLOR);
        finTotalPanel.add(newLeftLabel("Total Value:"), BorderLayout.WEST);
        int finTotalValue = transaction.getFinPricePer() * transaction.getQuantity();
        finTotalPanel.add(newRightLabel(Numbers.numberWithCommas(finTotalValue), ColorScheme.GRAND_EXCHANGE_ALCH), BorderLayout.EAST);
        contentPanel2.add(finTotalPanel);

        column2.add(contentPanel2, BorderLayout.CENTER);

        itemInfoContainer.add(column1);
        itemInfoContainer.add(column2);
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension preferredSize = super.getPreferredSize();
        return new Dimension(container.getPreferredSize().width, preferredSize.height);
    }
}