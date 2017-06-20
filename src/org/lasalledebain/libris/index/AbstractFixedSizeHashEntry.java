package org.lasalledebain.libris.index;

import org.lasalledebain.libris.hashfile.FixedSizeHashEntry;
import org.lasalledebain.libris.hashfile.HashEntry;
import org.lasalledebain.libris.hashfile.VariableSizeHashEntry;

public abstract class AbstractFixedSizeHashEntry implements FixedSizeHashEntry {

	protected int key;
	protected boolean oversize;

	public AbstractFixedSizeHashEntry() {
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
	
	@Override
	public int compareTo(Object comparand) {
		if (this == comparand) {
			return 0;
		} else if (this.getClass().isInstance(comparand)) {
			return Integer.compare(((AbstractFixedSizeHashEntry)comparand).key, key);
		} else {
			return -1;
		}
	}

}