package org.lasalledebain.libris.field;

public class FieldIntValue extends FieldValue {
	@Override
	public int getValueAsInt() {
		return value;
	}

	int value;
	public FieldIntValue(String data) {
		value = Integer.parseInt(data);
	}

	public FieldIntValue(int newValue) {
		value = newValue;
	}

	@Override
	public String getValueAsString() {
		return Integer.toString(value);
	}

	@Override
	protected boolean singleValueEquals(FieldValue comparand) {
		FieldIntValue other = (FieldIntValue) comparand;
		boolean valueEquals = (value == other.value);
		return valueEquals;
	}

	@Override
	public FieldValue duplicate() {
		return new FieldIntValue(value);
	}

}
