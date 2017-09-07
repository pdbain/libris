package org.lasalledebain.libris.index;

import java.io.ByteArrayOutputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;

import org.lasalledebain.libris.hashfile.HashBucket;
import org.lasalledebain.libris.hashfile.HashEntry;
import org.lasalledebain.libris.hashfile.VariableSizeHashEntry;

public abstract class AbstractVariableSizeHashEntry extends AbstractHashEntry implements VariableSizeHashEntry {

	public AbstractVariableSizeHashEntry(int key) {
		super(key);
	}

	@Override
	public byte[] getData() throws IOException {
		ByteArrayOutputStream b = new ByteArrayOutputStream(getDataLength());
		DataOutput s = new DataOutputStream(b);
		writeData(s);
		return b.toByteArray();
	}

	@Override
	public int getOverheadLength() {
		return 4 /* key */ + 2 /* offset */;
	}

	public int getTotalLength() {
		int result = getEntryLength() + getOverheadLength();
		return result;
	}

	@Override
	public int getEntryLength() {
		return isOversize()? OVERSIZE_HASH_ENTRY_LENGTH : getDataLength();
	}
	
	@Override
	public int compareTo(Object comparand) {
		if (this == comparand) {
			return 0;
		} else if (this.getClass().isInstance(comparand)) {
			return Integer.compare(((AbstractVariableSizeHashEntry)comparand).key, key);
		} else {
			return -1;
		}
	}
	public boolean isOversize() {
		return getDataLength() >= (HashBucket.BUCKET_SIZE/2);
	}

}