package org.lasalledebain.libris.search;

import java.util.Arrays;
import java.util.stream.StreamSupport;

import org.lasalledebain.libris.Field;
import org.lasalledebain.libris.Record;
import org.lasalledebain.libris.exception.DatabaseError;
import org.lasalledebain.libris.exception.InputException;

public class BooleanFilter<T extends Record> extends GenericFilter<T> {

	protected final boolean myValue;
	public BooleanFilter(int fieldId, boolean theValue, boolean incDefault) {
		super(incDefault, new int[] {fieldId});
		this.myValue = theValue;
	}

	public BooleanFilter(int fieldIds[], boolean theValue, boolean incDefault) {
		super(incDefault,fieldIds);
		this.myValue = theValue;
	}

	@Override
	public boolean test(Record rec) {
		return StreamSupport.intStream(Arrays.spliterator(myFieldList), true)
				.anyMatch(fieldNum -> {
					try {
						final Field fld = rec.getField(fieldNum, doIncludeDefault);
						return (null != fld) && (fld.isTrue());
					} catch (InputException e) {
						throw new DatabaseError("Illegal field "+fieldNum);
					} 
				});
	}

}
