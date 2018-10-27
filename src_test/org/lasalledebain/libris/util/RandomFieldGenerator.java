package org.lasalledebain.libris.util;

import java.util.HashSet;
import java.util.Random;

import org.lasalledebain.Utilities;

public class RandomFieldGenerator {
	int minWordLength, maxWordLength, minFieldLength, MaxFieldLength;
	Random rand;
	int keywordRatio;
	boolean addKeywords;
	public RandomFieldGenerator(int minWordLength, int maxWordLength, int minFieldLength, int maxFieldLength,
			Random rand, int keywordRatio) {
		super();
		this.minWordLength = minWordLength;
		this.maxWordLength = maxWordLength;
		this.minFieldLength = minFieldLength;
		MaxFieldLength = maxFieldLength;
		this.rand = rand;
		this.keywordRatio = keywordRatio;
		addKeywords = keywordRatio > 0;
	};

	public String makeFieldString(HashSet<String> keyWords) {
		int fieldSize = minFieldLength + rand.nextInt(MaxFieldLength - minFieldLength);
		StringBuilder b = new StringBuilder(fieldSize * ((minWordLength + maxWordLength)/2));
		while (fieldSize > 0) {
			final String randomWord = Utilities.makeRandomWord(rand, minWordLength, maxWordLength);
			if (addKeywords && 
					((randomWord.hashCode() % keywordRatio) == (keywordRatio - 1))) {
				keyWords.add(randomWord);
			}
			b.append(randomWord);
			--fieldSize;
			if (fieldSize > 0) {
				b.append(' ');
			}
		}
		return b.toString();
	}
}
