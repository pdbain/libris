package org.lasalledebain.libris.search;

import java.util.Arrays;
import java.util.List;

import org.lasalledebain.libris.Record;
import org.lasalledebain.libris.exception.InputException;
import org.lasalledebain.libris.indexes.ExactKeywordList;
import org.lasalledebain.libris.indexes.PrefixKeywords;
import org.lasalledebain.libris.indexes.RecordKeywords;

public class KeywordFilter implements RecordFilter {

	private int[] fieldList;
	private List<String> terms;
	private RecordKeywords recWords;

	public KeywordFilter(boolean exact, boolean caseSensitive, int searchList[], String searchTerms[]) {
		recWords = exact? new ExactKeywordList(caseSensitive): new PrefixKeywords(caseSensitive);
		fieldList = searchList;
		terms = Arrays.asList(searchTerms);
	}

	public boolean matches(Record rec) throws InputException {
		recWords.clear();
		rec.getKeywords(fieldList, recWords);
		return recWords.contains(terms);
	}		
}
