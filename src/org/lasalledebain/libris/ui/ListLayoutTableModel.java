package org.lasalledebain.libris.ui;
import javax.swing.table.AbstractTableModel;

import org.lasalledebain.libris.Field;
import org.lasalledebain.libris.GenericDatabase;
import org.lasalledebain.libris.LibrisDatabase;
import org.lasalledebain.libris.Record;
import org.lasalledebain.libris.RecordList;
import org.lasalledebain.libris.exception.DatabaseError;
import org.lasalledebain.libris.exception.InputException;
import org.lasalledebain.libris.ui.Layout;
import static java.util.Objects.nonNull;

import java.util.ArrayList;
import java.util.Iterator;

@SuppressWarnings("serial")
public class ListLayoutTableModel<RecordType extends Record> extends AbstractTableModel {

	private final RecordList<RecordType> myRecList;
	private final Layout myLayout;
	private final String[] myFields;
	final private ArrayList<RecordType> recList;
	Iterator<RecordType> recIterator;
	public ListLayoutTableModel(RecordList<RecordType> theRecList, Layout theLayout) {
		myRecList = theRecList;
		myLayout = theLayout;
		myFields = myLayout.getFieldIds();
		recList = new ArrayList<>();
		recIterator = theRecList.iterator();
	}

	@Override
	public String getColumnName(int column) {
		return (0 == column) ? "Record": myLayout.getFieldTitle(myFields[column-1]);
	}

	@Override
	public int getRowCount() {
		return myRecList.size();
	}

	@Override
	public int getColumnCount() {
		return myFields.length+1;
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
			Field f = rec.getField(myFields[columnIndex-1]);
			if (nonNull(f)) result = f.getValuesAsString();
			}
		} catch (InputException e) {
			throw new DatabaseError("Error getting record at "+rowIndex, e);
		}
		return result;
	}

}
