package org.lasalledebain.libris.ui;

import static java.util.Objects.nonNull;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.lasalledebain.libris.DatabaseRecord;
import org.lasalledebain.libris.LibrisDatabase;
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
	private final JPanel buttonBar;
	ArrayList<DatabaseRecordWindow> openRecords;
	private JButton prevButton;
	private JButton nextButton;
	private JButton newButton;
	private JButton enterButton;
	private LibrisDatabase myDatabase;
	private Layout<DatabaseRecord> recLayout;
	private Layout<DatabaseRecord> titleLayout;
	private JComboBox<String> layoutSelector;
	protected String[] layoutIds;
	private String[] titleFieldIds;
	private ModificationListener modListener;
	private JButton closeButton;
	private LibrisMenu menu;
	private boolean singleRecordMode;

	private final JTabbedPane singleRecordView;
	private final JPanel multipleRecordView;

	public RecordDisplayPanel(LibrisDatabase librisDatabase, LibrisGui theGui) {
		mainGui = theGui;
		myDatabase = librisDatabase;
		modListener = new ModificationListener();
		setLayout(new BorderLayout());
		openRecords = new ArrayList<DatabaseRecordWindow>();
		buttonBar = new JPanel();
		addButtonBar(theGui);
		singleRecordView = new JTabbedPane();
		multipleRecordView = new JPanel();
		singleRecordView.addChangeListener(new PaneChangeListener());
		add(singleRecordView);
		setMinimumSize(new Dimension(200, 50));
		setPreferredSize(new Dimension(1200, 750));
		menu = theGui.getMenu();
		singleRecordMode = false;
		add(multipleRecordView);
		mainGui.pack();
		remove(singleRecordView);
		add(multipleRecordView);
		mainGui.pack();
		remove(singleRecordView);
	}

	DatabaseRecordWindow addRecord(DatabaseRecord record, boolean editable) throws LibrisException {
		DatabaseRecordWindow rw = new DatabaseRecordWindow(mainGui, recLayout, record, editable, modListener);
		rw.setModified(false);
		openRecords.add(rw);
		String recordTitle = rw.createTitle(getTitleFieldIds());
		final JScrollPane recordTab = new JScrollPane(rw);
		singleRecordView.add(recordTitle, recordTab);
		recordModified();
		rw.setVisible(true);
		singleRecordView.setSelectedComponent(recordTab);
		menu.recordWindowOpened(editable);
		return rw;
	}

	public void displayRecord(int recId) throws LibrisException {
		if (singleRecordMode) {
		for (int i= 0; i < openRecords.size(); ++i) {
			RecordWindow r = openRecords.get(i);
			final int recordId = r.getRecordId();
			if (recordId == recId) {
				singleRecordView.setSelectedIndex(i);
				return; /* already have the record */
			}
		}
		DatabaseRecord rec = myDatabase.getRecord(recId);
		addRecord(rec, false);
		} else {
			recLayout.showRecord(recId);
		}
	}

	public Iterable<DatabaseRecordWindow> getOpenRecords() {
		return openRecords;
	}

	void enableNextButton(boolean enable) {
		nextButton.setEnabled(enable);	
	}

	void enablePrevButton(boolean enable) {
		prevButton.setEnabled(enable);	
	}

	public void addLayouts(Layouts layouts) {
		layoutIds = layouts.getLayoutIds();
		String lo = "";
		try {
			lo = myDatabase.getLayouts().getLayoutByUsage(LibrisXMLConstants.XML_LAYOUT_USER_NEWRECORD).getId();
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

	private synchronized void setRecLayout(Layout<DatabaseRecord> theLayout) throws DatabaseException, LibrisException {
		recLayout = theLayout;
		boolean oldMode = singleRecordMode;
		singleRecordMode = theLayout.isSingleRecord();
		if (singleRecordMode != oldMode) {
			if (singleRecordMode) {
				remove(multipleRecordView);
				add(singleRecordView);
				DatabaseRecordWindow currentRecordWindow = mainGui.getCurrentRecordWindow();
				mainGui.getMenu().enableFieldValueOperations(nonNull(currentRecordWindow) && currentRecordWindow.isEditable());
			} else {
				remove(singleRecordView);
				multipleRecordView.removeAll();
				theLayout.layOutFields(mainGui.getResultRecords(), mainGui,multipleRecordView, null);
				add(multipleRecordView);
				mainGui.getMenu().enableFieldValueOperations(false);
			}
			mainGui.pack();
			mainGui.repaint();
		}
	}

	private synchronized String[] getTitleFieldIds() throws DatabaseException {
		if (null == titleLayout) {
			titleLayout = myDatabase.getLayouts().getLayoutByUsage(LibrisXMLConstants.XML_LAYOUT_USER_SUMMARYDISPLAY);
			titleFieldIds = titleLayout.getFieldIds();
		}
		return titleFieldIds;
	}

	DatabaseRecordWindow getCurrentRecordWindow() {
		int currentRecordIndex = singleRecordView.getSelectedIndex();
		if (currentRecordIndex >= 0) {
			return openRecords.get(currentRecordIndex);
		}
		return null;
	}

	public void setCurrentRecordName(String newName) {
		int currentRecordIndex = singleRecordView.getSelectedIndex();
		singleRecordView.setTitleAt(currentRecordIndex, newName);
	}

	/**
	 * @param mainGui
	 */
	private void addButtonBar(LibrisGui mainGui) {

		buttonBar.add(prevButton = new JButton("\u2190"));
		buttonBar.add(newButton = new JButton("New"));
		NewRecordListener newRecordHandler = mainGui.menu.getNewRecordHandler();
		newButton.addActionListener(newRecordHandler);

		buttonBar.add(enterButton = new JButton("Enter"));
		enterButton.addActionListener(new closeListener(true));
		buttonBar.add(closeButton = new JButton("Close"));
		closeButton.addActionListener(new closeListener(false));
		enterButton.setEnabled(true);
		layoutSelector = new JComboBox<String>();
		buttonBar.add(layoutSelector);
		layoutSelector.addActionListener(new layoutSelectionListener());

		prevButton.addActionListener(new prevNextListener(false));
		prevButton.setEnabled(false);
		buttonBar.add(nextButton = new JButton("\u2192"));
		nextButton.setEnabled(false);
		nextButton.addActionListener(new prevNextListener(true));
		add(buttonBar, BorderLayout.NORTH);
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
				try {
					Layout theLayout = myDatabase.getLayouts().getLayout(layoutIds[selectedLayout]);
					setRecLayout(theLayout);
					boolean singleRecordLayout = theLayout.isSingleRecord();
					if (singleRecordLayout) {
						RecordWindow w = getCurrentRecordWindow();
						if (null != w) {
							w.setRecordLayout(recLayout);
						}
					}
				} catch (LibrisException exc) {
					mainGui.alert("exception "+exc+" "+exc.getMessage());
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
			try {
				doClose(evt, enter);
			} catch (Exception e) {
				mainGui.alert("Unxpected exception closing record", e);
			}
		}

	}

	void doClose(ActionEvent evt, boolean enter) {
		int selectedRecordIndex = singleRecordView.getSelectedIndex();
		if (selectedRecordIndex >= 0) {
			boolean closeWindow = false;
			DatabaseRecordWindow currentRecordWindow = openRecords.get(selectedRecordIndex);
			DatabaseRecord currentRecord = currentRecordWindow.getRecord();
			try {
				if (enter) {
					currentRecordWindow.enter();
					myDatabase.putRecord(currentRecord);
					if (currentRecordWindow.isModified()) {
						myDatabase.setModified(true);
					}
					mainGui.put(currentRecord);
				} else {
					int result = currentRecordWindow.checkClose();
					switch (result) {
					case Dialogue.CANCEL_OPTION: return;
					case Dialogue.YES_OPTION: myDatabase.putRecord(currentRecord);
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
				singleRecordView.remove(selectedRecordIndex);
				currentRecordWindow.close();
				currentRecord.setEditable(false);
			}
			adjustMenusForWindowClose();
		}
	}

	private void adjustMenusForWindowClose() {
		menu.recordWindowClosed();
		RecordWindow w = getCurrentRecordWindow();
		if (null != w) {
			menu.recordWindowOpened(w.isEditable());
		}
	}		

	private void recordModified() {
		DatabaseRecordWindow w = getCurrentRecordWindow();
		if (null != w) {
			boolean modified = w.isModified();

			DatabaseRecord theRecord = w.getRecord();
			menu.enableFieldValueOperations(theRecord.isEditable());
			enterButton.setEnabled(modified);
			mainGui.setRecordEnterRecordEnabled(modified);
			mainGui.setOpenArtifactInfoEnabled(theRecord.hasArtifact());
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
			int currentRecordIndex = singleRecordView.getSelectedIndex();
			removeRecord(currentRecordIndex);
		}
		adjustMenusForWindowClose();
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
		singleRecordView.remove(index);

	}
}
