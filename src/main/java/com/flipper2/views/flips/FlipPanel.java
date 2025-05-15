package com.flipper2.views.flips;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.util.UUID;
import java.util.function.Consumer;
import java.awt.Color;
import java.awt.event.*;
import java.awt.FlowLayout;

import com.flipper2.helpers.Numbers;
import com.flipper2.helpers.Timestamps;
import com.flipper2.helpers.UiUtilities;
import com.flipper2.helpers.CustomPanel;
import com.flipper2.models.Flip;
import com.flipper2.views.components.DeleteButton;
import com.flipper2.views.components.ItemHeader;

import net.runelite.client.ui.ColorScheme;
import net.runelite.client.game.ItemManager;

public class FlipPanel extends JPanel
{
	private Flip flip;
	private JPanel container;
	private JPanel itemInfoContainer;

	public FlipPanel(
		Flip flip,
		ItemManager itemManager,
		Consumer<UUID> removeFlipConsumer,
		boolean isPrompt
	)
	{
		this.flip = flip;
		setLayout(new BorderLayout());
		setBackground(ColorScheme.DARK_GRAY_COLOR);

		container = new JPanel(new BorderLayout());
		container.setBackground(ColorScheme.DARK_GRAY_COLOR);

		DeleteButton deleteFlipButton = new DeleteButton((ActionEvent action) -> {
			String describedBuy = flip.describeFlip();
			int input = isPrompt
				? JOptionPane.showConfirmDialog(null, "Delete flip of " + describedBuy + "?")
				: 0;
			if (input == 0)
			{
				removeFlipConsumer.accept(flip.getFlipId());
				setVisible(false);
			}
		});

		JPanel itemHeader = new ItemHeader(
			flip.getItemId(),
			flip.getSellPrice(),
			flip.getItemName(),
			itemManager,
			false,
			deleteFlipButton
		);
		container.add(itemHeader, BorderLayout.NORTH);

		JPanel centerPanel = new JPanel(new BorderLayout());
		centerPanel.setBackground(ColorScheme.DARK_GRAY_COLOR);

		JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
		titlePanel.setBackground(ColorScheme.DARK_GRAY_COLOR);
		titlePanel.setBorder(new EmptyBorder(4, 0, 4, 0));

		String amountFlippedText = "Amount Flipped: ";
		JLabel amountFlippedTextLabel = new JLabel(amountFlippedText);
		amountFlippedTextLabel.setForeground(Color.white);

		String amountFlippedInt = Integer.toString(this.flip.getQuantity());
		JLabel amountFlippedIntLabel = new JLabel(Numbers.toShortNumber(Integer.parseInt(amountFlippedInt)));
		amountFlippedIntLabel.setToolTipText(Numbers.numberWithCommas(amountFlippedInt));
		amountFlippedIntLabel.setForeground(ColorScheme.GRAND_EXCHANGE_PRICE);

		titlePanel.setOpaque(false);
		titlePanel.add(amountFlippedTextLabel);
		titlePanel.add(amountFlippedIntLabel);

		centerPanel.add(titlePanel, BorderLayout.NORTH);

		constructItemInfo();
		centerPanel.add(itemInfoContainer, BorderLayout.CENTER);

		container.add(centerPanel, BorderLayout.CENTER);

		JPanel datePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
		datePanel.setBackground(ColorScheme.DARK_GRAY_COLOR);
		datePanel.setBorder(new EmptyBorder(4, 0, 4, 0));

		String dateTextString = "Date: ";
		JLabel dateTextLabel = new JLabel(dateTextString);
		dateTextLabel.setForeground(Color.white);

		String dateString = Timestamps.format(flip.getCreatedAt());
		JLabel dateLabel = new JLabel(dateString);
		dateLabel.setForeground(ColorScheme.GRAND_EXCHANGE_ALCH);

		datePanel.setOpaque(false);
		datePanel.add(dateTextLabel);
		datePanel.add(dateLabel);

		container.add(datePanel, BorderLayout.SOUTH);

		container.setBorder(UiUtilities.ITEM_INFO_BORDER);
		this.add(container, BorderLayout.NORTH);
		this.setBorder(new EmptyBorder(0, 5, 3, 0));
	}

	private JLabel newLeftLabel(String text)
	{
		JLabel newLeftJLabel = new JLabel(text);
		newLeftJLabel.setForeground(ColorScheme.GRAND_EXCHANGE_PRICE);
		newLeftJLabel.setBorder(new EmptyBorder(2, 2, 2, 2));
		return newLeftJLabel;
	}

	private JLabel newRightLabel(String value, Color fontColor)
	{
		JLabel newRightLabel = new JLabel(value);
		newRightLabel.setHorizontalAlignment(JLabel.RIGHT);
		newRightLabel.setForeground(fontColor);
		newRightLabel.setBorder(new EmptyBorder(2, 2, 2, 2));
		return newRightLabel;
	}

