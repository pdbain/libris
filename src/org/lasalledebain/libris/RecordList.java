package org.lasalledebain.libris;

import java.util.Iterator;

import org.lasalledebain.libris.exception.InputException;

public abstract class RecordList<RecordType extends Record> implements Iterable<RecordType> { // TODO RecordList implement ListModel

	@Override
	public abstract Iterator<RecordType> iterator();

	public abstract RecordType getRecord(int id) throws InputException;

}
