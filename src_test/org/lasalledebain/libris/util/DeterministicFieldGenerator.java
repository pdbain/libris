package org.lasalledebain.libris.util;

import java.util.Arrays;
import java.util.HashSet;

public class DeterministicFieldGenerator implements FieldGenerator {

	private String wordList[];
	private int fieldCount;
	public DeterministicFieldGenerator(String[] wordList) {
		this.wordList = wordList;
		fieldCount = 0;
	}
	@Override
	public String makeFieldString(HashSet<String> keyWords) {
		
		final String field = wordList[fieldCount];
		keyWords.addAll(Arrays.asList(field.split("[^\\w]")));
		fieldCount = (fieldCount + 1) % wordList.length;
		return field;
	}

}
