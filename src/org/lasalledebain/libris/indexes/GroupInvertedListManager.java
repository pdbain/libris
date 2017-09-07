package org.lasalledebain.libris.indexes;

/**
 * Index of members of each group.
 * Inverted list format:
 * number of entries: 2 bytes
 * sorted list of record IDs, 4 bytes each
 * list of relatives count: 2 bytes per entry, same order as record IDs, terminated by 0
 *  	offset to start of child list - 12 MSbits
 *  	child count - 4 MSbits
 * child and affiliate lists, same order as record IDs
 *  	child ID list (4 bytes * child count)
 *  	affiliate ID list (4 bytes * affiliate count)
 *  if child or affiliate count is 15 or greater, the associated list is a 4-byte pointer to the location in the overflow file
 * 	
 */

public class GroupInvertedListManager {

}
