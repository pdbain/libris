package org.lasalledebain.libris.index;

public abstract class AbstractHashEntry {

	protected final int key;
	protected boolean oversize;
	public AbstractHashEntry(int key) {
		super();
		this.key = key;
	}


	public int getKey() {
		return key;
	}


	public void setOversize(boolean oversize) {
		this.oversize = oversize;
	}


	public abstract boolean isOversize();

}
