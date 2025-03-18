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
        newJLabel.setBorder(new EmptyBorder(2, 2, 2, 2));
        return newJLabel;
    }

    private JLabel newRightLabel(String value, Color fontColor) {
        JLabel newRightLabel = new JLabel(value);
        newRightLabel.setHorizontalAlignment(JLabel.RIGHT);
        newRightLabel.setForeground(fontColor);
        newRightLabel.setBorder(new EmptyBorder(2, 2, 2, 2));
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
        JPanel initQuantityPanel = new JPanel(new BorderLayout());
        initQuantityPanel.setBackground(ColorScheme.DARK_GRAY_COLOR);
        String quantityValueTextInit = Numbers.numberWithCommas(transaction.getTotalQuantity());
        JLabel quantityLabelInit = newLeftLabel("Quantity:");
        JLabel quantityValueLabelInit = newRightLabel(quantityValueTextInit, ColorScheme.GRAND_EXCHANGE_ALCH);
        initQuantityPanel.add(quantityLabelInit, BorderLayout.WEST);
        initQuantityPanel.add(quantityValueLabelInit, BorderLayout.EAST);
        contentPanel1.add(initQuantityPanel);

        // Init Price Per
        JPanel initPricePanel = new JPanel(new BorderLayout());
        initPricePanel.setBackground(ColorScheme.DARK_GRAY_COLOR);
        int initPricePer = transaction.getInitPricePer();
        String initPricePerText = Numbers.toShortNumber(initPricePer);
        JLabel pricePerLabelInit = newLeftLabel("Price Per:");
        JLabel pricePerValueLabelInit = newRightLabel(initPricePerText, ColorScheme.GRAND_EXCHANGE_ALCH);
        pricePerValueLabelInit.setToolTipText(Numbers.numberWithCommas(initPricePer)); // Tooltip
        initPricePanel.add(pricePerLabelInit, BorderLayout.WEST);
        initPricePanel.add(pricePerValueLabelInit, BorderLayout.EAST);
        contentPanel1.add(initPricePanel);

        // Tax Per (Conditional, for Sells)
        if (!transaction.isBuy()) {
            JPanel taxPanel = new JPanel(new BorderLayout());
            taxPanel.setBackground(ColorScheme.DARK_GRAY_COLOR);
            int initialTax = calculateTax(transaction.getInitPricePer());
            String initialTaxText = Numbers.toShortNumber(initialTax);
            JLabel taxLabel = newLeftLabel("Tax Per:");
            JLabel taxValueLabel = newRightLabel(initialTaxText, ColorScheme.PROGRESS_ERROR_COLOR);
            taxValueLabel.setToolTipText(Numbers.numberWithCommas(initialTax)); // Tooltip
            taxPanel.add(taxLabel, BorderLayout.WEST);
            taxPanel.add(taxValueLabel, BorderLayout.EAST);
            contentPanel1.add(taxPanel);
        }

        // Init Total
        JPanel initTotalPanel = new JPanel(new BorderLayout());
        initTotalPanel.setBackground(ColorScheme.DARK_GRAY_COLOR);
        int initTotalValue = transaction.getInitPricePer() * transaction.getQuantity();
        String initTotalValueText = Numbers.toShortNumber(initTotalValue);
        JLabel totalValueLabelInit =  newLeftLabel("Total Value:");
        JLabel totalValueValueLabelInit = newRightLabel(initTotalValueText, ColorScheme.GRAND_EXCHANGE_ALCH);
        totalValueValueLabelInit.setToolTipText(Numbers.numberWithCommas(initTotalValue)); // Tooltip
        initTotalPanel.add(totalValueLabelInit, BorderLayout.WEST);
        initTotalPanel.add(totalValueValueLabelInit, BorderLayout.EAST);
        contentPanel1.add(initTotalPanel);

        column1.add(contentPanel1, BorderLayout.CENTER);

        // --- Column 2 (Final) ---
        JPanel column2 = new JPanel(new BorderLayout());
        column2.setBackground(ColorScheme.DARK_GRAY_COLOR);

        JPanel contentPanel2 = new JPanel(new GridLayout(0, 1));
        contentPanel2.setBackground(ColorScheme.DARK_GRAY_COLOR);

        // Final Quantity
        JPanel finQuantityPanel = new JPanel(new BorderLayout());
        finQuantityPanel.setBackground(ColorScheme.DARK_GRAY_COLOR);
        String quantityValueTextFin = Numbers.numberWithCommas(transaction.getQuantity());
        JLabel quantityLabelFin = newLeftLabel("Quantity:");
        JLabel quantityValueLabelFin = newRightLabel(quantityValueTextFin, ColorScheme.GRAND_EXCHANGE_ALCH);
        finQuantityPanel.add(quantityLabelFin, BorderLayout.WEST);
        finQuantityPanel.add(quantityValueLabelFin, BorderLayout.EAST);
        contentPanel2.add(finQuantityPanel);

        // Fin Price Per
        JPanel finPricePanel = new JPanel(new BorderLayout());
        finPricePanel.setBackground(ColorScheme.DARK_GRAY_COLOR);
        int finPricePer = transaction.getFinPricePer();
        String finPricePerText = Numbers.toShortNumber(finPricePer);
        JLabel pricePerLabelFin = newLeftLabel("Price Per:");
        JLabel pricePerValueLabelFin = newRightLabel(finPricePerText, ColorScheme.GRAND_EXCHANGE_ALCH);
        pricePerValueLabelFin.setToolTipText(Numbers.numberWithCommas(finPricePer)); // Tooltip
        finPricePanel.add(pricePerLabelFin, BorderLayout.WEST);
        finPricePanel.add(pricePerValueLabelFin, BorderLayout.EAST);
        contentPanel2.add(finPricePanel);

        // Tax Per (Conditional, for Sells)
        if (!transaction.isBuy()) {
            JPanel taxPanel = new JPanel(new BorderLayout());
            taxPanel.setBackground(ColorScheme.DARK_GRAY_COLOR);
            int finalTax = calculateTax(transaction.getFinPricePer());
            String finalTaxText = Numbers.toShortNumber(finalTax);
            JLabel taxLabel = newLeftLabel("Tax Per:");
            JLabel taxValueLabel = newRightLabel(finalTaxText, ColorScheme.PROGRESS_ERROR_COLOR);
            taxValueLabel.setToolTipText(Numbers.numberWithCommas(finalTax)); // Tooltip
            taxPanel.add(taxLabel, BorderLayout.WEST);
            taxPanel.add(taxValueLabel, BorderLayout.EAST);
            contentPanel2.add(taxPanel);
        }

        // Fin Total
        JPanel finTotalPanel = new JPanel(new BorderLayout());
        finTotalPanel.setBackground(ColorScheme.DARK_GRAY_COLOR);
        int finTotalValue = transaction.getFinPricePer() * transaction.getQuantity();
        String finTotalValueText = Numbers.toShortNumber(finTotalValue);
        JLabel totalValueLabelFin = newLeftLabel("Total Value:");
        JLabel totalValueValueLabelFin = newRightLabel(finTotalValueText, ColorScheme.GRAND_EXCHANGE_ALCH);
        totalValueValueLabelFin.setToolTipText(Numbers.numberWithCommas(finTotalValue)); // Tooltip
        finTotalPanel.add(totalValueLabelFin, BorderLayout.WEST);
        finTotalPanel.add(totalValueValueLabelFin, BorderLayout.EAST);
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