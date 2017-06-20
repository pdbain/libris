package org.lasalledebain.libris.index;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.nio.ByteBuffer;

import org.lasalledebain.libris.exception.InternalError;
import org.lasalledebain.libris.hashfile.FixedSizeHashEntry;
import org.lasalledebain.libris.hashfile.HashEntry;

/**
 * Entry comprises 2 bytes:
 * 4 bytes for record ID
 * 8 bytes for record position
 *
 */
public class RecordPositionEntry extends AbstractFixedSizeHashEntry {

	public int getRecordId() {
		return key;
	}

	public void setRecordId(int recordId) {
		key = recordId;
	}

	public long getRecordPosition() {
		return recordPosition;
	}

	public void setRecordPosition(long recordPosition) {
		this.recordPosition = recordPosition;
	}

	long recordPosition;
	private boolean oversize;
	/**
	 * @param recordId
	 * @param position
	 */
	public RecordPositionEntry(int recordId, long position) {
		key = recordId;
		recordPosition = position;
	}

	public RecordPositionEntry() {
		return;
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
	public void readData(DataInput backingStore) throws IOException {
		key = backingStore.readInt();
		recordPosition = backingStore.readLong();
	}

	@Override
	public void readData(ByteBuffer buff, int length) {
		key = buff.getInt();
		recordPosition = buff.getLong();
	}

	@Override
	public void writeData(DataOutput backingStore) throws IOException {
		backingStore.writeInt(key);
		backingStore.writeLong(recordPosition);
	}

	@Override
	public void setData(byte[] dat) {
		if (dat.length != getRecordPositionEntryLength()) {
			throw new InternalError("wrong length "+dat.length);
		}
	}

	@Override
	public boolean isOversize() {
		return oversize;
	}

	@Override
	public void setOversize(boolean oversize) {
		this.oversize = oversize;
	}
}
