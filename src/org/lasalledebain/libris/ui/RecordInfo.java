/**
 * 
 */
package org.lasalledebain.libris.ui;

import org.lasalledebain.libris.Record;
import org.lasalledebain.libris.exception.FieldDataException;

class RecordInfo<RecordType extends Record> {
	private final RecordType rec;
	private final int recId;
	private final String recValue;
	/**
	 * @param rec
	 * @param fieldIds 
	 * @throws FieldDataException 
	 */
	public RecordInfo(RecordType rec, String[] fieldIds) {
		this.rec = rec;
		recId = rec.getRecordId();
		recValue = rec.generateTitle(fieldIds);
	}
	@Override
	public String toString() {
		return recValue;
	}
	
	public RecordType getRecord() {
		return rec;
	}

	public int getRecordId() {
		return recId;
	}
}