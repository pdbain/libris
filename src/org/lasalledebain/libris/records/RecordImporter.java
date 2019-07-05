package org.lasalledebain.libris.records;

import org.lasalledebain.libris.GenericDatabase;
import org.lasalledebain.libris.Record;
import org.lasalledebain.libris.exception.LibrisException;
import org.lasalledebain.libris.field.FieldValueStringList;

public abstract class RecordImporter<RecordType extends Record> {
	GenericDatabase<RecordType> db;

	public RecordImporter(GenericDatabase<RecordType> db) {
		this.db = db;
	}
	
	public abstract Record importRecord(FieldValueStringList[] fields) throws LibrisException;
}
