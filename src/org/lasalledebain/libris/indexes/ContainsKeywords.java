package org.lasalledebain.libris.indexes;

import org.lasalledebain.libris.search.RecordFilter.MATCH_TYPE;


public class ContainsKeywords extends ExactKeywordList {

	@Override
	public MATCH_TYPE getMatchType() {
		return MATCH_TYPE.MATCH_CONTAINS;
	}

	public ContainsKeywords(boolean caseSensitive) {
		super(caseSensitive);
	}

	@Override
	public boolean contains(Iterable<String> wordList) {
		for (String w: wordList) {
			String searchWord = caseSensitive ? w: w.toLowerCase();
			boolean found = keywordList.contains(searchWord);
			if (!found) {
				for (String k: keywordList) {
					if (k.contains(searchWord)) {
						found = true;
						break;
					}
				}
			}
			if (!found) {
				return false;
			}
		}
		return true;
	}

}
