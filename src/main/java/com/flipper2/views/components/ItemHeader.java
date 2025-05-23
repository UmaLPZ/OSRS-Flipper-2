package com.flipper2.views.components;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Color;
import java.awt.Dimension;

import com.flipper2.helpers.Numbers;
import com.flipper2.helpers.UiUtilities;

import static com.flipper2.helpers.UiUtilities.truncateString;

import net.runelite.client.game.ItemManager;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;
import net.runelite.client.util.AsyncBufferedImage;

import java.awt.event.*;

/**
 * Item header: Item Icon / Item Name / Number bought/soul out of total
 */
public class ItemHeader extends JPanel
{
	private ItemManager itemManager;
	private int itemId;
	private int pricePer;
	private String itemName;
	private JPanel topRightContainer;
	private JLabel costPerLabel;

	public ItemHeader(
		int itemId,
		int pricePer,
		String itemName,
		ItemManager itemManager,
		boolean isAddCostPer,
		JButton hoverButton
	)
	{
		this.itemId = itemId;
		this.pricePer = pricePer;
		this.itemName = itemName;
		this.itemManager = itemManager;
		this.setLayout(new BorderLayout());
		topRightContainer = new JPanel();
		topRightContainer.setBackground(ColorScheme.DARKER_GRAY_COLOR.darker());

		JPanel itemIconPanel = constructItemIcon();
		JLabel itemNameLabel = constructItemName();
		if (isAddCostPer)
		{
			this.costPerLabel = constructCostPerLabel();
			topRightContainer.add(costPerLabel);
		}

		hoverButton.setVisible(false);
		topRightContainer.add(hoverButton);

		add(topRightContainer, BorderLayout.EAST);
		setBackground(ColorScheme.DARKER_GRAY_COLOR.darker());
		add(itemIconPanel, BorderLayout.WEST);
		add(itemNameLabel, BorderLayout.CENTER);
		setBorder(new EmptyBorder(2, 1, 2, 5));

		this.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseEntered(MouseEvent me)
			{
				hoverButton.setVisible(true);
				if (isAddCostPer)
				{
					costPerLabel.setVisible(false);
				}
			}

			@Override
			public void mouseExited(MouseEvent me)
			{
				if (isAddCostPer)
				{
					costPerLabel.setVisible(true);
				}
				hoverButton.setVisible(false);
			}
		});

		hoverButton.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseEntered(MouseEvent me)
			{
				hoverButton.setVisible(true);
				if (isAddCostPer)
				{
					costPerLabel.setVisible(false);
				}
			}

			@Override
			public void mouseExited(MouseEvent me)
			{
				if (isAddCostPer)
				{
					costPerLabel.setVisible(true);
				}
				hoverButton.setVisible(false);
			}
		});
	}

	private JPanel constructItemIcon()
	{
		AsyncBufferedImage itemImage = itemManager.getImage(this.itemId);
		JLabel itemIcon = new JLabel();
		itemIcon.setAlignmentX(Component.LEFT_ALIGNMENT);
		itemIcon.setPreferredSize(UiUtilities.ICON_SIZE);
		if (itemImage != null)
		{
			itemImage.addTo(itemIcon);
		}
		JPanel itemIconPanel = new JPanel(new BorderLayout());
		itemIconPanel.setBackground(ColorScheme.DARKER_GRAY_COLOR.darker());
		itemIconPanel.add(itemIcon, BorderLayout.WEST);
		return itemIconPanel;
	}

	private JLabel constructItemName()
	{
		JLabel itemName = new JLabel(truncateString(this.itemName, 20), SwingConstants.CENTER);
		itemName.setForeground(Color.WHITE);
		itemName.setFont(FontManager.getRunescapeBoldFont());
		itemName.setPreferredSize(new Dimension(0, 0));
		itemName.setToolTipText(this.itemName);
		return itemName;
	}

	private JLabel constructCostPerLabel()
	{
		String costPerString = Numbers.numberWithCommas(this.pricePer);
		JLabel costPerLabel = new JLabel(costPerString + " gp");
		costPerLabel.setForeground(ColorScheme.GRAND_EXCHANGE_ALCH);
		return costPerLabel;
	}
}