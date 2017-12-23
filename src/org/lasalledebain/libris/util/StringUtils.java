package org.lasalledebain.libris.util;

import java.nio.charset.Charset;

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
}
