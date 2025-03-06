package com.flipper.views;

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

import com.flipper.helpers.UiUtilities;
import com.flipper.views.components.Tab;
import com.flipper.views.flips.FlipPage;
import com.flipper.views.inprogress.InProgressPage; // MODIFIED IMPORT - Changed from MarginPage
import com.flipper.views.transactions.TransactionPage;

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
        int columns = 3;
        topBar.setLayout(new GridLayout(1, columns));

        // Discord
        JLabel discord = new JLabel();
        discord.setToolTipText("Join the discord");
        discord.setHorizontalAlignment(JLabel.CENTER);
        discord.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                try {
                    Desktop desktop = Desktop.getDesktop();
                    URI discordUri = new URI("https://discord.gg/uBsWGSJ9Q7");
                    desktop.browse(discordUri);
                } catch (Exception error) {}
            }
        });
        ImageIcon discordIcon = new ImageIcon(ImageUtil.loadImageResource(getClass(), UiUtilities.discordIcon));
        discord.setIcon(discordIcon);
        topBar.add(discord);

        // Github
        JLabel github = new JLabel();
        github.setToolTipText("Flipper Pluggin Github");
        github.setHorizontalAlignment(JLabel.CENTER);
        github.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                try {
                    Desktop desktop = Desktop.getDesktop();
                    URI githubUri = new URI("https://github.com/Sir-Kyle-Richardson/OSRS-Flipper");
                    desktop.browse(githubUri);
                } catch (Exception error) {}
            }
        });
        ImageIcon githubIcon = new ImageIcon(ImageUtil.loadImageResource(getClass(), UiUtilities.githubIcon));
        github.setIcon(githubIcon);
        topBar.add(github);

        // Website
        JLabel website = new JLabel();
        website.setToolTipText("Flipper's website");
        website.setHorizontalAlignment(JLabel.CENTER);
        website.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                try {
                    Desktop desktop = Desktop.getDesktop();
                    URI githubUri = new URI("https://osrs-flipper.com/app/flip-finder");
                    desktop.browse(githubUri);
                } catch (Exception error) {}
            }
        });
        ImageIcon websiteIcon = new ImageIcon(ImageUtil.loadImageResource(getClass(), UiUtilities.flipperSmall));
        website.setIcon(websiteIcon);
        topBar.add(website);

        container.add(topBar);
        container.setBorder(new EmptyBorder(3, 0, 5, 0));
        return container;
    }

    public void renderLoggedInView(
            TransactionPage buyPage,
            TransactionPage sellPage,
            FlipPage flipPage,
            InProgressPage inProgressPage // Changed parameter type
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
            Tab inProgressTab = new Tab("In Progress", tabGroup, inProgressPage); // MODIFIED - Changed Tab Label and Page
            tabGroup.setBorder(new EmptyBorder(5, 0, 2, 0));
            tabGroup.addTab(buysTab);
            tabGroup.addTab(sellsTab);
            tabGroup.addTab(inProgressTab); // MODIFIED - Added InProgressTab
            // Initialize with buys tab open.
            tabGroup.select(buysTab); // Select inProgressTab
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