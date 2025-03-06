package com.flipper.views.inprogress;

import net.runelite.api.GrandExchangeOffer;
import net.runelite.api.ItemComposition;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.components.ThinProgressBar;
import net.runelite.client.util.QuantityFormatter;

import javax.annotation.Nullable;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.image.BufferedImage;

import static net.runelite.api.GrandExchangeOfferState.*;
import net.runelite.client.callback.ClientThread;
import javax.inject.Inject;

public class InProgressPanel extends JPanel {

    private final JLabel itemIcon = new JLabel();
    private final JLabel itemName = new JLabel();
    private final JLabel quantityProgressLabel = new JLabel();
    private final JLabel buySellLabel = new JLabel();

    // Left column labels (initial values)
    private final JLabel initialOfferValueLabel = new JLabel();
    private final JLabel initialPricePerLabel = new JLabel();

    // Right column labels (current values)
    private final JLabel currentOfferValueLabel = new JLabel();
    private final JLabel currentPricePerLabel = new JLabel();
    private final JLabel currentLabel = new JLabel(); // "Spent" or "Received"
    private final JLabel currentPriceLabel = new JLabel(); //"Spent Per" or "Received Per"


    private final ThinProgressBar progressBar = new ThinProgressBar();

    @Inject
    private ClientThread clientThread;

    public InProgressPanel(ItemComposition item, BufferedImage itemImage, GrandExchangeOffer offer) {
        setLayout(new BorderLayout());
        setBackground(ColorScheme.DARK_GRAY_COLOR);
        setBorder(new EmptyBorder(5, 5, 5, 5));

        // Top Panel (Buy/Sell Label)
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(ColorScheme.DARK_GRAY_COLOR);
        buySellLabel.setFont(FontManager.getRunescapeBoldFont());
        buySellLabel.setHorizontalAlignment(JLabel.CENTER);
        topPanel.add(buySellLabel, BorderLayout.CENTER);

        // Left Panel (Item Icon)
        JPanel leftPanel = new JPanel();
        leftPanel.setBackground(ColorScheme.DARK_GRAY_COLOR);
        itemIcon.setVerticalAlignment(JLabel.CENTER);
        itemIcon.setHorizontalAlignment(JLabel.CENTER);
        itemIcon.setPreferredSize(new Dimension(36, 36));
        itemIcon.setIcon(new ImageIcon(itemImage));
        leftPanel.add(itemIcon);

        // --- Center Panel (Two Columns + Item Name and Quantity) ---
        JPanel centerPanel = new JPanel(new GridBagLayout()); // Use GridBagLayout for main column layout
        centerPanel.setBackground(ColorScheme.DARK_GRAY_COLOR);
        centerPanel.setBorder(new EmptyBorder(0, 5, 0, 0));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 2, 5); // Add spacing

        // Item Name (centered above the columns)
        itemName.setFont(FontManager.getRunescapeSmallFont());
        itemName.setForeground(Color.WHITE);
        itemName.setToolTipText(item.getName());
        itemName.setHorizontalAlignment(JLabel.CENTER); // Center alignment
        gbc.gridwidth = 2; // Span both columns
        centerPanel.add(itemName, gbc);
        gbc.gridwidth = 1; // Reset for other labels
        gbc.gridy++;

        // --- Left Column (Initial Values) ---
        JPanel initialValuePanel = new JPanel(new GridLayout(4, 1)); // 4 rows, 1 column - Label & Value x2
        initialValuePanel.setBackground(ColorScheme.DARK_GRAY_COLOR);

        // Total Value (Label and Value)
        JLabel totalValueLabel = new JLabel("Total Value:");
        totalValueLabel.setFont(FontManager.getRunescapeSmallFont());
        totalValueLabel.setForeground(ColorScheme.LIGHT_GRAY_COLOR);
        initialOfferValueLabel.setFont(FontManager.getRunescapeSmallFont());
        initialOfferValueLabel.setForeground(ColorScheme.GRAND_EXCHANGE_ALCH);
        initialValuePanel.add(totalValueLabel);
        initialValuePanel.add(initialOfferValueLabel);

        // Price Per (Label and Value)
        JLabel pricePerLabel = new JLabel("Price Per:");
        pricePerLabel.setFont(FontManager.getRunescapeSmallFont());
        pricePerLabel.setForeground(ColorScheme.LIGHT_GRAY_COLOR);
        initialPricePerLabel.setFont(FontManager.getRunescapeSmallFont());
        initialPricePerLabel.setForeground(ColorScheme.GRAND_EXCHANGE_ALCH);
        initialValuePanel.add(pricePerLabel);
        initialValuePanel.add(initialPricePerLabel);

        gbc.gridx = 0; // Column 0
        gbc.weightx = 0.5; // Equal weight for columns
        centerPanel.add(initialValuePanel, gbc);

        // --- Right Column (Current Values) ---
        JPanel currentValuePanel = new JPanel(new GridLayout(4, 1)); // 4 rows, 1 column - Label & Value x2
        currentValuePanel.setBackground(ColorScheme.DARK_GRAY_COLOR);

        currentLabel.setFont(FontManager.getRunescapeSmallFont());
        currentLabel.setForeground(ColorScheme.LIGHT_GRAY_COLOR);
        currentOfferValueLabel.setFont(FontManager.getRunescapeSmallFont());
        currentOfferValueLabel.setForeground(ColorScheme.GRAND_EXCHANGE_ALCH);
        currentValuePanel.add(currentLabel);
        currentValuePanel.add(currentOfferValueLabel);

