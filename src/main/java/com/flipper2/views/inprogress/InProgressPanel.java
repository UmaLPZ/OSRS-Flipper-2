package com.flipper2.views.inprogress;

import com.flipper2.helpers.Log;
import com.flipper2.helpers.Timestamps;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.sql.Time;
import java.time.Instant;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.Color;
import java.awt.image.BufferedImage;
import com.flipper2.FlipperConfig;
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
	//Config
	private final FlipperConfig config;

	// Structure Panels
	private JPanel container;
	private InProgressHeader headerPanel;
	private JPanel titlePanel;
	private JPanel centerPanel;
	private JPanel itemInfoContainer;
	private JPanel offerStatusPanel;
	private JPanel bottomPanel;
	private JPanel datePanel;

	//titlePanel labels
	private JLabel offerTypeLabel;
	private JLabel offerInitLabel;
	private JLabel offerCurrentLabel;

	//itemInfoContainer labels
	private JLabel offerTotalValueLabel;
	private JLabel pricePerValueLabel;
	private JLabel actualSpentValueLabel;
	private JLabel actualPricePerValueLabel;

	//bottomPanel labels/components
	private JLabel quantityProgressLabel;
	private JProgressBar progressBar;
	private JLabel lastUpdateValueLabel;

	// data storage for config changes
	private ItemComposition currentItem;
	private BufferedImage currentItemImage;
	private GrandExchangeOffer currentOffer;
	private Instant lastUpdateTime;


	public InProgressPanel(ItemComposition item, BufferedImage itemImage, GrandExchangeOffer offer, FlipperConfig config)
	{
		this.config = config; // Store config FIRST

		// Store initial offer data
		this.currentItem = item;
		this.currentItemImage = itemImage;
		this.currentOffer = offer;

		setLayout(new BorderLayout());
		setBackground(ColorScheme.DARK_GRAY_COLOR);
		this.setBorder(new EmptyBorder(3, 5, 3, 5)); // Set border for InProgressPanel itself ONCE

		// Initialize main container for this panel's content
		this.container = new JPanel(new BorderLayout());
		this.container.setBackground(ColorScheme.DARK_GRAY_COLOR);
		this.container.setBorder(UiUtilities.ITEM_INFO_BORDER);

		// ** INITIALIZE progressBar and quantityProgressLabel EARLY **
		this.quantityProgressLabel = new JLabel(); // Initialize member
		this.quantityProgressLabel.setForeground(ColorScheme.GRAND_EXCHANGE_PRICE);
		this.quantityProgressLabel.setBorder(new EmptyBorder(3, 0, 3, 0));
		this.quantityProgressLabel.setHorizontalAlignment(JLabel.CENTER);

		this.progressBar = new JProgressBar(0, 100); // Initialize member
		this.progressBar.setBackground(ColorScheme.DARK_GRAY_COLOR);
		this.progressBar.setForeground(ColorScheme.PROGRESS_INPROGRESS_COLOR);
		this.progressBar.setStringPainted(true);

		this.datePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
		this.datePanel.setBackground(ColorScheme.DARK_GRAY_COLOR);
		this.datePanel.setOpaque(false);

		JLabel dateStaticLabel = new JLabel("Last Update: ");
		dateStaticLabel.setForeground(Color.white);

		this.datePanel.add(dateStaticLabel);

		this.lastUpdateValueLabel = new JLabel("---");
		this.lastUpdateValueLabel.setForeground(ColorScheme.GRAND_EXCHANGE_ALCH);
		this.lastUpdateValueLabel.setBorder(new EmptyBorder(1, 0, 1, 0));

		this.datePanel.add(this.lastUpdateValueLabel);

		if (this.config.isLastUpdate())
		{
			this.datePanel.setVisible(true);
		}
		else
		{
			this.datePanel.setVisible(false);
		}
		this.lastUpdateTime = null;
		// ** END INITIALIZATION **

		// Build the main sections using the NOW INITIALIZED members
		constructHeaderPanel();
		constructCenterContentPanel();
		constructOfferStatusPanel(); // This will now use the initialized progressBar & quantityProgressLabel

		// Add the constructed main sections to the internal container
		if (this.headerPanel != null)
		{
			this.container.add(this.headerPanel, BorderLayout.NORTH);
		}
		if (this.centerPanel != null)
		{
			this.container.add(this.centerPanel, BorderLayout.CENTER);
		}
		if (this.offerStatusPanel != null)
		{
			this.container.add(this.offerStatusPanel, BorderLayout.SOUTH);
		}

		// Add the internal container to this InProgressPanel itself
		this.add(this.container, BorderLayout.CENTER); // Usually, the main content container goes in the CENTER
		// Your original had NORTH, but CENTER is more typical for the primary content.
		// If you want it at NORTH, ensure nothing else is added to CENTER of `this`.

		// Populate with initial data if an offer is provided
		if (this.currentOffer != null)
		{ // Use this.currentOffer as it's already stored
			updateOfferData(this.currentItem, this.currentItemImage, this.currentOffer);
		}
		else
		{
			resetDataDisplayValues();
		}

	}

	private JLabel newLeftLabel(String text)
	{
		JLabel newLeftJLabel = new JLabel(text);
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

	private void constructHeaderPanel()
	{
		this.headerPanel = new InProgressHeader(this.currentItem, this.currentItemImage, this.currentOffer);

	}

	private void constructCenterContentPanel()
	{
		this.centerPanel = new JPanel(new BorderLayout());
		this.centerPanel.setBackground(ColorScheme.DARK_GRAY_COLOR);

		constructTitlePanel();
		constructItemInfoPanel();

		if (this.titlePanel != null)
		{
			this.centerPanel.add(this.titlePanel, BorderLayout.NORTH);
		}
		if (this.itemInfoContainer != null)
		{
			this.centerPanel.add(this.itemInfoContainer, BorderLayout.CENTER);
		}

	}

	private void constructTitlePanel()
	{
		this.titlePanel = new JPanel(new GridLayout(1, 3, 0, 0));
		this.titlePanel.setBackground(ColorScheme.DARK_GRAY_COLOR);
		this.titlePanel.setBorder(new EmptyBorder(2, 2, 2, 2));

		this.offerTypeLabel = new JLabel("Status");
		this.offerTypeLabel.setHorizontalAlignment(JLabel.LEFT);
		this.offerTypeLabel.setForeground(ColorScheme.PROGRESS_INPROGRESS_COLOR);

		this.offerInitLabel = new JLabel("Offered");
		this.offerInitLabel.setHorizontalAlignment(JLabel.CENTER);
		this.offerInitLabel.setForeground(Color.white);

		this.offerCurrentLabel = new JLabel("Spent/Received");
		this.offerCurrentLabel.setHorizontalAlignment(JLabel.CENTER);
		this.offerCurrentLabel.setForeground(Color.white);

		this.titlePanel.add(this.offerTypeLabel);
		this.titlePanel.add(this.offerInitLabel);
		this.titlePanel.add(this.offerCurrentLabel);
	}

	private void constructItemInfoPanel()
	{
		this.itemInfoContainer = new JPanel(new GridLayout(1, 3, 0, 0));
		this.itemInfoContainer.setBackground(ColorScheme.DARK_GRAY_COLOR);

		JPanel column1 = new JPanel(new BorderLayout());
		column1.setBackground(ColorScheme.DARK_GRAY_COLOR);

		JPanel staticLabelPanel = new JPanel(new GridLayout(0, 1));
		staticLabelPanel.setBackground(ColorScheme.DARK_GRAY_COLOR);

		JPanel offerTotalStaticLabelHolder = new CustomPanel(new BorderLayout(), true);
		offerTotalStaticLabelHolder.add(newLeftLabel("Offer Total:"), BorderLayout.WEST);
		staticLabelPanel.add(offerTotalStaticLabelHolder);

		JPanel pricePerStaticLabelHolder = new CustomPanel(new BorderLayout(), true);
		pricePerStaticLabelHolder.add(newLeftLabel("Price/Per:"), BorderLayout.WEST);
		staticLabelPanel.add(pricePerStaticLabelHolder);

		column1.add(staticLabelPanel, BorderLayout.CENTER);

		JPanel column2 = new JPanel(new BorderLayout());
		column2.setBackground(ColorScheme.DARK_GRAY_COLOR);

		JPanel offerValuesPanel = new JPanel(new GridLayout(0, 1));
		offerValuesPanel.setBackground(ColorScheme.DARK_GRAY_COLOR);

		this.offerTotalValueLabel = newRightLabel("0", ColorScheme.GRAND_EXCHANGE_ALCH);
		JPanel offerTotalValueHolder = new CustomPanel(new BorderLayout(), true);
		offerTotalValueHolder.add(this.offerTotalValueLabel, BorderLayout.CENTER);
		offerValuesPanel.add(offerTotalValueHolder);

		this.pricePerValueLabel = newRightLabel("0", ColorScheme.GRAND_EXCHANGE_ALCH);
		JPanel pricePerValueHolder = new CustomPanel(new BorderLayout(), true);
		pricePerValueHolder.add(this.pricePerValueLabel, BorderLayout.CENTER);
		offerValuesPanel.add(pricePerValueHolder);

		column2.add(offerValuesPanel, BorderLayout.CENTER);

		JPanel column3 = new JPanel(new BorderLayout());
		column3.setBackground(ColorScheme.DARK_GRAY_COLOR);

		JPanel actualValuesPanel = new JPanel(new GridLayout(0, 1));
		actualValuesPanel.setBackground(ColorScheme.DARK_GRAY_COLOR);

		this.actualSpentValueLabel = newRightLabel("0", ColorScheme.GRAND_EXCHANGE_ALCH);
		JPanel actualSpentValueHolder = new CustomPanel(new BorderLayout(), true);
		actualSpentValueHolder.add(this.actualSpentValueLabel, BorderLayout.CENTER);
		actualValuesPanel.add(actualSpentValueHolder);

		this.actualPricePerValueLabel = newRightLabel("0", ColorScheme.GRAND_EXCHANGE_ALCH);
		JPanel actualPricePerValueHolder = new CustomPanel(new BorderLayout(), true);
		actualPricePerValueHolder.add(this.actualPricePerValueLabel, BorderLayout.CENTER);
		actualValuesPanel.add(actualPricePerValueHolder);

		column3.add(actualValuesPanel, BorderLayout.CENTER);


		this.itemInfoContainer.add(column1);
		this.itemInfoContainer.add(column2);
		this.itemInfoContainer.add(column3);
	}

	private void constructOfferStatusPanel()
	{
		this.offerStatusPanel = new JPanel(new BorderLayout());
		this.offerStatusPanel.setBackground(ColorScheme.DARK_GRAY_COLOR);

		this.bottomPanel = new JPanel(new BorderLayout());
		this.bottomPanel.setBackground(ColorScheme.DARK_GRAY_COLOR);

		// These members are now guaranteed to be initialized by the constructor
		if (this.quantityProgressLabel != null)
		{
			this.bottomPanel.add(this.quantityProgressLabel, BorderLayout.NORTH);
		}
		if (this.progressBar != null)
		{
			this.bottomPanel.add(this.progressBar, BorderLayout.CENTER);
		}
		if (this.datePanel != null)
		{
			this.bottomPanel.add(this.datePanel, BorderLayout.SOUTH);
		}

		this.offerStatusPanel.add(this.bottomPanel, BorderLayout.CENTER);
	}

	private void updateOfferData(ItemComposition offerItem, BufferedImage itemImage, GrandExchangeOffer offer)
	{

		this.currentItem = offerItem;
		this.currentItemImage = itemImage;
		this.currentOffer = offer;

		this.lastUpdateTime = Instant.now();

		if (offer == null)
		{
			resetDataDisplayValues();
			return;
		}

		GrandExchangeOfferState state = offer.getState();
		boolean isBuy = checkIsBuy(state);
		boolean isActive = checkIsActive(state);
		boolean isBoughtSold = checkIsBoughtSold(state);
		boolean isCancelled = checkIsCancelState(state);


		if (this.headerPanel != null)
		{
			this.headerPanel.updateContent(offerItem, itemImage, offer);
		}

		if (this.titlePanel != null)
		{
			String offerTypeText;
			Color offerColorValue;
			if (isActive)
			{
				offerTypeText = "Active";
				offerColorValue = ColorScheme.PROGRESS_INPROGRESS_COLOR;
			}
			else if (isBoughtSold)
			{
				offerTypeText = "Complete";
				offerColorValue = ColorScheme.PROGRESS_COMPLETE_COLOR;
			}
			else if (isCancelled)
			{
				offerTypeText = "Cancelled";
				offerColorValue = ColorScheme.PROGRESS_ERROR_COLOR;
			}
			else
			{
				offerTypeText = "Error";
				offerColorValue = ColorScheme.PROGRESS_ERROR_COLOR;
			}

			if (this.offerTypeLabel != null)
			{
				this.offerTypeLabel.setText(offerTypeText);
				this.offerTypeLabel.setForeground(offerColorValue);
			}
			if (this.offerCurrentLabel != null)
			{
				this.offerCurrentLabel.setText(isBuy ? "Spent" : "Received");
			}
		}
		long price = offer.getPrice();
		long totalQuantity = offer.getTotalQuantity();
		long spent = offer.getSpent();
		long quantitySold = offer.getQuantitySold();

		if (this.itemInfoContainer != null)
		{
			if (this.offerTotalValueLabel != null)
			{
				long totalValueCalc = totalQuantity * price;
				this.offerTotalValueLabel.setText(Numbers.toShortNumber(totalValueCalc));
				this.offerTotalValueLabel.setToolTipText(Numbers.numberWithCommas(totalValueCalc));
			}

			if (this.pricePerValueLabel != null)
			{
				this.pricePerValueLabel.setText(Numbers.toShortNumber(price));
				this.pricePerValueLabel.setToolTipText(Numbers.numberWithCommas(price));
			}

			if (this.actualSpentValueLabel != null)
			{
				this.actualSpentValueLabel.setText(Numbers.toShortNumber(spent));
				this.actualSpentValueLabel.setToolTipText(Numbers.numberWithCommas(spent));
			}

			if (this.actualPricePerValueLabel != null)
			{
				long actualPricePerCalc = (quantitySold > 0) ? (spent / quantitySold) : 0;
				this.actualPricePerValueLabel.setText(Numbers.toShortNumber(actualPricePerCalc));
				this.actualPricePerValueLabel.setToolTipText(Numbers.numberWithCommas(actualPricePerCalc));
			}
		}

		if (this.offerStatusPanel != null)
		{
			if (this.quantityProgressLabel != null)
			{

				this.quantityProgressLabel.setText(Numbers.toShortNumber(quantitySold) + " / " + Numbers.toShortNumber(totalQuantity));
				this.quantityProgressLabel.setToolTipText(Numbers.numberWithCommas(quantitySold) + " / " + Numbers.numberWithCommas(totalQuantity));
			}

			if (this.progressBar != null)
			{

				int percentage = totalQuantity > 0 ? (int) (((double) quantitySold / totalQuantity) * 100) : 0;
				this.progressBar.setValue(percentage);
				this.progressBar.setString(percentage + "%");

				if (isActive && percentage <= 49)
				{
					this.progressBar.setForeground(ColorScheme.PROGRESS_INPROGRESS_COLOR);
				}
				else if (isActive && percentage >= 50 && percentage <= 99)
				{
					this.progressBar.setForeground(ColorScheme.PROGRESS_COMPLETE_COLOR.brighter());
				}
				else if (isBoughtSold && percentage == 100)
				{
					this.progressBar.setForeground(ColorScheme.PROGRESS_COMPLETE_COLOR);
				}
				else if (isCancelled)
				{
					this.progressBar.setForeground(ColorScheme.PROGRESS_ERROR_COLOR);
				}
				else
				{
					this.progressBar.setForeground(ColorScheme.DARK_GRAY_COLOR.brighter());
				}
			}
			this.lastUpdateValueLabel.setText(Timestamps.formatForPanel(this.lastUpdateTime, this.config.inProgressTimestampFormat()));
		}
	}

	public void updateOffer(ItemComposition item, BufferedImage itemImage, GrandExchangeOffer offer)
	{
		updateOfferData(item, itemImage, offer);
		this.revalidate();
		this.repaint();
	}

	public void refreshTimestampDisplayBasedOnConfig()
	{
		if (this.config.isLastUpdate())
		{
			this.datePanel.setVisible(true);
			Log.info("set to visible");
		}
		if (!this.config.isLastUpdate())
		{
			this.datePanel.setVisible(false);
			Log.info("set to hidden");

		}
		if (this.config.inProgressTimestampFormat() != null){
		this.lastUpdateValueLabel.setText(Timestamps.formatForPanel(this.lastUpdateTime, this.config.inProgressTimestampFormat()));
			Log.info("updated format");
		}
		this.revalidate();
		this.repaint();
	}

	private void resetDataDisplayValues()
	{
		if (this.headerPanel != null)
		{
			this.headerPanel.updateContent(null, null, null);
		}
		if (this.offerTypeLabel != null)
		{
			this.offerTypeLabel.setText("---");
			this.offerTypeLabel.setForeground(Color.WHITE);
		}
		if (this.offerCurrentLabel != null)
		{
			this.offerCurrentLabel.setText("---");
		}

		long zeroL = 0L;
		if (this.offerTotalValueLabel != null)
		{
			this.offerTotalValueLabel.setText(Numbers.toShortNumber(zeroL));
			this.offerTotalValueLabel.setToolTipText(Numbers.numberWithCommas(zeroL));
		}
		if (this.pricePerValueLabel != null)
		{
			this.pricePerValueLabel.setText(Numbers.toShortNumber(zeroL));
			this.pricePerValueLabel.setToolTipText(Numbers.numberWithCommas(zeroL));
		}
		if (this.actualSpentValueLabel != null)
		{
			this.actualSpentValueLabel.setText(Numbers.toShortNumber(zeroL));
			this.actualSpentValueLabel.setToolTipText(Numbers.numberWithCommas(zeroL));
		}
		if (this.actualPricePerValueLabel != null)
		{
			this.actualPricePerValueLabel.setText(Numbers.toShortNumber(zeroL));
			this.actualPricePerValueLabel.setToolTipText(Numbers.numberWithCommas(zeroL));
		}

		if (this.quantityProgressLabel != null)
		{
			this.quantityProgressLabel.setText(Numbers.toShortNumber(zeroL) + " / " + Numbers.toShortNumber(zeroL));
			this.quantityProgressLabel.setToolTipText(Numbers.numberWithCommas(zeroL) + " / " + Numbers.numberWithCommas(zeroL));
		}
		if (this.progressBar != null)
		{
			this.progressBar.setValue(0);
			this.progressBar.setString("0%");
			this.progressBar.setForeground(ColorScheme.PROGRESS_INPROGRESS_COLOR);
		}
		if (this.lastUpdateValueLabel != null)
		{
			this.lastUpdateValueLabel.setText("---");
		}
	}

	@Override
	public Dimension getPreferredSize()
	{
		Dimension preferredSize = super.getPreferredSize();
		return new Dimension(container.getPreferredSize().width, preferredSize.height);
	}
}