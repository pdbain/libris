package org.lasalledebain.libris.exception;

import java.util.Objects;

import org.lasalledebain.libris.ui.DatabaseUi;

public class Assertion {

	/**
	 * @param ui database UI
	 * @param message message for alert
	 * @param test condition to test
	 * @return result of test
	 */
	
	private static void displayMessage(DatabaseUi<?> ui, String message) {
		if (null == ui)  System.err.println(message);
		else ui.alert(message);
	}
	
	public static boolean assertTrue(DatabaseUi<?> ui, String message, boolean test) {
		if (!test) {
			displayMessage(ui, "Error: "+message);
		}
		return test;
	}

	public static boolean assertTrueError(String message, boolean test) {
		if (!test) {
			throw new DatabaseError("Error: "+message);
		}
		return test;
	}

	public static void assertError(String message) {
		throw new DatabaseError("Error: "+message);
	}

	public static boolean assertEquals(DatabaseUi<?> ui, String message, Object expected, Object actual) {
		boolean result = actual.equals(expected);
		if (!result) {
			displayMessage(ui, "Error: "+message+" expected: "+expected.toString()+" actual: "+actual.toString());
		}
		return result;
	}

	public static void assertTrueInputException(String message, boolean test) throws InputException {
		if (!test) {
			throw new InputException("Error: "+message);
		}
	}

	public static void assertTrueInputException(String message1, String message2, boolean test) throws InputException {
		if (!test) {
			throw new InputException("Error: "+message1+message2);
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

	public static void assertNotNullInputException(String message1, String message2, Object actual) throws InputException {
		boolean result = Objects.nonNull(actual);
		if (!result) {
			throw new InputException("Error: "+message1+" "+message2+" is null");
		}
	}

	public static void assertNotNullDatabaseException(String message1, String message2, Object actual) throws DatabaseException {
		if (!Objects.nonNull(actual)) {
			throw new DatabaseException("Error: "+message1+" "+message2+" is null");
		}
	}

	public static void assertNotNullError(String message, Object actual) throws InputException {
		boolean result = Objects.nonNull(actual);
		if (!result) {
			throw new DatabaseError("Error: "+message+" is null");
		}
	}
	
	public static void assertNullError(String message, Object actual) throws InputException {
		boolean result = Objects.isNull(actual);
		if (!result) {
			throw new DatabaseError("Error: "+message+" is not null");
		}
	}

	public static void assertNotNullError(String message1, String message2, Object actual) throws DatabaseError {
		boolean result = Objects.nonNull(actual);
		if (!result) {
			throw new DatabaseError("Error: "+message1+" "+message2+" is null");
		}
	}

	public static boolean assertNotNull(DatabaseUi<?> ui, String message, Object expected) {
		boolean result = Objects.nonNull(expected);
		if (!result) {
			displayMessage(ui, "Error: "+message+" object is null");
		}
		return result;
	}

	public static void assertTrueError(String messagePart1, String messagePart2, boolean test) {
		if (!test) {
			throw new DatabaseError("Error: "+messagePart1+messagePart2);
		}
	}

	public static void assertError(String messagePart1, String messagePart2) {
		throw new DatabaseError("Error: "+messagePart1+messagePart2);
	}

}
