/*
 * Copyright (c) 2020, Kyle Richardson <https://github.com/Sir-Kyle-Richardson>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.flipper2;

import com.flipper2.controllers.BuysController;
import com.flipper2.controllers.FlipsController;
import com.flipper2.controllers.SellsController;
import com.flipper2.helpers.GrandExchange;
import com.flipper2.helpers.Log;
import com.flipper2.helpers.Persistor;
import com.flipper2.helpers.UiUtilities;
import com.flipper2.models.Transaction;
import com.flipper2.views.TabManager;
import com.flipper2.views.inprogress.InProgressPage;
import com.google.gson.Gson;
import com.google.inject.Provides;

import java.io.IOException;
import java.util.List;

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

@PluginDescriptor(name = "Flipper2")
public class FlipperPlugin extends Plugin {
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
    private BuysController buysController;
    private SellsController sellsController;
    private FlipsController flipsController;

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
            Log.info("Flipper2 failed to start: " + e.getMessage());
        }
    }

    private void setUpNavigationButton() {
        navButton = NavigationButton
                .builder()
                .tooltip("Flipper2")
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
            Log.info("Failed to save Flipper2 files");
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
            if (offerState == GrandExchangeOfferState.SOLD ||
                    (offerState == GrandExchangeOfferState.CANCELLED_SELL && offer.getQuantitySold() > 0)) {
                Transaction sell = sellsController.upsertTransaction(offer, slot);
                if (sell != null) {
                    sellsController.saveTransactions();
                    List<Transaction> buys = buysController.getTransactions();
                    flipsController.upsertFlip(sell, buys);
                }
            }
        }

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