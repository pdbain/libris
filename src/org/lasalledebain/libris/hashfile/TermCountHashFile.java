package org.lasalledebain.libris.hashfile;

import java.io.IOException;
import java.io.RandomAccessFile;

import org.lasalledebain.libris.index.TermCountEntry;

public class TermCountHashFile extends StringKeyHashFile<TermCountEntry, TermCountHashBucket>
{
	public TermCountHashFile(RandomAccessFile backingStore)
			throws IOException {
		super(backingStore);

	}

	@Override
	protected TermCountHashBucket createBucket(int bucketNum) {
		return new TermCountHashBucket(backingStore, bucketNum);
	}
}
