package com.flipper2.helpers;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import net.runelite.client.ui.ColorScheme;

public class CustomPanel extends JPanel
{
	private static final Border DEFAULT_BOTTOM_BORDER = BorderFactory.createMatteBorder(0, 0, 1, 0, Color.BLACK);
	private static final Color DEFAULT_BACKGROUND = ColorScheme.DARK_GRAY_COLOR;

	public CustomPanel(LayoutManager layout, boolean addBottomBorder)
	{
		this(layout, addBottomBorder, null);
	}

	public CustomPanel(LayoutManager layout, boolean addBottomBorder, Color backgroundOverride)
	{
		super(layout);

		if (addBottomBorder)
		{
			setBorder(DEFAULT_BOTTOM_BORDER);
		}

		setBackground(backgroundOverride != null ? backgroundOverride : DEFAULT_BACKGROUND);
	}
}