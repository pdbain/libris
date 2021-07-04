package org.lasalledebain.libris.search;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.lasalledebain.libris.Record;
import org.lasalledebain.libris.indexes.ContainsKeywords;
import org.lasalledebain.libris.indexes.ExactKeywordList;
import org.lasalledebain.libris.indexes.PrefixKeywords;
import org.lasalledebain.libris.indexes.RecordKeywords;

public class RecordNameFilter<T extends Record> implements RecordFilter<T> {

	@Override
	public SEARCH_TYPE getType() {
		return SEARCH_TYPE.T_SEARCH_RECORD_NAME;
	}

	@Deprecated
	private List<String> terms;
	@Deprecated
	private RecordKeywords recWords;

	@Deprecated
	public RecordNameFilter(MATCH_TYPE theMatchType, boolean caseSensitive, String searchTerms[]) {
		switch (theMatchType) {
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
		terms = Arrays.asList(searchTerms);
	}

	public RecordNameFilter(MATCH_TYPE theMatchType, boolean caseSensitive, String theName) {
		switch (theMatchType) {
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
		terms = Collections.singletonList(theName);
	}

	public boolean matches(Record rec) {
		recWords.clear();
		String recName = rec.getName();
		recWords.addKeyword(recName);
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

	@Override
	public boolean test(Record rec) {
		return matches(rec);
	}
}
