package org.lasalledebain.libris.indexes;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Iterator;

import org.lasalledebain.libris.exception.DatabaseException;


/**
 * Manages space in the overflow list of group inverted index
 */
public class BucketOverflowFileManager {

	FileSpaceManager overflowFileManager;
	public BucketOverflowFileManager(FileSpaceManager overflowFileManager) {
		this.overflowFileManager = overflowFileManager;
	}

	public long put(byte[] data) throws DatabaseException {
		RecordHeader hdr = overflowFileManager.addNew(data.length);
		DataOutput op;
		try {
			op = hdr.getOutput();
			op.writeInt(data.length);
			op.write(data);
		} catch (IOException e) {
			throw new DatabaseException("Error writing hash bucket overflow "+overflowFileManager.getFilePath().getAbsolutePath(), e);
		}
		return hdr.getFilePosition();
	}
	
	public long update(long position, byte[] data) throws DatabaseException {
		RecordHeader hdr = overflowFileManager.getHeader(position);
		long len = hdr.getSize();
		long result = position;
		if (len >= data.length) {
			try {
				DataOutput op = hdr.getOutput();
				op.writeInt(data.length);
				op.write(data);
			} catch (IOException e) {
				throw new DatabaseException("Error writing hash bucket overflow "+overflowFileManager.getFilePath().getAbsolutePath(), e);
			}
		} else {
			remove(position);
			result = put(data);
		}
		
		return result;
	}
	
	public byte[] get(long position) throws DatabaseException {
		RecordHeader hdr = overflowFileManager.getHeader(position);
		DataInput ip;
		try {
			ip = hdr.getInput();
			long len = ip.readInt();
			byte[] buf = new byte[(int) len];
			ip.readFully(buf);
			return buf;
		} catch (IOException e) {
			throw new DatabaseException("Error reading bucket overflow data", e);
		}
	}
	
	public void remove(long position) throws DatabaseException {
		RecordHeader hdr = overflowFileManager.getHeader(position);
		overflowFileManager.remove(hdr);
	}
	public void flush() throws DatabaseException {
		overflowFileManager.flush();
	}

	public Iterator<Long> iterator() {
			return new OversizeEntryIterator(overflowFileManager.iterator());
		};

		private class OversizeEntryIterator implements Iterator<Long> {

			Iterator<RecordHeader> hdrIter;
			public OversizeEntryIterator(Iterator<RecordHeader> iterator) {
				hdrIter = iterator;
			}

			@Override
			public boolean hasNext() {
				return hdrIter.hasNext();
			}

			@Override
			public Long next() {
				RecordHeader hdr = hdrIter.next();
				return (null == hdr) ? null : hdr.getFilePosition();
			}

			@Override
			public void remove() {
				return;

			}
		}
}