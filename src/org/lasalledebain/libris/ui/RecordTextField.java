package org.lasalledebain.libris.ui;

import javax.swing.JTextField;
import javax.swing.text.JTextComponent;

import org.lasalledebain.libris.Record;

public class RecordTextField<RecordType extends Record> extends GenericTextControl<RecordType> {

	public RecordTextField(int height, int width, boolean editable) {
		super(height, width, editable);
	}
	public RecordTextField(String contents) {
		this(0,0, false);
		setFieldValue(contents);
	}

	@Override
	protected JTextComponent displayControls() {
		JTextField tempControl = new JTextField(mainValue);
		tempControl.getDocument().addDocumentListener(getModificationListener());
		tempControl.setCaretPosition(0);
		return tempControl;
	}
}
