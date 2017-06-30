package org.lasalledebain.libris.indexes;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

import org.lasalledebain.libris.FileAccessManager;
import org.lasalledebain.libris.exception.DatabaseException;
import org.lasalledebain.libris.hashfile.FixedSizeEntryHashBucket;
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
	private boolean readOnly;
	private AffiliateListEntryFactory eFactory;
	private HashFile<AffiliateListEntry> affiliateHashFile;
	private BucketOverflowFileManager bucketOverflowMgr;
	private static int[] empty;

	public AffiliateList(FileAccessManager hashTableFileMgr,
			FileAccessManager overflowFileMgr, boolean readOnly) throws DatabaseException {
		this.hashTableFileMgr = hashTableFileMgr;
		this.overflowFileMgr = overflowFileMgr;
		this.readOnly = readOnly;
		try {
			hashTableFile = readOnly?  hashTableFileMgr.getReadOnlyRandomAccessFile(): 
				hashTableFileMgr.getReadWriteRandomAccessFile();
			overflowFile = readOnly?  overflowFileMgr.getReadOnlyRandomAccessFile(): 
				overflowFileMgr.getReadWriteRandomAccessFile();

			overflowSpaceMgr = new FileSpaceManager(overflowFile, overflowFileMgr.getFile(), readOnly);
			bucketOverflowMgr = new BucketOverflowFileManager(overflowSpaceMgr);
		} catch (FileNotFoundException exc) {
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

	int[] getChildren(int recordId) throws DatabaseException {
		AffiliateListEntry entry;
		entry = getEntry(recordId);
		if (null == entry) {
			return empty;
		} else {
			return entry.getChildren();
		}
	}

	int[] getAffiliates(int recordId) throws DatabaseException {
		AffiliateListEntry entry;
		entry = getEntry(recordId);
		if (null == entry) {
			return empty;
		} else {
			return entry.getAffiliates();
		}
	}

	private AffiliateListEntry getEntry(int recordId) throws DatabaseException {
		AffiliateListEntry entry;
		try {
			entry = affiliateHashFile.getEntry(recordId);
		} catch (DatabaseException | IOException e) {
			throw new DatabaseException("Error getting affiliates for record "+recordId, e);
		}
		return entry;
	}
}
