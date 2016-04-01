package org.lasalledebain.libris;

import java.util.Iterator;

import org.lasalledebain.libris.exception.LibrisException;

public abstract class RecordList implements Iterable<Record> { // todo RecordList implement ListModel

	protected LibrisDatabase database;

	public RecordList(LibrisDatabase database) {
		this.database = database;
	}

	public abstract Record getRecord(RecordId id) throws LibrisException;

	@Override
	public abstract Iterator<Record> iterator();

}
