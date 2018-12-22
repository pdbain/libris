package org.lasalledebain.libris.indexes;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.BitSet;
import java.util.stream.IntStream;

public class BloomFilterSectionQuery extends BloomFilterSection {
	BitSet matchSignature;

	public BloomFilterSectionQuery(RandomAccessFile sigFile, int level) {
		super(sigFile, level);
	}

	public BloomFilterSectionQuery(RandomAccessFile sigFile, int baseId, IntStream hashes, int level) throws IOException {
		super(sigFile, baseId, level);
		int nbits = bytesPerRange * 8;
		matchSignature = new BitSet(nbits);
		hashes.forEach(i -> matchSignature.set(hashToBitNumber(i)));
	}

	public boolean match() {
		final boolean found = matchSignature.stream().allMatch(i -> recordSignature.get(hashToBitNumber(i)));
		return found;
	}

}
