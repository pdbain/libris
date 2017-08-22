package org.lasalledebain.libris.indexes;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

import org.lasalledebain.libris.FileAccessManager;
import org.lasalledebain.libris.exception.DatabaseError;
import org.lasalledebain.libris.exception.DatabaseException;
import org.lasalledebain.libris.exception.InternalError;
import org.lasalledebain.libris.exception.LibrisException;
import org.lasalledebain.libris.hashfile.HashBucket;
import org.lasalledebain.libris.hashfile.HashFile;
import org.lasalledebain.libris.hashfile.VariableSizeEntryHashBucket;
import org.lasalledebain.libris.index.AffiliateListEntry;
import org.lasalledebain.libris.index.AffiliateListEntry.AffiliateListEntryFactory;
import org.lasalledebain.libris.ui.Messages;

public class AffiliateList {

	private FileAccessManager overflowFileMgr = null;
	FileAccessManager hashTableFileMgr = null;
	RandomAccessFile hashTableFile;
	RandomAccessFile overflowFile;
	FileSpaceManager overflowSpaceMgr;
	private AffiliateListEntryFactory eFactory;
	private HashFile<AffiliateListEntry> affiliateHashFile;
	private BucketOverflowFileManager bucketOverflowMgr;
	private static int[] empty = new int[0];

	public AffiliateList(FileAccessManager hashTableFileMgr,
			FileAccessManager overflowFileMgr, boolean readOnly) throws DatabaseException {
		this.hashTableFileMgr = hashTableFileMgr;
		this.overflowFileMgr = overflowFileMgr;
		try {
			hashTableFile = readOnly?  hashTableFileMgr.getReadOnlyRandomAccessFile(): 
				hashTableFileMgr.getReadWriteRandomAccessFile();
			overflowFile = readOnly?  overflowFileMgr.getReadOnlyRandomAccessFile(): 
				overflowFileMgr.getReadWriteRandomAccessFile();

			overflowSpaceMgr = new FileSpaceManager(overflowFile, overflowFileMgr.getFile(), readOnly);
			overflowSpaceMgr.reset();
			bucketOverflowMgr = new BucketOverflowFileManager(overflowSpaceMgr);
		} catch (FileNotFoundException | LibrisException exc) {
			throw new DatabaseException(Messages.getString("AffiliateList.0")+hashTableFileMgr.getPath(), exc); //$NON-NLS-1$
		}
		eFactory = new AffiliateListEntry.AffiliateListEntryFactory();
		try {
			affiliateHashFile = new HashFile<AffiliateListEntry>(hashTableFile, 
					VariableSizeEntryHashBucket.getFactory(bucketOverflowMgr), eFactory);
		} catch (IOException e) {
			throw new DatabaseException(e);
		}
	}
	
	public void setSize(long expectedEntries) throws DatabaseException {
		long totalExpectedSize = 6 * expectedEntries; /* 4 bytes per entry + 5% overhead for length and parent */
		int requestedBuckets = (int) (totalExpectedSize/HashBucket.BUCKET_SIZE);
		affiliateHashFile.resize(requestedBuckets);
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
		if (null != affiliateHashFile) {
			try {
				affiliateHashFile.flush();
			} catch (IOException e) {
				throw new DatabaseException(e);
			}
		}
	}

	public int[] getChildren(int recordId) {
		AffiliateListEntry entry;
		entry = getEntry(recordId);
		if (null == entry) {
			return empty;
		} else {
			int[] result = entry.getChildren();
			if (null == result) {
				return empty;
			} else {
				return result;
			}
		}
	}

	public void addChild(int parent, int child) throws DatabaseException {
		try {
			AffiliateListEntry entry = eFactory.makeEntry(parent);
			entry.addChild(child);
			affiliateHashFile.addEntry(entry);
		} catch (DatabaseException | IOException e) {
			throw new InternalError("Error adding affiliates entry for record "+parent, e);
		}
	}

	public int[] getAffiliates(int recordId) throws DatabaseException {
		AffiliateListEntry entry;
		entry = getEntry(recordId);
		if (null == entry) {
			return empty;
		} else {
			return entry.getAffiliates();
		}
	}

	public void addAffiliates(int parent, int affiliates[], int occupancy, boolean isChildren) {
		try {
			AffiliateListEntry entry = getEntry(parent);
			if (null == entry) {
				entry = eFactory.makeEntry(parent);
			}
			entry.addAffiliates(affiliates, occupancy, isChildren);
			affiliateHashFile.addEntry(entry);
		} catch (DatabaseException | IOException e) {
			throw new InternalError("Error adding affiliates entry for record "+parent, e);
		}
	}

	private AffiliateListEntry getEntry(int recordId) {
		AffiliateListEntry entry;
		try {
			entry = affiliateHashFile.getEntry(recordId);
		} catch (DatabaseException | IOException e) {
			throw new DatabaseError("Error getting affiliates for record "+recordId, e);
		}
		return entry;
	}
}
