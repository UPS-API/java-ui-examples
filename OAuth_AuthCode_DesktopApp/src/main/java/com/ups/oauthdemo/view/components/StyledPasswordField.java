package com.ups.oauthdemo.view.components;

import java.awt.Cursor;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.JPasswordField;
import javax.swing.text.Document;

public class StyledPasswordField extends JPasswordField {
	private void setCustomStyling() {
		setEditable(false);
		setOpaque(false);
		setFont(new Font(Font.DIALOG, Font.PLAIN, 20));
		setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
		setBorder(BorderFactory.createEmptyBorder());
		setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));
	}

	public StyledPasswordField() {
		super();
		setCustomStyling();
	}

	public StyledPasswordField(Document doc, String txt, int columns) {
		super(doc, txt, columns);
		setCustomStyling();
	}

	public StyledPasswordField(int columns) {
		super(columns);
		setCustomStyling();
	}

	public StyledPasswordField(String text, int columns) {
		super(text, columns);
		setCustomStyling();
	}

	public StyledPasswordField(String text) {
		super(text);
		setCustomStyling();
	}
	

	
}
