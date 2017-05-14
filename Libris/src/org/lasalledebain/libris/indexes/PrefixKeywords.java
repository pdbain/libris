package org.lasalledebain.libris.indexes;

import java.util.AbstractSet;
import java.util.TreeSet;

public class PrefixKeywords extends RecordKeywords {

	private TreeSet<String> keywordList;
	public PrefixKeywords(boolean caseSensitive) {
		super(caseSensitive);
		keywordList = new TreeSet<String>();
	}

	@Override
	public
	boolean contains(Iterable<String> wordList) {
		for (String w: wordList) {
			String searchWord = caseSensitive ? w: w.toLowerCase();
			String result = keywordList.ceiling(searchWord);
			if ((null == result) || !result.startsWith(searchWord)) {
				return false;
			}
		}
		return true;
	}
	@Override
	protected AbstractSet<String> getKeywordList() {
		return keywordList;
	}

}
