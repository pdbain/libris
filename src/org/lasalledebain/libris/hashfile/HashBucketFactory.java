package org.lasalledebain.libris.hashfile;

import java.io.RandomAccessFile;

public interface HashBucketFactory<T extends HashEntry> {
	HashBucket<T>  createBucket(RandomAccessFile backingStore,
			int bucketNum, EntryFactory<T> fact);
}
