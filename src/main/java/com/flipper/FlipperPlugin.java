package com.flipper;

//import com.flipper.controllers.BuysController;
//import com.flipper.controllers.FlipsController;
//import com.flipper.controllers.SellsController;
//import com.flipper.controllers.InProgressController;
import com.flipper.helpers.Log;
import com.flipper.helpers.Persistor;
import com.flipper.helpers.UiUtilities;
import com.flipper.models.Flip;
import com.flipper.views.TabManager;
import com.flipper.views.inprogress.InProgressPage;

import com.google.inject.Provides;

import java.io.IOException;

import javax.inject.Inject;
import javax.swing.SwingUtilities;

import net.runelite.api.GrandExchangeOffer;
import net.runelite.api.GrandExchangeOfferState;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GrandExchangeOfferChanged;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ClientShutdown;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.util.ImageUtil;
import net.runelite.api.GameState;
import net.runelite.api.Client; // Import Client
import net.runelite.api.ItemComposition; // Import ItemComposition
import java.awt.image.BufferedImage; // Import BufferedImage

@PluginDescriptor(name = "Flipper")
public class FlipperPlugin extends Plugin {
    // Injects
    @Inject
    private ClientToolbar clientToolbar;
    @Inject
    private ItemManager itemManager;
    @Inject
    private FlipperConfig config;
    @Inject
    private ClientThread cThread;
    @Inject
    private Client client; // Inject Client

    // Controllers (Commented out for now)
    // private BuysController buysController;
    // private SellsController sellsController;
    // private FlipsController flipsController;
    // private InProgressController inProgressController;

    // Views
    private NavigationButton navButton;
    private TabManager tabManager;
    @Inject
    private InProgressPage inProgressPage; // Inject InProgressPage

    @Override
    protected void startUp() throws Exception {
        try {
            Persistor.setUp();
            this.tabManager = new TabManager();
            this.setUpNavigationButton();
            this.changeToLoggedInView();
        } catch (Exception e) {
            Log.info("Flipper failed to start");
        }
    }

    private void setUpNavigationButton() {
        navButton = NavigationButton
                .builder()
                .tooltip("Flipper")
                .icon(
                        ImageUtil.loadImageResource(
                                getClass(),
                                UiUtilities.flipperNavIcon
                        )
                )
                .priority(4)
                .panel(tabManager).build();
        clientToolbar.addNavigation(navButton);
    }

    private void flipFromMargin(Flip margin) {
    }

    private void changeToLoggedInView() {
        SwingUtilities.invokeLater(() -> {
            try {
                // Consumer<Flip> convertToFlipConsumer = (margin) -> {}; // Not needed

                // Comment out other controllers
                // flipsController = new FlipsController(...);
                // buysController = new BuysController(...);
                // sellsController = new SellsController(...);
                // inProgressController = new InProgressController(...); // Removed

                this.tabManager.renderLoggedInView(
                        null, // No buys page
                        null, // No sells page
                        null, // No flips page
                        inProgressPage
                );
            } catch (Exception e) { // Changed to Exception
                Log.info("Flipper: Failed to load required files");
            }
        });
    }

    private void saveAll() {
        // No saving for now
    }

    @Override
    protected void shutDown() throws Exception {
        clientToolbar.removeNavigation(navButton);
        // No saving
    }

    @Subscribe
    public void onClientShutdown(ClientShutdown clientShutdownEvent) throws IOException {
        // No saving
    }
    @Subscribe
    public void onGameStateChanged(GameStateChanged gameStateChanged)
    {
        if (gameStateChanged.getGameState() == GameState.LOGIN_SCREEN)
        {
            inProgressPage.resetOffers();
        }
    }

    @Subscribe
    public void onGrandExchangeOfferChanged(GrandExchangeOfferChanged newOfferEvent) {
        int slot = newOfferEvent.getSlot();
        GrandExchangeOffer offer = newOfferEvent.getOffer();
        GrandExchangeOfferState offerState = offer.getState();

        if (offerState == GrandExchangeOfferState.EMPTY && client.getGameState() != GameState.LOGGED_IN) {
            return;
        }

        // Call updatePanel for ALL states.  The updatePanel method will handle
        // creating, updating, *and* removing offer slots.
        updatePanel(slot, offer);
    }

    private void updatePanel(int slot, GrandExchangeOffer offer) {
        cThread.invoke(() -> {
            ItemComposition offerItem = itemManager.getItemComposition(offer.getItemId());
            // ALWAYS get the unstacked image (quantity = 1, shouldStack = false)
            BufferedImage itemImage = itemManager.getImage(offer.getItemId(), 1, false);
            SwingUtilities.invokeLater(() -> inProgressPage.updateOffer(offerItem, itemImage, offer, slot));
        });
    }

    @Provides
    FlipperConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(FlipperConfig.class);
    }
}