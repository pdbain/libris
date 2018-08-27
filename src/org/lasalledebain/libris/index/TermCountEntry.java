package org.lasalledebain.libris.index;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.nio.ByteBuffer;

import org.lasalledebain.libris.hashfile.StringKeyEntryFactory;
import org.lasalledebain.libris.hashfile.StringKeyHashEntry;
import org.lasalledebain.libris.util.ByteArraySlice;

/**
 * File format is:
 * 2 bytes: entry length
 * entry length - 4 bytes: term string
 * 4 bytes: term count
 *
 */
public class TermCountEntry extends StringKeyHashEntry{
	int termCount;

	public TermCountEntry(ByteArraySlice src) {
		termCount = src.getInt(src.getLength() - 4);
		keyBytes = src;
		keyBytes.setLength(src.getLength() - 4);
	}

	public TermCountEntry(String term, int initialCount) {
		keyBytes = new ByteArraySlice(term.getBytes());
		termCount = initialCount;
	}

	/**
	 * @return the termCount
	 */
	public int getTermCount() {
		return termCount;
	}

	/**
	 * @param termCount the termCount to set
	 */
	public void setTermCount(int termCount) {
		this.termCount = termCount;
	}
	
	public int incrementCount() {
		return ++termCount;
	}

	@Override
	public void writeData(DataOutput backingStore) throws IOException {
		backingStore.write(keyBytes.getBaseArray(), keyBytes.getOffset(), keyBytes.getLength());
		backingStore.writeInt(termCount);
	}

	@Override
	public int getTotalLength() {
		return getOverheadLength() + getEntryLength();
	}

	@Override
	public int getOverheadLength() {
		return 4;
	}

	@Override
	public int getEntryLength() {
		return getDataLength();
	}

	@Override
	public boolean isOversize() {
		return false;
	}

	@Override
	public void setOversize(boolean oversize) {
		return;
	}

	@Override
	public int getDataLength() {
		return keyBytes.getLength() + 4;
	}

	public static class TermCountEntryFactory implements StringKeyEntryFactory<TermCountEntry> {

		@Override
		public TermCountEntry makeEntry(int key, byte[] dat) {
			return null;
		}

		@Override
		public TermCountEntry makeEntry(int key, ByteBuffer src, int len) {
			return null;			
		}

		@Override
		public TermCountEntry makeEntry(DataInput backingStore) throws IOException {
			return null;
		}

		public TermCountEntry makeEntry(byte[] baseArray, int offset, int length) {
			return new TermCountEntry(new ByteArraySlice(baseArray, offset, length));
		}

	}
}
