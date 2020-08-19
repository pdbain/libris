package org.lasalledebain.libris.ui;
import static java.util.Objects.nonNull;

import java.util.ArrayList;

import javax.swing.table.AbstractTableModel;

import org.lasalledebain.libris.Field;
import org.lasalledebain.libris.GenericDatabase;
import org.lasalledebain.libris.Record;
import org.lasalledebain.libris.Schema;
import org.lasalledebain.libris.exception.InputException;
import org.lasalledebain.libris.exception.LibrisException;
import org.lasalledebain.libris.field.FieldValue;
import org.lasalledebain.libris.index.GroupDef;
import org.lasalledebain.libris.index.GroupDefs;
import org.lasalledebain.libris.index.GroupMember;

@SuppressWarnings("serial")
public class ListLayoutTableModel<RecordType extends Record> extends AbstractTableModel {

	private final LibrisLayout<RecordType> myLayout;
	private RecordType myRecord;
	private final static String[] columnNames = new String[] {"Field", "Main value", "Extra value"};
	private final ArrayList<String> titles;
	private final String[] myFieldIds;
	private final ArrayList<String[]> rows;
	public ListLayoutTableModel(RecordType theRecord, GenericDatabase<RecordType> db, LibrisLayout<RecordType> theLayout) throws LibrisException {
		myRecord = theRecord;
		myLayout = theLayout;
		titles = new ArrayList<>();
		rows = new ArrayList<>();
		Schema mySchema = theLayout.getSchema();
		int numGroups = mySchema.getNumGroups();
		if (numGroups > 0) {
			GroupDefs defs = mySchema.getGroupDefs();
			int groupNum = 0;
			for (GroupDef def: defs) {
				String groupName = def.getFieldTitle();
				GroupMember gm = theRecord.getMember(groupNum);
				if (null == gm) {
					continue;
				}
				int[] affiliations = gm.getAffiliations();
				if (affiliations.length == 0) continue;
				for (int affiliate: affiliations) {
					String recordName = db.getRecordName(affiliate);
					rows.add(new String[] {groupName, recordName, ""});
					groupName = "";
				}
				++groupNum;
			}
		}
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
					rows.add(new String[] {title, v.getValueAsString(), v.getExtraValueAsString()});
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
		return rows.size();
	}

	@Override
	public int getColumnCount() {
		return 3;
	}

	@Override
	public String getValueAt(int rowIndex, int columnIndex) {
		String[] row = rows.get(rowIndex);
		return row[columnIndex];
	}

}
