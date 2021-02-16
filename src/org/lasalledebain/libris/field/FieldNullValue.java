package org.lasalledebain.libris.field;

public class FieldNullValue extends FieldValue {

	@Override
	public String getValueAsString() {
		return "";
	}

	@Override
	protected boolean equals(FieldValue comparand) {
		return true;
	}

	@Override
	public FieldValue duplicate() {
		return this;
	}
}
