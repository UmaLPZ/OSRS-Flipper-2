package com.flipper2.views.flips;

import java.awt.Dimension;
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

		int quantity = this.flip.getQuantity();
		JLabel amountFlippedIntLabel = new JLabel(Numbers.toShortNumber(quantity));
		amountFlippedIntLabel.setToolTipText(Numbers.numberWithCommas(quantity));
		amountFlippedIntLabel.setForeground(ColorScheme.GRAND_EXCHANGE_PRICE);

		titlePanel.setOpaque(false);
		titlePanel.add(amountFlippedTextLabel);
		titlePanel.add(amountFlippedIntLabel);

		centerPanel.add(titlePanel, BorderLayout.NORTH);

		JPanel colTitlePanel = new JPanel(new GridLayout(1, 3, 0, 0));
		colTitlePanel.setBackground(ColorScheme.DARK_GRAY_COLOR);

		JLabel blankLabel = new JLabel("");
		colTitlePanel.add(blankLabel);

		JLabel initialLabel = new JLabel("Per");
		initialLabel.setHorizontalAlignment(JLabel.CENTER);
		initialLabel.setForeground(Color.white);
		colTitlePanel.add(initialLabel);

		JLabel finalLabel = new JLabel("Total");
		finalLabel.setHorizontalAlignment(JLabel.CENTER);
		finalLabel.setForeground(Color.white);
		colTitlePanel.add(finalLabel);

		centerPanel.add(colTitlePanel, BorderLayout.CENTER);

		constructItemInfo();
		centerPanel.add(itemInfoContainer, BorderLayout.SOUTH);

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
		newLeftJLabel.setVerticalAlignment(JLabel.CENTER);
		newLeftJLabel.setForeground(Color.white);
		newLeftJLabel.setBorder(new EmptyBorder(2, 2, 2, 2));
		return newLeftJLabel;
	}

	private JLabel newRightLabel(String value, Color fontColor)
	{
		JLabel newRightLabel = new JLabel(value);
		newRightLabel.setHorizontalAlignment(JLabel.CENTER);
		newRightLabel.setVerticalAlignment(JLabel.CENTER);
		newRightLabel.setForeground(fontColor);
		newRightLabel.setBorder(new EmptyBorder(2, 2, 2, 2));
		return newRightLabel;
	}

	private void constructItemInfo()
	{
		itemInfoContainer = new JPanel(new GridLayout(1, 3, 0, 0));
		itemInfoContainer.setBackground(ColorScheme.DARK_GRAY_COLOR);

		JPanel labelColumn = new JPanel(new BorderLayout());
		labelColumn.setBackground(ColorScheme.DARK_GRAY_COLOR);

		JPanel labelPanel = new JPanel(new GridLayout(0, 1));
		labelPanel.setBackground(ColorScheme.DARK_GRAY_COLOR);

		JPanel buyColPanel = new CustomPanel(new BorderLayout(), true);
		JLabel buyColLabel = newLeftLabel("Buy Price:");
		buyColPanel.add(buyColLabel, BorderLayout.WEST);
		labelPanel.add(buyColPanel);

		JPanel sellColPanel = new CustomPanel(new BorderLayout(), true);
		JLabel sellColLabel = newLeftLabel("Sell Price:");
		sellColPanel.add(sellColLabel, BorderLayout.WEST);
		labelPanel.add(sellColPanel);

		JPanel profitColPanel = new CustomPanel(new BorderLayout(), true);
		JLabel profitColLabel = newLeftLabel("Profit:");
		profitColPanel.add(profitColLabel, BorderLayout.WEST);
		labelPanel.add(profitColPanel);

		JPanel taxColPanel = new CustomPanel(new BorderLayout(), true);
		JLabel taxColLabel = newLeftLabel("Tax:");
		taxColPanel.add(taxColLabel, BorderLayout.WEST);
		labelPanel.add(taxColPanel);

		labelColumn.add(labelPanel, BorderLayout.CENTER);

		JPanel column1 = new JPanel(new BorderLayout());
		column1.setBackground(ColorScheme.DARK_GRAY_COLOR);

		JPanel contentPanel1 = new JPanel(new GridLayout(0, 1));
		contentPanel1.setBackground(ColorScheme.DARK_GRAY_COLOR);

		JPanel buyPerPricePanel = new CustomPanel(new BorderLayout(), true);
		int buyPricePer = flip.getBuyPrice();
		JLabel buyPerPriceValue = newRightLabel(Numbers.toShortNumber(buyPricePer), ColorScheme.GRAND_EXCHANGE_ALCH);
		buyPerPriceValue.setToolTipText(Numbers.numberWithCommas(buyPricePer));
		buyPerPricePanel.add(buyPerPriceValue, BorderLayout.CENTER);
		contentPanel1.add(buyPerPricePanel);

		JPanel sellPerPricePanel = new CustomPanel(new BorderLayout(), true);
		int sellPricePer = flip.getSellPrice();
		JLabel sellPerPriceValue = newRightLabel(Numbers.toShortNumber(sellPricePer), ColorScheme.GRAND_EXCHANGE_ALCH);
		sellPerPriceValue.setToolTipText(Numbers.numberWithCommas(sellPricePer));
		sellPerPricePanel.add(sellPerPriceValue, BorderLayout.CENTER);
		contentPanel1.add(sellPerPricePanel);

		JPanel profitPerPanel = new CustomPanel(new BorderLayout(), true);
		int profitPer = flip.getProfitPerItem();
		Color profitPerColor = flip.getTotalProfit() > 0 ? ColorScheme.GRAND_EXCHANGE_ALCH : ColorScheme.PROGRESS_ERROR_COLOR;
		JLabel profitPerValue = newRightLabel(Numbers.toShortNumber(profitPer), profitPerColor);
		profitPerValue.setToolTipText(Numbers.numberWithCommas(profitPer));
		profitPerPanel.add(profitPerValue, BorderLayout.CENTER);
		contentPanel1.add(profitPerPanel);

		JPanel taxPerPanel = new CustomPanel(new BorderLayout(), true);
		int taxPer = flip.getTaxPerItem();
		JLabel taxPerValue = newRightLabel(Numbers.toShortNumber(taxPer), ColorScheme.PROGRESS_ERROR_COLOR);
		taxPerValue.setToolTipText(Numbers.numberWithCommas(taxPer));
		taxPerPanel.add(taxPerValue, BorderLayout.CENTER);
		contentPanel1.add(taxPerPanel);

		column1.add(contentPanel1, BorderLayout.CENTER);

		JPanel column2 = new JPanel(new BorderLayout());
		column2.setBackground(ColorScheme.DARK_GRAY_COLOR);

		JPanel contentPanel2 = new JPanel(new GridLayout(0, 1));
		contentPanel2.setBackground(ColorScheme.DARK_GRAY_COLOR);

		JPanel buyTotalPanel = new CustomPanel(new BorderLayout(), true);
		long totalBuyValue = flip.getTotalBuy();
		JLabel buyTotalValueLabel = newRightLabel(Numbers.toShortNumber((int) totalBuyValue), ColorScheme.GRAND_EXCHANGE_ALCH);
		buyTotalValueLabel.setToolTipText(Numbers.numberWithCommas((int) totalBuyValue));
		buyTotalPanel.add(buyTotalValueLabel, BorderLayout.CENTER);
		contentPanel2.add(buyTotalPanel);

		JPanel sellTotalPanel = new CustomPanel(new BorderLayout(), true);
		long totalSellValue = flip.getTotalSell();
		JLabel sellTotalValueLabel = newRightLabel(Numbers.toShortNumber((int) totalSellValue), ColorScheme.GRAND_EXCHANGE_ALCH);
		sellTotalValueLabel.setToolTipText(Numbers.numberWithCommas((int) totalSellValue));
		sellTotalPanel.add(sellTotalValueLabel, BorderLayout.CENTER);
		contentPanel2.add(sellTotalPanel);

		JPanel totalProfitPanel = new CustomPanel(new BorderLayout(), true);
		long totalProfitValue = flip.getTotalProfit();
		Color profitColor = totalProfitValue > 0 ? ColorScheme.GRAND_EXCHANGE_ALCH : ColorScheme.PROGRESS_ERROR_COLOR;
		JLabel totalProfitValueLabel = newRightLabel(Numbers.toShortNumber((int) totalProfitValue), profitColor);
		totalProfitValueLabel.setToolTipText(Numbers.numberWithCommas((int) totalProfitValue));
		totalProfitPanel.add(totalProfitValueLabel, BorderLayout.CENTER);
		contentPanel2.add(totalProfitPanel);

		JPanel totalTaxPanel = new CustomPanel(new BorderLayout(), true);
		int totalTaxValue = flip.getTotalTax();
		JLabel totalTaxValueLabel = newRightLabel(Numbers.toShortNumber(totalTaxValue), ColorScheme.PROGRESS_ERROR_COLOR);
		totalTaxValueLabel.setToolTipText(Numbers.numberWithCommas(totalTaxValue));
		totalTaxPanel.add(totalTaxValueLabel, BorderLayout.CENTER);
		contentPanel2.add(totalTaxPanel);

		column2.add(contentPanel2, BorderLayout.CENTER);

		itemInfoContainer.add(labelColumn);
		itemInfoContainer.add(column1);
		itemInfoContainer.add(column2);
	}

	@Override
	public Dimension getPreferredSize()
	{
		Dimension preferredSize = super.getPreferredSize();
		return new Dimension(container.getPreferredSize().width, preferredSize.height);
	}
}