package org.lasalledebain.libris.hashfile;

import java.io.RandomAccessFile;

import org.lasalledebain.libris.exception.DatabaseException;

public abstract class NumericKeyHashBucket <T extends NumericKeyHashEntry> extends HashBucket<T> {

	/** 
	 * @param backingStore File to hold buckets
	 * @param bucketNum position in the file
	 */
	public NumericKeyHashBucket(RandomAccessFile backingStore, int bucketNum) {
		super(backingStore, bucketNum);
	}

	@Override
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
