package com.ups.oauthdemo.view.components;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JButton;

public class StyledButton extends JButton {

	private void setCustomStyling() {
		setAlignmentX(java.awt.Component.CENTER_ALIGNMENT);
		setBackground(new Color(255, 196, 0));
		setFont(new Font(Font.DIALOG, Font.BOLD, 30));
		setFocusPainted(false);
		setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
	}

	public StyledButton() {
		super();
		setCustomStyling();
	}

	public StyledButton(Action a) {
		super(a);
		setCustomStyling();
	}

	public StyledButton(Icon icon) {
		super(icon);
		setCustomStyling();
	}

	public StyledButton(String text, Icon icon) {
		super(text, icon);
		setCustomStyling();
	}

	public StyledButton(String text) {
		super(text);
		setCustomStyling();
	}

}
