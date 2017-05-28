package org.lasalledebain.libris.ui;

import java.awt.Component;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Vector;

import javax.swing.JList;
import javax.swing.ListSelectionModel;

import org.lasalledebain.libris.LibrisDatabase;
import org.lasalledebain.libris.Record;
import org.lasalledebain.libris.RecordId;
import org.lasalledebain.libris.exception.FieldDataException;
import org.lasalledebain.libris.exception.InputException;
import org.lasalledebain.libris.field.FieldIntValue;
import org.lasalledebain.libris.field.FieldValue;
import org.lasalledebain.libris.index.GroupDef;
import org.lasalledebain.libris.indexes.KeyIntegerTuple;

public class NameList extends GuiControl {

	private  JList control;
	private final GroupDef grpDef;
	private final LibrisDatabase dBase;
	private boolean isEditable;
	Vector <KeyIntegerTuple> affiliateInfo;
	private final LibrisWindowedUi windowedUi;

	public NameList(LibrisWindowedUi ui, LibrisDatabase db, Record rec, GroupDef gd) throws InputException {
		grpDef = gd;
		windowedUi = ui;
		dBase = db;
		isEditable = true;
		setValues(rec, gd);
	}

	public void setValues(Record rec, GroupDef gd) throws InputException {
		int[] affiliates = rec.getAffiliates(gd.getGroupNum());
		setFieldValues(affiliates);
	}

	private void setFieldValues(int[] affiliates) throws InputException {
		int affLen = affiliates.length;
		affiliateInfo = new Vector<KeyIntegerTuple>(affLen);
		if (0 == affLen) {
			affiliateInfo.add(new KeyIntegerTuple("<none>", RecordId.getNullId()));
		} else {
			for (int i = 0; i < affLen; ++i) {
				int recordNumber = affiliates[i];
				affiliateInfo.add(new KeyIntegerTuple(dBase.getRecordName(recordNumber), recordNumber));
			}
		}
		control = new JList(affiliateInfo);
		if (affLen > 0) {
			control.setSelectedIndex(0);
			// TODO add menu item to edit affiliate list
		}
		control.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		GroupMouseListener listener = new GroupMouseListener();
		control.addMouseListener(listener);
	}

	@Override
	public FieldValue getFieldValue() throws FieldDataException {
		return null;
	}

	@Override
	public Component getGuiComponent() {
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
	public void setEditable(boolean edtble) {
		isEditable = edtble;
	}

	@Override
	public boolean isEditable() {
		return isEditable;
	}
	
	@Override
	public int getNumValues() {
		return affiliateInfo.size();
	}

	@Override
	public Iterable<FieldValue> getFieldValues() throws FieldDataException {
		return new Iterable<FieldValue>() {

			@Override
			public Iterator<FieldValue> iterator() {
				return new Iterator<FieldValue>() {

					int i = 0;
					@Override
					public boolean hasNext() {
						boolean result = false;
						if ((0 == i) && (1 == affiliateInfo.size())) {
							KeyIntegerTuple entry = affiliateInfo.get(0);
							result = (RecordId.getNullId() != entry.getValue());
						} else {
							result = (i < affiliateInfo.size());
						}
						return result;
					}

					@Override
					public FieldValue next() {
						if (!hasNext()) {
							throw new NoSuchElementException();
						}
						KeyIntegerTuple v = affiliateInfo.get(i);
						++i;
						FieldIntValue fv = new FieldIntValue(v.getValue());
						return fv;
					}

					@Override
					public void remove() {
						return;
					}
					
				};
			}
			
		};
	}

	class GroupMouseListener implements MouseListener {

		private GuiControl uiField;
		public GroupMouseListener() {
			uiField = NameList.this;
		}

		@Override
		public void mouseClicked(MouseEvent arg0) {
			if (uiField.isEditable() && (2 == arg0.getClickCount())) {
				new AffiliateEditor(uiField, windowedUi.getMainFrame(), dBase.getNamedRecordIndex(), affiliateInfo, control, grpDef.getFieldTitle());
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

}
