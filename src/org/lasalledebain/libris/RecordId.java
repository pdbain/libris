package org.lasalledebain.libris;

import org.lasalledebain.libris.exception.DatabaseException;

/**
 * Record ID
  */
public class RecordId {

	public static int toId(String idString) throws DatabaseException {
		int result;
		try {
			result = Integer.parseInt(idString);
		} catch (NumberFormatException e) {
			throw new DatabaseException("malformed record ID: "+idString, e);
		}
		return result;
	}

	public static int getNullId() {
		return 0;
	}

	public static String toString(int id) {
		return Integer.toString(id);
	}

	public static boolean isNull(int id) {
		return 0 == id;
	}
	public static boolean isNull(Record rec) {
		return 0 == rec.id;
	}
}
