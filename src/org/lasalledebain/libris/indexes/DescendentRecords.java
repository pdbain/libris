package org.lasalledebain.libris.indexes;

import java.util.Iterator;

import org.lasalledebain.libris.ArrayRecordIterator;
import org.lasalledebain.libris.Record;
import org.lasalledebain.libris.RecordList;

class DescendentRecordIterator implements Iterable<Record> {
	RecordList masterList;
	int root;
	private AffiliateList parentList;
	public DescendentRecordIterator(RecordList masterList, int root, AffiliateList parentList) {
		this.masterList = masterList;
		this.parentList = parentList;
		this.root = root;
	}

	@Override
	public Iterator<Record> iterator() {
		return new RecordIterator(root);
	}

	class RecordIterator implements Iterator<Record> {
		private Iterator<Record> myIterator;

		public RecordIterator(int currentRoot) {
			myIterator = new ArrayRecordIterator(masterList, parentList.getChildren(currentRoot)).iterator();
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
		public Record next() {
			Record result;
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