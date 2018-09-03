package org.lasalledebain.libris.field;

public class EmptyFieldValue extends FieldValue {

	@Override
	public String getValueAsString() {
		return "";
	}

	@Override
	protected boolean singleValueEquals(FieldValue comparand) {
		return false;
	}

	@Override
	public FieldValue duplicate() {
		return this;
	}

	@Override
	public boolean isTrue() {
		return false;
	}

	@Override
	public boolean isEmpty() {
		return true;
	}

}
