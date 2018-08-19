package org.lasalledebain.libris.hashfile;

public interface VariableSizeEntryFactory<T extends VariableSizeHashEntry> extends NumericKeyEntryFactory {
	public T makeEntry(int key);

}
