package org.lasalledebain.libris.field;

import org.lasalledebain.libris.Field;
import org.lasalledebain.libris.FieldTemplate;
import org.lasalledebain.libris.exception.FieldDataException;

public class UrlField extends GenericField implements Field {
	public UrlField(FieldTemplate template) {
		super(template);
	}

	@Override
	public void addValue(String data) throws FieldDataException {

		try {
			addFieldValue(new FieldIntValue(data));
		} catch (NumberFormatException e) {
			throw new FieldDataException("invalid data: "+data+" for integer field "+getFieldId());
		}
	}

	@Override
	public void addIntegerValue(int data) throws FieldDataException {
		addFieldValue(new FieldIntValue(data));
	}

	@Override
	public Field duplicate() throws FieldDataException {
		UrlField f = new UrlField(template);
		copyValues(f);
		return f;
	}

}
