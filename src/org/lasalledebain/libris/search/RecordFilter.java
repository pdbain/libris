package org.lasalledebain.libris.search;

import java.util.function.Predicate;

import org.lasalledebain.libris.Record;

public interface RecordFilter extends Predicate<Record>{
	public enum MATCH_TYPE {
		MATCH_EXACT,
		MATCH_PREFIX,
		MATCH_CONTAINS
	}
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
	
}

