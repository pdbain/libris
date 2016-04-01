package org.lasalledebain.libris.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.lasalledebain.libris.LibrisDatabase;
import org.lasalledebain.libris.Record;
import org.lasalledebain.libris.RecordId;
import org.lasalledebain.libris.exception.DatabaseException;
import org.lasalledebain.libris.exception.LibrisException;
import org.lasalledebain.libris.ui.LibrisMenu.NewRecordListener;
import org.lasalledebain.libris.xmlUtils.LibrisXMLConstants;

public class RecordDisplayPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private LibrisGui mainGui;
	JPanel buttonBar;
	private JTabbedPane openRecordPanes;
	ArrayList<RecordWindow> openRecords;
	private JButton prevButton;
	private JButton nextButton;
	private JButton newButton;
	private JButton enterButton;
	private LibrisDatabase database;
	private Layout recLayout;
	private Layout titleLayout;
	private JComboBox layoutSelector;
	protected String[] layoutIds;
	private String[] titleFieldIds;
	private ModificationListener modListener;
	private JButton closeButton;

	public RecordDisplayPanel(LibrisDatabase librisDatabase, LibrisGui mainGui) {
		this.mainGui = mainGui;
		this.database = librisDatabase;
		modListener = new ModificationListener();
		setLayout(new BorderLayout());
		openRecords = new ArrayList<RecordWindow>();
		addButtonBar(mainGui);
		openRecordPanes = new JTabbedPane();
		openRecordPanes.addChangeListener(new PaneChangeListener());
		add(openRecordPanes);
		setMinimumSize(new Dimension(200, 50));
		setPreferredSize(new Dimension(890, 500));
	}

	void addRecord(Record record, boolean editable) throws DatabaseException {
		RecordWindow rw = new RecordWindow(database.getUi(), recLayout, record, true, modListener);
		rw.setModified(false);
		rw.setEditable(editable);
		addRecordWindow(rw);
	}
