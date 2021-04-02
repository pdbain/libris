package org.lasalledebain.libris;

import java.util.Iterator;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.lasalledebain.libris.exception.InputException;

public abstract class RecordList<RecordType extends Record> implements Iterable<RecordType> { // TODO RecordList implement ListModel

	@Override
	public abstract Iterator<RecordType> iterator();

	public abstract RecordType getRecord(int id) throws InputException;

	public RecordType getFirstRecord() throws InputException {
		return iterator().next();
	}

	public abstract int size();

	public Stream<RecordType> asStream() {
		return StreamSupport.stream(spliterator(), false);
	}
	}
