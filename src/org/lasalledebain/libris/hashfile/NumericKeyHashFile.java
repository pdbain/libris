package org.lasalledebain.libris.hashfile;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.HashMap;

import org.lasalledebain.libris.exception.DatabaseException;
// TODO make consecutive records hash to the same bucket
public abstract class NumericKeyHashFile<EntryType extends NumericKeyHashEntry, 
BucketType extends NumericKeyHashBucket<EntryType>> 
extends HashFile<EntryType, BucketType> {

	HashMap<Integer, NumericKeyHashBucket<EntryType>> bucketCache;

	public NumericKeyHashFile(RandomAccessFile backingStore)
			throws IOException {
		super(backingStore);
		bucketCache = new HashMap<Integer, NumericKeyHashBucket<EntryType>>();
	}
	
	public EntryType getEntry(int key) throws IOException, DatabaseException {
		int bucketNum = findHomeBucket(key);
		BucketType homeBucket = getBucket(bucketNum);
		EntryType foundEntry = homeBucket.getEntry(key);
		if (null != foundEntry) {
			return foundEntry;
		} else {
			NumericKeyHashBucket<EntryType> overflowBucket = getBucket(numBuckets-1);
			foundEntry = overflowBucket.getEntry(key);
			if (null != foundEntry) {
				return foundEntry;
			}
		}
		return null;
	}

	@Override
	protected int findHomeBucket(EntryType entry) {
		return findHomeBucket(entry.getKey());
	}

	protected int findHomeBucket(long key) {
		int hashedKey = hash(key);
		int homeBucket = hashToBucketNumber(hashedKey);
		return homeBucket;
	}


}
