package com.flipper2.views.inprogress;

import com.flipper2.FlipperConfig;
import com.flipper2.helpers.Log;
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
	private final FlipperConfig config;


	public InProgressPage(FlipperConfig config)
	{
		this.config = config;
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
		JPanel scrollContainer = new JPanel();
		scrollContainer.setLayout(new BorderLayout());
		container = new JPanel();
		container.setBorder(new EmptyBorder(2, 0, 0, 0));
		container.setLayout(new BoxLayout(container, BoxLayout.PAGE_AXIS));
		container.setBackground(ColorScheme.DARK_GRAY_COLOR);

		JScrollPane scrollPane = new JScrollPane(scrollContainer);
		scrollPane.setBackground(ColorScheme.LIGHT_GRAY_COLOR);
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

		scrollContainer.add(container, BorderLayout.PAGE_START);
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
			offerSlot = new InProgressPanel(item, itemImage, newOffer, config);
			inProgressPanels[slot] = offerSlot;
			container.add(offerSlot);
		}

		offerSlot.updateOffer(item, itemImage, newOffer);

		revalidate();
		repaint();
	}
	public void notifyPanelsOfConfigChange() {
		if (inProgressPanels == null) return; // Safety check

		boolean needsRepaint = false;
		for (InProgressPanel panel : inProgressPanels) {
			if (panel != null) { // Only act on existing, non-null panels
				panel.refreshTimestampDisplayBasedOnConfig();
				needsRepaint = true; // Assume panel handles its own repaint, but page might need if size changes
			}
		}
		// If any panel's structure might have changed (e.g. datePanel added/removed),
		// the InProgressPage might need to revalidate its layout if panel sizes changed.
		// Individual panels call their own revalidate/repaint, which is usually enough.
		// If panel preferred sizes change significantly, this page might need revalidation.
		if (needsRepaint) { // This check is a bit redundant if panels repaint themselves.
//			this.revalidate(); // Usually not needed if children handle it.
//			this.repaint();  // Usually not needed if children handle it.
			Log.info("went through");
		}
	}
}