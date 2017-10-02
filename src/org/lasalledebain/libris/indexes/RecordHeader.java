package org.lasalledebain.libris.indexes;

import java.io.ByteArrayOutputStream;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Iterator;

import org.lasalledebain.libris.LibrisDatabase;
import org.lasalledebain.libris.exception.DatabaseException;


/**
 * header
 * 	4 bytes each forward and backward pointer
 * 	4 bytes size, excluding header and size fields
 */
public class RecordHeader implements Iterable<RecordHeader>{
	public void setReusable(boolean reusable) {
		this.reusable = reusable;
	}


	private static final int headerLength = 3*4;
	public static int getHeaderLength() {
		return headerLength;
	}

	protected long dataSize;
	protected long nextHeader;
	protected long prevHeader;
	private long filePosition;
	private RandomAccessFile recordsFileStore;
	private boolean reusable;
	public RecordHeader(RandomAccessFile recFile) {
		this.nextHeader = 0;
		this.prevHeader = 0;
		this.dataSize = 0;
		this.filePosition = -1;
		recordsFileStore = recFile;
		reusable = false;
	}

	public RecordHeader(RandomAccessFile recFile, long headerPosition) throws DatabaseException {
		this(recFile);
		this.filePosition = headerPosition;
		try {
			recordsFileStore.seek(headerPosition);
			DataInput headerSource = recFile;
			readHeader(headerSource);
		} catch (IOException e) {
			throw new DatabaseException("Error accessing records file", e);
		}
	}

	public RecordHeader(DataInput headerSource) throws IOException {
		readHeader(headerSource);
	}
	/**
	 * @param headerSource
	 * @throws IOException
	 */
	private void readHeader(DataInput headerSource) throws IOException {
		prevHeader = headerSource.readInt();
		nextHeader = headerSource.readInt();
		dataSize = headerSource.readInt() & 0x0ffffffff;
	}

	public static RecordHeader createRecordHeaderFromDataPosition(
			RandomAccessFile recFile, long dataPosition) throws IOException, DatabaseException {
		return new RecordHeader(recFile, dataPosition-headerLength);
	}

	public boolean isReusable() {
		return reusable;
	}

	public void add(RecordHeader hdr) throws DatabaseException {
		if (0 == nextHeader) {
			nextHeader = hdr.getFilePosition();
		} else { /* make the last element in the list point to the new element */
			RecordHeader lastHeader = new RecordHeader(recordsFileStore, prevHeader); 
			lastHeader.nextHeader = hdr.getFilePosition();
			lastHeader.save();
		}
		hdr.prevHeader = prevHeader;
		hdr.nextHeader = 0;
		prevHeader = hdr.getFilePosition();
		hdr.save();
		save();
	}

	public void remove(RecordHeader current) throws DatabaseException {
		long currPrev = current.getPrev();
		long currNext = current.getNext();
		RecordHeader nextHdr = (0 == currNext)? null: new RecordHeader(recordsFileStore, currNext);
		RecordHeader prevHdr = (0 == currPrev)? null: new RecordHeader(recordsFileStore, currPrev);
		if (null != nextHdr) {
			nextHdr.setPrev(currPrev);
			nextHdr.save();
		}
		if (null != prevHdr) {
			prevHdr.setNext(currNext);
			prevHdr.save();
		}
		if (current.getFilePosition() == nextHeader) {
			/* deleting the first element */
			nextHeader = currNext;
		}
		if (current.getFilePosition() == prevHeader) {
			/* deleting the last element */
			prevHeader = currPrev;
		}
		save();
	}

	
	public static byte[] formatHeader(int prev, int next, int dataSize) throws IOException {
		ByteArrayOutputStream result = new ByteArrayOutputStream(12);
		DataOutputStream resultStream = new DataOutputStream(result);
		resultStream.writeInt(prev);
		resultStream.writeInt(next);
		resultStream.writeInt(dataSize);
		return result.toByteArray();
	}
	
	public void save() throws DatabaseException  {	
		try {
			recordsFileStore.seek(filePosition);
			byte[] hdrBytes = toByteArray();
			recordsFileStore.write(hdrBytes);
		} catch (IOException e) {
			throw new DatabaseException("Error accessing database file", e);
		}
	}

	/**
	 * @return
	 * @throws IOException
	 */
	public byte[] toByteArray() throws IOException {
		byte hdrBytes[] = formatHeader((int) prevHeader, (int) nextHeader, (int) dataSize);
		return hdrBytes;
	}

	public long getFilePosition() {
		return filePosition;
	}

	public long getDataPosition() {
		return filePosition+headerLength;
	}
	
	public DataOutput getOutput() throws IOException {
		recordsFileStore.seek(getDataPosition());
		return recordsFileStore;
	}

	public DataInput getInput() throws IOException {
		recordsFileStore.seek(getDataPosition());
		return recordsFileStore;
	}

	@Override
	public String toString() {
		return "filePosition:"+filePosition+" prevHeader:"+prevHeader+" nextHeader:"+nextHeader;
	}

	public boolean hasNext() {
		return 0 != nextHeader;
	}

	public void setFilePosition(long filePosition) {
		this.filePosition = filePosition;
	}

	public long getSize() {
		return dataSize;
	}
	public void setSize(long size) {
		this.dataSize = size;
	}
	public long getNext() {
		return nextHeader;
	}
	public void setNext(long next) {
		this.nextHeader = next;
	}
	public long getPrev() {
		return prevHeader;
	}
	public void setPrev(long prev) {
		this.prevHeader = prev;
	}


	@Override
	public Iterator<RecordHeader> iterator() {
		return new RecordHeaderIterator(this);
	}


	protected class RecordHeaderIterator implements Iterator<RecordHeader> {
		private RecordHeader root;
		private RecordHeader cursor;
		/**
		 * @param root
		 */
		public RecordHeaderIterator(RecordHeader root) {
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
					result = new RecordHeader(recordsFileStore, root.getNext());
				} else {
					if (0 != cursor.getNext()) {
						result = new RecordHeader(recordsFileStore, cursor.getNext());
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
			} catch (Exception e) {
				LibrisDatabase.setLastException(e);
			}
		}
	}
}
