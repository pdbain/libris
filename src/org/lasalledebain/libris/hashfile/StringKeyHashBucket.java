package org.lasalledebain.libris.hashfile;

import java.io.RandomAccessFile;

public abstract class StringKeyHashBucket<EntryType extends StringKeyHashEntry> extends HashBucket<EntryType> {

	public StringKeyHashBucket(RandomAccessFile backingStore, int bucketNum) {
		super(backingStore, bucketNum);
	}
	
	public abstract EntryType get(String key);
}
