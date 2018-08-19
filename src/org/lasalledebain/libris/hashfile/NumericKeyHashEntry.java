package org.lasalledebain.libris.hashfile;

/**
 * HashEntry objects are immutable.
 *
 * @param <T> subtype
 */
public interface NumericKeyHashEntry extends Comparable<NumericKeyHashEntry>, HashEntry{

	int getKey();
	
}
