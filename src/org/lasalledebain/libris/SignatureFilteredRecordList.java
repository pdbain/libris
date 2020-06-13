package org.lasalledebain.libris;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;

import org.lasalledebain.libris.exception.DatabaseError;
import org.lasalledebain.libris.exception.InputException;
import org.lasalledebain.libris.exception.UserErrorException;
import org.lasalledebain.libris.indexes.SignatureFilteredIdList;

public class SignatureFilteredRecordList<RecordType extends Record> extends RecordList<RecordType> {

	GenericDatabase<RecordType> dataBase;
	private Collection<String> terms;
	int limit;

	public SignatureFilteredRecordList(GenericDatabase<RecordType> dataBase, Collection<String> terms) throws UserErrorException, IOException {
		this.dataBase = dataBase;
		this.terms = terms;
		limit = dataBase.getLastRecordId();
	}

	@Override
	public Iterator<RecordType> iterator() {
		try {
			return new RecordIterator();
		} catch (IOException | UserErrorException e) {
			throw new DatabaseError("Error creating search list", e);
		}
	}

	@Override
	public RecordType getRecord(int id) throws InputException {
		return dataBase.getRecord(id);
	}

	private class RecordIterator implements Iterator<RecordType> {
		RecordType currentRecord;
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
		public RecordType next() {
			if (hasNext()) {
				RecordType temp = currentRecord;
				currentRecord = null;
				return temp;
			} else {
				throw new NoSuchElementException();
			}
		}

	}

	@Override
	public int size() {
		return -1;
	}
}
