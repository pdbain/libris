package org.lasalledebain.libris.hashfile;

import java.io.RandomAccessFile;

public interface StringKeyHashBucketFactory
<EntryType extends StringKeyHashEntry,  BucketType extends StringKeyHashBucket<EntryType>, FactoryType extends StringKeyEntryFactory<EntryType>> 
extends HashBucketFactory<EntryType, StringKeyHashBucket<EntryType>, FactoryType> {
	BucketType  createBucket(RandomAccessFile backingStore, int bucketNum, FactoryType fact);
}
