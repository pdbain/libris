package org.lasalledebain.libris.util;

/**
 * This adds convenience methods for the original code.
 *
 */
public class LibrisStemmer extends Stemmer {
	public LibrisStemmer(char[] word) {
		super();
		add(word, word.length);
	}

	public LibrisStemmer(String word, boolean convertToLower) {
		this(toLower(word, convertToLower));
	}

	public LibrisStemmer(String word) {
		this(word.toCharArray());
	}

	private static char [] toLower(String word, boolean convertToLower) {
		if (!convertToLower) {
			return word.toCharArray();
		} else {
			char[] normalizedWord = word.toLowerCase().toCharArray();
			return normalizedWord;
		}
	}
}
