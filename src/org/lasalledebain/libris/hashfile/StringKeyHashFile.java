package org.lasalledebain.libris.hashfile;

import java.io.IOException;
import java.io.RandomAccessFile;

import org.lasalledebain.libris.exception.DatabaseException;
import org.lasalledebain.libris.util.ByteArraySlice;

public abstract class StringKeyHashFile<EntryType extends StringKeyHashEntry, 
BucketType extends StringKeyHashBucket<EntryType>, FactoryType extends EntryFactory<EntryType>> 
extends HashFile<EntryType, BucketType, FactoryType> {

	public StringKeyHashFile(RandomAccessFile backingStore, HashBucketFactory<EntryType, BucketType, FactoryType> bFact)
			throws IOException {
		super(backingStore, bFact);
	}

	@Override
	protected int findHomeBucket(long key) {
		final int bucketNum = hashToBucketNumber(key);
		return bucketNum;
	}

	protected int findHomeBucket(ByteArraySlice key) {
		return findHomeBucket(key.hashCode());
	}

	protected int findHomeBucket(String key) {
		return findHomeBucket(new ByteArraySlice(key));
	}

	public EntryType getEntry(String key) throws DatabaseException, IOException {
		ByteArraySlice keyBytes = new ByteArraySlice(key);
		int bucketNum = findHomeBucket(keyBytes);
		BucketType homeBucket = getBucket(bucketNum);
		EntryType foundEntry = homeBucket.getEntry(keyBytes);
		if (null != foundEntry) {
			return foundEntry;
		} else {
			BucketType overflowBucket = getBucket(numBuckets-1);
			foundEntry = overflowBucket.getEntry(key);
			if (null != foundEntry) {
				return foundEntry;
			}
		}
		return null;
	}

	@Override
	protected int findHomeBucket(EntryType entry) {
		return findHomeBucket(entry.keyBytes);
	}
}
