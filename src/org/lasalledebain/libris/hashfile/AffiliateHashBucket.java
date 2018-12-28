package org.lasalledebain.libris.hashfile;

import java.io.RandomAccessFile;

import org.lasalledebain.libris.index.AffiliateListEntry;
import org.lasalledebain.libris.indexes.BucketOverflowFileManager;

// TODO write and use AffiliateHashBucket
public class AffiliateHashBucket extends VariableSizeEntryHashBucket<AffiliateListEntry> {

	public AffiliateHashBucket(RandomAccessFile backingStore, int bucketNum, BucketOverflowFileManager overflowMgr,
			EntryFactory<AffiliateListEntry> eFact) {
		super(backingStore, bucketNum, overflowMgr, eFact);
		// TODO Auto-generated constructor stub
	}

}
