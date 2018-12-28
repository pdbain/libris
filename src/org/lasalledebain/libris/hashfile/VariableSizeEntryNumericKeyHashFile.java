package org.lasalledebain.libris.hashfile;

import java.io.IOException;
import java.io.RandomAccessFile;

import org.lasalledebain.libris.indexes.BucketOverflowFileManager;

public abstract class VariableSizeEntryNumericKeyHashFile<EntryType extends VariableSizeHashEntry> 
extends NumericKeyHashFile<EntryType, NumericKeyHashBucket<EntryType>> {

	protected final BucketOverflowFileManager overflowMgr;

	public VariableSizeEntryNumericKeyHashFile(RandomAccessFile backingStore,
			BucketOverflowFileManager overflowMgr) throws IOException {
		super(backingStore);
		this.overflowMgr = overflowMgr;
	}
}
