package org.lasalledebain.libris.hashfile;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.HashMap;

import org.lasalledebain.libris.exception.DatabaseException;

public class NumericKeyHashFile<EntryType extends NumericKeyHashEntry, 
BucketType extends NumericKeyHashBucket<EntryType>, FactoryType extends NumericKeyEntryFactory<EntryType>> 
extends HashFile<EntryType, BucketType,FactoryType> {

	HashMap<Integer, NumericKeyHashBucket<EntryType>> bucketCache;
	public NumericKeyHashFile(RandomAccessFile backingStore, 
			NumericKeyHashBucketFactory<EntryType, BucketType, FactoryType> bFact, FactoryType eFact)
			throws IOException {
		super(backingStore, bFact);
		this.entryFact = eFact;
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
