package org.lasalledebain.libris.exception;

import java.util.Objects;

import org.lasalledebain.libris.ui.LibrisUi;

public class Assertion {

	public static boolean assertTrue(LibrisUi ui, String message, boolean test) {
		if (!test) {
			ui.alert("Error: "+message);
		}
		return test;
	}

	public static boolean assertEquals(LibrisUi ui, String message, Object expected, Object actual) {
		boolean result = actual.equals(expected);
		if (!result) {
			ui.alert("Error: "+message+" expected: "+expected.toString()+" actual: "+actual.toString());
		}
		return result;
	}

	public static boolean assertNotNull(LibrisUi ui, String message, Object expected) {
		boolean result = Objects.nonNull(expected);
		if (!result) {
			ui.alert("Error: "+message+" object is null");
		}
		return result;
	}

}
