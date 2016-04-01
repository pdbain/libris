package org.lasalledebain.libris;

import org.lasalledebain.libris.exception.DatabaseException;

import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;

/**
 * Record ID
 * Record IDs comprise two parts:
 * 1. a branch, by default 0
 * 2. record number, staring at 1
 * The master database copy creates records with branch 0, while database copy <n> use branch <n>, n > 0
 * Textually, record IDs are represented as a decimal B.R, where B is the branch ID and R is the record number.
 * An integer R is equivalent to 0.R
 *
 */
public class RecordId implements Comparable<RecordId>{
	final private int numericId;
	private boolean hashed;
	private int hashValue;
	static HashFunction hashFunction = Hashing.murmur3_32();
	static private final RecordId nullId = new RecordId(0);

	public RecordId(Integer recordNum) {
		numericId = (null == recordNum)? 0: recordNum;
	}

	public RecordId(String idString) throws DatabaseException {
		try {
			numericId = Integer.parseInt(idString);
		} catch (NumberFormatException e) {
			throw new DatabaseException("malformed record ID: "+idString, e);
		}
	}

	public String toString() {
		String result = Integer.toString(numericId);
		return result;
	}

	public int getRecordNumber() {
		return numericId;
	}

	@Override
	public boolean equals(Object comparand) {
		if (comparand.getClass().equals(RecordId.class)) {
			RecordId other = (RecordId) comparand;
			if  (numericId != other.numericId) {
				return false;
			} else {
				return true;
			}
		} else {
			return false;
		}
	}

	@Override
	public int hashCode() {
		if (!hashed) {
			hashValue = hashFunction.hashInt(numericId).asInt();
			hashed = true;
		}
		return hashValue;
	}

	public RecordId increment() throws DatabaseException {
		RecordId newId = new RecordId(numericId+1);
		return newId;
	}

	@Override
	public int compareTo(RecordId comparand) {
		int result = 0;
		if (numericId < comparand.numericId) {
			result = -1;
		} else	if (numericId > comparand.numericId) {
				result = 1;
		}
		return result;
	}

	public static RecordId getNullId() {
		return nullId;
	}
	
	public boolean isNull() {
		return 0 == numericId;
	}
}