        currentPriceLabel.setFont(FontManager.getRunescapeSmallFont());
        currentPriceLabel.setForeground(ColorScheme.LIGHT_GRAY_COLOR);

        currentPricePerLabel.setFont(FontManager.getRunescapeSmallFont());
        currentPricePerLabel.setForeground(ColorScheme.GRAND_EXCHANGE_ALCH);
        currentValuePanel.add(currentPriceLabel);
        currentValuePanel.add(currentPricePerLabel);

        gbc.gridx = 1; // Column 1
        gbc.weightx = 0.5; // Equal weight
        centerPanel.add(currentValuePanel, gbc);

        // --- Quantity Progress (centered below the columns) ---
        gbc.gridy++;
        gbc.gridx = 0;
        gbc.gridwidth = 2; // Span both columns
        quantityProgressLabel.setFont(FontManager.getRunescapeSmallFont());
        quantityProgressLabel.setForeground(ColorScheme.LIGHT_GRAY_COLOR);
        quantityProgressLabel.setHorizontalAlignment(JLabel.CENTER);
        centerPanel.add(quantityProgressLabel, gbc);

        // --- Main Panel Layout (using BorderLayout) ---
        JPanel mainContentPanel = new JPanel(new BorderLayout()); // Holds everything except topPanel and progressBar
        mainContentPanel.setBackground(ColorScheme.DARK_GRAY_COLOR);

        JPanel combinedItemAndCenterPanel = new JPanel(); // Holds itemNamePanel, leftPanel and centerPanel
        combinedItemAndCenterPanel.setLayout(new BoxLayout(combinedItemAndCenterPanel, BoxLayout.X_AXIS)); // Arrange horizontally
        combinedItemAndCenterPanel.setBackground(ColorScheme.DARK_GRAY_COLOR);

        combinedItemAndCenterPanel.add(leftPanel);
        combinedItemAndCenterPanel.add(centerPanel);


        mainContentPanel.add(itemName, BorderLayout.NORTH);  // itemName above
        mainContentPanel.add(combinedItemAndCenterPanel, BorderLayout.CENTER);
        mainContentPanel.add(quantityProgressLabel, BorderLayout.SOUTH); // quantityProgress below

        add(topPanel, BorderLayout.NORTH); // buySellLabel at the very top
        add(mainContentPanel, BorderLayout.CENTER); // All the main content
        add(progressBar, BorderLayout.SOUTH); // progressBar at the bottom
    }

    public void updateOffer(ItemComposition item, BufferedImage itemImage, @Nullable GrandExchangeOffer offer) {
        if (offer == null || offer.getState() == EMPTY) {
            return;
        }

        boolean buying = offer.getState() == BUYING || offer.getState() == BOUGHT || offer.getState() == CANCELLED_BUY;
        String offerType = buying ? "Buy" : "Sell";
        Color offerTypeColor = buying ? ColorScheme.GRAND_EXCHANGE_ALCH : ColorScheme.PROGRESS_ERROR_COLOR;

        SwingUtilities.invokeLater(() -> {
            itemIcon.setIcon(new ImageIcon(itemImage));
            itemName.setText(item.getName());
            itemName.setToolTipText(item.getName());

            // Initial Values
            int initialTotalPrice = offer.getPrice() * offer.getTotalQuantity();
            int initialPricePer = offer.getPrice();
            initialOfferValueLabel.setText(QuantityFormatter.formatNumber(initialTotalPrice) + " gp");
            initialPricePerLabel.setText(QuantityFormatter.formatNumber(initialPricePer) + " gp");

            // Current Values
            int currentTotalPrice = offer.getQuantitySold() * offer.getPrice();
            int currentPricePer = offer.getQuantitySold() > 0 ? offer.getSpent() / offer.getQuantitySold() : 0;
            currentOfferValueLabel.setText(QuantityFormatter.formatNumber(currentTotalPrice) + " gp");
            currentPricePerLabel.setText(QuantityFormatter.formatNumber(currentPricePer) + " gp");

            // Set dynamic labels based on buy/sell
            currentLabel.setText(buying ? "Spent:" : "Received:");
            currentPriceLabel.setText(buying ? "Spent Per:" : "Received Per:");

            // Combined progress label
            quantityProgressLabel.setText(
                    QuantityFormatter.quantityToRSDecimalStack(offer.getQuantitySold()) + " / " +
                            QuantityFormatter.quantityToRSDecimalStack(offer.getTotalQuantity())
            );

            buySellLabel.setText(offerType);
            buySellLabel.setForeground(offerTypeColor);

            progressBar.setForeground(getProgressColor(offer));
            progressBar.setMaximumValue(offer.getTotalQuantity());
            progressBar.setValue(offer.getQuantitySold());

            revalidate();
            repaint();
        });
    }

    private Color getProgressColor(GrandExchangeOffer offer) {
        if (offer.getState() == CANCELLED_BUY || offer.getState() == CANCELLED_SELL) {
            return ColorScheme.PROGRESS_ERROR_COLOR;
        }

        if (offer.getQuantitySold() == offer.getTotalQuantity()) {
            return ColorScheme.PROGRESS_COMPLETE_COLOR;
        }

        return ColorScheme.PROGRESS_INPROGRESS_COLOR;
    }
}