package org.lasalledebain.libris.search;

import static java.util.Objects.nonNull;

import java.util.function.Predicate;
import java.util.stream.StreamSupport;

import org.lasalledebain.libris.Field;
import org.lasalledebain.libris.Record;
import org.lasalledebain.libris.exception.DatabaseError;
import org.lasalledebain.libris.exception.InputException;
import org.lasalledebain.libris.field.EnumField;
import org.lasalledebain.libris.field.FieldEnumValue;

public class EnumFilter implements RecordFilter {

	private final int myFieldId;
	private final FieldEnumValue myValue;
	private final boolean includeDefault;

	public EnumFilter(int fieldId, FieldEnumValue theValue, boolean incDefault) {
		this.myValue = theValue;
		myFieldId = fieldId;
		includeDefault = incDefault;
	}

	@Override
	public boolean test(Record rec) {
		boolean result = false;
		Field theField;
		try {
			theField = includeDefault? 
					rec.getFieldOrDefault(myFieldId): 
						rec.getField(myFieldId);
		} catch (InputException e) {
			throw new DatabaseError("Illegal field "+myFieldId);
		}
		if (nonNull(theField) && (theField instanceof EnumField)) {
			EnumField theEnumField = (EnumField) theField;
			result = StreamSupport
					.stream(theEnumField
							.getFieldValues()
							.spliterator(), false)
					.anyMatch(Predicate.isEqual(myValue));
		}
		return result;
	}
}
