package org.lasalledebain.libris.hashfile;

import java.io.IOException;
import java.io.RandomAccessFile;

import org.lasalledebain.libris.exception.DatabaseException;
import org.lasalledebain.libris.index.TermCountEntry;
import org.lasalledebain.libris.index.TermCountEntry.TermCountEntryFactory;

public class TermCountHashFile extends StringKeyHashFile<TermCountEntry, TermCountHashBucket, TermCountEntryFactory>
{
	public TermCountHashFile(RandomAccessFile backingStore, 
			HashBucketFactory<TermCountEntry, TermCountHashBucket, TermCountEntryFactory> bFact)
			throws IOException {
		super(backingStore, bFact);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void addEntry(TermCountEntry entry) throws IOException, DatabaseException {
		// TODO Auto-generated method stub

	}

	@Override
	protected int findHomeBucket(long key) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public TermCountEntry getEntry(int recordId) throws IOException, DatabaseException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected int findHomeBucket(TermCountEntry entry) {
		// TODO Auto-generated method stub
		return 0;
	}


}
