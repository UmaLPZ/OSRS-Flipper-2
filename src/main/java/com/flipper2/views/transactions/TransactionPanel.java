package com.flipper2.views.transactions;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Color;
import java.awt.event.*;
import java.awt.Dimension;

import com.flipper2.helpers.UiUtilities;
import com.flipper2.helpers.Numbers;
import com.flipper2.helpers.Timestamps;
import com.flipper2.helpers.CustomPanel;
import com.flipper2.models.Transaction;
import com.flipper2.views.components.DeleteButton;
import com.flipper2.views.components.ItemHeader;

import net.runelite.client.game.ItemManager;
import net.runelite.client.ui.ColorScheme;

import java.util.UUID;
import java.util.function.Consumer;

public class TransactionPanel extends JPanel
{
	private Transaction transaction;
	private JPanel container;
	private JPanel itemInfoContainer;

	public TransactionPanel(
		String name,
		Transaction transaction,
		ItemManager itemManager,
		Consumer<UUID> removeTransactionConsumer,
		boolean isPrompt
	)
	{
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
	)
	{
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
			if (input == 0)
			{
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

		JPanel titlePanel = new JPanel(new GridLayout(1, 3, 0, 0));
		titlePanel.setBackground(ColorScheme.DARK_GRAY_COLOR);

		JLabel blankLabel = new JLabel("");
		titlePanel.add(blankLabel);

		JLabel initialLabel = new JLabel("Initial");
		initialLabel.setHorizontalAlignment(JLabel.CENTER);
		initialLabel.setForeground(Color.white);
		titlePanel.add(initialLabel);

		JLabel finalLabel = new JLabel("Final");
		finalLabel.setHorizontalAlignment(JLabel.CENTER);
		finalLabel.setForeground(Color.white);
		titlePanel.add(finalLabel);

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

		String dateString = Timestamps.format(transaction.getCreatedTime());
		JLabel dateLabel = new JLabel(dateString);
		dateLabel.setForeground(ColorScheme.GRAND_EXCHANGE_ALCH);

		datePanel.setOpaque(false);
		datePanel.add(dateTextLabel);
		datePanel.add(dateLabel);

		container.add(datePanel, BorderLayout.SOUTH);

		container.setBorder(UiUtilities.ITEM_INFO_BORDER);
		this.add(container, BorderLayout.NORTH);
		this.setBorder(new EmptyBorder(1, 5, 1, 0));
	}

	private JLabel newLeftLabel(String text)
	{
		JLabel newLeftJLabel = new JLabel(text);
		newLeftJLabel.setVerticalAlignment(JLabel.CENTER);
		newLeftJLabel.setForeground(Color.white);
		newLeftJLabel.setBorder(new EmptyBorder(3, 2, 2, 2));
		return newLeftJLabel;
	}

	private JLabel newRightLabel(String value, Color fontColor)
	{
		JLabel newRightLabel = new JLabel(value);
		newRightLabel.setHorizontalAlignment(JLabel.CENTER);
		newRightLabel.setVerticalAlignment(JLabel.CENTER);
		newRightLabel.setForeground(fontColor);
		newRightLabel.setBorder(new EmptyBorder(3, 2, 2, 2));
		return newRightLabel;
	}

	private int calculateTax(int pricePer)
	{
		int tax = (int) Math.floor(pricePer * Transaction.TAX_RATE);
		return Math.min(tax, Transaction.MAX_TAX);
	}

	private void constructItemInfo()
	{
		itemInfoContainer = new JPanel(new GridLayout(1, 3, 0, 0));
		itemInfoContainer.setBackground(ColorScheme.DARK_GRAY_COLOR);

		JPanel labelColumn = new JPanel(new BorderLayout());
		labelColumn.setBackground(ColorScheme.DARK_GRAY_COLOR);

		JPanel labelPanel = new JPanel(new GridLayout(0, 1));
		labelPanel.setBackground(ColorScheme.DARK_GRAY_COLOR);

		JPanel initQuantityLabel = new CustomPanel(new BorderLayout(), true);
		JLabel quantityLabelInit = newLeftLabel("Quantity:");
		initQuantityLabel.add(quantityLabelInit, BorderLayout.WEST);
		labelPanel.add(initQuantityLabel);

		JPanel initPriceLabel = new CustomPanel(new BorderLayout(), true);
		JLabel priceLabelInit = newLeftLabel("Price/Per:");
		initPriceLabel.add(priceLabelInit, BorderLayout.WEST);
		labelPanel.add(initPriceLabel);

		if (!transaction.isBuy())
		{
			JPanel taxPanelLabel = new CustomPanel(new BorderLayout(), true);
			JLabel taxLabel = newLeftLabel("Tax/Per:");
			taxPanelLabel.add(taxLabel, BorderLayout.WEST);
			labelPanel.add(taxPanelLabel);
		}

		JPanel initTotalLabel = new CustomPanel(new BorderLayout(), true);
		JLabel totalLabelInit = newLeftLabel("Total Value:");
		initTotalLabel.add(totalLabelInit, BorderLayout.WEST);
		labelPanel.add(initTotalLabel);

		labelColumn.add(labelPanel, BorderLayout.CENTER);

		JPanel column1 = new JPanel(new BorderLayout());
		column1.setBackground(ColorScheme.DARK_GRAY_COLOR);

		JPanel contentPanel1 = new JPanel(new GridLayout(0, 1));
		contentPanel1.setBackground(ColorScheme.DARK_GRAY_COLOR);


		JPanel initQuantityPanel = new CustomPanel(new BorderLayout(), true);
		String quantityValueTextInit = Numbers.numberWithCommas(transaction.getTotalQuantity());

		JLabel quantityValueLabelInit = newRightLabel(quantityValueTextInit, ColorScheme.GRAND_EXCHANGE_ALCH);

		initQuantityPanel.add(quantityValueLabelInit, BorderLayout.CENTER);
		contentPanel1.add(initQuantityPanel);

		JPanel initPricePanel = new CustomPanel(new BorderLayout(), true);
		int initPricePer = transaction.getInitPricePer();
		String initPricePerText = Numbers.toShortNumber(initPricePer);

		JLabel pricePerValueLabelInit = newRightLabel(initPricePerText, ColorScheme.GRAND_EXCHANGE_ALCH);
		pricePerValueLabelInit.setToolTipText(Numbers.numberWithCommas(initPricePer));

		initPricePanel.add(pricePerValueLabelInit, BorderLayout.CENTER);
		contentPanel1.add(initPricePanel);

		if (!transaction.isBuy())
		{
			JPanel taxPanel = new CustomPanel(new BorderLayout(), true);
			int initialTax = calculateTax(transaction.getInitPricePer());
			String initialTaxText = Numbers.toShortNumber(initialTax);

			JLabel taxValueLabel = newRightLabel(initialTaxText, ColorScheme.PROGRESS_ERROR_COLOR);
			taxValueLabel.setToolTipText(Numbers.numberWithCommas(initialTax));

			taxPanel.add(taxValueLabel, BorderLayout.CENTER);
			contentPanel1.add(taxPanel);
		}

		JPanel initTotalPanel = new CustomPanel(new BorderLayout(), true);
		int initTotalValue = transaction.getInitPricePer() * transaction.getQuantity();
		String initTotalValueText = Numbers.toShortNumber(initTotalValue);

		JLabel totalValueValueLabelInit = newRightLabel(initTotalValueText, ColorScheme.GRAND_EXCHANGE_ALCH);
		totalValueValueLabelInit.setToolTipText(Numbers.numberWithCommas(initTotalValue));

		initTotalPanel.add(totalValueValueLabelInit, BorderLayout.CENTER);
		contentPanel1.add(initTotalPanel);

		column1.add(contentPanel1, BorderLayout.CENTER);

		JPanel column2 = new JPanel(new BorderLayout());
		column2.setBackground(ColorScheme.DARK_GRAY_COLOR);

		JPanel contentPanel2 = new JPanel(new GridLayout(0, 1));
		contentPanel2.setBackground(ColorScheme.DARK_GRAY_COLOR);

		JPanel finQuantityPanel = new CustomPanel(new BorderLayout(), true);
		String quantityValueTextFin = Numbers.numberWithCommas(transaction.getQuantity());

		JLabel quantityValueLabelFin = newRightLabel(quantityValueTextFin, ColorScheme.GRAND_EXCHANGE_ALCH);
		finQuantityPanel.add(quantityValueLabelFin, BorderLayout.CENTER);
		contentPanel2.add(finQuantityPanel);

		JPanel finPricePanel = new CustomPanel(new BorderLayout(), true);
		int finPricePer = transaction.getFinPricePer();
		String finPricePerText = Numbers.toShortNumber(finPricePer);
		JLabel pricePerValueLabelFin = newRightLabel(finPricePerText, ColorScheme.GRAND_EXCHANGE_ALCH);
		pricePerValueLabelFin.setToolTipText(Numbers.numberWithCommas(finPricePer));
		finPricePanel.add(pricePerValueLabelFin, BorderLayout.CENTER);
		contentPanel2.add(finPricePanel);

		if (!transaction.isBuy())
		{
			JPanel taxPanel = new CustomPanel(new BorderLayout(), true);
			int finalTax = calculateTax(transaction.getFinPricePer());
			String finalTaxText = Numbers.toShortNumber(finalTax);
			JLabel taxValueLabel = newRightLabel(finalTaxText, ColorScheme.PROGRESS_ERROR_COLOR);
			taxValueLabel.setToolTipText(Numbers.numberWithCommas(finalTax));
			taxPanel.add(taxValueLabel, BorderLayout.CENTER);
			contentPanel2.add(taxPanel);
		}

		JPanel finTotalPanel = new CustomPanel(new BorderLayout(), true);
		int finTotalValue = transaction.getFinPricePer() * transaction.getQuantity();
		String finTotalValueText = Numbers.toShortNumber(finTotalValue);
		JLabel totalValueValueLabelFin = newRightLabel(finTotalValueText, ColorScheme.GRAND_EXCHANGE_ALCH);
		totalValueValueLabelFin.setToolTipText(Numbers.numberWithCommas(finTotalValue));
		finTotalPanel.add(totalValueValueLabelFin, BorderLayout.CENTER);
		contentPanel2.add(finTotalPanel);

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