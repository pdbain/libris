package org.lasalledebain.libris.ui;

import javax.swing.JTextField;

public class RecordTextField extends GenericTextControl {

	public RecordTextField(int height, int width, boolean editable) {
		super(height, width, editable);
	}
	public RecordTextField(String contents) {
		this(0,0, false);
		setFieldValue(contents);
	}

	@Override
	protected void displayControls() {
		JTextField tempControl = new JTextField(mainValue);
		tempControl.getDocument().addDocumentListener(getModificationListener());
		tempControl.setCaretPosition(0);
		control = tempControl;
	}
}
