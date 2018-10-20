package org.lasalledebain.libris;

import java.io.IOException;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;

import org.lasalledebain.libris.exception.DatabaseError;
import org.lasalledebain.libris.exception.InputException;
import org.lasalledebain.libris.exception.UserErrorException;
import org.lasalledebain.libris.indexes.SignatureFilteredIdList;

public class SignatureFilteredRecordList extends RecordList {

	LibrisDatabase dataBase;
	private Iterable<String> terms;
	int limit;

	public SignatureFilteredRecordList(LibrisDatabase dataBase, Iterable<String> terms) throws UserErrorException, IOException {
		this.dataBase = dataBase;
		this.terms = terms;
		limit = dataBase.getLastRecordId();
	}

	@Override
	public Iterator<Record> iterator() {
		try {
			return new RecordIterator();
		} catch (IOException | UserErrorException e) {
			throw new DatabaseError("Error creating search list", e);
		}
	}

	@Override
	public Record getRecord(int id) throws InputException {
		return dataBase.getRecord(id);
	}

	private class RecordIterator implements Iterator<Record> {
		Record currentRecord;
		private SignatureFilteredIdList filteredIdSource;
		public RecordIterator() throws UserErrorException, IOException {
			filteredIdSource = dataBase.indexMgr.makeSignatureFilteredIdIterator(terms);
			currentRecord = null;
		}

		@Override
		public boolean hasNext() {
			if (Objects.isNull(currentRecord)) {
				int id;
				try {
					id = filteredIdSource.next(limit);
					if (LibrisConstants.NULL_RECORD_ID != id) {
						currentRecord = dataBase.getRecord(id);
					}
				} catch (IOException | InputException e) {
					throw new DatabaseError("Error querying search list", e);

				}
			}
			return Objects.nonNull(currentRecord);
		}

		@Override
		public Record next() {
			if (hasNext()) {
				Record temp = currentRecord;
				currentRecord = null;
				return temp;
			} else {
				throw new NoSuchElementException();
			}
		}

	}
}
