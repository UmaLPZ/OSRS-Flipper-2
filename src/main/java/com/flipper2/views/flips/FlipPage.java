package com.flipper2.views.flips;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;
import javax.swing.*;
import javax.swing.SwingConstants;

import java.awt.*;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.function.Consumer;

import com.flipper2.helpers.Numbers;
import com.flipper2.helpers.UiUtilities;
import com.flipper2.views.components.SearchBar;
import com.flipper2.views.components.Toggle;

import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;
import net.runelite.client.util.ImageUtil;

public class FlipPage extends JPanel
{
	private JPanel container;
	private JLabel totalProfitValueLabel;
	private JCheckBox shortenProfitCheckbox;

	private Consumer<String> onSearchTextChanged;
	private Runnable refreshFlipsRunnable;
	private Runnable toggleIsTrackingFlipsRunnable;
	private Toggle trackFlipsToggle;

	public FlipPage(
		Runnable refreshFlipsRunnable,
		Consumer<String> onSearchTextChanged,
		Runnable toggleIsTrackingFlipsRunnable,
		Boolean isTrackingFlips
	)
	{
		this.refreshFlipsRunnable = refreshFlipsRunnable;
		this.onSearchTextChanged = onSearchTextChanged;
		this.toggleIsTrackingFlipsRunnable = toggleIsTrackingFlipsRunnable;
		this.setLayout(new BorderLayout());
		this.setBackground(ColorScheme.DARK_GRAY_COLOR);
		constructTotalProfitContainer();
		this.build(isTrackingFlips);
		setTotalProfit("0", true);
	}

	public void addFlipPanel(FlipPanel flipPanel)
	{
		container.add(flipPanel, BorderLayout.CENTER);
		this.revalidate();
	}

	public void resetContainer(boolean isTrackingFlips)
	{
		trackFlipsToggle.setSelected(isTrackingFlips);
		trackFlipsToggle.revalidate();
		container.removeAll();
	}

