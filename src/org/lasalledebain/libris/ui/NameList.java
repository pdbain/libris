package org.lasalledebain.libris.ui;

import static org.lasalledebain.libris.RecordId.NULL_RECORD_ID;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Vector;

import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.ListSelectionModel;

import org.lasalledebain.libris.DatabaseRecord;
import org.lasalledebain.libris.GenericDatabase;
import org.lasalledebain.libris.Record;
import org.lasalledebain.libris.exception.DatabaseError;
import org.lasalledebain.libris.exception.FieldDataException;
import org.lasalledebain.libris.exception.InputException;
import org.lasalledebain.libris.field.FieldValue;
import org.lasalledebain.libris.index.GroupDef;
import org.lasalledebain.libris.indexes.KeyIntegerTuple;;

public class NameList extends GuiControl {

	private final JList<KeyIntegerTuple> control;
	private final GroupDef grpDef;
	private final GenericDatabase<DatabaseRecord> dBase;
	Vector <KeyIntegerTuple> affiliateInfo;
	private final LibrisWindowedUi windowedUi;
	final Record currentRecord;
	public static final KeyIntegerTuple NULL_ID_TUPLE = makeNullTuple();

	private static KeyIntegerTuple makeNullTuple() {
		try {
			return new KeyIntegerTuple("<none>", NULL_RECORD_ID);
		} catch (InputException e) {
			throw new DatabaseError("Unexpected exception", e);
		}
	}

	public NameList(LibrisWindowedUi ui, GenericDatabase<DatabaseRecord> db, Record rec, GroupDef gd, boolean editable) throws InputException {
		super(0, 0, editable);
		grpDef = gd;
		windowedUi = ui;
		dBase = db;
		currentRecord = rec;
		affiliateInfo = new Vector<KeyIntegerTuple>(0);
		control = displayControls();
		setValues(rec, gd);
	}

	public void setValues(Record rec, GroupDef gd) throws InputException {
		int[] affiliates = rec.getAffiliates(gd.getGroupNum());
		setFieldValues(affiliates);
	}

	private void setFieldValues(int[] affiliates) throws InputException {
		int affLen = affiliates.length;
		affiliateInfo.setSize(affLen);
		if (0 == affLen) {
			affiliateInfo.add(NULL_ID_TUPLE);
		} else {
			for (int i = 0; i < affLen; ++i) {
				int recordNumber = affiliates[i];
				affiliateInfo.setElementAt(new KeyIntegerTuple(dBase.getRecordName(recordNumber), recordNumber), i);
			}
		}
	}

	@Override
	protected JList <KeyIntegerTuple> displayControls() {
		JList<KeyIntegerTuple> tempControl = new JList<KeyIntegerTuple>(affiliateInfo);
		if (affiliateInfo.size() > 0) {
			tempControl.setSelectedIndex(0);
			// TODO add menu item to edit affiliate list
		}
		tempControl.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		GroupMouseListener listener = new GroupMouseListener();
		tempControl.addMouseListener(listener);
		return tempControl;
	}

	@Override
	public FieldValue getFieldValue() throws FieldDataException {
		return null;
	}

	@Override
	public JComponent getGuiComponent() {
		return control;
	}

	@Override
	public void setFieldValue(String controlValue) throws FieldDataException {
		try {
			int id = Integer.parseInt(controlValue);
			setFieldValues(new int[] {id});
		} catch (NumberFormatException e) {
			throw new FieldDataException("Illegal record ID: ", e);
		} catch (InputException e) {
			throw new FieldDataException("Error reading record ID: ", e);
		}
	}

	@Override
	public void setFieldValue(FieldValue newValue) throws FieldDataException {
		int id = newValue.getValueAsInt();
		try {
			setFieldValues(new int[] {id});
		} catch (InputException e) {
			throw new FieldDataException("Error reading record ID: ", e);
			}
	}

	@Override
	public int getNumValues() {
		return affiliateInfo.size();
	}

	class GroupMouseListener implements MouseListener {

		private GuiControl uiField;
		public GroupMouseListener() {
			uiField = NameList.this;
		}

		@Override
		public void mouseClicked(MouseEvent evt) {
			int clickCount = evt.getClickCount();
			windowedUi.setSelectedGroup(grpDef);
			switch (clickCount) {
			case 1: 
				break;
			case 2: 
				new AffiliateEditor(currentRecord, uiField, windowedUi, dBase.getNamedRecordIndex(), affiliateInfo, control, grpDef);
				break;
			} 
		}

		@Override
		public void mouseEntered(MouseEvent arg0) {
			return;
		}

		@Override
		public void mouseExited(MouseEvent arg0) {
			return;
		}

		@Override
		public void mousePressed(MouseEvent arg0) {
			return;
		}

		@Override
		public void mouseReleased(MouseEvent arg0) {
			return;
		}

	}

	@Override
	public boolean isEmpty() {
		return affiliateInfo.size() > 0;
	}

	@Override
	public void setEmpty(boolean empty) {
		affiliateInfo.setSize(0);
	}

	@Override
	protected void copyValuesFromControls() {
		return;
	}
}
