package org.lasalledebain.libris.hashfile;

import java.io.DataInput;
import java.io.IOException;

public interface StringKeyEntryFactory <EntryType extends StringKeyHashEntry> extends EntryFactory<EntryType> {
	public EntryType makeEntry(DataInput backingStore) throws IOException;
}
