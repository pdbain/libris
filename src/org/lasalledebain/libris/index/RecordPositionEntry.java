package org.lasalledebain.libris.index;

import java.io.DataOutput;
import java.io.IOException;

import org.lasalledebain.libris.hashfile.NumericKeyHashEntry;

/**
 * Entry comprises 2 bytes:
 * 4 bytes for record ID
 * 8 bytes for record position
 *
 */
public class RecordPositionEntry extends AbstractFixedSizeHashEntry {

	final protected long recordPosition;
	public int getRecordId() {
		return key;
	}

	public long getRecordPosition() {
		return recordPosition;
	}

	/**
	 * @param recordId
	 * @param position
	 */
	public RecordPositionEntry(int recordId, long position) {
		super(recordId);
		recordPosition = position;
	}

	@Override
	public int getTotalLength() {
		return getRecordPositionEntryLength();
	}

	@Override
	public int getEntryLength() {
		return getTotalLength();
	}

	@Override
	public int getDataLength() {
		return getTotalLength();
	}

	@Override
	public int getOverheadLength() {
		return 0;
	}

	public static int getRecordPositionEntryLength() {
		return 12;
	}

	@Override
	public void writeData(DataOutput backingStore) throws IOException {
		backingStore.writeInt(key);
		backingStore.writeLong(recordPosition);
	}

	@Override
	public boolean isOversize() {
		return oversize;
	}

	@Override
	public void setOversize(boolean oversize) {
		this.oversize = oversize;
	}

	@Override
	public int compareTo(NumericKeyHashEntry comparand) {
		if (this == comparand) {
			return 0;
		} else if (this.getClass().isInstance(comparand)) {
			return Integer.compare(((AbstractNumericKeyHashEntry)comparand).key, key);
		} else {
			return -1;
		}
	}
}
