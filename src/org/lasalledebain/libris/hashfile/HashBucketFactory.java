package org.lasalledebain.libris.hashfile;

import java.io.RandomAccessFile;

public interface HashBucketFactory<EntryType extends HashEntry, BucketType extends HashBucket<EntryType>> {
	BucketType  createBucket(RandomAccessFile backingStore, int bucketNum, EntryFactory<EntryType> fact);
}
