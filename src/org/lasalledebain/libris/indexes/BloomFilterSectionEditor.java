package org.lasalledebain.libris.indexes;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.BitSet;
import java.util.Objects;

public class BloomFilterSectionEditor extends BloomFilterSection {
	public BloomFilterSectionEditor(RandomAccessFile sigFile, int baseId, Iterable<String> terms, int level) throws IOException {
		super(sigFile, level);
		setTerms(baseId, terms);
	}

	public BloomFilterSectionEditor(RandomAccessFile sigFile, int level) {
		super(sigFile, level);
	}
	
	/**
	 * Sets the current signature to the empty set and changes the base ID.
	 * @param recId
	 */
	public void initialize(int recId) {
		if (null != recordSignature) {
			recordSignature.clear();
		} else {
			recordSignature = new BitSet();
		}
		setBaseId(recId);
	}

	/**
	 * Save the signature to the signature file.
	 * @throws IOException
	 */
	public void store() throws IOException {
		if (Objects.isNull(recordSignature)) {
			return;
		}
		signatureFile.seek(currentBaseId * bytesPerRange);
		byte[] bitArray = recordSignature.toByteArray();
		signatureFile.write(bitArray, 0, bitArray.length);
		/* pad with zeros */
		int i = bitArray.length;
		while (i < bytesPerRange) {
			int writeLength = Math.min(zeros.length, bytesPerRange - i);
			signatureFile.write(zeros, 0, writeLength);
			i += writeLength;
		}
	}
	public void addTerms(Iterable<String> terms) {
		SignatureBitList bitList = new SignatureBitList(terms);
		while (bitList.hasNext()) {
			int n = nextBit(bitList);
			recordSignature.set(n);
		}
	}

	/**
	 * Sets the bit mask for record ID (or ID range containing the record)
	 *  signature to the hash values of the terms.
	 * @param baseId
	 * @param terms
	 * @throws IOException 
	 */
	public void setTerms(int baseId, Iterable<String> terms) throws IOException {
		recordSignature = new BitSet();
		setBaseId(baseId);
		addTerms(terms);
	}

	public boolean match(Iterable<String> matchTerms) {
		SignatureBitList bitList = new SignatureBitList(matchTerms);
		while (bitList.hasNext()) {
			if (!recordSignature.get(nextBit(bitList))) {
				return false;
			}
		}
		return true;
	}

}
