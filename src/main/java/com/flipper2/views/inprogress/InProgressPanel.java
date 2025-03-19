package com.flipper2.views.inprogress;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.Color;
import java.awt.image.BufferedImage;

import com.flipper2.helpers.Numbers;
import com.flipper2.helpers.UiUtilities;
import com.flipper2.views.components.InProgressHeader;

import net.runelite.api.GrandExchangeOffer;
import net.runelite.api.GrandExchangeOfferState;
import net.runelite.client.ui.ColorScheme;
import net.runelite.api.ItemComposition;

public class InProgressPanel extends JPanel {
    private JPanel container;
    private JPanel itemInfoContainer;
    private JLabel quantityProgressLabel;
    private JProgressBar progressBar;

    public InProgressPanel(ItemComposition item, BufferedImage itemImage, GrandExchangeOffer offer) {
        setLayout(new BorderLayout());
        setBackground(ColorScheme.DARK_GRAY_COLOR);

        container = new JPanel(new BorderLayout());
        container.setBackground(ColorScheme.DARK_GRAY_COLOR);

        quantityProgressLabel = new JLabel();
        quantityProgressLabel.setForeground(ColorScheme.GRAND_EXCHANGE_PRICE);
        quantityProgressLabel.setHorizontalAlignment(JLabel.CENTER);

        progressBar = new JProgressBar(0, 100);
        progressBar.setBackground(ColorScheme.DARK_GRAY_COLOR);
        progressBar.setForeground(ColorScheme.GRAND_EXCHANGE_PRICE);
        progressBar.setStringPainted(true);

        constructItemInfo();

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

    private void constructItemInfo() {
        itemInfoContainer = new JPanel(new GridLayout(1, 2, 0, 0));
        itemInfoContainer.setBackground(ColorScheme.DARK_GRAY_COLOR);

        JPanel column1 = new JPanel(new BorderLayout());
        column1.setBackground(ColorScheme.DARK_GRAY_COLOR);

        JPanel contentPanel1 = new JPanel(new GridLayout(0, 1));
        contentPanel1.setBackground(ColorScheme.DARK_GRAY_COLOR);

        JPanel totalValuePanel = new JPanel(new BorderLayout());
        totalValuePanel.setBackground(ColorScheme.DARK_GRAY_COLOR);
        totalValuePanel.add(newLeftLabel("Total Value:"), BorderLayout.WEST);
        totalValuePanel.add(newRightLabel("0", ColorScheme.GRAND_EXCHANGE_ALCH), BorderLayout.EAST);
        contentPanel1.add(totalValuePanel);

        JPanel pricePerPanel = new JPanel(new BorderLayout());
        pricePerPanel.setBackground(ColorScheme.DARK_GRAY_COLOR);
        pricePerPanel.add(newLeftLabel("Price Per:"), BorderLayout.WEST);
        pricePerPanel.add(newRightLabel("0", ColorScheme.GRAND_EXCHANGE_ALCH), BorderLayout.EAST);
        contentPanel1.add(pricePerPanel);

        column1.add(contentPanel1, BorderLayout.CENTER);

        JPanel column2 = new JPanel(new BorderLayout());
        column2.setBackground(ColorScheme.DARK_GRAY_COLOR);

        JPanel contentPanel2 = new JPanel(new GridLayout(0, 1));
        contentPanel2.setBackground(ColorScheme.DARK_GRAY_COLOR);

        JPanel spentPanel = new JPanel(new BorderLayout());
        spentPanel.setBackground(ColorScheme.DARK_GRAY_COLOR);
        spentPanel.add(newLeftLabel("Spent:"), BorderLayout.WEST);
        spentPanel.add(newRightLabel("0", ColorScheme.GRAND_EXCHANGE_ALCH), BorderLayout.EAST);
        contentPanel2.add(spentPanel);

        JPanel spentPerPanel = new JPanel(new BorderLayout());
        spentPerPanel.setBackground(ColorScheme.DARK_GRAY_COLOR);
        spentPerPanel.add(newLeftLabel("Spent Per:"), BorderLayout.WEST);
        spentPerPanel.add(newRightLabel("0", ColorScheme.GRAND_EXCHANGE_ALCH), BorderLayout.EAST);
        contentPanel2.add(spentPerPanel);

        column2.add(contentPanel2, BorderLayout.CENTER);

        itemInfoContainer.add(column1);
        itemInfoContainer.add(column2);
    }
    public void updateOffer(ItemComposition offerItem, BufferedImage itemImage, GrandExchangeOffer offer) {
        if (container.getComponentCount() == 3) {
            container.remove(2);
            container.remove(1);
            container.remove(0);
        }
        InProgressHeader header = new InProgressHeader(offerItem, itemImage, offer);
        container.add(header, BorderLayout.NORTH);

        JPanel titlePanel = new JPanel(new GridLayout(1, 1, 0, 0));
        titlePanel.setBackground(ColorScheme.DARK_GRAY_COLOR);
        String offerType = offer.getState() == GrandExchangeOfferState.BUYING ? "Buy" : "Sell";
        JLabel offerTypeLabel = new JLabel(offerType);
        offerTypeLabel.setHorizontalAlignment(JLabel.CENTER);
        offerTypeLabel.setForeground(ColorScheme.GRAND_EXCHANGE_PRICE);
        titlePanel.add(offerTypeLabel);

        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBackground(ColorScheme.DARK_GRAY_COLOR);
        centerPanel.add(titlePanel, BorderLayout.NORTH);
        constructItemInfo();
        centerPanel.add(itemInfoContainer, BorderLayout.CENTER);
        container.add(centerPanel, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBackground(ColorScheme.DARK_GRAY_COLOR);
        bottomPanel.add(quantityProgressLabel, BorderLayout.NORTH);
        bottomPanel.add(progressBar, BorderLayout.SOUTH);
        container.add(bottomPanel, BorderLayout.SOUTH);

        int quantitySold = offer.getQuantitySold();
        int totalQuantity = offer.getTotalQuantity();
        int spent = offer.getSpent();
        int price = offer.getPrice();

        quantityProgressLabel.setText(Numbers.numberWithCommas(quantitySold) + " / " + Numbers.numberWithCommas(totalQuantity));

        int percentage = totalQuantity > 0 ? (int) (((double) quantitySold / totalQuantity) * 100) : 0;
        progressBar.setValue(percentage);
        progressBar.setString(percentage + "%");

        JPanel contentPanel1 = (JPanel) ((JPanel) itemInfoContainer.getComponent(0)).getComponent(0);
        JPanel contentPanel2 = (JPanel) ((JPanel) itemInfoContainer.getComponent(1)).getComponent(0);

        JLabel totalValueValue = (JLabel) ((JPanel) contentPanel1.getComponent(0)).getComponent(1);
        int totalValue = totalQuantity * price;
        totalValueValue.setText(Numbers.toShortNumber(totalValue));
        totalValueValue.setToolTipText(Numbers.numberWithCommas(totalValue));


        JLabel pricePerValue = (JLabel) ((JPanel) contentPanel1.getComponent(1)).getComponent(1);
        pricePerValue.setText(Numbers.toShortNumber(price));
        pricePerValue.setToolTipText(Numbers.numberWithCommas(price));


        JLabel spentValue = (JLabel) ((JPanel) contentPanel2.getComponent(0)).getComponent(1);
        spentValue.setText(Numbers.toShortNumber(spent));
        spentValue.setToolTipText(Numbers.numberWithCommas(spent));

        int spentPer = (quantitySold > 0) ? spent / quantitySold : 0;
        JLabel spentPerValue = (JLabel) ((JPanel) contentPanel2.getComponent(1)).getComponent(1);
        spentPerValue.setText(Numbers.toShortNumber(spentPer));
        spentPerValue.setToolTipText(Numbers.numberWithCommas(spentPer));

        revalidate();
        repaint();
    }

    public void reset() {
        container.removeAll();
        revalidate();
        repaint();
    }
}