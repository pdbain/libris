package org.lasalledebain.libris.ui;
import static java.util.Objects.nonNull;

import java.util.ArrayList;

import javax.swing.table.AbstractTableModel;

import org.lasalledebain.libris.Field;
import org.lasalledebain.libris.Record;
import org.lasalledebain.libris.exception.FieldDataException;
import org.lasalledebain.libris.exception.InputException;
import org.lasalledebain.libris.exception.LibrisException;
import org.lasalledebain.libris.field.FieldValue;

@SuppressWarnings("serial")
public class ListLayoutTableModel<RecordType extends Record> extends AbstractTableModel {

	private final Layout myLayout;
	private RecordType myRecord;
	private final static String[] columnNames = new String[] {"Field", "Main value", "Extra value"};
	private final ArrayList<FieldValue> values;
	private final ArrayList<String> titles;
	private final String[] myFieldIds;
	public ListLayoutTableModel(RecordType theRecord, Layout theLayout) throws LibrisException {
		myRecord = theRecord;
		myLayout = theLayout;
		values = new ArrayList<>();
		titles = new ArrayList<>();
		myFieldIds = myLayout.getFieldIds();
		for (int fieldnum = 0; fieldnum < myFieldIds.length; ++fieldnum) {
			String id = myFieldIds[fieldnum];
			Field f;
			try {
				f = myLayout.getField(myRecord, id);
			} catch (InputException e) {
				f = null;
			}
			if (nonNull(f)) {
				String title = myLayout.getFieldTitle(fieldnum);
				for (FieldValue v: f.getFieldValues()) {
					titles.add(title);
					title = "";
					values.add(v);
				}
			}
		}
	}

	@Override
	public String getColumnName(int column) {
		return columnNames[column];
	}

	@Override
	public int getRowCount() {
		return values.size();
	}

	@Override
	public int getColumnCount() {
		return 3;
	}

	@Override
	public String getValueAt(int rowIndex, int columnIndex) {
		String result;
		switch (columnIndex) {
		case 0: result = titles.get(rowIndex); break;
		case 1: try {
			result = values.get(rowIndex).getMainValueAsString();
		} catch (FieldDataException e) {
			result = "<unknown>";
		} break;
		case 2: result = values.get(rowIndex).getExtraValueAsKey(); break;
		default: result = "<unknown>";
		}
		return result;
	}

}
