package org.lasalledebain.libris.field;

import org.lasalledebain.libris.Field;
import org.lasalledebain.libris.FieldTemplate;
import org.lasalledebain.libris.exception.FieldDataException;

public class StringField extends GenericField implements Field {
	

	public StringField(FieldTemplate template) {
		super(template);
	}

	@Override
	public void addValue(String data) throws FieldDataException {
		addFieldValue(new FieldSingleStringValue(data));
	}

	@Override
	public void addIntegerValue(int data) throws FieldDataException {
		addFieldValue(new FieldSingleStringValue(Integer.toString(data)));
	}

	@Override
	public Field duplicate() throws FieldDataException {
		StringField otherField = new StringField(template);
		copyValues(otherField);
		return otherField;
	}

}