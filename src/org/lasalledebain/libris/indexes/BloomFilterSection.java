package org.lasalledebain.libris.indexes;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.BitSet;
import java.util.Iterator;
import java.util.Objects;

import org.lasalledebain.libris.LibrisConstants;
import org.lasalledebain.libris.util.Murmur3;
import org.lasalledebain.libris.util.StringUtils;

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
	public int getRecordsPerSet() {
		return recordsPerSet;
	}

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

	public BloomFilterSection(RandomAccessFile sigFile, int level) {
		signatureFile = sigFile;
		recordsPerSet = bytesPerLevel[level];
		setSize = BASIC_SET_SIZE_BITS * recordsPerSet;
		bytesPerRange = BASIC_SET_SIZE_BYTES * recordsPerSet;
	}

	public int nextBit(SignatureBitList bitList) {
		return bitList.next() & (setSize - 1);
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

	static class SignatureBitList {
		private byte[] currentTerm;
		private int currentLength;
		private Iterator<String> termList;
		SignatureBitList(Iterable<String> terms) {
			termList = terms.iterator();
			startNextTerm();
		}

		boolean hasNext() {
			return null != currentTerm;
		}

		int next() {
			int result = Murmur3.hash32(currentTerm, currentLength);
			if (currentLength < currentTerm.length) {
				++currentLength;
			} else {
				startNextTerm();
			}
			return result;
		}

		public void startNextTerm() {
			currentTerm = null;
			while ((null == currentTerm) && termList.hasNext()) {
				String nextTerm = termList.next();
				if (nextTerm.length() < LibrisConstants.MINIMUM_TERM_LENGTH) {
					continue;
				}
				currentTerm = StringUtils.toCanonicalBytes(nextTerm);
				currentLength = LibrisConstants.MINIMUM_TERM_LENGTH;
			}
		}		
	}

	public static int calculateSignatureLevels(int numRecords) {
		int levels = 1;
		int numSigs = levels * BRANCH_FACTOR;
		int numTopLevelSignatures = numRecords / BRANCH_FACTOR;
		while (numSigs < numTopLevelSignatures) {
			++levels;
			numSigs *= BRANCH_FACTOR;
		}
		return levels;
	}
}
