package com.flipper.views.transactions;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.Color;
import java.awt.event.*;
import java.awt.Dimension;

import com.flipper.helpers.UiUtilities;
import com.flipper.helpers.Numbers;
import com.flipper.helpers.Timestamps;
import com.flipper.models.Transaction;
import com.flipper.views.components.DeleteButton;
import com.flipper.views.components.ItemHeader;

import net.runelite.api.ItemComposition;
import net.runelite.client.game.ItemManager;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.callback.ClientThread;

import java.util.UUID;
import java.util.function.Consumer;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;

public class TransactionPanel extends JPanel {
    JPanel container;

    private Transaction transaction;

    private final ClientThread clientThread;

    // Left column labels
    private final JLabel totalValueLabel = new JLabel("Total Value:");
    private final JLabel pricePerLabel = new JLabel("Price Per:");

    // Right column values
    private final JLabel totalValueValue = new JLabel();
    private final JLabel pricePerValue = new JLabel();

    public TransactionPanel(
            Transaction transaction,
            ItemManager itemManager,
            Consumer<UUID> removeTransactionConsumer,
            boolean isPrompt,
            ClientThread clientThread
    ) {
        this.clientThread = clientThread;
        init(
                transaction,
                itemManager,
                removeTransactionConsumer,
                isPrompt
        );
    }

    private void init(
            Transaction transaction,
            ItemManager itemManager,
            Consumer<UUID> removeTransactionConsumer,
            boolean isPrompt
    ) {
        setLayout(new BorderLayout());
        setBackground(ColorScheme.DARK_GRAY_COLOR);

        DeleteButton deleteTransactionButton = new DeleteButton((ActionEvent action) -> {
            String describeTransaction = transaction.describeTransaction();
            int input = isPrompt
                    ? JOptionPane.showConfirmDialog(
                    null,
                    "Delete transaction of " + describeTransaction + "?"
            )
                    : 0;
            if (input == 0) {
                removeTransactionConsumer.accept(transaction.getId());
                setVisible(false);
            }
        });

        container = new JPanel();
        container.setLayout(new BorderLayout());
        container.setBackground(ColorScheme.DARK_GRAY_COLOR);

        // Use clientThread.invokeLater to get item composition
        clientThread.invoke(() -> {
            ItemComposition itemComp = itemManager.getItemComposition(transaction.getItemId());

            SwingUtilities.invokeLater(() -> { // Now safe for UI updates
                container.add(
                        new ItemHeader(
                                transaction.getItemId(),
                                transaction.getPricePer(),
                                itemComp.getName(),
                                itemManager,
                                false,
                                deleteTransactionButton,
                                clientThread
                        ),
                        BorderLayout.NORTH
                );

                constructItemInfo();
                container.setBorder(UiUtilities.ITEM_INFO_BORDER);
                this.add(container, BorderLayout.NORTH);
                this.setBorder(new EmptyBorder(0, 5, 3, 5));
            });
        });

    }


    private void constructItemInfo() {
        JPanel itemInfoContainer = new JPanel(new BorderLayout());
        itemInfoContainer.setBackground(ColorScheme.DARK_GRAY_COLOR);

        // --- Main Content Panel (Two Columns) ---
        JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setBackground(ColorScheme.DARK_GRAY_COLOR);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 2, 5);

        // --- Left Column (Labels) ---
        JPanel leftPanel = new JPanel(new GridLayout(3, 1)); // 3 rows, 1 column
        leftPanel.setBackground(ColorScheme.DARK_GRAY_COLOR);
        totalValueLabel.setForeground(ColorScheme.LIGHT_GRAY_COLOR);
        pricePerLabel.setForeground(ColorScheme.LIGHT_GRAY_COLOR);
        JLabel quantityLabel = new JLabel("Quantity:");
        quantityLabel.setForeground(ColorScheme.LIGHT_GRAY_COLOR);

        leftPanel.add(totalValueLabel);
        leftPanel.add(pricePerLabel);
        leftPanel.add(quantityLabel);

        gbc.gridx = 0; // Column 0
        gbc.weightx = 0.5; // Share horizontal space
        centerPanel.add(leftPanel, gbc);


        // --- Right Column (Values) ---
        JPanel rightPanel = new JPanel(new GridLayout(3, 1)); // 3 rows, 1 column
        rightPanel.setBackground(ColorScheme.DARK_GRAY_COLOR);
        String totalValueString = Numbers.numberWithCommas(transaction.getTotalPrice()) + " gp";
        totalValueValue.setText(totalValueString);
        totalValueValue.setForeground(ColorScheme.GRAND_EXCHANGE_ALCH);
        totalValueValue.setHorizontalAlignment(JLabel.RIGHT); // Right-align the text

        String pricePerString = Numbers.numberWithCommas(transaction.getPricePer()) + " gp";
        pricePerValue.setText(pricePerString);
        pricePerValue.setForeground(ColorScheme.GRAND_EXCHANGE_ALCH);
        pricePerValue.setHorizontalAlignment(JLabel.RIGHT);

        String quantityString = Numbers.numberWithCommas(transaction.getQuantity());
        JLabel quantityValue = new JLabel(quantityString);
        quantityValue.setForeground(ColorScheme.GRAND_EXCHANGE_ALCH);
        quantityValue.setHorizontalAlignment(JLabel.RIGHT);

        rightPanel.add(totalValueValue);
        rightPanel.add(pricePerValue);
        rightPanel.add(quantityValue);


        gbc.gridx = 1; // Column 1
        gbc.weightx = 0.5; // Share horizontal space
        centerPanel.add(rightPanel, gbc);

        // --- Add centerPanel to itemInfoContainer ---
        itemInfoContainer.add(centerPanel, BorderLayout.CENTER);

        container.add(itemInfoContainer, BorderLayout.CENTER); // Add to the main container
    }
}