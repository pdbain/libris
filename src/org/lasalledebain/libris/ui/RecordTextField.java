package org.lasalledebain.libris.ui;

import java.awt.Component;

import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.lasalledebain.libris.exception.FieldDataException;
import org.lasalledebain.libris.field.FieldSingleStringValue;
import org.lasalledebain.libris.field.FieldValue;

public class RecordTextField extends GuiControl {

	JTextField control;
	public RecordTextField(int height, int width) {
		control = new JTextField();
		control.getDocument().addDocumentListener(getModificationListener());
	}

	public RecordTextField(String contents) {
		this(0,0);
		setFieldValue(contents);
	}

	@Override
	public void setFieldValue(FieldValue newValue) throws FieldDataException {
		setFieldValue(newValue.getValueAsString());
	}
	@Override
	public void setFieldValue(String controlValue) {
		control.setText(controlValue);
		control.setCaretPosition(0);
		empty = false;
	}
	@Override
	public FieldValue getFieldValue() throws FieldDataException {
		return new FieldSingleStringValue(control.getText());
	}
	@Override
	public Component getGuiComponent() {
		return control; 
	}
	@Override
	public void setEditable(boolean editable) {
		control.setEditable(editable);
	}
	
	@Override
	public boolean isEditable() {
		return control.isEditable();
	}

}
