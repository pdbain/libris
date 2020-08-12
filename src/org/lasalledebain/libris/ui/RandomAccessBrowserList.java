/**
 * 
 */
package org.lasalledebain.libris.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import javax.swing.ListModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import org.lasalledebain.libris.Record;
import org.lasalledebain.libris.RecordList;
import org.lasalledebain.libris.exception.InputException;

public class RandomAccessBrowserList<RecordType extends Record> extends RecordList<RecordType> implements ListModel {

	HashSet<ListDataListener> listeners;
	ArrayList<RecordInfo<RecordType>> recList;

	int startIndex, endIndex;
	HashMap<Integer, RecordInfo<RecordType>> rowMap;
	private String[] fieldIds;
	public RandomAccessBrowserList(String[] fieldIds) {
		this.fieldIds = fieldIds;
		listeners = new HashSet<ListDataListener>(1);
		recList = new ArrayList<RecordInfo<RecordType>>(BrowserWindow.LIST_LIMIT);
		rowMap = new HashMap<Integer, RecordInfo<RecordType>>(BrowserWindow.LIST_LIMIT);
		startIndex = endIndex = 0;
	}
	public int add(Record rec) {
		int posn = 0;
		final Integer recId = rec.getRecordId();
		if (!rowMap.containsKey(recId)) {
			RecordInfo row = new RecordInfo(rec, fieldIds);
			if (recList.size() == BrowserWindow.LIST_LIMIT) {
				recList.remove(0);
			}
			recList.add(row);
			posn = recList.size() - 1;
			rowMap.put(row.getRecordId(), row);
			ListDataEvent evt = new ListDataEvent(this, ListDataEvent.INTERVAL_ADDED, posn, posn);
			for (ListDataListener l: listeners) {
				l.contentsChanged(evt);
			}
		}
		return(posn);
	}

	public int remove(Record rec) {
		int posn = 0;
		final int recId = rec.getRecordId();
		if (rowMap.containsKey(recId)) {
			RecordInfo victim = rowMap.remove(recId);
			int victimPos = recList.indexOf(victim);
			recList.remove(victimPos);
			posn = recList.size() - 1;
			ListDataEvent evt = new ListDataEvent(this, ListDataEvent.INTERVAL_REMOVED, victimPos, victimPos);
			for (ListDataListener l: listeners) {
				l.contentsChanged(evt);
			}
		}
		return(posn);
	}
	@Override
	public void addListDataListener(ListDataListener l) {
		listeners.add(l);
	}

	public ArrayList<RecordInfo<RecordType>> getRecList() {
		return recList;
	}

	@Override
	public RecordInfo<RecordType> getElementAt(int index) {
		return recList.get(index);
	}

	@Override
	public int getSize() {
		return recList.size();
	}

	@Override
	public void removeListDataListener(ListDataListener l) {
		listeners.remove(l);
	}
	@Override
	public Iterator<RecordType> iterator() {
		return new RecordIterator();
	}

	@Override
	public RecordType getRecord(int id) throws InputException {
		return rowMap.get(id).getRecord();
	}

	@Override
	public int size() {
		return recList.size();
	}

	private class RecordIterator implements Iterator<RecordType> {
		int index;
		public RecordIterator() {
			index = 0;
		}

		@Override
		public boolean hasNext() {
			return index < recList.size();
		}

		@Override
		public RecordType next() {
			return recList.get(index++).getRecord();
		}

	}
}