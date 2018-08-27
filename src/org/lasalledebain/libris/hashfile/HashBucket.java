package org.lasalledebain.libris.hashfile;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Iterator;

import org.lasalledebain.libris.exception.DatabaseException;

public abstract class HashBucket<T extends HashEntry>  implements Iterable<T>{
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
	 * @note if an entry with newEntry's key exists in the bucket, it is removed before adding newEntry.
	 */
	public abstract  boolean addEntry(T newEntry) throws DatabaseException;

	public int getOccupancy() {
		return occupancy;
	}

	public static int getBucketSize() {
		return BUCKET_SIZE;
	}

	public abstract void write() throws DatabaseException;

	public void clear() {
		occupancy = 4;
		dirty = true;
	}
	

	public int getNumEntries() throws IOException {
		int numEntries;
		if (dirty) {
			numEntries = getNumEntriesImpl();
		} else {
			backingStore.seek(filePosition);
			numEntries = backingStore.readInt();
		}
		return numEntries;
	}

	protected abstract int getNumEntriesImpl();

	public int getAge() {
		return age;
	}

	public void setAge(int age) {
		this.age = age;
	}
	public boolean isDirty() {
		return dirty;
	}

	public void setDirty(boolean dirty) {
		this.dirty = dirty;
	}

	public abstract void read() throws IOException, DatabaseException;

}
