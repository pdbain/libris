package org.lasalledebain.libris.hashfile;

import java.io.RandomAccessFile;

import org.lasalledebain.libris.exception.DatabaseException;
import org.lasalledebain.libris.index.AffiliateListEntry;
import org.lasalledebain.libris.indexes.BucketOverflowFileManager;

// TODO write and use AffiliateHashBucket
public class AffiliateHashBucket extends VariableSizeEntryHashBucket<AffiliateListEntry> {

	public AffiliateHashBucket(RandomAccessFile backingStore, int bucketNum, BucketOverflowFileManager overflowManager,
			EntryFactory<AffiliateListEntry> eFact) {
		super(backingStore, bucketNum, overflowManager, eFact);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void addToBucket(int key, AffiliateListEntry newEntry) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean addEntry(AffiliateListEntry newEntry) throws DatabaseException {
		// TODO Auto-generated method stub
		return false;
	}

	public static AffiliateListEntry.AffiliateListEntryFactory getFactory() {
		return new AffiliateListEntry.AffiliateListEntryFactory();
	}

}
