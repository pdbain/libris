/**
 * 
 */
package org.lasalledebain.libris.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import javax.swing.ListModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import org.lasalledebain.libris.Record;
import org.lasalledebain.libris.RecordId;

public class RandomAccessBrowserList implements
	ListModel {

		HashSet<ListDataListener> listeners;
		ArrayList<BrowserRow> rowList;
		int startIndex, endIndex;
		HashMap<RecordId, BrowserRow> rowMap;
		private String[] fieldIds;
		public RandomAccessBrowserList(String[] fieldIds) {
			this.fieldIds = fieldIds;
			listeners = new HashSet<ListDataListener>(1);
			rowList = new ArrayList<BrowserRow>(BrowserWindow.LIST_LIMIT);
			rowMap = new HashMap<RecordId, BrowserRow>(BrowserWindow.LIST_LIMIT);
			startIndex = endIndex = 0;
		}
		public int add(Record rec) {
			int posn = 0;
			final RecordId recId = rec.getRecordId();
			if (!rowMap.containsKey(recId)) {
				BrowserRow row = new BrowserRow(rec, fieldIds);
				if (rowList.size() == BrowserWindow.LIST_LIMIT) {
					rowList.remove(0);
				}
				rowList.add(row);
				posn = rowList.size() - 1;
				rowMap.put(row.getRecordId(), row);
				ListDataEvent evt = new ListDataEvent(this, ListDataEvent.INTERVAL_ADDED, posn, posn);
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

		@Override
		public Object getElementAt(int index) {
			return rowList.get(index);
		}

		@Override
		public int getSize() {
			return rowList.size();
		}

		@Override
		public void removeListDataListener(ListDataListener l) {
			listeners.remove(l);
		}

}