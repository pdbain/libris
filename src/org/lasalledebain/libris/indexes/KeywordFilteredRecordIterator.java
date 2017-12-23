package org.lasalledebain.libris.indexes;

import static org.lasalledebain.libris.LibrisConstants.NULL_RECORD_ID;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Objects;

import org.lasalledebain.libris.exception.UserErrorException;;

public class KeywordFilteredRecordIterator {

	/*
	 * This filters at a given level.  It calls lower levels recursively.
	 */
	protected final KeywordFilteredRecordIterator subFilter;
	int cursor = NULL_RECORD_ID;
	protected final BloomFilterSectionQuery bloomFilter;
	private final int recordsPerSet;
	private boolean first;

	public KeywordFilteredRecordIterator(RandomAccessFile signatureFile, int level, Iterable<String> terms,
			KeywordFilteredRecordIterator subFilter) throws UserErrorException, IOException {
		this.subFilter = subFilter;
		cursor = NULL_RECORD_ID;
		first = true;
		bloomFilter = new BloomFilterSectionQuery(signatureFile, cursor, terms, level);
		recordsPerSet = bloomFilter.getRecordsPerSet();
	}

	public int next(int limit) throws IOException {
		if (Objects.isNull(subFilter)) {
			return scanForNextMatch(limit);
		} else {
			int result = NULL_RECORD_ID;
			do {
				int subResult = subFilter.next(bloomFilter.lastIdInRange(limit));
				if (NULL_RECORD_ID != subResult) {
					return subResult;
				}
				result = scanForNextMatch(limit);
			} while (NULL_RECORD_ID != result);
			return NULL_RECORD_ID;
		}
	}
	
	public int getRecordsPerRange() {
		return bloomFilter.recordsPerSet;
	}

	private int scanForNextMatch(int limit) throws IOException {
		if (first) {
			first = false;
			bloomFilter.load(1);
			if (1 == recordsPerSet) {
				++cursor;
			}
			if (bloomFilter.match()) {
				return 1;
			}
		}
		while ((cursor + recordsPerSet) <= limit) {
			cursor += recordsPerSet;
			bloomFilter.load(cursor);
			if (bloomFilter.match()) {
				return cursor;
			}
		}
		return NULL_RECORD_ID;
	}
}