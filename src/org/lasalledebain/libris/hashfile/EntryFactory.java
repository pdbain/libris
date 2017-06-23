package org.lasalledebain.libris.hashfile;

@SuppressWarnings("unchecked")
public interface EntryFactory <T extends HashEntry> {
	public T makeEntry();
}
