package org.lasalledebain.libris.search;

import java.util.Arrays;
import java.util.Collections;

import org.lasalledebain.libris.Record;
import org.lasalledebain.libris.exception.DatabaseError;
import org.lasalledebain.libris.exception.InputException;
import org.lasalledebain.libris.indexes.ContainsKeywords;
import org.lasalledebain.libris.indexes.ExactKeywordList;
import org.lasalledebain.libris.indexes.PrefixKeywords;
import org.lasalledebain.libris.indexes.RecordKeywords;
import org.lasalledebain.libris.search.RecordFilter.SEARCH_TYPE;

public class TextFilter<T extends Record> extends GenericFilter<T> {

	@Override
	public SEARCH_TYPE getType() {
		return SEARCH_TYPE.T_SEARCH_KEYWORD;
	}

	private Iterable<String> terms;
	private RecordKeywords recWords;

	public TextFilter(MATCH_TYPE matchType, boolean caseSensitive, boolean includeDefault, int fieldList[], String searchTerms[]) {
		this(matchType, caseSensitive, includeDefault, fieldList, Arrays.asList(searchTerms));
	}
	
	public TextFilter(MATCH_TYPE matchType, boolean caseSensitive, int fieldList[], String searchTerms[]) {
		this(matchType, caseSensitive, false, fieldList, Arrays.asList(searchTerms));
	}
	
	public TextFilter(MATCH_TYPE matchType, boolean caseSensitive, int fieldList[], String searchTerm) {
		this(matchType, caseSensitive, false, fieldList, Collections.singletonList(searchTerm));
	}
	
	public TextFilter(MATCH_TYPE matchType, boolean caseSensitive, boolean includeDefault, int fieldList[], String searchTerm) {
		this(matchType, caseSensitive, includeDefault, fieldList, Collections.singletonList(searchTerm));
	}
	
	public TextFilter(MATCH_TYPE matchType, boolean caseSensitive, boolean includeDefault, int fieldList[], Iterable<String> searchTerms) {
		super(includeDefault, fieldList);
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
		terms = searchTerms;
	}

	@Override
	public boolean test(Record rec) {
		recWords.clear();
		try {
			rec.getKeywords(myFieldList, doIncludeDefault, recWords);
		} catch (InputException e) {
			throw new DatabaseError("Error getting keywords from record "+rec.getRecordId(), e);
		}
		return recWords.contains(terms);
	}
	
}
