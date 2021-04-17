package org.lasalledebain.libris.search;

import org.lasalledebain.libris.Record;

public abstract class GenericFilter<T extends Record> implements RecordFilter<T> {

	protected final int myFieldList[];
	protected final boolean doIncludeDefault;

	public GenericFilter(boolean includeDefault, int[] theFieldList) {
		doIncludeDefault = includeDefault;
		myFieldList = theFieldList;
	}

	@Override
	public abstract boolean test(T t);

}
