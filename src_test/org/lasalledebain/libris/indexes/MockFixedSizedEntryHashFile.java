package org.lasalledebain.libris.indexes;

import java.io.IOException;
import java.io.RandomAccessFile;

import org.lasalledebain.libris.hashfile.NumericKeyHashBucket;
import org.lasalledebain.libris.hashfile.NumericKeyHashEntry;
import org.lasalledebain.libris.hashfile.NumericKeyHashFile;

public class MockFixedSizedEntryHashFile extends NumericKeyHashFile<NumericKeyHashEntry, 
NumericKeyHashBucket<NumericKeyHashEntry>> {

	public MockFixedSizedEntryHashFile(RandomAccessFile backingStore) throws IOException {
		super(backingStore);
	}

	@Override
	protected NumericKeyHashBucket createBucket(int bucketNum) {
		return new MockFixedSizeEntryBucket(backingStore, bucketNum);
	}
}
