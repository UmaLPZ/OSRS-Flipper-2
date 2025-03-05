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
    private final JLabel totalOfferValueLabel = new JLabel();
    private final JLabel valueSoFarLabel = new JLabel();
    private final JLabel offerStateLabel = new JLabel();
    private final JLabel buySellLabel = new JLabel();

    private final ThinProgressBar progressBar = new ThinProgressBar();
    @Inject
    private ClientThread clientThread;

    // Keep track of the last quantity sold to detect changes
    private int lastQuantitySold = -1;

    public InProgressPanel(ItemComposition item, BufferedImage itemImage, GrandExchangeOffer offer) {
        setLayout(new BorderLayout());
        setBackground(ColorScheme.DARK_GRAY_COLOR);
        setBorder(new EmptyBorder(5, 5, 5, 5));

        // Top Panel (Buy/Sell Label)
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(ColorScheme.DARK_GRAY_COLOR);
        buySellLabel.setFont(FontManager.getRunescapeBoldFont()); // Make it stand out
        buySellLabel.setHorizontalAlignment(JLabel.CENTER);
        topPanel.add(buySellLabel, BorderLayout.CENTER); // Centered at the top

        // Left Panel (Item Icon)
        JPanel leftPanel = new JPanel();
        leftPanel.setBackground(ColorScheme.DARK_GRAY_COLOR);
        itemIcon.setVerticalAlignment(JLabel.CENTER);
        itemIcon.setHorizontalAlignment(JLabel.CENTER);
        itemIcon.setPreferredSize(new Dimension(36, 36));
        itemIcon.setIcon(new ImageIcon(itemImage));
        leftPanel.add(itemIcon);

        // Right Panel (Text Information)
        JPanel rightPanel = new JPanel(new GridLayout(5, 1)); // 5 rows, 1 column
        rightPanel.setBackground(ColorScheme.DARK_GRAY_COLOR);
        rightPanel.setBorder(new EmptyBorder(0, 5, 0, 0)); // Add some padding

        itemName.setText(item.getName());
        itemName.setFont(FontManager.getRunescapeSmallFont());
        itemName.setForeground(Color.WHITE);
        itemName.setToolTipText(item.getName()); // Add tool tip for long item names

        quantityProgressLabel.setFont(FontManager.getRunescapeSmallFont());
        quantityProgressLabel.setForeground(ColorScheme.LIGHT_GRAY_COLOR);

        totalOfferValueLabel.setFont(FontManager.getRunescapeSmallFont());
        totalOfferValueLabel.setForeground(ColorScheme.GRAND_EXCHANGE_ALCH); // Example color

        valueSoFarLabel.setFont(FontManager.getRunescapeSmallFont());
        valueSoFarLabel.setForeground(ColorScheme.GRAND_EXCHANGE_ALCH); // Example color

        offerStateLabel.setFont(FontManager.getRunescapeSmallFont());
        offerStateLabel.setForeground(ColorScheme.LIGHT_GRAY_COLOR);

        rightPanel.add(itemName);
        rightPanel.add(quantityProgressLabel); // Combined progress
        rightPanel.add(totalOfferValueLabel);
        rightPanel.add(valueSoFarLabel);
        rightPanel.add(offerStateLabel);

        // Main Panel Layout
        add(topPanel, BorderLayout.NORTH); // Buy/Sell label at the top
        add(leftPanel, BorderLayout.WEST);
        add(rightPanel, BorderLayout.CENTER);
        add(progressBar, BorderLayout.SOUTH);

    }

    public void updateOffer(ItemComposition item, BufferedImage itemImage, @Nullable GrandExchangeOffer offer) {
        if (offer == null || offer.getState() == EMPTY) {
            return;
        } else {
            boolean buying = offer.getState() == BUYING || offer.getState() == BOUGHT || offer.getState() == CANCELLED_BUY;
            String offerType = buying ? "Buy" : "Sell"; // Determine offer type
            Color offerTypeColor = buying ? ColorScheme.GRAND_EXCHANGE_ALCH : ColorScheme.PROGRESS_ERROR_COLOR; // Example colors


            // Update labels
            SwingUtilities.invokeLater(() -> {
                itemIcon.setIcon(new ImageIcon(itemImage));
                itemName.setText(item.getName());
                itemName.setToolTipText(item.getName()); // Update tooltip as well

                // Combined progress label: "50/1000"
                quantityProgressLabel.setText(
                        QuantityFormatter.quantityToRSDecimalStack(offer.getQuantitySold()) + " / " +
                                QuantityFormatter.quantityToRSDecimalStack(offer.getTotalQuantity())
                );

                totalOfferValueLabel.setText("Total Value: " + QuantityFormatter.formatNumber(offer.getPrice() * offer.getTotalQuantity()) + " gp");
                valueSoFarLabel.setText((buying ? "Spent So Far: " : "Received So Far: ") + QuantityFormatter.formatNumber(offer.getSpent()) + " gp"); // Clearer wording
                offerStateLabel.setText("State: " + offer.getState());

                buySellLabel.setText(offerType); // Set Buy/Sell label text
                buySellLabel.setForeground(offerTypeColor);

                progressBar.setForeground(getProgressColor(offer));
                progressBar.setMaximumValue(offer.getTotalQuantity());
                progressBar.setValue(offer.getQuantitySold());

                // Update lastQuantitySold for the *next* update
                lastQuantitySold = offer.getQuantitySold();

                revalidate();
                repaint();
            });
        }
        revalidate();
    }

    private Color getProgressColor(GrandExchangeOffer offer)
    {
        if (offer.getState() == CANCELLED_BUY || offer.getState() == CANCELLED_SELL)
        {
            return ColorScheme.PROGRESS_ERROR_COLOR;
        }

        if (offer.getQuantitySold() == offer.getTotalQuantity())
        {
            return ColorScheme.PROGRESS_COMPLETE_COLOR;
        }

        return ColorScheme.PROGRESS_INPROGRESS_COLOR;
    }
}