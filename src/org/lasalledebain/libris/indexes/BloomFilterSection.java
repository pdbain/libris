package org.lasalledebain.libris.indexes;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.BitSet;
import java.util.Objects;

/**
 * This class implements keyword searching using a Bloom filter  There is one filter for 
 * each level of resolution of the filter.  Level 1 filters have a bit mask for each record,
 * level n have a bit mask for BRANCHFACTOR**n records.  
 * Only one bit mask exists in a given BloomFilterSection at any given time.
 */

public abstract class BloomFilterSection {
	public static final int BRANCH_FACTOR = 16;
	protected static final int BASIC_SET_SIZE_BYTES = 128;
	protected static final int BASIC_SET_SIZE_BITS = BASIC_SET_SIZE_BYTES * 8;
	protected static final byte zeros[] = new byte[256];
	protected int setSize;
	protected int bytesPerRange;
	protected RandomAccessFile signatureFile;
	protected final int recordsPerSet;
	protected int currentBaseId;
	protected BitSet recordSignature;
	public final static int MAX_LEVEL = 4;
	public static final int bytesPerLevel[] = getLevels(MAX_LEVEL);

	static int[] getLevels(int max) {
		int size = 1;
		int result[] = new int[max + 1];
		for (int i = 0; i <= max; ++i) {
			result[i] = size;
			size *= BRANCH_FACTOR;
		}
		return result;
	}
	
	public BloomFilterSection(RandomAccessFile sigFile, int baseId, Iterable<String> terms, int level) throws IOException {
		this(sigFile, level);
	}

	public BloomFilterSection(RandomAccessFile sigFile, int baseId, int level) throws IOException {
		this(sigFile, level);
	}

	public BloomFilterSection(RandomAccessFile sigFile, int level) {
		signatureFile = sigFile;
		recordsPerSet = bytesPerLevel[level];
		setSize = BASIC_SET_SIZE_BITS * recordsPerSet;
		bytesPerRange = BASIC_SET_SIZE_BYTES * recordsPerSet;
	}

	public int getRecordsPerSet() {
		return recordsPerSet;
	}

	public boolean isLoaded(int id) {
		return (getCanonicalId(id) == currentBaseId) && Objects.nonNull(recordSignature);
	}

	protected void setBaseId(int baseId) {
		currentBaseId = getCanonicalId(baseId);
	}

	private int getCanonicalId(int id) {
		return id & ~(recordsPerSet - 1);
	}
	
	public int lastIdInRange(int limit) {
		return Math.min(limit, currentBaseId + recordsPerSet - 1);
	}
	/**
	 * Loads the portion of the signature file containing the specified recordID.
	 * @param baseId Record ID to load
	 * @throws IOException on error reading signature file
	 */
	public void load(int baseId) throws IOException {
		if (Objects.isNull(recordSignature) || !isLoaded(baseId)) {
			setBaseId(baseId);
			int signatureStart = currentBaseId * bytesPerRange;
			if (signatureFile.length() >= (signatureStart + bytesPerRange)) {
				signatureFile.seek(signatureStart);
				byte[] bitArray = new byte[bytesPerRange];
				signatureFile.readFully(bitArray, 0, bitArray.length);
				recordSignature = BitSet.valueOf(bitArray);
			} else {
				recordSignature = new BitSet();
			}
		}
	}

	protected int hashToBitNumber(int hash) {
		final int bitNum = hash & (setSize - 1);
		return bitNum;
	}
}
