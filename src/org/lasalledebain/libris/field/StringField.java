package org.lasalledebain.libris.field;

import org.lasalledebain.libris.Field;
import org.lasalledebain.libris.FieldTemplate;
import org.lasalledebain.libris.exception.FieldDataException;
import org.lasalledebain.libris.exception.InputException;

public class StringField extends GenericField<FieldSingleStringValue> {
	
	@Override
	public FieldSingleStringValue valueOf(FieldValue original) throws FieldDataException {
		return (original instanceof FieldSingleStringValue) ? (FieldSingleStringValue) original: valueOf(original.getMainValueAsString());
	}

	@Override
	public boolean isText() {
		return true;
	}

	public StringField(FieldTemplate template) {
		super(template);
	}

	protected FieldSingleStringValue valueOf(String data) {
		return new FieldSingleStringValue(data);
	}

	@Override
	protected FieldValue valueOf(int value, String extraValue) throws FieldDataException {
		try {
			return new FieldStringPairValue(Integer.toString(value), extraValue);
		} catch (InputException e) {
			throw new FieldDataException("error in field "+getFieldId(), e);
		}
	}

	@Override
	public void addValue(String data) throws FieldDataException {
		addFieldValue(valueOf(data));
	}

	@Override
	public void addIntegerValue(int data) throws FieldDataException {
		addFieldValue(valueOf(Integer.toString(data)));
	}

	@Override
	public Field duplicate() throws FieldDataException {
		StringField otherField = new StringField(template);
		copyValues(otherField);
		return otherField;
	}

}