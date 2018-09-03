package org.lasalledebain.libris.hashfile;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.Iterator;

import org.lasalledebain.libris.exception.DatabaseException;
import org.lasalledebain.libris.index.TermCountEntry;
import org.lasalledebain.libris.index.TermCountEntry.TermCountEntryFactory;
import org.lasalledebain.libris.util.ByteArraySlice;

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

	HashMap<ByteArraySlice, TermCountEntry> entries;
	public TermCountHashBucket(RandomAccessFile backingStore, int bucketNum, TermCountEntryFactory fact) {
		this(backingStore, bucketNum);
	}

	public TermCountHashBucket(RandomAccessFile backingStore, int bucketNum) {
		super(backingStore, bucketNum);
		entries = new HashMap<>();
		occupancy = 2;
	}

	@Override
	public boolean addEntry(TermCountEntry newEntry) throws DatabaseException {
		final int newOccupancy = newEntry.getTotalLength() + occupancy;
		if (newOccupancy > BUCKET_SIZE) {
			return false;
		} else {
			entries.put(newEntry.getKey(), newEntry);
			occupancy = newOccupancy;
			dirty = true;
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
				for (TermCountEntry e: entries.values()) {
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
		clear();
		long fileSize = backingStore.length();
		if (filePosition >= fileSize) {
			return;
		}
		backingStore.seek(filePosition);
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
			final TermCountEntry newEntry = TermCountEntry.makeEntry(mainBuffer, posAndLen[0], posAndLen[1]);
			entries.put(newEntry.getKey(), newEntry);		
		}
		occupancy = 2 /* numEntries */
				+ (4 * numEntries) /* index */
				+ totalLen;
	}

	@Override
	public TermCountEntry getEntry(String key) {
		final ByteArraySlice keyBytes = new ByteArraySlice(key);
		return getEntry(keyBytes);
	}

	@Override
	public TermCountEntry getEntry(ByteArraySlice keyBytes) {
		return entries.get(keyBytes);
	}

	public TermCountEntry get(ByteArraySlice key) {
		return entries.get(key);
	}

	@Override
	public Iterator<TermCountEntry> iterator() {
		return entries.values().iterator();
	}

	protected int getNumEntriesImpl() {
		return entries.size();
	}
	
	public int getNumEntries() throws IOException {
	int numEntries;
	if (dirty) {
		numEntries = getNumEntriesImpl();
	} else {
		backingStore.seek(filePosition);
		numEntries = backingStore.readShort();
	}
	return numEntries;
}
	
	@Deprecated
	public static class TermCountBucketFactory
	implements HashBucketFactory<TermCountEntry, TermCountHashBucket, TermCountEntryFactory> {
		private TermCountEntryFactory entryFactory;

		public TermCountBucketFactory() {
			entryFactory = new TermCountEntry.TermCountEntryFactory();
		}

		@Override
		public TermCountHashBucket createBucket(RandomAccessFile backingStore, int bucketNum, TermCountEntryFactory fact) {
			return new TermCountHashBucket(backingStore, bucketNum, entryFactory);
		}

		public TermCountHashBucket createBucket(RandomAccessFile backingStore, int bucketNum) {
			return new TermCountHashBucket(backingStore, bucketNum, entryFactory);
		}

	}

	@Override
	public void clear() {
		occupancy = 2;
		entries.clear();
	}

}
