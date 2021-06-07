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

public class EnumFilter<T extends Record> extends GenericFilter<T> {

	@Override
	public SEARCH_TYPE getType() {
		return SEARCH_TYPE.T_SEARCH_ENUM;
	}

	protected final FieldEnumValue myValue;
	public EnumFilter(int fieldId, FieldEnumValue theValue, boolean incDefault) {
		super(incDefault, new int[] {fieldId});
		this.myValue = theValue;
	}

	public EnumFilter(int fieldIds[], FieldEnumValue theValue, boolean incDefault) {
		super(incDefault, fieldIds);
		this.myValue = theValue;
	}

	@Override
	public boolean test(Record rec) {
		boolean result = false;
		Field theField;
		for (int myFieldId: myFieldList) {
			try {
				theField = rec.getField(myFieldId, doIncludeDefault);
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
				if (result) break;
			}
		}
		return result;
	}
}
