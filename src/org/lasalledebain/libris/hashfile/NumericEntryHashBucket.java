package org.lasalledebain.libris.hashfile;

import java.io.RandomAccessFile;

import org.lasalledebain.libris.exception.DatabaseException;

public abstract class NumericEntryHashBucket <T extends NumericKeyHashEntry> extends HashBucket<T> {

	/** 
	 * @param backingStore File to hold buckets
	 * @param bucketNum position in the file
	 * @param fact Factory for hash bucket entries
	 */
	public NumericEntryHashBucket(RandomAccessFile backingStore, int bucketNum) {
		super(backingStore, bucketNum);
	}

	/**
	 * @param newEntry Entry to add to the bucket
	 * @return true if the entry was added, false if the bucket was full
	 * @throws DatabaseException 
	 * @note if and entry with newEntry's key exists in the bucket, it is removed before adding newEntry.
	 */
	public  boolean addEntry(T newEntry) throws DatabaseException {
		int key = newEntry.getKey();
		int netLength = newEntry.getTotalLength();
		T oldEntry = getEntry(key);
		if (null != oldEntry) {
			netLength -= oldEntry.getTotalLength();
		}
		if ((netLength + occupancy) > BUCKET_SIZE) {
			if (null != oldEntry) {
				removeFromBucket(key);
				occupancy -= oldEntry.getTotalLength();
			}
			return false;
		} else {
			addToBucket(key, newEntry);
			occupancy += Math.abs(netLength);
			dirty = true;
			return true;
		}
	}
	
	protected abstract T removeFromBucket(int key);

	protected abstract void addToBucket(int key, T newEntry);

	public abstract T getEntry(int key);
}
