package org.lasalledebain.libris.indexes;

import java.io.IOException;
import java.io.RandomAccessFile;

import org.lasalledebain.hashtable.MockVariableSizeEntryBucket;
import org.lasalledebain.libris.hashfile.NumericKeyHashBucket;
import org.lasalledebain.libris.hashfile.VariableSizeEntryNumericKeyHashFile;
import org.lasalledebain.libris.hashfile.VariableSizeHashEntry;

public class MockVariableSizeEntryNumericKeyHashFile extends VariableSizeEntryNumericKeyHashFile<VariableSizeHashEntry> {

	public MockVariableSizeEntryNumericKeyHashFile(RandomAccessFile backingStore, BucketOverflowFileManager overflowManager) throws IOException {
		super(backingStore, overflowManager);
	}

	@Override
	protected NumericKeyHashBucket<VariableSizeHashEntry> createBucket(int bucketNum) {
		return new MockVariableSizeEntryBucket(backingStore, bucketNum, overflowMgr);
	}

}
