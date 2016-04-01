package org.lasalledebain.libris.ui;

import java.awt.Component;

import javax.swing.JTextArea;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.lasalledebain.libris.exception.FieldDataException;
import org.lasalledebain.libris.field.FieldSingleStringValue;
import org.lasalledebain.libris.field.FieldValue;

public class TextBox extends GuiControl {

	JTextArea control;
	public TextBox(int height, int width) {
		if ((height > 0) && (width > 0)) {
			control = new JTextArea(height, width);
		} else {
			control = new JTextArea();
		}
		control.setLineWrap(true);
		addModificationListener();
	}

	protected void addModificationListener() {
		control.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void removeUpdate(DocumentEvent e) {
				setModified(true);
			}
			@Override
			public void insertUpdate(DocumentEvent e) {
				setModified(true);
			}
			@Override
			public void changedUpdate(DocumentEvent e) {
				setModified(true);
			}
		});
	}

	@Override
	public void setFieldValue(String controlValue) {
		control.setText(controlValue);
		control.setCaretPosition(0);
	}
	@Override
	public Component getGuiComponent() {
		return control;
	}
	@Override
	public FieldValue getFieldValue() throws FieldDataException {
		return new FieldSingleStringValue(control.getText());
	}
	@Override
	public void setFieldValue(FieldValue newValue) throws FieldDataException {
		setFieldValue(newValue.getValueAsString());
	}
	@Override
	public void setEditable(boolean editable) {
		control.setEditable(editable);
	}
}
