package org.lasalledebain.libris.hashfile;

import java.io.IOException;
import java.io.RandomAccessFile;

import org.lasalledebain.libris.index.RecordPositionEntry;

public class RecordPositionHashFile extends NumericKeyHashFile<RecordPositionEntry, NumericKeyHashBucket<RecordPositionEntry>, NumericKeyEntryFactory<RecordPositionEntry>> {

	public RecordPositionHashFile(RandomAccessFile backingStore, NumericKeyHashBucketFactory bFact,
			NumericKeyEntryFactory eFact) throws IOException {
		super(backingStore, bFact, eFact);
		// TODO Auto-generated constructor stub
	}

}
