package org.lasalledebain.libris.search;

import org.lasalledebain.libris.Record;
import org.lasalledebain.libris.exception.InputException;

public interface RecordFilter {
	public static enum SEARCH_TYPE {
		T_SEARCH_KEYWORD("Keyword"), T_SEARCH_ENUM("Enumeration"), T_SEARCH_RECORD_NAME("Record name");
		String name;

		@Override
		public String toString() {
			return name;
		}

		private SEARCH_TYPE(String name) {
			this.name = name;
		}
		
	}
	public abstract boolean matches(Record rec) throws InputException;
	
}