	private void constructItemInfo()
	{
		itemInfoContainer = new JPanel(new GridLayout(1, 2, 0, 0));
		itemInfoContainer.setBackground(ColorScheme.DARK_GRAY_COLOR);

		JPanel column1 = new JPanel(new BorderLayout());
		column1.setBackground(ColorScheme.DARK_GRAY_COLOR);

		JPanel contentPanel1 = new JPanel(new GridLayout(0, 1));
		contentPanel1.setBackground(ColorScheme.DARK_GRAY_COLOR);

		JPanel buyPricePanel = new CustomPanel(new BorderLayout(), true);
		int buyPrice = flip.getBuyPrice();
		JLabel buyPriceLabel = newLeftLabel("Buy Price:");
		buyPriceLabel.setForeground(Color.white);
		JLabel buyPriceValue = newRightLabel(Numbers.toShortNumber(buyPrice), ColorScheme.GRAND_EXCHANGE_ALCH);
		buyPriceValue.setToolTipText(Numbers.numberWithCommas(buyPrice));
		buyPricePanel.add(buyPriceLabel, BorderLayout.WEST);
		buyPricePanel.add(buyPriceValue, BorderLayout.EAST);
		contentPanel1.add(buyPricePanel);

		JPanel profitPerPanel = new CustomPanel(new BorderLayout(), true);
		int profitEach = flip.getSellPrice() - flip.getBuyPrice() - flip.getTax();
		String profitEachText = Numbers.toShortNumber(profitEach);
		Color profitEachColor = profitEach > 0 ? ColorScheme.GRAND_EXCHANGE_ALCH : ColorScheme.PROGRESS_ERROR_COLOR;
		JLabel profitPerLabel = newLeftLabel("Profit Per:");
		profitPerLabel.setForeground(Color.white);
		JLabel profitPerValue = newRightLabel(profitEachText, profitEachColor);
		profitPerValue.setToolTipText(Numbers.numberWithCommas(profitEach));
		profitPerPanel.add(profitPerLabel, BorderLayout.WEST);
		profitPerPanel.add(profitPerValue, BorderLayout.EAST);
		contentPanel1.add(profitPerPanel);

		JPanel taxPerPanel = new CustomPanel(new BorderLayout(), true);
		int taxPer = flip.getTax();
		String taxPerText = Numbers.toShortNumber(taxPer);
		JLabel taxPerLabel = newLeftLabel("Tax Per:");
		taxPerLabel.setForeground(Color.white);
		JLabel taxPerValue = newRightLabel(taxPerText, ColorScheme.PROGRESS_ERROR_COLOR);
		taxPerValue.setToolTipText(Numbers.numberWithCommas(taxPer));
		taxPerPanel.add(taxPerLabel, BorderLayout.WEST);
		taxPerPanel.add(taxPerValue, BorderLayout.EAST);
		contentPanel1.add(taxPerPanel);

		column1.add(contentPanel1, BorderLayout.CENTER);

		JPanel column2 = new JPanel(new BorderLayout());
		column2.setBackground(ColorScheme.DARK_GRAY_COLOR);

		JPanel contentPanel2 = new JPanel(new GridLayout(0, 1));
		contentPanel2.setBackground(ColorScheme.DARK_GRAY_COLOR);

		JPanel sellPricePanel = new CustomPanel(new BorderLayout(), true);
		sellPricePanel.setBackground(ColorScheme.DARK_GRAY_COLOR);
		int sellPrice = flip.getSellPrice();
		String sellPriceText = Numbers.toShortNumber(sellPrice);
		JLabel sellPriceLabel = newLeftLabel("Sell Price:");
		sellPriceLabel.setForeground(Color.white);
		JLabel sellPriceValue = newRightLabel(sellPriceText, ColorScheme.GRAND_EXCHANGE_ALCH);
		sellPriceValue.setToolTipText(Numbers.numberWithCommas(sellPrice));

		sellPricePanel.add(sellPriceLabel, BorderLayout.WEST);
		sellPricePanel.add(sellPriceValue, BorderLayout.EAST);
		contentPanel2.add(sellPricePanel);

		JPanel totalProfitPanel = new CustomPanel(new BorderLayout(), true);
		int totalProfit = flip.getTotalProfit();
		String totalProfitText = Numbers.toShortNumber(totalProfit);
		Color profitColor = totalProfit > 0 ? ColorScheme.GRAND_EXCHANGE_ALCH : ColorScheme.PROGRESS_ERROR_COLOR;
		JLabel totalProfitLabel = newLeftLabel("Total Profit:");
		totalProfitLabel.setForeground(Color.white);
		JLabel totalProfitValue = newRightLabel(totalProfitText, profitColor);
		totalProfitValue.setToolTipText(Numbers.numberWithCommas(totalProfit));
		totalProfitPanel.add(totalProfitLabel, BorderLayout.WEST);
		totalProfitPanel.add(totalProfitValue, BorderLayout.EAST);
		contentPanel2.add(totalProfitPanel);

		JPanel totalTaxPanel = new CustomPanel(new BorderLayout(), true);
		int totalTax = flip.getTotalTax();
		String totalTaxText = Numbers.toShortNumber(totalTax);
		JLabel totalTaxLabel = newLeftLabel("Total Tax:");
		totalTaxLabel.setForeground(Color.white);
		JLabel totalTaxValue = newRightLabel(totalTaxText, ColorScheme.PROGRESS_ERROR_COLOR);
		totalTaxValue.setToolTipText(Numbers.numberWithCommas(totalTax));
		totalTaxPanel.add(totalTaxLabel, BorderLayout.WEST);
		totalTaxPanel.add(totalTaxValue, BorderLayout.EAST);
		contentPanel2.add(totalTaxPanel);

		column2.add(contentPanel2, BorderLayout.CENTER);

		itemInfoContainer.add(column1);
		itemInfoContainer.add(column2);
	}
}