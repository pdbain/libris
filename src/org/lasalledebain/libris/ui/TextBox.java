package org.lasalledebain.libris.ui;

import javax.swing.JTextArea;
import javax.swing.text.JTextComponent;

import org.lasalledebain.libris.Record;

public class TextBox<RecordType extends Record> extends GenericTextControl<RecordType> {

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
