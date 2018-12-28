package org.lasalledebain.libris.hashfile;

import java.io.IOException;
import java.io.RandomAccessFile;

public abstract class VariableSizeEntryNumericKeyHashFile<EntryType extends VariableSizeHashEntry> 
extends NumericKeyHashFile<EntryType, NumericKeyHashBucket<EntryType>, NumericKeyEntryFactory<EntryType>> {

	public VariableSizeEntryNumericKeyHashFile(RandomAccessFile backingStore, NumericKeyHashBucketFactory bFact,
			NumericKeyEntryFactory eFact) throws IOException {
		super(backingStore, bFact, eFact);
	}

}
