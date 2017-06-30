package org.lasalledebain.libris.indexes;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Iterator;

import org.lasalledebain.libris.LibrisDatabase;
import org.lasalledebain.libris.exception.DatabaseException;
import org.lasalledebain.libris.exception.LibrisException;
import org.lasalledebain.libris.exception.UserErrorException;

public class FileSpaceManager implements Iterable<RecordHeader> {
	RandomAccessFile dataFile;
	private RecordHeader entryList;
	private RecordHeader freeList;
	private boolean readOnly;
	private File dataFileName;
	private static final long MINIMUM_FILE_LENGTH = 2*RecordHeader.getHeaderLength(); /* head & tail, record and free */

	public FileSpaceManager(File dataFileName, boolean readOnly) throws FileNotFoundException {
		this.readOnly = readOnly;
		this.dataFileName = dataFileName;
		this.dataFile = new RandomAccessFile(dataFileName, readOnly? "r": "rw");
	}

	public FileSpaceManager(RandomAccessFile raFile, File dFile, boolean readOnly) {
		this.readOnly = readOnly;
		this.dataFile = raFile;
		this.dataFileName = dFile;
	}

	public void open() {
		entryList = new RecordHeader(dataFile);
		entryList.setFilePosition(0);
		freeList = new RecordHeader(dataFile);
		freeList.setFilePosition(RecordHeader.getHeaderLength());
	}
	
	public void close() throws DatabaseException {
		flush();
		try {
			dataFile.close();
		} catch (IOException e) {
			throw new DatabaseException("error closing file", e);
		}
	}

	/**
	 * Initialize the file and open it.
	 * @throws LibrisException
	 */
	public void reset() throws LibrisException {
		if (readOnly) {
			throw new UserErrorException("attempting to recreate records file of read-only database");
		}

		try {
			dataFile.setLength(MINIMUM_FILE_LENGTH);
			open();
			flush();
		} catch (IOException e) {
			throw new DatabaseException("Error accessing "+dataFileName);
		}
	}

	public void flush() throws DatabaseException {
		entryList.save();
		freeList.save();
	}

	public long getFileSize() throws IOException {
		return dataFile.length();
	}
	public RecordHeader addNew(int entrySize) throws DatabaseException {

		try {
			RecordHeader result = null;
			Iterator<RecordHeader> rhi = freeList.iterator();
			while (rhi.hasNext()) {
				RecordHeader rh = rhi.next();
				if (rh.getSize() >= entrySize) {
					rhi.remove();
					result = rh;
					break;
				}
			} 

			if (null == result) {
				/* no free space.  Extend the file */
				long allocatedPosition = dataFile.length();
				dataFile.setLength(allocatedPosition+RecordHeader.getHeaderLength()+entrySize);
				result = new RecordHeader(dataFile);
				result.setFilePosition(allocatedPosition);
				result.setSize(entrySize);
			}
			entryList.add(result);
			return result;
		} catch (IOException e) {
			throw new DatabaseException("error writing record file", e);
		}
	}
	
	public void remove(RecordHeader r) throws DatabaseException {
		entryList.remove(r);
		freeList.add(r);
	}
	
	@Override
	public Iterator<RecordHeader> iterator() {
		return new RecordHeaderIterator(entryList, freeList);
	}


	protected class RecordHeaderIterator implements Iterator<RecordHeader> {
		private RecordHeader root;
		private RecordHeader cursor;
		/**
		 * @param root
		 */
		public RecordHeaderIterator(RecordHeader root, RecordHeader freelist) {
			this.root = root;
		}

		@Override
		public boolean hasNext() {
			return ((0 != root.getNext()) && ((null == cursor) || (cursor.getFilePosition() != root.getPrev())));
		}

		@Override
		public RecordHeader next() {
			RecordHeader result = null;
			try {
				if (null == cursor) {
					result = new RecordHeader(dataFile, root.getNext());
				} else {
					if (0 != cursor.getNext()) {
						result = new RecordHeader(dataFile, cursor.getNext());
					}
				}
			} catch (DatabaseException e) {
				LibrisDatabase.setLastException(e);
			}
			cursor = result;
			return result;
		}

		@Override
		public void remove() {
			try {
				root.remove(cursor);
				freeList.add(cursor);
			} catch (Exception e) {
				LibrisDatabase.setLastException(e);
			}
		}
	}


	public File getFilePath() {
		return dataFileName;
	}

	public RecordHeader getHeader(long position) throws DatabaseException {
		RecordHeader hdr = new RecordHeader(dataFile, position);
		return hdr;
	}

}
