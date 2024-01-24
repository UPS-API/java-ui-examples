package com.ups.oauthdemo.view.components;

import java.awt.Cursor;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.JTextField;
import javax.swing.text.Document;

public class StyledTextField extends JTextField {
	private void setCustomStyling() {
		setEditable(false);
		setOpaque(false);
		setFont(new Font(Font.DIALOG, Font.PLAIN, 17));
		setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
		setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));
	}
	
	public StyledTextField() {
		super();
		setCustomStyling();
	}

	public StyledTextField(Document doc, String text, int columns) {
		super(doc, text, columns);
		setCustomStyling();
	}

	public StyledTextField(int columns) {
		super(columns);
		setCustomStyling();
	}

	public StyledTextField(String text, int columns) {
		super(text, columns);
		setCustomStyling();
	}

	public StyledTextField(String text) {
		super(text);
		setCustomStyling();
	}

	
}
