package com.flipper2.views.components;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Color;
import java.awt.image.BufferedImage;

import com.flipper2.helpers.UiUtilities;

import net.runelite.api.GrandExchangeOffer;
import net.runelite.api.GrandExchangeOfferState;
import net.runelite.client.game.ItemManager;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;
import net.runelite.api.ItemComposition;

import static com.flipper2.helpers.GrandExchange.checkIsBuy;
import static com.flipper2.helpers.UiUtilities.truncateString;

public class InProgressHeader extends JPanel
{
	private JLabel itemIconLabel;
	private JLabel itemNameLabel;
	private JLabel offerTypeLabelInt;

	public InProgressHeader(ItemComposition item, BufferedImage itemImage, GrandExchangeOffer offer)
	{
		this.setLayout(new BorderLayout());
		this.setBackground(ColorScheme.DARKER_GRAY_COLOR.darker());
		this.setBorder(new EmptyBorder(2, 1, 2, 5));

		JPanel itemIconPanel = new JPanel(new BorderLayout());
		itemIconPanel.setBackground(ColorScheme.DARKER_GRAY_COLOR.darker());
		this.itemIconLabel = new JLabel();
		this.itemIconLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
		this.itemIconLabel.setPreferredSize(UiUtilities.ICON_SIZE);
		itemIconPanel.add(this.itemIconLabel, BorderLayout.WEST);

		this.itemNameLabel = new JLabel("", SwingConstants.CENTER);
		this.itemNameLabel.setForeground(Color.white);
		this.itemNameLabel.setFont(FontManager.getRunescapeBoldFont());

		this.offerTypeLabelInt = new JLabel("");
		this.offerTypeLabelInt.setHorizontalAlignment(JLabel.RIGHT);


		this.add(itemIconPanel, BorderLayout.WEST);
		this.add(this.itemNameLabel, BorderLayout.CENTER);
		this.add(this.offerTypeLabelInt, BorderLayout.EAST);

		updateContent(item, itemImage, offer);

	}

	public void updateContent(ItemComposition item, BufferedImage itemImage, GrandExchangeOffer offer)
	{
		if (itemImage != null)
		{
			this.itemIconLabel.setIcon(new ImageIcon(itemImage));
		}
		else
		{
			this.itemIconLabel.setIcon(null);
		}

		if (item != null)
		{
			this.itemNameLabel.setText(UiUtilities.truncateString(item.getName(), 20));
			this.itemNameLabel.setToolTipText(item.getName());
		}
		else
		{
			this.itemNameLabel.setText("---");
			this.itemNameLabel.setToolTipText(null);
		}

		if (offer != null)
		{
			GrandExchangeOfferState state = offer.getState();
			boolean isBuy = checkIsBuy(state);
			String offerTypeText = (isBuy ? "Buy" : "Sell");
			this.offerTypeLabelInt.setText(offerTypeText);

			if (isBuy)
			{
				this.offerTypeLabelInt.setForeground(ColorScheme.GRAND_EXCHANGE_ALCH);
			}
			else
			{
				this.offerTypeLabelInt.setForeground(ColorScheme.GRAND_EXCHANGE_PRICE);
			}
		}
		else
		{
			this.offerTypeLabelInt.setText("");
			this.offerTypeLabelInt.setForeground(Color.WHITE); // Default color
		}
	}
};
