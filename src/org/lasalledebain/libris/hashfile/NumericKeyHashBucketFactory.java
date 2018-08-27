package org.lasalledebain.libris.hashfile;

import java.io.RandomAccessFile;

public interface NumericKeyHashBucketFactory<EntryType extends NumericKeyHashEntry, 
BucketType extends NumericKeyHashBucket<EntryType>, FactoryType extends NumericKeyEntryFactory<EntryType>> 
extends HashBucketFactory<EntryType, BucketType, FactoryType> {
	BucketType  createBucket(RandomAccessFile backingStore, int bucketNum, NumericKeyEntryFactory<EntryType> fact);
}
