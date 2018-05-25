package org.lasalledebain.libris.index;

import java.io.DataInput;
import java.io.IOException;
import java.nio.ByteBuffer;

import org.lasalledebain.libris.hashfile.FixedSizeEntryFactory;

public class RecordPositionEntryFactory implements FixedSizeEntryFactory<RecordPositionEntry>{


	public RecordPositionEntry makeEntry(int recordId, long position) {
		return new RecordPositionEntry(recordId, position);
	}

	@Override
	public int getEntrySize() {
		return RecordPositionEntry.getRecordPositionEntryLength();
	}

	@Override
	public RecordPositionEntry makeEntry(int key, byte[] dat) {
		return makeEntry(key, ByteBuffer.wrap(dat), dat.length);
	}

	@Override
	public RecordPositionEntry makeEntry(int key, ByteBuffer src, int len) {
		return new RecordPositionEntry(key, src.getLong(0));

	}

	@Override
	public RecordPositionEntry makeEntry(DataInput src) throws IOException {
		int key = src.readInt();
		long position = src.readLong();
		return new RecordPositionEntry(key, position);
	}

}
