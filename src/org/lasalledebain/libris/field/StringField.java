package org.lasalledebain.libris.field;

import org.lasalledebain.libris.Field;
import org.lasalledebain.libris.FieldTemplate;
import org.lasalledebain.libris.exception.FieldDataException;

public class StringField extends GenericField implements Field {
	
	@Override
	public boolean isText() {
		return true;
	}

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

	@Override
	public void addValueGeneral(FieldValue fieldData) throws FieldDataException {
		addValue(fieldData.getMainValueAsString());
	}

	@Override
	protected boolean isValueCompatible(FieldValue fv) {
		return fv instanceof FieldSingleStringValue;
	}

}