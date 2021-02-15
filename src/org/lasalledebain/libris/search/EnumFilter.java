package org.lasalledebain.libris.search;

import java.util.stream.StreamSupport;

import org.lasalledebain.libris.Field;
import org.lasalledebain.libris.Record;
import org.lasalledebain.libris.exception.InputException;
import org.lasalledebain.libris.field.FieldEnumValue;

public class EnumFilter implements RecordFilter {

	private final int myFieldId;
	private final FieldEnumValue myValue;
	
	public EnumFilter(int fieldId, FieldEnumValue theValue) {
		this.myValue = theValue;
		myFieldId = fieldId;
	}

	@Override
	public boolean matches(Record rec) throws InputException {
		boolean result = false;
		Field theField = rec.getField(myFieldId);
		if (null != theField) {
			StreamSupport.stream(theField.getFieldValues().spliterator(), false);
		}
		return result;
	}

}