package org.lasalledebain.libris.indexes;

import java.util.AbstractSet;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.lasalledebain.libris.search.RecordFilter.MATCH_TYPE;
import org.lasalledebain.libris.util.StringUtils;

public abstract class RecordKeywords {
	
	public static RecordKeywords createRecordKeywords(boolean exact, boolean caseSensitive) {
		return exact? (new ExactKeywordList(caseSensitive)) : (new PrefixKeywords(caseSensitive));
	}

	protected RecordKeywords(boolean caseSensitive) {
		this.caseSensitive = caseSensitive;
	}

	public boolean isCaseSensitive() {
		return caseSensitive;
	}

	protected final boolean caseSensitive;
	protected abstract AbstractSet<String> getKeywordList();
	public abstract Stream<String> wordStream();
	public abstract int estimateSize();
	
	/**
	 * Determine if the keyword list contains all words in wordList.
	 * If case-insensitive matching is used, wordList must be lower-case.
	 * @param wordList
	 * @return
	 */
	public abstract boolean contains(Iterable<String> wordList);
		
	public void addKeyword(String keyWord) {
		String kw;
		if (caseSensitive) {
			kw = keyWord;
		} else {
			kw = keyWord.toLowerCase();
		}
		getKeywordList().add(kw);
	}
	
	public void addKeywords(Iterable<String> keyWords) {
		for (String keyWord: keyWords) {
			addKeyword(keyWord);
		}
	}
	public void clear() {
		getKeywordList().clear();
	}

	public Iterable<String> getKeywords() {
		return getKeywordList();
	}
	
	public abstract MATCH_TYPE getMatchType();
}
