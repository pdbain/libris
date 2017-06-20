package org.lasalledebain.libris.index;

import org.lasalledebain.libris.hashfile.HashEntry;
import org.lasalledebain.libris.hashfile.VariableSizeHashEntry;

public abstract class AbstractVariableSizeHashEntry implements VariableSizeHashEntry {

	protected int key;
	protected boolean oversize;

	public AbstractVariableSizeHashEntry() {
		super();
	}

	public void setOversize(boolean oversize) {
		this.oversize = oversize;
	}

	public int getKey() {
		return key;
	}

	public boolean keyEquals(HashEntry other) {
		return key == other.getKey();
	}

	public void setKey(int newKey) {
		key = newKey;
	}

	public int getOverheadLength() {
		return 0;
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

}