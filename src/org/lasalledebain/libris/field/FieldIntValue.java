package org.lasalledebain.libris.field;

import org.lasalledebain.libris.exception.FieldDataException;

public class FieldIntValue extends FieldValue {
	@Override
	public int getValueAsInt() {
		return value;
	}

	int value;
	public FieldIntValue(String data) throws FieldDataException {
		try {
			value = Integer.parseInt(data);
		} catch (NumberFormatException e) {
			throw new FieldDataException("Cannot parse "+data+" as int", e);
		}
	}

	public FieldIntValue(int newValue) {
		value = newValue;
	}

	@Override
	public String getValueAsString() {
		return Integer.toString(value);
	}

	@Override
	protected boolean equals(FieldValue comparand) {
		FieldIntValue other = (FieldIntValue) comparand;
		boolean valueEquals = (value == other.value);
		return valueEquals;
	}
}
