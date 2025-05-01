package com.flipper2.views.components;

import javax.swing.ImageIcon;
import javax.swing.JButton;

import com.flipper2.helpers.UiUtilities;

import net.runelite.client.util.ImageUtil;
import java.awt.event.*;

public class DeleteButton extends JButton
{
	public DeleteButton(ActionListener onClick)
	{
		ImageIcon deleteIcon = new ImageIcon(ImageUtil.loadImageResource(getClass(), UiUtilities.deleteX));
		setIcon(deleteIcon);
		setBorderPainted(false);
		setContentAreaFilled(false);
		setFocusPainted(true);
		setOpaque(false);
		addActionListener(onClick);
	}
}