	private void constructTotalProfitContainer()
	{
		JPanel container = new JPanel();
		container.setLayout(new BorderLayout());
		JPanel totalProfitContainer = new JPanel(new BorderLayout());
		totalProfitContainer.setBackground(ColorScheme.DARK_GRAY_COLOR);


		JPanel totalProfitHeader = new JPanel(new BorderLayout());
		totalProfitHeader.setBackground(ColorScheme.DARK_GRAY_COLOR);
		totalProfitHeader.setBorder(new EmptyBorder(2, 2, 0, 2));

		JLabel totalProfitLabel = new JLabel("Flip Profit");
		totalProfitLabel.setFont(FontManager.getRunescapeBoldFont());
		totalProfitLabel.setHorizontalAlignment(JLabel.CENTER);
		totalProfitLabel.setForeground(Color.WHITE);

		ImageIcon refreshIcon = new ImageIcon(ImageUtil.loadImageResource(getClass(), UiUtilities.refreshIcon));
		JLabel refreshFlips = new JLabel();
		refreshFlips.setToolTipText("Refresh flips");
		refreshFlips.setIcon(refreshIcon);
		refreshFlips.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseClicked(MouseEvent e)
			{
				try
				{
					refreshFlipsRunnable.run();
				}
				catch (Exception error)
				{
				}
			}
		});

		totalProfitHeader.add(totalProfitLabel, BorderLayout.CENTER);
		totalProfitContainer.add(totalProfitHeader, BorderLayout.NORTH);

		totalProfitValueLabel = new JLabel();
		totalProfitValueLabel.setFont(FontManager.getRunescapeFont());
		totalProfitValueLabel.setHorizontalAlignment(JLabel.CENTER);
		totalProfitValueLabel.setForeground(ColorScheme.GRAND_EXCHANGE_ALCH);
		totalProfitValueLabel.setBorder(new EmptyBorder(2, 0, 0, 0));
		totalProfitContainer.add(totalProfitValueLabel, BorderLayout.CENTER);

		JPanel checkboxPanel = new JPanel(new BorderLayout());
		checkboxPanel.setBackground(ColorScheme.DARK_GRAY_COLOR);
		checkboxPanel.setBorder(new EmptyBorder(0, 2, 0, 2));
		shortenProfitCheckbox = new JCheckBox("Use ShortHand");
		shortenProfitCheckbox.setSelected(true);
		shortenProfitCheckbox.setBackground(ColorScheme.DARK_GRAY_COLOR);
		shortenProfitCheckbox.setForeground(Color.WHITE);

		shortenProfitCheckbox.setHorizontalTextPosition(SwingConstants.LEFT);

		shortenProfitCheckbox.addActionListener(e -> {
			String currentTotalProfit = totalProfitValueLabel.getToolTipText();
			if (currentTotalProfit != null)
			{
				setTotalProfit(currentTotalProfit, shortenProfitCheckbox.isSelected());
			}
			else
			{
				setTotalProfit("0", shortenProfitCheckbox.isSelected());
			}
		});
		checkboxPanel.add(shortenProfitCheckbox, BorderLayout.EAST);
		checkboxPanel.add(refreshFlips, BorderLayout.WEST);
		totalProfitContainer.add(checkboxPanel, BorderLayout.SOUTH);
		totalProfitContainer.setBorder(UiUtilities.ITEM_INFO_BORDER);
		container.add(totalProfitContainer, BorderLayout.NORTH);

		SearchBar searchBar = new SearchBar(this.onSearchTextChanged);
		container.add(searchBar, BorderLayout.CENTER);

		this.trackFlipsToggle = new Toggle("Track Flips", true, this.toggleIsTrackingFlipsRunnable);
		container.add(trackFlipsToggle, BorderLayout.SOUTH);

		container.setBorder(new EmptyBorder(0, 0, 3, 0));
		this.add(container, BorderLayout.NORTH);

	}

	/**
	 * Sets the total profit label, formatting it according to the useShortFormat flag.
	 *
	 * @param totalProfitStr The total profit value (as a String).
	 * @param useShortFormat Whether to use the shortened number format.
	 */
	public void setTotalProfit(String totalProfitStr, boolean useShortFormat)
	{
		try
		{
			int totalProfit = Integer.parseInt(totalProfitStr.replace(",", ""));
			String formattedProfit = useShortFormat
				? Numbers.toShortNumber(totalProfit)
				: Numbers.numberWithCommas(totalProfit);
			totalProfitValueLabel.setText(formattedProfit);
			totalProfitValueLabel.setToolTipText(Numbers.numberWithCommas(totalProfit));

			if (totalProfit >= 0)
			{
				totalProfitValueLabel.setForeground(ColorScheme.GRAND_EXCHANGE_ALCH);
			}
			else
			{
				totalProfitValueLabel.setForeground(ColorScheme.PROGRESS_ERROR_COLOR);
			}
		}
		catch (NumberFormatException e)
		{
			totalProfitValueLabel.setText("Error");
			totalProfitValueLabel.setToolTipText(null);
		}
	}

	/**
	 * Overload setTotalProfit to use existing checkbox
	 *
	 * @param totalProfitStr The total profit value (as a String).
	 */
	public void setTotalProfit(String totalProfitStr)
	{
		setTotalProfit(totalProfitStr, this.shortenProfitCheckbox.isSelected());
	}

	public void build(Boolean isTrackingFlips)
	{
		JPanel scrollContainer = new JPanel();
		scrollContainer.setLayout(new BorderLayout());
		container = new JPanel();
		container.setLayout(new BoxLayout(container, BoxLayout.PAGE_AXIS));
		container.setBackground(ColorScheme.DARK_GRAY_COLOR);

		JScrollPane scrollPane = new JScrollPane(scrollContainer);
		scrollPane.setBackground(ColorScheme.LIGHT_GRAY_COLOR);
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

		scrollContainer.add(container, BorderLayout.PAGE_START);
		this.add(scrollPane, BorderLayout.CENTER);

		this.trackFlipsToggle.setSelected(isTrackingFlips);
	}
}