package org.lasalledebain.libris.indexes;

import java.io.DataInput;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;

import org.lasalledebain.hashtable.MockFixedSizeHashEntry;
import org.lasalledebain.libris.hashfile.FixedSizeEntryHashBucket;
import org.lasalledebain.libris.hashfile.FixedSizeHashEntry;

public class MockFixedSizeEntryBucket2 extends FixedSizeEntryHashBucket<FixedSizeHashEntry> {

	final int len;
	public MockFixedSizeEntryBucket2(RandomAccessFile backingStore, int bucketNum) {
		super(backingStore, bucketNum);
		len = 42;
	}

	public MockFixedSizeEntryBucket2(RandomAccessFile backingStore, int bucketNum, int length) {
		super(backingStore, bucketNum);
		len = length;
	}

	public MockFixedSizeHashEntry makeEntry(int key, byte[] dat) {
		return new MockFixedSizeHashEntry(key, dat);
	}


	public MockFixedSizeHashEntry makeEntry(int key, ByteBuffer src, int len) {
		return new MockFixedSizeHashEntry(key, src, len);
	}
	
	public MockFixedSizeHashEntry makeEntry(DataInput backingStore) throws IOException {
		return new MockFixedSizeHashEntry(backingStore, len);
	}
}
