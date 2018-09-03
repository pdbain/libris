package org.lasalledebain.libris.hashfile;

public interface VariableSizeEntryFactory<EntryType extends VariableSizeHashEntry> extends NumericKeyEntryFactory<EntryType> {
	public EntryType makeEntry(int key);
}
