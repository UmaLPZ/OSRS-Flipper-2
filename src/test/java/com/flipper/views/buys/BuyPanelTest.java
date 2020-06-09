package com.flipper.views.buys;

import javax.swing.JFrame;

import com.flipper.models.Transaction;
import com.flipper.views.ViewTestSetup;

import net.runelite.api.ItemComposition;
import net.runelite.client.game.ItemManager;


public class BuyPanelTest {
    static Transaction buy;

    public static void setUp() {
        buy = new Transaction(
            1,
            1000,
            ViewTestSetup.mockItemId,
            100,
            ViewTestSetup.testItemName,
            true,
            true
        );
    }

    public static void main(String[] args) throws Exception {
        setUp();
        ViewTestSetup.setUp();
        BuyPanel buyPanel = new BuyPanel(buy, ViewTestSetup.itemManager);
        ViewTestSetup.launch(buyPanel);
    }
}