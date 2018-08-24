package org.lasalledebain.libris.indexes;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

import org.lasalledebain.libris.FileAccessManager;
import org.lasalledebain.libris.exception.DatabaseException;
import org.lasalledebain.libris.hashfile.FixedSizeEntryHashBucket;
import org.lasalledebain.libris.hashfile.HashFile;
import org.lasalledebain.libris.hashfile.NumericKeyHashBucket;
import org.lasalledebain.libris.hashfile.NumericKeyHashFile;
import org.lasalledebain.libris.hashfile.NumericKeyHashFile;
import org.lasalledebain.libris.index.RecordPositionEntry;
import org.lasalledebain.libris.index.RecordPositionEntryFactory;
import org.lasalledebain.libris.ui.Messages;

public class FileRecordMap extends LibrisRecordMap {

	FileAccessManager backingStoreFileMgr = null;
	RandomAccessFile backingStore;
	private boolean readOnly;
	private RecordPositionEntryFactory eFactory;
	private HashFile<RecordPositionEntry, NumericKeyHashBucket<RecordPositionEntry>> indexHashFile;

	/**
	 * @param positionFileMgr hash file of record positions
	 * @param readOnly prevent updates
	 * @throws DatabaseException
	 */
	public FileRecordMap(FileAccessManager positionFileMgr, boolean readOnly) throws DatabaseException {
		backingStoreFileMgr = positionFileMgr;
		this.readOnly = readOnly;
		try {
			backingStore = readOnly?  backingStoreFileMgr.getReadOnlyRandomAccessFile(): backingStoreFileMgr.getReadWriteRandomAccessFile();
		} catch (FileNotFoundException exc) {
			throw new DatabaseException(Messages.getString("FileRecordMap.0")+positionFileMgr.getPath(), exc); //$NON-NLS-1$
		}
		eFactory = new RecordPositionEntryFactory();
		try {
			indexHashFile = new NumericKeyHashFile<RecordPositionEntry, NumericKeyHashBucket<RecordPositionEntry>>(backingStore, FixedSizeEntryHashBucket.getFactory(), eFactory);
		} catch (IOException e) {
			throw new DatabaseException(e);
		}
	}

	@Override
	public long getRecordPosition(int recordId) throws DatabaseException {
		try {
			RecordPositionEntry rec = indexHashFile.getEntry(recordId);
			if (null != rec) {
				return rec.getRecordPosition();
			}
		} catch (IOException e) {
			throw new DatabaseException(e);
		}
		return 0;		
	}

	@Override
	public void putRecordPosition(int recordIdNum, long recordPosition) throws DatabaseException {
		if (readOnly) {
			throw new DatabaseException(Messages.getString("FileRecordMap.2")); //$NON-NLS-1$
		}
		try {
			indexHashFile.addEntry(eFactory.makeEntry(recordIdNum, recordPosition));
		} catch (IOException e) {
			throw new DatabaseException(e);
		}
	}
	

	@Override
	public boolean setSize(int numRecords) {
		try {
			int recordsPerBucket = FixedSizeEntryHashBucket.entriesPerBucket(eFactory);
			int requestedBuckets = (numRecords+recordsPerBucket-1)/recordsPerBucket;
			return indexHashFile.resize(requestedBuckets);
		} catch (DatabaseException e) {
			return false;
		}
	}

	@Override
	public int size() throws DatabaseException {
		return indexHashFile.getNumEntries();
	}

	@Override
	public void close() throws DatabaseException {
		flush();
		try {
			backingStoreFileMgr.releaseRaFile(backingStore);
		} catch (IOException e) {
			throw new DatabaseException(e);
		}
	}

	@Override
	public void flush() throws DatabaseException {
		if (null != indexHashFile) {
			try {
				indexHashFile.flush();
			} catch (IOException e) {
				throw new DatabaseException(e);
			}
		}
	}

}