// FIXME summary view not shown
	public void displayRecord(RecordId recId) throws LibrisException {
		for (int i= 0; i < openRecords.size(); ++i) {
			RecordWindow r = openRecords.get(i);
			final RecordId recordId = r.getRecordId();
			if ((null != recordId) && recordId.equals(recId)) {
				openRecordPanes.setSelectedIndex(i);
				return; /* already have the record */
			}
		}
		Record rec = database.getRecord(recId);
		addRecord(rec, false);
	}

	/**
	 * @param record
	 * @param titleFieldIds
	 * @param rw
	 * @throws DatabaseException 
	 */
	public void addRecordWindow(RecordWindow rw) throws DatabaseException {
		openRecords.add(rw);
		String recordTitle = rw.createTitle(getTitleFieldIds());
		final JScrollPane recordTab = new JScrollPane(rw);
		openRecordPanes.add(recordTitle, recordTab);
		recordModified();
		rw.setVisible(true);
		openRecordPanes.setSelectedComponent(recordTab);
	}

	public Iterable<RecordWindow> getOpenRecords() {
		return openRecords;
	}

	public void addLayouts(Layouts layouts) {
		layoutIds = layouts.getLayoutIds();
		String lo = "";
		try {
			lo = database.getLayouts().getLayoutByUsage(LibrisXMLConstants.XML_LAYOUT_USER_NEWRECORD).getId();
		} catch (DatabaseException e) {
			mainGui.alert("exception "+e+" "+e.getMessage()+" reading layouts");
		}
		for (String s: layoutIds) {
			layoutSelector.addItem(layouts.getLayout(s).getTitle());
			if (s.equals(lo)) {
				layoutSelector.setSelectedIndex(layoutSelector.getItemCount()-1);
			}
		}
	}

	private synchronized void setRecLayout(Layout recLayout) {
		this.recLayout = recLayout;
	}

	private synchronized String[] getTitleFieldIds() throws DatabaseException {
		if (null == titleLayout) {
			titleLayout = database.getLayouts().getLayoutByUsage(LibrisXMLConstants.XML_LAYOUT_USER_SUMMARYDISPLAY);
			titleFieldIds = titleLayout.getFieldIds();
		}
		return titleFieldIds;
	}

	RecordWindow getCurrentRecordWindow() {
		int currentRecordIndex = openRecordPanes.getSelectedIndex();
		if (currentRecordIndex >= 0) {
			return openRecords.get(currentRecordIndex);
		}
		return null;
	}
	
	/**
	 * @param mainGui
	 */
	private void addButtonBar(LibrisGui mainGui) {
		buttonBar = new JPanel();
		
		buttonBar.add(newButton = new JButton("New"));
		NewRecordListener newRecordHandler = mainGui.menu.getNewRecordHandler();
		newButton.addActionListener(newRecordHandler);
		
		buttonBar.add(enterButton = new JButton("Enter"));
		enterButton.addActionListener(new closeListener(true));
		buttonBar.add(closeButton = new JButton("Close"));
		closeButton.addActionListener(new closeListener(false));
		enterButton.setEnabled(true);
		JLabel layoutLabel = new JLabel("Layout");
		layoutLabel.setLabelFor(layoutSelector);
		buttonBar.add(layoutLabel);
		layoutSelector = new JComboBox();
		buttonBar.add(layoutSelector);
		layoutSelector.addActionListener(new layoutSelectionListener());
		add(buttonBar, BorderLayout.NORTH);
		
		buttonBar.add(prevButton = new JButton("Previous"));
		prevButton.addActionListener(new prevNextListener(false));
		buttonBar.add(nextButton = new JButton("Next"));
		nextButton.addActionListener(new prevNextListener(true));
	}

	private class prevNextListener implements ActionListener {
		boolean ifNext = false;
		public prevNextListener(boolean ifNext) {
			this.ifNext = ifNext;
		}
		@Override
		public void actionPerformed(ActionEvent e) {
			mainGui.goToPrevOrNextRecord(ifNext);
		}
	}
	private class layoutSelectionListener implements ActionListener {
		
		@Override
		public void actionPerformed(ActionEvent e) {
			int selectedLayout = layoutSelector.getSelectedIndex();
			if (selectedLayout >= 0) {
				setRecLayout(database.getLayouts().getLayout(layoutIds[selectedLayout]));
				RecordWindow w = getCurrentRecordWindow();
				if (null != w) {
					try {
						w.setRecordLayout(recLayout);
					} catch (LibrisException exc) {
						mainGui.alert("exception "+exc+" "+exc.getMessage());
					}
				}
			}
		}	
	}
	
	private class closeListener implements ActionListener {
		
		private boolean enter;

		public closeListener(boolean enter) {
			this.enter = enter;
		}

		@Override
		public void actionPerformed(ActionEvent evt) {
			doClose(evt, enter); 
		}

	}
	
	void doClose(ActionEvent evt, boolean enter) {
		int selectedRecordIndex = openRecordPanes.getSelectedIndex();
		if (selectedRecordIndex >= 0) {
			boolean closeWindow = false;
			RecordWindow currentRecordWindow = openRecords.get(selectedRecordIndex);
			Record currentRecord = currentRecordWindow.getRecord();
			try {
				if (enter) {
					database.checkIfdatabaseIndexed(Messages.getString("LibrisDatabase.unindexed_do_index"));
					currentRecordWindow.enter();
					database.put(currentRecord);
					mainGui.put(currentRecord);
				} else {
					int result = currentRecordWindow.checkClose();
					switch (result) {
					case Dialogue.CANCEL_OPTION: return;
					case Dialogue.YES_OPTION: database.put(currentRecord);
					case Dialogue.NO_OPTION: closeWindow = true; break;
					default: closeWindow = true;
					}
				}
			} catch (LibrisException e) {
				mainGui.alert("exception "+e+" "+e.getMessage()+" entering record");
				e.printStackTrace();
			}
			int modifiers = (null == evt)? 0: evt.getModifiers();
			if (closeWindow || (enter && (0 == (modifiers & ActionEvent.SHIFT_MASK)))) {
				openRecords.remove(selectedRecordIndex);
				openRecordPanes.remove(selectedRecordIndex);
				currentRecordWindow.close();
			}
		}
	}		

	private void recordModified() {
		RecordWindow w = getCurrentRecordWindow();
		if (null != w) {
			boolean modified = w.isModified();
			enterButton.setEnabled(modified);
			mainGui.setRecordEnterRecordEnabled(modified);
			closeButton.setEnabled(true);
		} else {
			enterButton.setEnabled(false);
			closeButton.setEnabled(false);
			mainGui.setRecordEnterRecordEnabled(false);
		}
		buttonBar.repaint();
	}

	public class ModificationListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			recordModified();
		}
	}
	
	public class PaneChangeListener implements ChangeListener {
		public void stateChanged(ChangeEvent e) {
			recordModified();
		}
	}


	public void close(boolean allRecords) {
		if (allRecords) {
			for (int i = openRecords.size()-1; i >= 0; --i) {
				removeRecord(i);
			}
		} else {
			int currentRecordIndex = openRecordPanes.getSelectedIndex();
			removeRecord(currentRecordIndex);
		}
	}

	private void removeRecord(int index) {
		if (index < 0) {
			return;
		}
		RecordWindow rw = openRecords.get(index);
		if (null != rw) {
			rw.checkClose();
			rw.close();
		}
		openRecords.remove(index);
		openRecordPanes.remove(index);

	}
}
