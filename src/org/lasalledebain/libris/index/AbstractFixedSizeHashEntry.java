package org.lasalledebain.libris.index;

import org.lasalledebain.libris.hashfile.FixedSizeHashEntry;
import org.lasalledebain.libris.hashfile.HashEntry;

public abstract class AbstractFixedSizeHashEntry extends AbstractHashEntry implements FixedSizeHashEntry {

	public AbstractFixedSizeHashEntry(int theKey) {
		super(theKey);
	}

	public void setOversize(boolean oversize) {
		this.oversize = oversize;
	}

	public boolean keyEquals(HashEntry other) {
		return key == other.getKey();
	}

	@Override
	public int compareTo(Object comparand) {
		if (this == comparand) {
			return 0;
		} else if (this.getClass().isInstance(comparand)) {
			return Integer.compare(((AbstractHashEntry)comparand).key, key);
		} else {
			return -1;
		}
	}

}