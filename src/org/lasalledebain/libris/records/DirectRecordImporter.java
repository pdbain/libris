package org.lasalledebain.libris.records;

import org.lasalledebain.libris.GenericDatabase;
import org.lasalledebain.libris.Record;
import org.lasalledebain.libris.exception.DatabaseException;
import org.lasalledebain.libris.exception.InputException;
import org.lasalledebain.libris.field.FieldValueStringList;

public class DirectRecordImporter<RecordType extends Record> extends RecordImporter<RecordType> {

	public DirectRecordImporter(GenericDatabase<RecordType> db) {
		super(db);
	}

	@Override
	public
	RecordType importRecord(FieldValueStringList[] fields) throws DatabaseException, InputException {
		RecordType rec = db.newRecord();
		short fieldNum = 0;
		for (FieldValueStringList values: fields) {
			for (String value: values) {
				rec.addFieldValue(fieldNum, value);
			}
			++fieldNum;
		}
		return rec;
	}

}
