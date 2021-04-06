package org.lasalledebain.libris.search;

import org.lasalledebain.libris.Record;

public class GenericFilter implements RecordFilter {

	protected final int myFieldList[];
	protected final boolean doIncludeDefault;

	public GenericFilter(boolean includeDefault, int[] theFieldList) {
		doIncludeDefault = includeDefault;
		myFieldList = theFieldList;
	}

	@Override
	public boolean test(Record t) {
		// TODO Auto-generated method stub
		return false;
	}

}
