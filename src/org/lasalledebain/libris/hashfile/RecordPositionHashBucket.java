package org.lasalledebain.libris.hashfile;

import java.io.RandomAccessFile;

import org.lasalledebain.libris.index.RecordPositionEntry;

public class RecordPositionHashBucket extends FixedSizeEntryHashBucket<RecordPositionEntry> {

	public RecordPositionHashBucket(RandomAccessFile backingStore, int bucketNum) {
		super(backingStore, bucketNum);
	}

}
