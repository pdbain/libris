package org.lasalledebain.libris;

import org.lasalledebain.libris.exception.FieldDataException;

public interface RecordFactory<RecordType extends Record> {

	RecordType makeRecord(boolean editable);

}