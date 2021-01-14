package org.lasalledebain.libris.indexes;

import java.io.IOException;
import java.io.RandomAccessFile;

import org.lasalledebain.hashtable.MockVariableSizeEntryBucket;
import org.lasalledebain.libris.hashfile.HashBucket;
import org.lasalledebain.libris.hashfile.VariableSizeEntryNumericKeyHashFile;
import org.lasalledebain.libris.hashfile.VariableSizeHashEntry;

public class MockVariableSizeEntryNumericKeyHashFile extends VariableSizeEntryNumericKeyHashFile {

	public MockVariableSizeEntryNumericKeyHashFile(RandomAccessFile backingStore, BucketOverflowFileManager overflowManager) throws IOException {
		super(backingStore, overflowManager);
	}

	@Override
	protected HashBucket<VariableSizeHashEntry> createBucket(int bucketNum) {
		return new MockVariableSizeEntryBucket(backingStore, bucketNum, overflowMgr);
	}

}
