package org.lasalledebain.libris.util;

import java.nio.charset.Charset;
import java.util.Objects;

public class StringUtils {
	public static final Charset standardCharset = Charset.forName("ISO-8859-1");
	
	/**
	 * Converts a string to lower case and then to bytes using a fixed character set (ISO Latin-1).
	 * @param s String
	 * @return
	 */
	public static byte[] toCanonicalBytes(String s) {
		return s.toLowerCase().getBytes(standardCharset);
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
	
	public static boolean stringEquals(String a, String b) {
		if (Objects.isNull(a) && Objects.isNull(b)) {
			return true;
		} else 	if (Objects.nonNull(a) && Objects.nonNull(b)) {
			return a.equals(b);
		} else return false;
	}
}
