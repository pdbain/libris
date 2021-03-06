package org.lasalledebain.libris.indexes;

import java.util.Iterator;

import org.lasalledebain.libris.ArrayRecordIterator;
import org.lasalledebain.libris.Record;
import org.lasalledebain.libris.RecordList;

class DescendentRecordIterator<RecordType extends Record> implements Iterable<RecordType> {
	RecordList<RecordType> masterList;
	int root;
	private AffiliateList<RecordType> parentList;
	public DescendentRecordIterator(RecordList<RecordType> masterList, int root, AffiliateList<RecordType> parentList) {
		this.masterList = masterList;
		this.parentList = parentList;
		this.root = root;
	}

	@Override
	public Iterator<RecordType> iterator() {
		return new RecordIterator(root);
	}

	class RecordIterator implements Iterator<RecordType> {
		private Iterator<RecordType> myIterator;

		public RecordIterator(int currentRoot) {
			myIterator = new ArrayRecordIterator<RecordType>(masterList, parentList.getChildren(currentRoot)).iterator();
			subIterator = null;
		}

		RecordIterator subIterator;

		@Override
		public boolean hasNext() {
			boolean result;
			if (null == subIterator) {
				result = myIterator.hasNext();
			} else {
				result = subIterator.hasNext();
				if (!result) {
					subIterator = null;
					result = myIterator.hasNext();
				}
			}
			return result;
		}

		@Override
		public RecordType next() {
			RecordType result;
			if (null == subIterator) {
				result = myIterator.next();
				subIterator = new RecordIterator(result.getRecordId());
			} else {
				result = subIterator.next();
				if (!subIterator.hasNext()) {
					subIterator = null;
				}
			}
				return result;
		}

	}
}