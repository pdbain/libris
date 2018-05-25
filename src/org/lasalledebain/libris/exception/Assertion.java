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

	public static void assertTrueInputException(String message, boolean test) throws InputException {
		if (!test) {
			throw new InputException("Error: "+message);
		}
	}

	public static void assertEqualsInputException(String message, Object expected, Object actual) throws InputException {
		boolean result = actual.equals(expected);
		if (!result) {
			throw new InputException("Error: "+message+" expected: "+expected.toString()+" actual: "+actual.toString());
		}
	}

	public static void assertNotNullInputException(String message, Object actual) throws InputException {
		boolean result = Objects.nonNull(actual);
		if (!result) {
			throw new InputException("Error: "+message+" is null");
		}
	}

	public static boolean assertNotNull(LibrisUi ui, String message, Object expected) {
		boolean result = Objects.nonNull(expected);
		if (!result) {
			ui.alert("Error: "+message+" object is null");
		}
		return result;
	}

}
