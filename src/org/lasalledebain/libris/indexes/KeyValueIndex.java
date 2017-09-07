package org.lasalledebain.libris.indexes;

import java.util.Iterator;

import org.lasalledebain.libris.exception.InputException;

public interface KeyValueIndex<T extends KeyValueTuple> {

	public boolean addElement(T newTuple) throws InputException;

	public void removeElement(String string) throws InputException;

	public T getByName(String key) throws InputException;

	public T getPredecessor(String prefix, boolean inclusive) throws InputException;

	public T getSuccessor(String prefix) throws InputException;

	public Iterator<T> iterator();

	public Iterator<T> iterator(String prefix) throws InputException;

	public  void flush() throws InputException;

	public  void close() throws InputException;

}