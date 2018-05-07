package org.lasalledebain.libris.ui;

import javax.swing.JTextArea;

public class TextBox extends GenericTextControl {

	public TextBox(int height, int width, boolean editable) {
		super(height, width, editable);
	}

	protected void displayControls() {
		final JTextArea tempControl;
		tempControl = ((height > 0) && (width > 0)) ?
				new JTextArea(height, width): new JTextArea();
				tempControl.setLineWrap(true);
				control = tempControl;
				addModificationListener();
	}
}
