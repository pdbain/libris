package org.lasalledebain.libris.indexes;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;
import java.util.stream.IntStream;

import org.lasalledebain.libris.FileAccessManager;
import org.lasalledebain.libris.LibrisConstants;
import org.lasalledebain.libris.Record;
import org.lasalledebain.libris.RecordList;
import org.lasalledebain.libris.exception.DatabaseError;
import org.lasalledebain.libris.exception.DatabaseException;
import org.lasalledebain.libris.exception.LibrisException;
import org.lasalledebain.libris.hashfile.AffiliateHashFile;
import org.lasalledebain.libris.hashfile.NumericKeyHashBucket;
import org.lasalledebain.libris.index.AffiliateListEntry;

public class AffiliateList<RecordType extends Record> {

	private final FileAccessManager overflowFileMgr;
	private final FileAccessManager hashTableFileMgr;
	private final RandomAccessFile hashTableFile;
	private final RandomAccessFile overflowFile;
	private final FileSpaceManager overflowSpaceMgr;
	private final  AffiliateHashFile myHashFile;
	private final  BucketOverflowFileManager bucketOverflowMgr;
	public AffiliateList(FileAccessManager theFileMgr,
			FileAccessManager overflowFileMgr, boolean readOnly) throws DatabaseException {
		this.hashTableFileMgr = theFileMgr;
		this.overflowFileMgr = overflowFileMgr;
		try {
			hashTableFile = readOnly?  theFileMgr.getReadOnlyRandomAccessFile(): 
				theFileMgr.getReadWriteRandomAccessFile();
			overflowFile = readOnly?  overflowFileMgr.getReadOnlyRandomAccessFile(): 
				overflowFileMgr.getReadWriteRandomAccessFile();

			overflowSpaceMgr = new FileSpaceManager(overflowFile, overflowFileMgr.getFile(), readOnly);
			if (!readOnly) {
				overflowSpaceMgr.reset();
			}
			bucketOverflowMgr = new BucketOverflowFileManager(overflowSpaceMgr);
		} catch (FileNotFoundException | LibrisException exc) {
			throw new DatabaseException("Error creating file manager "+theFileMgr.getPath(), exc); //$NON-NLS-1$
		}
		try {
			myHashFile = new AffiliateHashFile(hashTableFile,bucketOverflowMgr);
		} catch (IOException e) {
			throw new DatabaseException(e);
		}
	}
	
	public void setSize(long expectedEntries) throws DatabaseException {
		long totalExpectedSize = 6 * expectedEntries; /* 4 bytes per entry + 5% overhead for length and parent */
		int requestedBuckets = (int) (totalExpectedSize/NumericKeyHashBucket.BUCKET_SIZE);
		myHashFile.resize(requestedBuckets);
	}

	public void close() throws DatabaseException {
		flush();
		try {
			hashTableFileMgr.releaseRaFile(hashTableFile);
			overflowFileMgr.releaseRaFile(overflowFile);
		} catch (IOException e) {
			throw new DatabaseException(e);
		}
	}

	public void flush() throws DatabaseException {
		if (null != myHashFile) {
			try {
				myHashFile.flush();
			} catch (IOException e) {
				throw new DatabaseException(e);
			}
		}
	}

	public void addChild(int parent, int child) {
		try {
			AffiliateListEntry oldEntry = getEntry(parent);
			AffiliateListEntry newEntry;
			if (null == oldEntry) {
				newEntry = new AffiliateListEntry(parent, child, true);
			} else {
				newEntry = new AffiliateListEntry(oldEntry, child, true);
			}
			myHashFile.addEntry(newEntry);
		} catch (DatabaseException | IOException e) {
			throw new DatabaseError("Error adding affiliates entry for record "+parent, e);
		}
	}

	public void addAffiliate(int dest, int src) {
		try {
			AffiliateListEntry oldEntry = getEntry(dest);
			AffiliateListEntry newEntry;
			if (null == oldEntry) {
				newEntry = new AffiliateListEntry(dest, src, false);
			} else {
				newEntry = new AffiliateListEntry(oldEntry, src, false);
			}
			myHashFile.addEntry(newEntry);
		} catch (DatabaseException | IOException e) {
			throw new DatabaseError("Error adding affiliates entry for record "+dest, e);
		}
	}

	public int[] getAffiliates(int recordId) {
		AffiliateListEntry entry;
		entry = getEntry(recordId);
		if (null == entry) {
			return LibrisConstants.emptyIntList;
		} else {
			return entry.getAffiliates();
		}
	}

	public void addAffiliates(int parent, int affiliates[], boolean isChildren) {
		try {
			AffiliateListEntry oldEntry = getEntry(parent);
			AffiliateListEntry newEntry;
			int newChildren[]  = isChildren? affiliates: LibrisConstants.emptyIntList;
			int newAffiliates[]  = isChildren? LibrisConstants.emptyIntList: affiliates;
			if (null == oldEntry) {
				newEntry = new AffiliateListEntry(parent, newChildren, newAffiliates);
			} else {
				newEntry = new AffiliateListEntry(oldEntry, affiliates, false);
			}
			myHashFile.addEntry(newEntry);
		} catch (DatabaseException | IOException e) {
			throw new DatabaseError("Error adding affiliates entry for record "+parent, e);
		}
	}

	private AffiliateListEntry getEntry(int recordId) {
		AffiliateListEntry entry;
		try {
			entry = myHashFile.getEntry(recordId);
		} catch (DatabaseException | IOException e) {
			throw new DatabaseError("Error getting affiliates for record "+recordId, e);
		}
		return entry;
	}

	public IntStream getChildren(int recordId) {
		AffiliateListEntry entry = getEntry(recordId);
		return (null == entry) ? IntStream.empty(): Arrays.stream(entry.getChildren());
	}

	/**
	 * @param recordId root
	 * @return stream comprising depth-first traversal of the family tree starting at recordId
	 */
	public IntStream getFamily(int recordId) {
		AffiliateListEntry entry = getEntry(recordId);
		final IntStream selfStream = IntStream.of(recordId);
		return (null == entry) ? selfStream: IntStream.concat(selfStream, getChildren(recordId).flatMap(r -> getFamily(r)));
	}
}
