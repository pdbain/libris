package org.lasalledebain.libris.indexes;

import static org.lasalledebain.libris.util.StringUtils.wordsToHashStream;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.BitSet;
import java.util.Collection;
import java.util.Objects;
import java.util.stream.IntStream;

public class BloomFilterSectionEditor extends BloomFilterSection {
	public BloomFilterSectionEditor(RandomAccessFile sigFile, int baseId, IntStream hashes, int level) throws IOException {
		super(sigFile, level);
		recordSignature = new BitSet();
		setBaseId(baseId);
		addHashes(hashes);
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
	 * Ensure the section corresponds to recId.
	 * Null operation if recId's section is loaded.
	 * Saves current section if not.
	 * @param recId record ID 
	 * @throws IOException in case of error
	 */
	public void switchTo(int recId) throws IOException {
		if (!isLoaded(recId)) {
			store();
			initialize(recId);
		}
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
	
	public void addHash(int hash) {
		recordSignature.set(hashToBitNumber(hashToBitNumber(hash)));
	}

	public void addHashes(IntStream hashes) {
		hashes.forEach(hash -> recordSignature.set(hashToBitNumber(hash)));
	}

	/**
	 * Sets the bit mask for record ID (or ID range containing the record)
	 *  signature to the hash values of the terms.
	 * @param baseId
	 * @param terms
	 * @throws IOException 
	 */
	public void setTerms(int baseId, IntStream hashes) throws IOException {
		recordSignature = new BitSet();
		setBaseId(baseId);
		addHashes(hashes);
	}

	public boolean match(IntStream hashes) {
		return hashes.allMatch(i -> recordSignature.get(hashToBitNumber(i)));
	}

	public void addTerms(Collection<String> words) {
		addHashes(wordsToHashStream(words));
	}

	public boolean match(Collection<String> words) {
		match(wordsToHashStream(words));
		return false;
	}

	public void setTerms(int recId, Collection<String> terms) throws IOException {
		setTerms(recId, wordsToHashStream(terms));
	}

}
