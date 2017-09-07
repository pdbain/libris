package org.lasalledebain.libris.hashfile;

public interface VariableSizeEntryFactory<T extends VariableSizeHashEntry> extends EntryFactory {
	public T makeEntry(int key);

}
