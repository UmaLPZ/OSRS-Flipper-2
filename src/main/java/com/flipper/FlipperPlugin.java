// FlipperPlugin.java
package com.flipper;

import com.flipper.controllers.BuysController;
import com.flipper.controllers.FlipsController;
import com.flipper.controllers.SellsController;
import com.flipper.helpers.GrandExchange;
import com.flipper.helpers.Log;
import com.flipper.helpers.Persistor;
import com.flipper.helpers.UiUtilities;
import com.flipper.models.Flip;
import com.flipper.models.Transaction;
import com.flipper.views.TabManager;
import com.flipper.views.inprogress.InProgressPage;
import com.google.inject.Provides;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

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
import net.runelite.api.Client;
import net.runelite.api.ItemComposition;
import java.awt.image.BufferedImage;

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
    private Client client;

    // Controllers
    private BuysController buysController;
    private SellsController sellsController;
    private FlipsController flipsController;

    // Views
    private NavigationButton navButton;
    private TabManager tabManager;
    @Inject
    private InProgressPage inProgressPage;

    @Override
    protected void startUp() throws Exception {
        try {
            Persistor.setUp();
            this.tabManager = new TabManager();
            this.setUpNavigationButton();
            this.buysController = new BuysController(itemManager, config, cThread);
            this.sellsController = new SellsController(itemManager, config, cThread);
            this.flipsController = new FlipsController(itemManager, config, cThread);
            this.changeToLoggedInView();

        } catch (Exception e) {
            Log.info("Flipper failed to start: " + e.getMessage());
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

    private void changeToLoggedInView() {
        SwingUtilities.invokeLater(() -> {
            this.tabManager.renderLoggedInView(
                    buysController.getPage(),
                    sellsController.getPage(),
                    flipsController.getPage(),
                    inProgressPage
            );
        });
    }

    private void saveAll() {
        try {
            buysController.saveTransactions();
            sellsController.saveTransactions();
            flipsController.saveTransactions();
        } catch (Exception error) {
            Log.info("Failed to save Flipper files");
        }
    }

    @Override
    protected void shutDown() throws Exception {
        clientToolbar.removeNavigation(navButton);
        this.saveAll();
    }

    @Subscribe
    public void onClientShutdown(ClientShutdown clientShutdownEvent) throws IOException {
        this.saveAll();
    }

    @Subscribe
    public void onGrandExchangeOfferChanged(GrandExchangeOfferChanged newOfferEvent) {
        int slot = newOfferEvent.getSlot();
        GrandExchangeOffer offer = newOfferEvent.getOffer();
        GrandExchangeOfferState offerState = offer.getState();

        if (offerState == GrandExchangeOfferState.EMPTY && client.getGameState() != GameState.LOGGED_IN) {
            return;
        }

        if (GrandExchange.checkIsBuy(offerState)) {
            // Handle buy offers
            if (offerState == GrandExchangeOfferState.BOUGHT ||
                    (offerState == GrandExchangeOfferState.CANCELLED_BUY && offer.getQuantitySold() > 0)) {
                Transaction buy = buysController.upsertTransaction(offer, slot);
                if (buy != null) {
                    buysController.saveTransactions();
                    List<Transaction> sells = sellsController.getTransactions();
                    flipsController.upsertFlip(buy, sells);
                }
            }
        } else {
            // Handle sell offers
            if (offerState == GrandExchangeOfferState.SOLD ||
                    (offerState == GrandExchangeOfferState.CANCELLED_SELL && offer.getQuantitySold() > 0)) {
                Transaction sell = sellsController.upsertTransaction(offer, slot); // Use upsertTransaction for sells
                if (sell != null) {
                    sellsController.saveTransactions();
                    List<Transaction> buys = buysController.getTransactions();
                    flipsController.upsertFlip(sell, buys);
                }
            }
        }

        // Always update the InProgressPage
        updatePanel(slot, offer);
    }

    private void updatePanel(int slot, GrandExchangeOffer offer) {
        cThread.invoke(() -> {
            ItemComposition offerItem = itemManager.getItemComposition(offer.getItemId());
            BufferedImage itemImage = itemManager.getImage(offer.getItemId(), 1, false);
            SwingUtilities.invokeLater(() -> inProgressPage.updateOffer(offerItem, itemImage, offer, slot));
        });
    }

    @Subscribe
    public void onGameStateChanged(GameStateChanged gameStateChanged)
    {
        if (gameStateChanged.getGameState() == GameState.LOGIN_SCREEN)
        {
            inProgressPage.resetOffers();
        }
    }

    @Provides
    FlipperConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(FlipperConfig.class);
    }
}