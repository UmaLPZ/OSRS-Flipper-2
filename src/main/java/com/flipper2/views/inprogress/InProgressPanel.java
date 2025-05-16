package com.flipper2.views.inprogress;

import java.awt.Dimension;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.Color;
import java.awt.image.BufferedImage;

import com.flipper2.helpers.Numbers;
import com.flipper2.helpers.UiUtilities;
import com.flipper2.helpers.CustomPanel;
import com.flipper2.views.components.InProgressHeader;

import net.runelite.api.GrandExchangeOffer;
import net.runelite.api.GrandExchangeOfferState;
import net.runelite.client.ui.ColorScheme;
import net.runelite.api.ItemComposition;

import static com.flipper2.helpers.GrandExchange.checkIsActive;
import static com.flipper2.helpers.GrandExchange.checkIsBoughtSold;
import static com.flipper2.helpers.GrandExchange.checkIsBuy;
import static com.flipper2.helpers.GrandExchange.checkIsCancelState;

public class InProgressPanel extends JPanel
{
	private JPanel container;
	private JPanel itemInfoContainer;
	private JLabel quantityProgressLabel;
	private JProgressBar progressBar;

	public InProgressPanel(ItemComposition item, BufferedImage itemImage, GrandExchangeOffer offer)
	{
		setLayout(new BorderLayout());
		setBackground(ColorScheme.DARK_GRAY_COLOR);

		container = new JPanel(new BorderLayout());
		container.setBackground(ColorScheme.DARK_GRAY_COLOR);

		quantityProgressLabel = new JLabel();
		quantityProgressLabel.setForeground(ColorScheme.GRAND_EXCHANGE_PRICE);
		quantityProgressLabel.setBorder(new EmptyBorder(3, 0, 3, 0));
		quantityProgressLabel.setHorizontalAlignment(JLabel.CENTER);

		progressBar = new JProgressBar(0, 100);
		progressBar.setBackground(ColorScheme.DARK_GRAY_COLOR);
		progressBar.setForeground(ColorScheme.PROGRESS_INPROGRESS_COLOR);
		progressBar.setStringPainted(true);

		constructItemInfo();

		container.setBorder(UiUtilities.ITEM_INFO_BORDER);
		this.add(container, BorderLayout.NORTH);
		this.setBorder(new EmptyBorder(3, 5, 3, 5));
	}

	private JLabel newLeftLabel(String text)
	{
		JLabel newLeftJLabel = new JLabel(text);
		newLeftJLabel.setVerticalAlignment(JLabel.CENTER);
		newLeftJLabel.setVerticalAlignment(JLabel.CENTER);
		newLeftJLabel.setForeground(Color.white);
		newLeftJLabel.setBorder(new EmptyBorder(4, 2, 3, 2));
		return newLeftJLabel;
	}

	private JLabel newRightLabel(String value, Color fontColor)
	{
		JLabel newRightLabel = new JLabel(value);
		newRightLabel.setHorizontalAlignment(JLabel.CENTER);
		newRightLabel.setVerticalAlignment(JLabel.CENTER);
		newRightLabel.setForeground(fontColor);
		newRightLabel.setBorder(new EmptyBorder(4, 2, 3, 2));
		return newRightLabel;
	}

	private void constructItemInfo()
	{
		itemInfoContainer = new JPanel(new GridLayout(1, 3, 0, 0));
		itemInfoContainer.setBackground(ColorScheme.DARK_GRAY_COLOR);

		JPanel column1 = new JPanel(new BorderLayout());
		column1.setBackground(ColorScheme.DARK_GRAY_COLOR);

		JPanel labelPanel = new JPanel(new GridLayout(0, 1));
		labelPanel.setBackground(ColorScheme.DARK_GRAY_COLOR);

		JPanel offerTotalPanelLabel = new CustomPanel(new BorderLayout(), true);
		offerTotalPanelLabel.add(newLeftLabel("Offer Total:"), BorderLayout.WEST);
		labelPanel.add(offerTotalPanelLabel);

		JPanel pricePerPanelLabel = new CustomPanel(new BorderLayout(), true);
		pricePerPanelLabel.add(newLeftLabel("Price/Per:"), BorderLayout.WEST);
		labelPanel.add(pricePerPanelLabel);

		column1.add(labelPanel, BorderLayout.CENTER);


		JPanel column2 = new JPanel(new BorderLayout());
		column2.setBackground(ColorScheme.DARK_GRAY_COLOR);

		JPanel contentPanel1 = new JPanel(new GridLayout(0, 1));
		contentPanel1.setBackground(ColorScheme.DARK_GRAY_COLOR);

		JPanel totalValuePanel = new CustomPanel(new BorderLayout(), true);
		totalValuePanel.add(newRightLabel("0", ColorScheme.GRAND_EXCHANGE_ALCH));

		contentPanel1.add(totalValuePanel);

		JPanel pricePerPanel = new CustomPanel(new BorderLayout(), true);
		pricePerPanel.add(newRightLabel("0", ColorScheme.GRAND_EXCHANGE_ALCH));
		contentPanel1.add(pricePerPanel);

		column2.add(contentPanel1, BorderLayout.CENTER);

		JPanel column3 = new JPanel(new BorderLayout());
		column3.setBackground(ColorScheme.DARK_GRAY_COLOR);

		JPanel contentPanel2 = new JPanel(new GridLayout(0, 1));
		contentPanel2.setBackground(ColorScheme.DARK_GRAY_COLOR);

		JPanel spentPanel = new CustomPanel(new BorderLayout(), true);
		spentPanel.add(newRightLabel("0", ColorScheme.GRAND_EXCHANGE_ALCH));
		contentPanel2.add(spentPanel);

		JPanel spentPerPanel = new CustomPanel(new BorderLayout(), true);
		spentPerPanel.add(newRightLabel("0", ColorScheme.GRAND_EXCHANGE_ALCH));
		contentPanel2.add(spentPerPanel);

		column3.add(contentPanel2, BorderLayout.CENTER);

		itemInfoContainer.add(column1);
		itemInfoContainer.add(column2);
		itemInfoContainer.add(column3);
	}

