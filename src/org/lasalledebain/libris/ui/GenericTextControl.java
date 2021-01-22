package org.lasalledebain.libris.ui;

import javax.swing.JComponent;
import javax.swing.text.JTextComponent;

import org.lasalledebain.libris.exception.FieldDataException;
import org.lasalledebain.libris.field.FieldSingleStringValue;
import org.lasalledebain.libris.field.FieldValue;

public abstract class GenericTextControl extends GuiControl {

	protected String mainValue;
	protected final JTextComponent control;
	protected abstract JTextComponent displayControls();
	public GenericTextControl(int height, int width, boolean editable) {
		super(height, width, editable);
		mainValue = EMPTY_TEXT_VALUE;
		control = displayControls();
		if (editable) {
			addModificationListener();
		}
		control.setEnabled(editable);
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
	public FieldValue getFieldValue() throws FieldDataException {
		copyValuesFromControls();
		return new FieldSingleStringValue(mainValue);
	}

	protected void addModificationListener() {
		control.getDocument().addDocumentListener(getModificationListener());
	}

	@Override
	public JComponent getGuiComponent() {
		return control;
	}

	 protected void copyValuesFromControls() {
		if (isEditable()) {
			mainValue = control.getText();
		}
	}

	 void copyValuesToControls() {
		 control.setText(mainValue);
	 }

}
