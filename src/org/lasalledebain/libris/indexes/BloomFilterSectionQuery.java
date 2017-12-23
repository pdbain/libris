package org.lasalledebain.libris.indexes;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.BitSet;

public class BloomFilterSectionQuery extends BloomFilterSection {
	BitSet matchSignature;

	public BloomFilterSectionQuery(RandomAccessFile sigFile, int level) {
		super(sigFile, level);
	}
	public BloomFilterSectionQuery(RandomAccessFile sigFile, int baseId, Iterable<String> terms, int level) throws IOException {
		super(sigFile, baseId, terms, level);
		int nbits = bytesPerRange * 8;
		matchSignature = new BitSet(nbits);
		SignatureBitList bitList = new SignatureBitList(terms);
		while (bitList.hasNext()) {
			int n = nextBit(bitList);
			matchSignature.set(n);
		}
	}

	public boolean match() {
		return matchSignature.stream().allMatch(i -> recordSignature.get(i));
	}

}
