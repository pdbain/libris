package org.lasalledebain.libris.ui;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;

import org.lasalledebain.libris.Record;
import org.lasalledebain.libris.exception.FieldDataException;
import org.lasalledebain.libris.field.FieldValue;

@SuppressWarnings("serial")
public class FieldValueArranger<RecordType extends Record> extends JDialog {
	 
    private JButton upButton;
    private JButton downButton;
    private JButton deleteButton;
    private JButton cancelButton;
    private JButton okayButton;
	private JList<String> valueList;
	private FieldValue valueArray[];
	private int numValues;
	int selectedValue;
	private Frame parentFrame;
	boolean fieldUpdated;
 
	public FieldValueArranger(JFrame parent, final MultipleValueUiField fld) {
		super(parent, "Arrange field values", true);
		fieldUpdated = false;
		parentFrame = parent;
		ArrayList <FieldValue> valueBuff = new ArrayList<FieldValue>();
		for (FieldValue fv: fld) {
			valueBuff.add(fv);
		}
		numValues = valueBuff.size();
		valueArray = new FieldValue[numValues];
		if (numValues > 0) {
			selectedValue = numValues-1;
		} else {
			JOptionPane.showMessageDialog(parent, "No values in this field");
			return;
		}
		valueBuff.toArray(valueArray);
		JPanel panel = new JPanel(new GridBagLayout());
		valueList = new JList<String>();
		setList();
		valueList.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
		panel.add(new JScrollPane(valueList));

		upButton = new JButton("Up");
		upButton.addActionListener(new ActionListener() {public void actionPerformed(ActionEvent e) {moveSelection(false);}});
		parent.getRootPane().setDefaultButton(upButton);
		downButton = new JButton("Down");
		downButton.addActionListener(new ActionListener() {public void actionPerformed(ActionEvent e) {moveSelection(true);}});
		deleteButton = new JButton("Delete");
		deleteButton.addActionListener(new ActionListener() {public void actionPerformed(ActionEvent e) {deleteSelection();}});
		cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(new ActionListener() {public void actionPerformed(ActionEvent e) {dispose();}});
		okayButton = new JButton("Okay");
		okayButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
			try {
				fld.setFieldValues(valueArray);
				fieldUpdated = true;
			} catch (FieldDataException e1) {
				JOptionPane.showMessageDialog(parentFrame, e1.getMessage());
			}
			dispose();
		}});
		JPanel bp = new JPanel();
		BoxLayout buttonLayout = new BoxLayout(bp, BoxLayout.Y_AXIS);
		bp.setLayout(buttonLayout);
		bp.add(upButton);
		bp.add(downButton);
		bp.add(deleteButton);
		bp.add(okayButton);
		bp.add(cancelButton);
		getContentPane().add(panel, BorderLayout.CENTER);
		getContentPane().add(bp, BorderLayout.EAST);
		getRootPane().setDefaultButton(upButton);

		pack();
		setResizable(false);
		setLocationRelativeTo(parent);
	}
 
    @Override
	public void setVisible(boolean arg0) {
		if (numValues > 0) {
			super.setVisible(arg0);
		}
	}

	public boolean isFieldUpdated() {
		return fieldUpdated;
	}

	private void setList() {
    	String valueStrings[] = new String[numValues];

		for (int i = 0; i < numValues; ++i) {
			valueStrings[i] = valueArray[i].getValueAsString();
		}
		valueList.setListData(valueStrings);
		valueList.setSelectedIndex(selectedValue);
	}

	private void moveSelection(boolean down) {
		selectedValue = valueList.getSelectedIndex();
		if (selectedValue >= 0) {
			if (down) {
				if (selectedValue < (numValues - 1)) {
					FieldValue temp = valueArray[selectedValue+1];
					valueArray[selectedValue+1] = valueArray[selectedValue];
					valueArray[selectedValue] = temp;
					++selectedValue;
				}
			} else {
				if (selectedValue > 0) {
					FieldValue temp = valueArray[selectedValue-1];
					valueArray[selectedValue-1] = valueArray[selectedValue];
					valueArray[selectedValue] = temp;
					--selectedValue;
				}
			}
			setList();
		}
	}
	
	private void deleteSelection() {
		selectedValue = valueList.getSelectedIndex();
		if (selectedValue >= 0) {
			for (int i = selectedValue; i < (numValues - 1); ++i) {
				valueArray[i] = valueArray[i+1];
			}
			--numValues;
			setList();
		}
	}
 }
