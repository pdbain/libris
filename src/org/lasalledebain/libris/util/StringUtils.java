package org.lasalledebain.libris.util;

import java.nio.charset.Charset;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.Spliterator;
import java.util.Spliterator.OfInt;
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
}
