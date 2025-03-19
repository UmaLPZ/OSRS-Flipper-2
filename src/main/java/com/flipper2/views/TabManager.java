package com.flipper2.views;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.Desktop;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URI;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

import com.flipper2.helpers.UiUtilities;
import com.flipper2.views.components.Tab;
import com.flipper2.views.inprogress.InProgressPage;
import com.flipper2.views.transactions.TransactionPage;
import com.flipper2.views.flips.FlipPage;

import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.PluginPanel;
import net.runelite.client.ui.components.materialtabs.MaterialTabGroup;
import net.runelite.client.util.ImageUtil;

public class TabManager extends PluginPanel {

    public TabManager() {
        super(false);
        this.setLayout(new BorderLayout());
    }

    private JPanel constructTopBar() {
        JPanel container = new JPanel();
        container.setBackground(ColorScheme.DARK_GRAY_COLOR);
        container.setLayout(new BorderLayout());
        container.setBorder(new EmptyBorder(0, 0, 5, 0));
        JPanel topBar = new JPanel();
        topBar.setBackground(ColorScheme.DARK_GRAY_COLOR);
        int columns = 1;
        topBar.setLayout(new GridLayout(1, columns));

        JLabel github = new JLabel();
        github.setToolTipText("Flipper2 Pluggin Github");
        github.setHorizontalAlignment(JLabel.RIGHT);
        github.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                try {
                    Desktop desktop = Desktop.getDesktop();
                    URI githubUri = new URI("https://github.com/UmaLPZ/OSRS-Flipper-2");
                    desktop.browse(githubUri);
                } catch (Exception error) {}
            }
        });
        ImageIcon githubIcon = new ImageIcon(ImageUtil.loadImageResource(getClass(), UiUtilities.githubIcon));
        github.setIcon(githubIcon);
        topBar.add(github);

        container.add(topBar);
        container.setBorder(new EmptyBorder(0, 0, 5, 5));
        return container;
    }

    public void renderLoggedInView(
            TransactionPage buyPage,
            TransactionPage sellPage,
            FlipPage flipPage,
            InProgressPage inProgressPage
    ) {
        SwingUtilities.invokeLater(() -> {
            this.removeAll();
            JPanel topBar = this.constructTopBar();
            JPanel display = new JPanel();
            JPanel header = new JPanel(new BorderLayout());
            header.setBorder(new EmptyBorder(5, 0, 0, 0));
            MaterialTabGroup tabGroup = new MaterialTabGroup(display);
            Tab buysTab = new Tab("Buys", tabGroup, buyPage);
            Tab sellsTab = new Tab("Sells", tabGroup, sellPage);
            Tab flipsTab = new Tab("Flips", tabGroup, flipPage);
            Tab inProgressTab = new Tab("In Progress", tabGroup, inProgressPage);
            tabGroup.setBorder(new EmptyBorder(5, 0, 2, 0));
            tabGroup.addTab(buysTab);
            tabGroup.addTab(sellsTab);
            tabGroup.addTab(flipsTab);
            tabGroup.addTab(inProgressTab);
            tabGroup.select(buysTab);
            JPanel tabGroupContainer = new JPanel();
            tabGroupContainer.setBackground(ColorScheme.DARKER_GRAY_COLOR.darker());
            tabGroupContainer.add(tabGroup);
            header.add(topBar, BorderLayout.NORTH);
            header.add(tabGroupContainer, BorderLayout.CENTER);
            add(header, BorderLayout.NORTH);
            add(display, BorderLayout.CENTER);
            this.revalidate();
        });
    }
}