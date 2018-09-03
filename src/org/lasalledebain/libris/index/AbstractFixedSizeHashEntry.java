package org.lasalledebain.libris.index;

import org.lasalledebain.libris.hashfile.FixedSizeHashEntry;
import org.lasalledebain.libris.hashfile.NumericKeyHashEntry;

public abstract class AbstractFixedSizeHashEntry extends AbstractNumericKeyHashEntry implements FixedSizeHashEntry {

	public AbstractFixedSizeHashEntry(int theKey) {
		super(theKey);
	}

	public void setOversize(boolean oversize) {
		this.oversize = oversize;
	}

	public boolean keyEquals(NumericKeyHashEntry other) {
		return key == other.getKey();
	}

}