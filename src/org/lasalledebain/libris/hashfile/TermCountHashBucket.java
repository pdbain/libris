package org.lasalledebain.libris.hashfile;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.HashSet;
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
public class TermCountHashBucket extends StringKeyHashBucket<TermCountEntry> {

	HashSet<TermCountEntry> entries;
	TermCountEntryFactory myFact;
	public TermCountHashBucket(RandomAccessFile backingStore, int bucketNum, TermCountEntryFactory fact) {
		super(backingStore, bucketNum);
		entries = new HashSet<>();
		myFact = fact;
		occupancy = 0;
	}

	@Override
	public boolean addEntry(TermCountEntry newEntry) throws DatabaseException {
		final int newOccupancy = newEntry.getTotalLength() + occupancy;
		if (newOccupancy > BUCKET_SIZE) {
			return false;
		} else {
			entries.add(newEntry);
			occupancy = newOccupancy;
			return true;
		}
	}

	@Override
	public void write() throws DatabaseException {
		if (!dirty) {
			return;
		}
		try {
			backingStore.seek(filePosition);
			backingStore.writeShort(entries.size());
			if (null != entries) {
				int numEntries = entries.size();
				short [][] positionInfo = new short[numEntries][2];
				short offset = 0;
				int cursor = 0;
				ByteArrayOutputStream bos = new ByteArrayOutputStream(occupancy);
				DataOutputStream dos = new DataOutputStream(bos);
				for (TermCountEntry e: entries) {
					short entryLen = (short) e.getEntryLength();
					positionInfo[cursor][0] = offset;
					positionInfo[cursor][1] = entryLen;
					e.writeData(dos);
					offset += entryLen;
					++cursor;
				}
				for (int i = 0; i < cursor; ++i) {
					backingStore.writeShort(positionInfo[i][0]);
					backingStore.writeShort(positionInfo[i][1]);
				}
				backingStore.write(bos.toByteArray(), 0, offset);
			} else {
				backingStore.writeInt(0);		
			}
		} catch (IOException e) {
			throw new DatabaseException("Error writing hash entry", e);
		}

		dirty = false;
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
		entries.clear();
		for (short[] posAndLen: positionInfo) {
			final TermCountEntry newEntry = myFact.makeEntry(mainBuffer, posAndLen[0], posAndLen[1]);
			entries.add(newEntry);		
		}
		occupancy = 2 /* numEntries */
				+ (4 * numEntries) /* index */
				+ totalLen;
	}

	@Override
	public Iterator<TermCountEntry> iterator() {
		return entries.iterator();
	}

	@Override
	protected int getNumEntriesImpl() {
		return entries.size();
	}
	
	public static class TermCountBucketFactory
	implements StringKeyHashBucketFactory<TermCountEntry, TermCountHashBucket, TermCountEntryFactory> {

		@Override
		public TermCountHashBucket createBucket(RandomAccessFile backingStore, int bucketNum, TermCountEntryFactory fact) {
			// TODO Auto-generated method stub
			return new TermCountHashBucket(backingStore, bucketNum, fact);
		}

	}

}
