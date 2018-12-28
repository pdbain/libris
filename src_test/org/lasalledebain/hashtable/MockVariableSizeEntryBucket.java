package org.lasalledebain.hashtable;

import java.io.RandomAccessFile;
import java.nio.ByteBuffer;

import org.lasalledebain.libris.hashfile.EntryFactory;
import org.lasalledebain.libris.hashfile.VariableSizeEntryHashBucket;
import org.lasalledebain.libris.hashfile.VariableSizeHashEntry;
import org.lasalledebain.libris.indexes.BucketOverflowFileManager;

public class MockVariableSizeEntryBucket extends VariableSizeEntryHashBucket<VariableSizeHashEntry> {

	@Override
	protected VariableSizeHashEntry makeEntry(int entryId, byte[] dat) {
		return new MockVariableSizeHashEntry(entryId, dat);
	}

	@Override
	protected VariableSizeHashEntry makeEntry(int entryId, ByteBuffer dat, int length) {
		return new MockVariableSizeHashEntry(entryId, dat, length);
	}

	public MockVariableSizeEntryBucket(RandomAccessFile backingStore, int bucketNum,
			BucketOverflowFileManager overflowManager, EntryFactory<MockVariableSizeHashEntry> eFact) {
		super(backingStore, bucketNum, overflowManager);
	}

}
