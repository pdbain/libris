package org.lasalledebain.libris.field;

import java.net.URL;

import org.lasalledebain.libris.Field;
import org.lasalledebain.libris.FieldTemplate;
import org.lasalledebain.libris.exception.FieldDataException;

public class LocationField extends GenericField implements Field {
	/* (non-Javadoc)
	 * @see org.lasalledebain.libris.field.GenericField#addURLValue(java.net.URL)
	 */
	@Override
	public void addURLValue(URL value) throws FieldDataException {
		addValue(value.toString());
	}

	/* (non-Javadoc)
	 * @see org.lasalledebain.libris.field.GenericField#addValuePair(java.lang.String, java.lang.String)
	 */
	@Override
	public void addValuePair(String value, String extraValue) throws FieldDataException {
		// TODO Auto-generated method stub
		super.addValuePair(value, extraValue);
	}

	public LocationField(FieldTemplate template) {
		super(template);
	}

	@Override
	public void addValue(String data) throws FieldDataException {
		addFieldValue(new FieldLocationValue(data, null));
	}

	@Override
	public void addIntegerValue(int data) throws FieldDataException {
		throw new  FieldDataException("Invalid data for "+template.getFieldTitle());
	}

	@Override
	public Field duplicate() throws FieldDataException {
		LocationField f = new LocationField(template);
		copyValues(f);
		return f;
	}

}

