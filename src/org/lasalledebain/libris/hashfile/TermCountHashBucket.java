package org.lasalledebain.libris.hashfile;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Iterator;

import org.lasalledebain.libris.exception.DatabaseException;
import org.lasalledebain.libris.index.TermCountEntry;
import org.lasalledebain.libris.index.TermCountEntry.TermCountEntryFactory;

/**
 * File format:
 * 2 bytes num_entries: number of entries
 * 4 * num_entries: index
 * 
 * 	index comprises:
 * 		2 bytes offset
 * 		2 bytes length
 *  remainder of file is entries
 * 
 */
public class TermCountHashBucket extends StringKeyHashBucket {

	TermCountEntry[] entries;
	TermCountEntryFactory fact;
	public TermCountHashBucket(RandomAccessFile backingStore, int bucketNum) {
		super(backingStore, bucketNum);
	}

	@Override
	public boolean addEntry(HashEntry newEntry) throws DatabaseException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void write() throws DatabaseException {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void read() throws IOException, DatabaseException {
		short numEntries = backingStore.readShort();
		short [][] positionInfo = new short[numEntries][2];
		int totalLen = 0;
		for (int i = 0; i < numEntries; ++i) {
			positionInfo[i][0] = backingStore.readShort();
			final short len = backingStore.readShort();
			positionInfo[i][1] = len;
			totalLen += len;
		}
		byte mainBuffer[] = new byte[totalLen];
		backingStore.readFully(mainBuffer);
		entries = new TermCountEntry[numEntries];
		int entryIndex = 0;
		for (short[] posAndLen: positionInfo) {
			final TermCountEntry newEntry = fact.makeEntry(mainBuffer, posAndLen[0], posAndLen[1]);
			entries[entryIndex] = newEntry;
			
		}
	}

	@Override
	public Iterator<TermCountEntry> iterator() {
		return Arrays.asList(entries).iterator();
	}

	@Override
	protected int getNumEntriesImpl() {
		return entries.length;
	}

}
