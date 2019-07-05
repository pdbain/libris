package org.lasalledebain.libris.records;

import org.lasalledebain.libris.GenericDatabase;
import org.lasalledebain.libris.Record;
import org.lasalledebain.libris.exception.DatabaseException;
import org.lasalledebain.libris.exception.InputException;
import org.lasalledebain.libris.field.FieldValueStringList;

public class DirectRecordImporter extends RecordImporter {

	public DirectRecordImporter(GenericDatabase db) {
		super(db);
	}

	@Override
	public
	Record importRecord(FieldValueStringList[] fields) throws DatabaseException, InputException {
		Record rec = db.newRecord();
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
