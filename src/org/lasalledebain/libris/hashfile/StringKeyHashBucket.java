package org.lasalledebain.libris.hashfile;

import java.io.RandomAccessFile;

import org.lasalledebain.libris.util.ByteArraySlice;

public abstract class StringKeyHashBucket<EntryType extends StringKeyHashEntry> extends HashBucket<EntryType> {

	public StringKeyHashBucket(RandomAccessFile backingStore, int bucketNum) {
		super(backingStore, bucketNum);
	}
	
	public abstract EntryType getEntry(String key);
	public abstract EntryType getEntry(ByteArraySlice keyBytes);
}
