package org.lasalledebain.libris.ui;

import javax.swing.JTextArea;
import javax.swing.text.JTextComponent;

public class TextBox extends GenericTextControl {

	public TextBox(int height, int width, boolean editable) {
		super(height, width, editable);
	}

	protected JTextComponent displayControls() {
		final JTextArea tempControl;
		tempControl = ((height > 0) && (width > 0)) ?
				new JTextArea(height, width): new JTextArea();
				tempControl.setLineWrap(true);
				return tempControl;
	}
}
