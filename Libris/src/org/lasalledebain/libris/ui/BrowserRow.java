/**
 * 
 */
package org.lasalledebain.libris.ui;

import org.lasalledebain.libris.Record;
import org.lasalledebain.libris.RecordId;
import org.lasalledebain.libris.exception.FieldDataException;

class BrowserRow {
	Record rec;
	RecordId recId;
	private String recValue;
	/**
	 * @param rec
	 * @param fieldIds 
	 * @throws FieldDataException 
	 */
	public BrowserRow(Record rec, String[] fieldIds) {
		this.rec = rec;
		recId = rec.getRecordId();
		recValue = rec.generateTitle(fieldIds);
	}
	@Override
	public String toString() {
		return recValue;
	}
	public RecordId getRecordId() {
		return recId;
	}
}