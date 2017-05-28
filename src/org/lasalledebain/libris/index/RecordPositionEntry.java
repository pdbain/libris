package org.lasalledebain.libris.index;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.nio.ByteBuffer;

import org.lasalledebain.libris.exception.InternalError;
import org.lasalledebain.libris.hashfile.HashEntry;

@SuppressWarnings("unchecked")
public class RecordPositionEntry implements HashEntry {

	public int getRecordId() {
		return recordId;
	}

	public void setRecordId(int recordId) {
		this.recordId = recordId;
	}

	public long getRecordPosition() {
		return recordPosition;
	}

	public void setRecordPosition(long recordPosition) {
		this.recordPosition = recordPosition;
	}

	int recordId;
	long recordPosition;
	private boolean oversize;
	/**
	 * @param recordId
	 * @param position
	 */
	public RecordPositionEntry(int recordId, long position) {
		this.recordId = recordId;
		recordPosition = position;
	}

	public RecordPositionEntry() {
		return;
	}

	@Override
	public void setKey(int newKey) {
		recordId = newKey;
	}

	@Override
	public int getKey() {
		return recordId;
	}

	@Override
	public Integer getIntegerKey() {
		return new Integer(recordId);
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
		recordId = backingStore.readInt();
		recordPosition = backingStore.readLong();
	}

	@Override
	public void readData(ByteBuffer buff, int length) {
		recordId = buff.getInt();
		recordPosition = buff.getLong();
	}

	@Override
	public void writeData(DataOutput backingStore) throws IOException {
		backingStore.writeInt(recordId);
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
