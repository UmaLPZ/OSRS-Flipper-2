package com.flipper2.helpers;

import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;

import net.runelite.client.ui.ColorScheme;

public class UiUtilities
{
	public static final Dimension ICON_SIZE = new Dimension(32, 32);
	public static final Border ITEM_INFO_BORDER = new CompoundBorder(
		BorderFactory.createMatteBorder(0, 0, 0, 0, ColorScheme.DARK_GRAY_COLOR),
		BorderFactory.createLineBorder(ColorScheme.DARKER_GRAY_COLOR.darker(), 4));


	public static String truncateString(String string, int length)
	{
		if (string.length() > length)
		{
			return string.substring(0, length) + "...";
		}
		return string;
	}

	public static final String FLIPPER_NAV_ICON = "/flipper_nav_button.png";
	public static final String DELETE_X = "/delete_x.png";
	public static final String REFRESH_ICON = "/refresh.png";
	public static final String GITHUB_ICON = "/github.png";
	public static final int ITEMS_PER_PAGE = 15;


}