package com.flipper2.helpers;

import java.awt.Color;
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

	public static String truncateString(String string, int length) {
		if (string.length() > length) {
			return string.substring(0, length) + "...";
		}
		return string;
	}
	public static final String flipperNavIcon = "/flipper_nav_button.png";
	public static final String deleteX = "/delete_x.png";
	public static final String refreshIcon = "/refresh.png";
	public static final String githubIcon = "/github.png";
	public static final int ITEMS_PER_PAGE = 15;


	public static final Color F2_GREEN = Color.decode("#2ea043");
	public static final Color F2_ORANGE = Color.decode("#e78709");
	public static final Color F2_BLUE = Color.decode("#388bfd");
	public static final Color F2_RED = Color.decode("#da3633");
	public static final Color F2_FONT_COLOR = Color.decode("#c9d1d9");
}