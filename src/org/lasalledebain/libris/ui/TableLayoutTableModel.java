package org.lasalledebain.libris.ui;
import static java.util.Objects.nonNull;

import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.table.AbstractTableModel;

import org.lasalledebain.libris.Field;
import org.lasalledebain.libris.Record;
import org.lasalledebain.libris.RecordList;
import org.lasalledebain.libris.exception.DatabaseError;
import org.lasalledebain.libris.exception.InputException;

@SuppressWarnings("serial")
public class TableLayoutTableModel<RecordType extends Record> extends AbstractTableModel {

	public static final String RECORD_ID = "Record ID";
	private final RecordList<RecordType> myRecList;
	private final LibrisSwingLayout myLayout;
	private final String[] myFieldIds;
	final private ArrayList<RecordType> recList;
	Iterator<RecordType> recIterator;
	public TableLayoutTableModel(RecordList<RecordType> theRecList, LibrisSwingLayout theLayout) {
		myRecList = theRecList;
		myLayout = theLayout;
		myFieldIds = myLayout.getFieldIds();
		recList = new ArrayList<>();
		recIterator = theRecList.iterator();
	}

	@Override
	public String getColumnName(int column) {
		String result;
		if (0 == column) result = RECORD_ID;
		else result = myLayout.getFieldTitle(column-1);
		return result;
	}

	@Override
	public int getRowCount() {
		return myRecList.size();
	}

	@Override
	public int getColumnCount() {
		return myFieldIds.length+ 1;
	}

	@Override
	public String getValueAt(int rowIndex, int columnIndex) {
		String result = "<blank>";
		while ((recList.size() <= rowIndex) && recIterator.hasNext())
			recList.add(recIterator.next());
		if (recList.size() > rowIndex) try {
			Record rec = recList.get(rowIndex);
			if (0 == columnIndex) {
				result = Integer.toString(rec.getRecordId());
			} else {
				Field theField = rec.getField(myFieldIds[columnIndex-1]);
				result = nonNull(theField)? theField.getValuesAsString(): "<blank>";
			}
		} catch (InputException e) {
			throw new DatabaseError("Error getting record at "+rowIndex, e);
		}
		return result;
	}

	public int getRowForRecord(int recId) {
		int i = 0;
		while ((i < recList.size()) && (recList.get(i).getRecordId() != recId)) ++i;
		if (i >= recList.size()) {
			i = 0;
			while (recIterator.hasNext()) {
				RecordType rec = recIterator.next();
				recList.add(rec);
				if (rec.getRecordId() == recId) {
					i = recList.size() - 1;
				}
			}
		}
		return i;
	}

}
