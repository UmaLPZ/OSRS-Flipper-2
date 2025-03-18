// InProgressHeader.java
package com.flipper.views.components;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Color;
import java.awt.image.BufferedImage;

import com.flipper.helpers.UiUtilities;

import net.runelite.api.GrandExchangeOffer;
import net.runelite.api.GrandExchangeOfferState;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;
import net.runelite.client.util.AsyncBufferedImage;
import net.runelite.api.ItemComposition; // Import ItemComposition

public class InProgressHeader extends JPanel {

    public InProgressHeader(ItemComposition item, BufferedImage itemImage, GrandExchangeOffer offer) {
        this.setLayout(new BorderLayout());
        this.setBackground(ColorScheme.DARKER_GRAY_COLOR.darker());

        JPanel itemIconPanel = constructItemIcon(itemImage);
        JLabel itemNameLabel = constructItemName(item.getName());
        JLabel offerTypeLabel = constructOfferTypeLabel(offer);

        this.add(itemIconPanel, BorderLayout.WEST);
        this.add(itemNameLabel, BorderLayout.CENTER);
        this.add(offerTypeLabel, BorderLayout.EAST); // Add to the EAST

        this.setBorder(new EmptyBorder(2, 1, 2, 5));
    }

    private JPanel constructItemIcon(BufferedImage itemImage) {
        JLabel itemIcon = new JLabel();
        itemIcon.setAlignmentX(Component.LEFT_ALIGNMENT);
        itemIcon.setPreferredSize(UiUtilities.ICON_SIZE);
        itemIcon.setIcon(new ImageIcon(itemImage)); // Use helper method
        JPanel itemIconPanel = new JPanel(new BorderLayout());
        itemIconPanel.setBackground(ColorScheme.DARKER_GRAY_COLOR.darker());
        itemIconPanel.add(itemIcon, BorderLayout.WEST);
        return itemIconPanel;
    }

    private JLabel constructItemName(String itemName) {
        JLabel itemNameLabel = new JLabel(itemName, SwingConstants.CENTER);
        itemNameLabel.setForeground(Color.WHITE);
        itemNameLabel.setFont(FontManager.getRunescapeBoldFont());
        return itemNameLabel;
    }

    private JLabel constructOfferTypeLabel(GrandExchangeOffer offer) {
        String offerType = offer.getState() == GrandExchangeOfferState.BUYING ? "Buy" : "Sell";
        JLabel offerTypeLabel = new JLabel(offerType);
        offerTypeLabel.setHorizontalAlignment(JLabel.RIGHT); // Right-align the text

        if (offer.getState() == GrandExchangeOfferState.BUYING) {
            offerTypeLabel.setForeground(ColorScheme.GRAND_EXCHANGE_ALCH);
        } else { // It's a selling state
            offerTypeLabel.setForeground(ColorScheme.PROGRESS_ERROR_COLOR);
        }
        return offerTypeLabel;
    }
}