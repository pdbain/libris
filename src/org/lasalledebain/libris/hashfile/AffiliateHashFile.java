package org.lasalledebain.libris.hashfile;

import java.io.IOException;
import java.io.RandomAccessFile;

import org.lasalledebain.libris.index.AffiliateListEntry;
import org.lasalledebain.libris.indexes.BucketOverflowFileManager;

public class AffiliateHashFile extends VariableSizeEntryNumericKeyHashFile<AffiliateListEntry> {

	public AffiliateHashFile(RandomAccessFile backingStore, BucketOverflowFileManager overflowMgr) throws IOException {
		super(backingStore, overflowMgr);
	}

	@Override
	protected NumericKeyHashBucket<AffiliateListEntry> createBucket(int bucketNum) {
		return new AffiliateHashBucket(backingStore, bucketNum, overflowMgr);
	}

}
