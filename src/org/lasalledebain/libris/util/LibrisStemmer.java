package org.lasalledebain.libris.util;

public class LibrisStemmer extends Stemmer {
	public LibrisStemmer(char[] word) {
		super(word);
	}

	public LibrisStemmer(String word, boolean convertToLower) {
		super(toLower(word, convertToLower));
	}

	public LibrisStemmer(String word) {
		super(word.toCharArray());
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
