package org.lasalledebain.libris;

import org.lasalledebain.libris.exception.InputException;

public interface RecordIdNameMapper {
	int getId(String recName) throws InputException;
	String getName(int recId) throws InputException;
}
