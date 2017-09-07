package org.lasalledebain.libris.indexes;

import java.util.AbstractSet;
import java.util.HashSet;

import org.lasalledebain.libris.search.RecordFilter.MATCH_TYPE;

public class ExactKeywordList extends RecordKeywords {

	protected HashSet<String> keywordList;
	public ExactKeywordList(boolean caseSensitive) {
		super(caseSensitive);
		keywordList = new HashSet<String>();
	}

	@Override
	public
	boolean contains(Iterable<String> wordList) {
		for (String w: wordList) {
			String searchWord = caseSensitive ? w: w.toLowerCase();
			if (!keywordList.contains(searchWord)) {
				return false;
			}
		}
		return true;
	}

	@Override
	protected AbstractSet<String> getKeywordList() {
		return keywordList;
	}

	@Override
	public MATCH_TYPE getMatchType() {
		return MATCH_TYPE.MATCH_EXACT;
	}
}
