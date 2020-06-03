package org.lasalledebain.libris.hashfile;

import java.io.IOException;
import java.io.RandomAccessFile;

import org.lasalledebain.libris.exception.DatabaseException;
import org.lasalledebain.libris.index.RecordPositionEntry;

public class RecordPositionHashFile extends NumericKeyHashFile<RecordPositionEntry, RecordPositionHashBucket> {

	public RecordPositionHashFile(RandomAccessFile backingStore) throws IOException {
		super(backingStore);
	}

	@Override
	protected RecordPositionHashBucket createBucket(int bucketNum) {
		return new RecordPositionHashBucket(backingStore, bucketNum);
	}

	public void addEntry(int recordIdNum, long recordPosition) throws DatabaseException, IOException {
		addEntry(new RecordPositionEntry(recordIdNum, recordPosition));
		
	}

}
