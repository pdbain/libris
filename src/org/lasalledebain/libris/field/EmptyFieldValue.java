package org.lasalledebain.libris.field;

public class EmptyFieldValue extends FieldValue {

	@Override
	public String getValueAsString() {
		return "";
	}

	@Override
	public boolean equals(FieldValue comparand) {
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
