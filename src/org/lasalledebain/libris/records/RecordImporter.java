package org.lasalledebain.libris.records;

import org.lasalledebain.libris.LibrisDatabase;
import org.lasalledebain.libris.Record;
import org.lasalledebain.libris.exception.LibrisException;
import org.lasalledebain.libris.field.FieldValueStringList;

public abstract class RecordImporter {
	LibrisDatabase db;

	public RecordImporter(LibrisDatabase db) {
		this.db = db;
	}
	
	public abstract Record importRecord(FieldValueStringList[] fields) throws LibrisException;
}
