package org.lasalledebain.libris.index;

public abstract class AbstractNumericKeyHashEntry  extends AbstractHashEntry {

	protected final int key;
	protected boolean oversize;
	public AbstractNumericKeyHashEntry(int key) {
		super();
		this.key = key;
	}


	public int getKey() {
		return key;
	}


	public void setOversize(boolean oversize) {
		this.oversize = oversize;
	}



}
