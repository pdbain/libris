package org.lasalledebain.libris;

import java.util.Iterator;

import org.lasalledebain.libris.exception.InputException;

public abstract class RecordList<RecordType extends Record> implements Iterable<Record> { // TODO RecordList implement ListModel

	@Override
	public abstract Iterator<Record> iterator();

	public abstract Record getRecord(int id) throws InputException;

}
