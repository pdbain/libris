package org.lasalledebain.libris.indexes;

import java.io.IOException;
import java.io.RandomAccessFile;

import org.lasalledebain.libris.hashfile.NumericKeyHashFile;

public class MockFixedSizedEntryHashFile extends NumericKeyHashFile {

	public MockFixedSizedEntryHashFile(RandomAccessFile backingStore) throws IOException {
		super(backingStore);
	}

	@Override
	protected MockFixedSizeEntryBucket createBucket(int bucketNum) {
		return new MockFixedSizeEntryBucket(backingStore, bucketNum);
	}
}
