package org.lasalledebain.libris.hashfile;

import java.io.RandomAccessFile;

@SuppressWarnings("unchecked")
public interface HashBucketFactory<T extends HashEntry<HashEntry>> {
	@SuppressWarnings("unchecked")
	HashBucket<T>  createBucket(RandomAccessFile backingStore,
			int bucketNum, EntryFactory fact);
}
