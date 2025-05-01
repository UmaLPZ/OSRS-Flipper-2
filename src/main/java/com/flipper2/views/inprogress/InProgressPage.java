package com.flipper2.views.inprogress;

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;

import java.awt.BorderLayout;

import net.runelite.client.ui.ColorScheme;
import net.runelite.api.GrandExchangeOffer;
import net.runelite.api.ItemComposition;
import java.awt.image.BufferedImage;
import java.util.Arrays;


public class InProgressPage extends JPanel
{
	private JPanel container;
	private static final int MAX_OFFERS = 8;
	private InProgressPanel[] inProgressPanels = new InProgressPanel[MAX_OFFERS];

	public InProgressPage()
	{
		this.setLayout(new BorderLayout());
		this.setBackground(ColorScheme.DARK_GRAY_COLOR);
		this.build();
	}

	public void resetOffers()
	{
		container.removeAll();
		Arrays.fill(inProgressPanels, null);
		revalidate();
		repaint();
	}

	public void build()
	{
		container = new JPanel();
		container.setBorder(new EmptyBorder(5, 0, 0, 0));
		container.setLayout(new BoxLayout(container, BoxLayout.PAGE_AXIS));
		container.setBackground(ColorScheme.DARK_GRAY_COLOR);

		JPanel scrollContainer = new JPanel();
		scrollContainer.setLayout(new BorderLayout());
		scrollContainer.setBackground(ColorScheme.DARK_GRAY_COLOR);
		scrollContainer.setBorder(new EmptyBorder(0, 0, 0, 0));
		scrollContainer.add(container, BorderLayout.PAGE_START);

		JScrollPane scrollPane = new JScrollPane(scrollContainer);
		scrollPane.setBackground(ColorScheme.DARK_GRAY_COLOR);
		scrollPane.setBorder(new EmptyBorder(0, 0, 0, 0));
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

		this.add(scrollPane, BorderLayout.CENTER);
	}

	public void updateOffer(ItemComposition item, BufferedImage itemImage, GrandExchangeOffer newOffer, int slot)
	{
		/* If slot was previously filled, and is now empty, remove it from the list */
		if (newOffer == null || newOffer.getState() == net.runelite.api.GrandExchangeOfferState.EMPTY)
		{
			if (inProgressPanels[slot] != null)
			{
				container.remove(inProgressPanels[slot]);
				inProgressPanels[slot] = null;
				revalidate();
				repaint();
			}
			return;
		}
		/* If slot was empty, and is now filled, add it to the list */
		InProgressPanel offerSlot = inProgressPanels[slot];
		if (offerSlot == null)
		{
			offerSlot = new InProgressPanel(item, itemImage, newOffer);
			inProgressPanels[slot] = offerSlot;
			container.add(offerSlot);
		}

		offerSlot.updateOffer(item, itemImage, newOffer);

		revalidate();
		repaint();
	}
}