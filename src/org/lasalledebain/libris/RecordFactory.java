package org.lasalledebain.libris;

public interface RecordFactory<RecordType extends Record> {

	RecordType makeRecord(boolean editable);

	RecordType makeRecordUnchecked(boolean editable);

}