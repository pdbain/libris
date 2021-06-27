package org.lasalledebain.libris.ui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.EnumSet;
import java.util.Vector;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.ListSelectionListener;

import org.lasalledebain.libris.Field.FieldType;
import org.lasalledebain.libris.FieldTemplate;
import org.lasalledebain.libris.Schema;

@SuppressWarnings("serial")
public class FieldChooser extends JPanel {
	private final JList<FieldInfo> fieldList;
	private int numSearchFields;
	private final int[] searchFieldIndices;
	boolean includeRecordName;
	private Vector<FieldInfo> searchFieldList;
	@SuppressWarnings("unused")
	private boolean multiSelect;

	public FieldChooser(Schema schem, EnumSet<FieldType> searchFieldTypes, String label) {
		this(schem, searchFieldTypes, true, label, null);
	}

	public FieldChooser(Schema schem, EnumSet<FieldType> searchFieldTypes, boolean multiSelect, String label, int[] selectedFields) {
		searchFieldIndices = new int[schem.getNumFields()];
		searchFieldList = getSearchFieldList(schem,searchFieldTypes);
		setLayout(new BorderLayout());
		includeRecordName = false;
		this.multiSelect = multiSelect;

		fieldList = new JList<FieldInfo>(searchFieldList);
		JScrollPane fieldListScrollPane = new JScrollPane(fieldList);
		fieldList.setVisibleRowCount(4);
		numSearchFields = searchFieldList.size();
		add(new JLabel(label), BorderLayout.NORTH);
		add(fieldListScrollPane, BorderLayout.CENTER);
		if (multiSelect) {
			if (null == selectedFields) {
				selectAll();
			} else {
				setSelectedFields(selectedFields);
			}
			JPanel buttonBar = new JPanel();
			buttonBar.setLayout(new BoxLayout(buttonBar, BoxLayout.Y_AXIS));
			final JButton selectAllButton = new JButton("Select all");
			buttonBar.add(selectAllButton);
			selectAllButton.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent arg0) {
					selectAll();
				}		
			});

			final JButton invertSelectionButton = new JButton("Invert selection");
			invertSelectionButton.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent arg0) {
					int[] currentSelections = fieldList.getSelectedIndices();
					int newSelections[] = new int[numSearchFields - currentSelections.length];
					int oldCursor = 0;
					int newCursor = 0;
					for (int i = 0; i < numSearchFields; ++i) {
						if ((oldCursor < currentSelections.length) && (currentSelections[oldCursor] == i)) {
							++oldCursor;
						} else {
							newSelections[newCursor] = i;
							++newCursor;
						}
					}
					fieldList.setSelectedIndices(newSelections);
				}

			});
			buttonBar.add(invertSelectionButton);
			add(buttonBar, BorderLayout.EAST);
		} else {
			selectFirst();
		}
	}
	public void addListSelectionListener​(ListSelectionListener listener) {
		fieldList.addListSelectionListener(listener);
	}

	private Vector<FieldInfo> getSearchFieldList(Schema schem, EnumSet<FieldType> searchFieldTypes) {
		final int numSchemaFields = schem.getNumFields();
		Vector<FieldInfo> searchList = new Vector<FieldInfo>(numSchemaFields);
		for (int i = 0; i < numSchemaFields; ++i) {
			FieldTemplate ft = schem.getFieldTemplate(i);
			if (searchFieldTypes.contains(ft.getFtype())) {
				searchList.add(new FieldInfo(i, ft.getFieldTitle()));
				searchFieldIndices[ft.getFieldNum()] = searchList.size() - 1;
			}
		}
		return searchList;
	}

	static class FieldInfo {
		protected FieldInfo(int fieldNum, String title) {
			this.fieldNum = fieldNum;
			this.title = title;
		}
		@Override
		public String toString() {
			return title;
		}
		String title;
		int fieldNum;
	}

	public int[] getFieldNums() {
		int[] selectedFields = fieldList.getSelectedIndices();
		int[] result = new int[selectedFields.length];
		for (int i = 0; i <  selectedFields.length; ++i) {
			FieldInfo fi = searchFieldList.get(selectedFields[i]);
			result[i] = fi.fieldNum;
		}
		return result;
	}
	public int getFieldNum() {
		int selectedField = fieldList.getSelectedIndex();
		FieldInfo fi = searchFieldList.get(selectedField);
		return fi.fieldNum;
	}
	public void setSelectedIndex(int index) {
			fieldList.setSelectedIndex(index);
	}
	public void setSelectedFields(int[] fieldIds) {
		int[] indices = new int[fieldIds.length];
		for (int i = 0; i < fieldIds.length; ++i) {
			indices[i] = searchFieldIndices[fieldIds[i]];
		}
		fieldList.setSelectedIndices(indices);
	}

	private void selectAll() {
		fieldList.setSelectionInterval(0, numSearchFields-1);
	}
	private void selectFirst() {
		fieldList.setSelectedIndex(0);
	}
}
