package org.lasalledebain.libris.hashfile;

import java.io.IOException;
import java.io.RandomAccessFile;

import org.lasalledebain.libris.hashfile.TermCountHashBucket.TermCountBucketFactory;
import org.lasalledebain.libris.index.TermCountEntry;
import org.lasalledebain.libris.index.TermCountEntry.TermCountEntryFactory;

public class TermCountHashFile extends StringKeyHashFile<TermCountEntry, TermCountHashBucket, TermCountEntryFactory>
{
	public TermCountHashFile(RandomAccessFile backingStore)
			throws IOException {
		super(backingStore, new TermCountBucketFactory());

	}

}
