package org.lasalledebain.libris.field;

public class FieldSingleStringValue extends FieldValue {
	@Override
	public boolean isEmpty() {
		return (null == value) || value.isEmpty();
	}
	String value;
	public FieldSingleStringValue(String value) {
		this.value = value;
	}
	@Override
	public String getValueAsString() {
		return value;
	}
	@Override
	protected boolean singleValueEquals(FieldValue comparand) {
		FieldSingleStringValue other = (FieldSingleStringValue) comparand;
		boolean valueEquals = (value == other.value);
		if (!valueEquals && (null != value)) {
			valueEquals = value.equals(other.value);
		}
		return valueEquals;
	}
	@Override
	public FieldValue duplicate() {
		return new FieldSingleStringValue(value);
	}
}
