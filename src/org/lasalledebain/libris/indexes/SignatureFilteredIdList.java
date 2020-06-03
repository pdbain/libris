package org.lasalledebain.libris.indexes;

import static org.lasalledebain.libris.LibrisConstants.NULL_RECORD_ID;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Collection;
import java.util.Objects;
import java.util.stream.IntStream;

import org.lasalledebain.libris.exception.UserErrorException;
import static org.lasalledebain.libris.util.StringUtils.wordsToHashStream;

public class SignatureFilteredIdList {

	/*
	 * This filters at a given level.  It calls lower levels recursively.
	 */
	protected final SignatureFilteredIdList subFilter;
	int cursor = NULL_RECORD_ID;
	protected final BloomFilterSectionQuery bloomFilter;
	private final int recordsPerSet;
	private boolean first;

	public SignatureFilteredIdList(RandomAccessFile signatureFile, int level, IntStream hashes,
			SignatureFilteredIdList subFilter) throws UserErrorException, IOException {
		this.subFilter = subFilter;
		cursor = NULL_RECORD_ID;
		first = true;
		bloomFilter = new BloomFilterSectionQuery(signatureFile, cursor, hashes, level);
		recordsPerSet = bloomFilter.getRecordsPerSet();
	}

	public SignatureFilteredIdList(RandomAccessFile signatureFile, int level, Collection<String> terms,
			SignatureFilteredIdList subFilter) throws UserErrorException, IOException {
		this.subFilter = subFilter;
		cursor = NULL_RECORD_ID;
		first = true;
		bloomFilter = new BloomFilterSectionQuery(signatureFile, cursor, wordsToHashStream(terms), level);
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