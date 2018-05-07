package org.lasalledebain.libris.ui;

import java.awt.Component;

import javax.swing.text.JTextComponent;

import org.lasalledebain.libris.exception.FieldDataException;
import org.lasalledebain.libris.field.FieldSingleStringValue;
import org.lasalledebain.libris.field.FieldValue;

public abstract class GenericTextControl extends GuiControl {

	protected String mainValue;
	protected JTextComponent control;
	public GenericTextControl(int height, int width, boolean editable) {
		super(height, width, editable);
		mainValue = EMPTY_TEXT_VALUE;
		displayControls();
	}

	@Override
	public void setFieldValue(FieldValue newValue) throws FieldDataException {
		setFieldValue(newValue.getValueAsString());
	}

	@Override
	public void setFieldValue(String controlValue) {
		mainValue = controlValue;
		copyValuesToControls();
	}

	@Override
	public boolean isEmpty() {
		return mainValue.isEmpty();
	}
	@Override
	public void setEmpty(boolean empty) {
		setFieldValue(EMPTY_TEXT_VALUE);
	}

	@Override
	public void setEditable(boolean newEditableValue) {
		copyValuesFromControls();
		editable = newEditableValue;
		displayControls();
	}
	@Override
	public FieldValue getFieldValue() throws FieldDataException {
		copyValuesFromControls();
		return new FieldSingleStringValue(mainValue);
	}

	protected void addModificationListener() {
		control.getDocument().addDocumentListener(getModificationListener());
	}

	@Override
	public Component getGuiComponent() {
		return control;
	}

	protected void copyValuesFromControls() {
		if (isEditable()) {
			mainValue = control.getText();
		}
	}

	protected void copyValuesToControls() {
		if (isEditable()) {
			control.setText(mainValue);
		}
	}

}
