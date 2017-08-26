package org.lasalledebain.libris.hashfile;

public interface FixedSizeEntryFactory<T extends FixedSizeHashEntry> extends EntryFactory {
	public int getEntrySize();

}
