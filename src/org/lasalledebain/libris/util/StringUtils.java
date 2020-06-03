package org.lasalledebain.libris.util;

import static java.util.Objects.nonNull;
import static java.util.Objects.isNull;
import static org.lasalledebain.libris.util.LibrisStemmer.stem;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Spliterator;
import java.util.function.Function;
import java.util.function.IntConsumer;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.lasalledebain.libris.LibrisConstants;

public class StringUtils {
	public static class StringHashSpliterator implements Spliterator.OfInt {
	
		private static final int FNV_PRIME = 16777619;
		public StringHashSpliterator(String word) {
			stringBytes = getBytes(word);
			hash = 0;
			long limit = Math.min(stringBytes.length, LibrisConstants.MINIMUM_TERM_LENGTH) - 1;
			for (index = 0; index < limit; ++index) {
				nextHash(stringBytes[index]);
			}
		}
	
		byte[] stringBytes;
		int hash;
		int index;
		@Override
		public long estimateSize() {
			return Math.max(0, stringBytes.length - LibrisConstants.MINIMUM_TERM_LENGTH);
		}
		
		private void nextHash(byte b) {
			hash = (hash ^ b) * FNV_PRIME;
		}
	
		@Override
		public int characteristics() {
			return IMMUTABLE;
		}
	
		@Override
		public java.util.Spliterator.OfInt trySplit() {
			return null;
		}
	
		@Override
		public boolean tryAdvance(IntConsumer action) {
			if (index >= stringBytes.length) { 
				return false;
			} else {
				nextHash(stringBytes[index++]);
				action.accept(hash);
				return true;
			}
		}
		
	}

	static class TermAndFrequency implements Comparable<TermAndFrequency> {
		String term;
		Float frequency;
		@Override
		/* sort in descending order */
		public int compareTo(TermAndFrequency o) {
			return o.frequency.compareTo(frequency);
		}
		public TermAndFrequency(String term, Float frequency) {
			this.term = term;
			this.frequency = frequency;
		}	
	}

	public static final Charset standardCharset = Charset.forName("ISO-8859-1");
	
	/**
	 * Converts a string to lower case and then to bytes using a fixed character set (ISO Latin-1).
	 * @param s String
	 * @return
	 */
	public static byte[] getBytes(String s) {
		return s.getBytes(standardCharset);
	}
	
	public static String stripSuffix(String fileName) {
		int suffixStart = fileName.lastIndexOf('.');
		if (suffixStart < 0) {
			return fileName;
		} else {
			return fileName.substring(0, suffixStart);
		}
	}
	
	public static String changeFileExtension(String original, String newExtension) {
		return stripSuffix(original)+"."+newExtension;
	}

	public static IntStream wordStreamToHashStream(final Stream<String> wordStream) {
		return wordStream.map(s->s.toLowerCase())
				.map(s -> StreamSupport.intStream(new StringHashSpliterator(s), false))
				.reduce(IntStream::concat)
				.orElseGet(IntStream::empty);
	}
	
	public static Stream<IntStream> wordStreamToHashStreams(final Stream<String> wordStream) {
		return wordStream.map(s->s.toLowerCase())
				.map(s -> StreamSupport.intStream(new StringHashSpliterator(s), false));
	}
	
	public static IntStream wordToHashStream(final String word) {
		return wordsToHashStream(Collections.singleton(word));
	}
	
	public static IntStream wordsToHashStream(final Collection<String> words) {
		return wordStreamToHashStream(words.stream());
	}

	public static Stream<String> getTerms(String docString, boolean stem) {
		final String[] words = docString.split("[\\p{Space}\\p{Punct}]+");
		final Stream<String> wordStream = Arrays.stream(words).map(StringUtils::deleteNonwordChars);
		if (stem) {
			return wordStream.map(w -> w.toLowerCase());
		} else {
			return wordStream;
		}
	}
	
	public static String deleteNonwordChars(String original) {
		return original.replaceAll("\\W+", "");
	}
	
	public static String replaceNonwordOrSpaceChars(String original, String replacement) {
		return original.replaceAll("[^\\p{IsLatin}\\p{Punct}]+", replacement);
	}
	
	public static String normalize(String term) {
		return term.toLowerCase();
	}

	public static Stream<String> chooseTerms(Stream<String> terms, Function<String, Integer> documentFrequency, int limit) {
		HashMap<String, Float> termCounts = new HashMap<>();
		terms.forEach(term -> termCounts.merge(term, (float) 1.0, (t,f) -> t + f));
		HashMap<String, ArrayList<TermAndFrequency>> representativeTerms = new HashMap<>();
		for (String t: termCounts.keySet()) {
			String stemmedTerm = stem(t);
			TermAndFrequency tAndF = new TermAndFrequency(t, termCounts.get(t));
			representativeTerms.merge(stemmedTerm, new ArrayList<TermAndFrequency>(Collections.singleton(tAndF)), (a, taf) -> {a.add(tAndF); return a;});
		}
		Stream<TermAndFrequency> representativeTermStream = representativeTerms.keySet().stream().map(stemmedTerm -> {
			ArrayList<TermAndFrequency> termList = representativeTerms.get(stemmedTerm);
			float total = 0;
			float max = 0;
			String maxTerm = null;
			for (TermAndFrequency taf: termList) {
				total += taf.frequency;
				if (taf.frequency > max) {
					max = taf.frequency;
					maxTerm = taf.term;
				}
			}
			Float docFrequency = documentFrequency.apply(stemmedTerm) + (float) 1.0;
			final TermAndFrequency newTaf = new TermAndFrequency(maxTerm, total/docFrequency);
			return newTaf;
		});
		return representativeTermStream.filter(tf -> tf.term.length() > 2).sorted().limit(limit).map(tf -> tf.term);
	}

	public static boolean isStringEmpty(String text) {
		return isNull(text) || text.isEmpty();
	}
}
