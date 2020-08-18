package org.lasalledebain.libris.ui;

import java.util.Objects;

import javax.swing.JComponent;
import javax.swing.JPanel;

import org.lasalledebain.libris.Record;
import org.lasalledebain.libris.exception.FieldDataException;
import org.lasalledebain.libris.field.FieldSingleStringValue;
import org.lasalledebain.libris.field.FieldStringPairValue;
import org.lasalledebain.libris.field.FieldValue;

public abstract class ValuePairField<RecordType extends Record> extends GuiControl<RecordType> {

	protected final JPanel control;
	JPanel pairArea;
	protected int width;
	String mainValue, extraValue;
	public ValuePairField(int height, int width, boolean editable) {
		super(height, width, editable);
		this.width = width;
		mainValue = EMPTY_TEXT_VALUE;
		extraValue = EMPTY_TEXT_VALUE;
		control = displayControls();
	}

	@Override
	public boolean isEmpty() {
		return mainValue.isEmpty();
	}

	@Override
	protected abstract JPanel displayControls();

	@Override
	public JComponent getGuiComponent() {
		return control;
	}

	@Override
	public void setFieldValue(String controlValue) throws FieldDataException {
		setFieldValue(controlValue, null, false);
	}
	
	public void setFieldValue(FieldValue val) throws FieldDataException {
		String firstValue = val.getMainValueAsString();
		String secondValue = val.getExtraValueAsString();
		boolean twoValues = Objects.nonNull(secondValue) && !secondValue.isEmpty();
		setFieldValue(firstValue, secondValue, twoValues);
	}

	public void setFieldValue(String firstValue, String secondValue, boolean twoValues) {
		mainValue = firstValue;
		extraValue = twoValues? secondValue: EMPTY_TEXT_VALUE;
		copyValuesToControls();
	}

	@Override
	public void setEmpty(boolean empty) {
		mainValue = extraValue = EMPTY_TEXT_VALUE;
	}
	
	protected abstract void copyValuesFromControls();

	protected abstract void copyValuesToControls();

	@Override
	public FieldValue getFieldValue() throws FieldDataException {
		if (editable) {
			checkFieldValues();
			copyValuesFromControls();
		}

		FieldValue result;
		if (!isEmpty()) {
			if (extraValue.isEmpty()) {
				result = new FieldSingleStringValue(mainValue);				
			} else {	
				result = new FieldStringPairValue(mainValue, extraValue);
			}
		} else {
			result = FieldValue.getEmptyfieldvaluesingleton();
		}
		return result;
	}

	protected void checkFieldValues() throws FieldDataException {
		return;
	}
}
