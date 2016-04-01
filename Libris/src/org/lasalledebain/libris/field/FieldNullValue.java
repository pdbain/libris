package org.lasalledebain.libris.field;

public class FieldNullValue extends FieldValue {

	@Override
	public boolean isEmpty() {
		// TODO Auto-generated method stub
		return super.isEmpty();
	}

	@Override
	public String getValueAsString() {
		return "";
	}

	@Override
	protected boolean singleValueEquals(FieldValue comparand) {
		return true;
	}

	@Override
	public FieldValue duplicate() {
		return this;
	}
}
