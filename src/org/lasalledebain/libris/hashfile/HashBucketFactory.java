package org.lasalledebain.libris.hashfile;

import java.io.RandomAccessFile;

public interface HashBucketFactory<EntryType extends HashEntry, BucketType extends HashBucket<EntryType>, FactoryType extends EntryFactory<EntryType>>  {
	BucketType  createBucket(RandomAccessFile backingStore, int bucketNum, FactoryType fact);

}
