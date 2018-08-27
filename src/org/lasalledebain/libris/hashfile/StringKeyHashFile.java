package org.lasalledebain.libris.hashfile;

import java.io.IOException;
import java.io.RandomAccessFile;

public abstract class StringKeyHashFile<EntryType extends StringKeyHashEntry, 
BucketType extends StringKeyHashBucket<EntryType>, FactoryType extends EntryFactory<EntryType>> 
extends HashFile<EntryType, BucketType, FactoryType> {

	public StringKeyHashFile(RandomAccessFile backingStore, HashBucketFactory<EntryType, BucketType, FactoryType> bFact)
			throws IOException {
		super(backingStore, bFact);
	}

}
