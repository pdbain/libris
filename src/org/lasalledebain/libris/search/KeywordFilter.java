package org.lasalledebain.libris.search;

import java.util.Arrays;

import org.lasalledebain.libris.Record;
import org.lasalledebain.libris.exception.DatabaseError;
import org.lasalledebain.libris.exception.InputException;
import org.lasalledebain.libris.indexes.ContainsKeywords;
import org.lasalledebain.libris.indexes.ExactKeywordList;
import org.lasalledebain.libris.indexes.PrefixKeywords;
import org.lasalledebain.libris.indexes.RecordKeywords;

public class KeywordFilter implements RecordFilter {

	private int[] myFieldList;
	private Iterable<String> terms;
	private RecordKeywords recWords;

	public KeywordFilter(MATCH_TYPE matchType, boolean caseSensitive, int fieldList[], String searchTerms[]) {
		this(matchType, caseSensitive, fieldList, Arrays.asList(searchTerms));
	}
	
	public KeywordFilter(MATCH_TYPE matchType, boolean caseSensitive, int fieldList[], Iterable<String> searchTerms) {

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
		myFieldList = fieldList;
		terms = searchTerms;
	}

	@Override
	public boolean test(Record rec) {
		recWords.clear();
		try {
			rec.getKeywords(myFieldList, recWords);
		} catch (InputException e) {
			throw new DatabaseError("Error getting keywords from record "+rec.getRecordId(), e);
		}
		return recWords.contains(terms);
	}
	
}
