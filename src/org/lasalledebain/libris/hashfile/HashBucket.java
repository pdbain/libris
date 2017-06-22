package org.lasalledebain.libris.hashfile;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Iterator;

import org.lasalledebain.libris.exception.DatabaseException;

@SuppressWarnings("unchecked")
public abstract class HashBucket <T extends HashEntry> implements Iterable<T> {

	protected int occupancy;
	public static final int BUCKET_SIZE = 4096;
	protected final int bucketNumber;
	RandomAccessFile backingStore;
	protected boolean dirty;
	protected final long filePosition;
	protected int age = 0;

	/** 
	 * @param backingStore File to hold buckets
	 * @param bucketNum position in the file
	 * @param fact Factory for hash bucket entries
	 */
	public HashBucket(RandomAccessFile backingStore, int bucketNum) {
		this.backingStore = backingStore;
		this.bucketNumber = bucketNum;
		filePosition = bucketNumber*BUCKET_SIZE;
	}

	public abstract Iterator<T> iterator();

	/**
	 * @param newEntry Entry to add to the bucket
	 * @return true if the entry was added, false if the bucket was full
	 * @throws DatabaseException 
	 */
	public  boolean addElement(T newEntry) throws DatabaseException {
		int key = newEntry.getKey();
		int netLength = newEntry.getTotalLength();
		T oldEntry = findEntry(key);
		if (null != oldEntry) {
			netLength -= oldEntry.getTotalLength();
		}
		if ((netLength + occupancy) > BUCKET_SIZE) {
			return false;
		} else {
			addToBucket(key, newEntry);
			occupancy += Math.abs(netLength);
			dirty = true;
			return true;
		}
	}

	protected abstract void addToBucket(int key, T newEntry);

	public int getOccupancy() {
		return occupancy;
	}

	public static int getBucketSize() {
		return BUCKET_SIZE;
	}
	
	public abstract T findEntry(int key);

	public abstract void write() throws DatabaseException;

	public boolean isDirty() {
		return dirty;
	}

	public void setDirty(boolean dirty) {
		this.dirty = dirty;
	}

	public void clear() {
		occupancy = 4;
		dirty = true;
	}
	
	public abstract void read() throws IOException, DatabaseException;

	public int getNumEntries() throws IOException {
		backingStore.seek(filePosition);
		int numEntries = backingStore.readInt();
		return numEntries;
	}

	public int getAge() {
		return age;
	}

	public void setAge(int age) {
		this.age = age;
	}
}
