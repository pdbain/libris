package org.lasalledebain.libris.hashfile;

import java.io.IOException;
import java.io.RandomAccessFile;

import org.lasalledebain.libris.index.AffiliateListEntry;

public class AffiliateHashFile extends VariableSizeEntryNumericKeyHashFile<AffiliateListEntry> {

	public AffiliateHashFile(RandomAccessFile backingStore, NumericKeyHashBucketFactory bFact,
			NumericKeyEntryFactory eFact) throws IOException {
		super(backingStore, bFact, eFact);
		// TODO Auto-generated constructor stub
	}

}
