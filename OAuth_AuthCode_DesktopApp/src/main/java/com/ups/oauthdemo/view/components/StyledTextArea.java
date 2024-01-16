package com.ups.oauthdemo.view.components;

import java.awt.Cursor;
import java.awt.Font;

import javax.swing.JTextArea;
import javax.swing.text.Document;

public class StyledTextArea extends JTextArea {
	private void setCustomStyling() {
		setLineWrap(true);
		setWrapStyleWord(true);
		setEditable(false);
		setOpaque(false);
		setFont(new Font(Font.DIALOG, Font.PLAIN, 15));
		setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
	}
	
	public StyledTextArea() {
		super();
		setCustomStyling();
	}

	public StyledTextArea(Document doc, String text, int rows, int columns) {
		super(doc, text, rows, columns);
		setCustomStyling();
	}

	public StyledTextArea(Document doc) {
		super(doc);
		setCustomStyling();
	}

	public StyledTextArea(int rows, int columns) {
		super(rows, columns);
		setCustomStyling();
	}

	public StyledTextArea(String text, int rows, int columns) {
		super(text, rows, columns);
		setCustomStyling();
	}

	public StyledTextArea(String text) {
		super(text);
		setCustomStyling();
	}
	
}