	public void updateOffer(ItemComposition offerItem, BufferedImage itemImage, GrandExchangeOffer offer)
	{
		GrandExchangeOfferState state = offer.getState();
		boolean isBuy = checkIsBuy(state);
		boolean isActive = checkIsActive(state);
		boolean isBoughtSold = checkIsBoughtSold(state);
		boolean isCancelled = checkIsCancelState(state);

		if (container.getComponentCount() == 3)
		{
			container.remove(2);
			container.remove(1);
			container.remove(0);
		}
		InProgressHeader header = new InProgressHeader(offerItem, itemImage, offer);
		container.add(header, BorderLayout.NORTH);

		JPanel titlePanel = new JPanel(new GridLayout(1, 3, 0, 0));
		titlePanel.setBackground(ColorScheme.DARK_GRAY_COLOR);
		titlePanel.setBorder(new EmptyBorder(2, 2, 2, 2));


		String offerType;
		Color offerColor;
		if (isActive)
		{
			offerType = "Active";
			offerColor = ColorScheme.PROGRESS_INPROGRESS_COLOR;
		}
		else if (isBoughtSold)
		{
			offerType = "Complete";
			offerColor = ColorScheme.PROGRESS_COMPLETE_COLOR;
		}
		else if (isCancelled)
		{
			offerType = "Cancelled";
			offerColor = ColorScheme.PROGRESS_ERROR_COLOR;
		}
		else
		{
			offerType = "Error";
			offerColor = ColorScheme.PROGRESS_ERROR_COLOR;
		}

		JLabel offerTypeLabel = new JLabel(offerType);
		offerTypeLabel.setHorizontalAlignment(JLabel.LEFT);
		offerTypeLabel.setForeground(offerColor);

		String offerInit = ("Offered");
		JLabel offerInitLabel = new JLabel(offerInit);
		offerInitLabel.setHorizontalAlignment(JLabel.CENTER);
		offerInitLabel.setForeground(Color.white);

		String offerCurrent = (isBuy ? "Spent" : "Received");
		JLabel offerCurrentLabel = new JLabel(offerCurrent);
		offerCurrentLabel.setHorizontalAlignment(JLabel.CENTER);
		offerCurrentLabel.setForeground(Color.white);

		titlePanel.add(offerTypeLabel);
		titlePanel.add(offerInitLabel);
		titlePanel.add(offerCurrentLabel);


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

		quantityProgressLabel.setText(Numbers.toShortNumber(quantitySold) + " / " + Numbers.toShortNumber(totalQuantity));
		quantityProgressLabel.setToolTipText(Numbers.numberWithCommas(quantitySold) + " / " + Numbers.numberWithCommas(totalQuantity));

		int percentage = totalQuantity > 0 ? (int) (((double) quantitySold / totalQuantity) * 100) : 0;
		progressBar.setValue(percentage);
		progressBar.setString(percentage + "%");
		if (isActive && percentage <= 49)
		{
			progressBar.setForeground(ColorScheme.PROGRESS_INPROGRESS_COLOR);
		}
		else if (isActive && percentage >= 50 && percentage <= 99)
		{
			progressBar.setForeground(ColorScheme.PROGRESS_COMPLETE_COLOR.brighter());
		}
		else if (isBoughtSold && percentage == 100)
		{
			progressBar.setForeground(ColorScheme.PROGRESS_COMPLETE_COLOR);
		}
		else if (isCancelled)
		{
			progressBar.setForeground(ColorScheme.PROGRESS_ERROR_COLOR);
		}


		JPanel contentPanel1 = (JPanel) ((JPanel) itemInfoContainer.getComponent(1)).getComponent(0);
		JPanel contentPanel2 = (JPanel) ((JPanel) itemInfoContainer.getComponent(2)).getComponent(0);

		JLabel totalValueValue = (JLabel) ((JPanel) contentPanel1.getComponent(0)).getComponent(0);
		int totalValue = totalQuantity * price;
		totalValueValue.setText(Numbers.toShortNumber(totalValue));
		totalValueValue.setToolTipText(Numbers.numberWithCommas(totalValue));


		JLabel pricePerValue = (JLabel) ((JPanel) contentPanel1.getComponent(1)).getComponent(0);
		pricePerValue.setText(Numbers.toShortNumber(price));
		pricePerValue.setToolTipText(Numbers.numberWithCommas(price));


		JLabel spentValue = (JLabel) ((JPanel) contentPanel2.getComponent(0)).getComponent(0);
		spentValue.setText(Numbers.toShortNumber(spent));
		spentValue.setToolTipText(Numbers.numberWithCommas(spent));

		int spentPer = (quantitySold > 0) ? spent / quantitySold : 0;
		JLabel spentPerValue = (JLabel) ((JPanel) contentPanel2.getComponent(1)).getComponent(0);
		spentPerValue.setText(Numbers.toShortNumber(spentPer));
		spentPerValue.setToolTipText(Numbers.numberWithCommas(spentPer));

		revalidate();
		repaint();
	}

	@Override
	public Dimension getPreferredSize()
	{
		Dimension preferredSize = super.getPreferredSize();
		return new Dimension(container.getPreferredSize().width, preferredSize.height);
	}

	public void reset()
	{
		container.removeAll();
		revalidate();
		repaint();
	}
}