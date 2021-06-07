package org.lasalledebain.libris.search;

import java.util.function.Predicate;

import org.lasalledebain.libris.Record;

public interface RecordFilter<T extends Record> extends Predicate<T>{
	public enum MATCH_TYPE {
		MATCH_EXACT,
		MATCH_PREFIX,
		MATCH_CONTAINS
	}

	public static enum SEARCH_TYPE {
		T_SEARCH_KEYWORD("Keyword"), T_SEARCH_ENUM("Enumeration"), T_SEARCH_RECORD_NAME("Record name"),
		T_SEARCH_BOOLEAN("Boolean"), T_SEARCH_AFFILIATES("Affiliates");
		String name;

		@Override
		public String toString() {
			return name;
		}

		private SEARCH_TYPE(String name) {
			this.name = name;
		}
		
	}
	
	abstract SEARCH_TYPE getType();
}

