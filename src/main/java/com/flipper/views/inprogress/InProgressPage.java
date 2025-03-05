package com.flipper.views.inprogress;

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;

import java.awt.BorderLayout;
import java.util.function.Consumer;

import net.runelite.client.ui.ColorScheme;
import net.runelite.api.GrandExchangeOffer; // Import
import net.runelite.api.ItemComposition;   // Import
import java.awt.image.BufferedImage;       // Import
import java.util.Arrays; // For Arrays.fill


public class InProgressPage extends JPanel {
    private JPanel container;
    private static final int MAX_OFFERS = 8; // Add this constant

    private InProgressPanel[] inProgressPanels = new InProgressPanel[MAX_OFFERS]; // Array of offer slots

    public InProgressPage() { // Removed onSearchTextChangedCallback parameter
        this.setLayout(new BorderLayout());
        this.setBackground(ColorScheme.DARK_GRAY_COLOR);
        // this.addSearchBar(onSearchTextChangedCallback); // Removed search bar for now
        this.build();
    }

    public void resetOffers()
    {
        container.removeAll();
        Arrays.fill(inProgressPanels, null);
        revalidate();
        repaint();
    }

    private void addSearchBar(Consumer<String> onSearchTextChangedCallback) {
        // Search bar removed for initial simplification
        // SearchBar searchBar = new SearchBar(onSearchTextChangedCallback);
        // this.add(searchBar, BorderLayout.NORTH);
    }

    public void resetContainer() {
        this.container.removeAll();
    }

    // Remove addInProgressPanel (it's no longer needed)

    public void build() {
        // This panel holds the actual offers. It's inside the scroll pane.
        container = new JPanel();
        container.setBorder(new EmptyBorder(5, 0, 0, 0));
        container.setLayout(new BoxLayout(container, BoxLayout.PAGE_AXIS));
        container.setBackground(ColorScheme.DARK_GRAY_COLOR); // Correct background for content

        // This panel wraps the container, allowing the scroll pane to work correctly.
        JPanel scrollContainer = new JPanel();
        scrollContainer.setLayout(new BorderLayout());
        scrollContainer.setBackground(ColorScheme.DARK_GRAY_COLOR); // Background for scroll container
        scrollContainer.setBorder(new EmptyBorder(0,0,0,0)); // Remove border
        scrollContainer.add(container, BorderLayout.PAGE_START); // Add container to the *top*

        // The scroll pane itself.
        JScrollPane scrollPane = new JScrollPane(scrollContainer); // Use scrollContainer, not container
        scrollPane.setBackground(ColorScheme.DARK_GRAY_COLOR); // This is less important now
        scrollPane.setBorder(new EmptyBorder(0,0,0,0)); // Remove border
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        // Add the scroll pane to the main panel.
        this.add(scrollPane, BorderLayout.CENTER);
    }
    public void updateOffer(ItemComposition item, BufferedImage itemImage, GrandExchangeOffer newOffer, int slot) {
        /* If slot was previously filled, and is now empty, remove it from the list */
        if (newOffer == null || newOffer.getState() == net.runelite.api.GrandExchangeOfferState.EMPTY) {
            if (inProgressPanels[slot] != null) {
                container.remove(inProgressPanels[slot]);
                inProgressPanels[slot] = null;
                revalidate();
                repaint();
            }
            return;
        }

        /* If slot was empty, and is now filled, add it to the list */
        InProgressPanel offerSlot = inProgressPanels[slot];
        if (offerSlot == null) {
            offerSlot = new InProgressPanel(item, itemImage, newOffer); // Corrected constructor
            inProgressPanels[slot] = offerSlot;
            container.add(offerSlot); // Add directly, no constraints needed with BoxLayout
        }

        offerSlot.updateOffer(item, itemImage, newOffer); // Corrected method call

        revalidate();
        repaint();
    }
}