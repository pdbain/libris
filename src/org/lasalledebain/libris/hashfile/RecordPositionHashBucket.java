package org.lasalledebain.libris.hashfile;

import java.io.DataInput;
import java.io.IOException;
import java.io.RandomAccessFile;

import org.lasalledebain.libris.index.RecordPositionEntry;

public class RecordPositionHashBucket extends FixedSizeEntryHashBucket<RecordPositionEntry> {

	public RecordPositionHashBucket(RandomAccessFile backingStore, int bucketNum) {
		super(backingStore, bucketNum);
	}

	@Override
	public RecordPositionEntry makeEntry(DataInput src) throws IOException {
		int key = src.readInt();
		long position = src.readLong();
		return new RecordPositionEntry(key, position);
	}
	
	public static int entriesPerBucket() {
		int entrySize = RecordPositionEntry.getRecordPositionEntryLength();
		return getBucketSize()/entrySize;
	}

}
