package org.lasalledebain.libris.search;

import java.util.Arrays;
import java.util.List;

import org.lasalledebain.libris.Record;
import org.lasalledebain.libris.exception.InputException;
import org.lasalledebain.libris.indexes.ContainsKeywords;
import org.lasalledebain.libris.indexes.ExactKeywordList;
import org.lasalledebain.libris.indexes.PrefixKeywords;
import org.lasalledebain.libris.indexes.RecordKeywords;

public class KeywordFilter implements RecordFilter {

	private int[] fieldList;
	private List<String> terms;
	private RecordKeywords recWords;

	public enum MATCH_TYPE {
		MATCH_EXACT,
		MATCH_PREFIX,
		MATCH_CONTAINS
	}
	
	public KeywordFilter(MATCH_TYPE matchType, boolean caseSensitive, int searchList[], String searchTerms[]) {
		switch (matchType) {
		case MATCH_EXACT:
			recWords = new ExactKeywordList(caseSensitive);
			break;
		case MATCH_PREFIX:
			recWords = new PrefixKeywords(caseSensitive);
			break;
		case MATCH_CONTAINS:
			recWords = new ContainsKeywords(caseSensitive);
			break;
		}
		fieldList = searchList;
		terms = Arrays.asList(searchTerms);
	}

	public boolean matches(Record rec) throws InputException {
		recWords.clear();
		rec.getKeywords(fieldList, recWords);
		return recWords.contains(terms);
	}
	
	public MATCH_TYPE getMatchType() {
		return recWords.getMatchType();
	}
	
	public boolean isCaseSenitive() {
		return recWords.isCaseSensitive();
	}
	
	public Iterable<String> getKeywords() {
		return recWords.getKeywords();
	}

	public int[] getFieldList() {
		return fieldList;
	}
}
