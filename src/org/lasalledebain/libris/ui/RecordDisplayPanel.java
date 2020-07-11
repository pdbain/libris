package org.lasalledebain.libris.ui;

import static java.util.Objects.nonNull;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.lasalledebain.libris.DatabaseRecord;
import org.lasalledebain.libris.LibrisDatabase;
import org.lasalledebain.libris.exception.DatabaseException;
import org.lasalledebain.libris.exception.LibrisException;
import org.lasalledebain.libris.ui.LibrisMenu.NewRecordListener;
import org.lasalledebain.libris.xmlUtils.LibrisXMLConstants;

@SuppressWarnings("serial")
public class RecordDisplayPanel extends JPanel {

	private LibrisGui mainGui;
	private final JPanel buttonBar;
	ArrayList<DatabaseRecordWindow> openRecords;
	private JButton prevButton;
	private JButton nextButton;
	private JButton newButton;
	private JButton enterButton;
	private LibrisDatabase myDatabase;
	private LibrisSwingLayout<DatabaseRecord> recLayout;
	private LibrisSwingLayout<DatabaseRecord> titleLayout;
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
		multipleRecordView = new JPanel(new GridLayout());
		singleRecordView.addChangeListener(new PaneChangeListener());
		add(singleRecordView);
		setMinimumSize(new Dimension(200, 50));
		setPreferredSize(new Dimension(1200, 750));
		menu = theGui.getMenu();
	}

	DatabaseRecordWindow addRecord(DatabaseRecord record, boolean editable) throws LibrisException {
		DatabaseRecordWindow rw = new DatabaseRecordWindow(mainGui, recLayout, record, editable, modListener);
		rw.setModified(false);
		openRecords.add(rw);
		String recordTitle = rw.createTitle(getTitleFieldIds());
		singleRecordView.add(recordTitle, rw);
		recordModified();
		rw.setVisible(true);
		singleRecordView.setSelectedComponent(rw);
		menu.recordWindowOpened(editable);
		mainGui.mainFrame.repaint();
		return rw;
	}

	public void displayRecord(int recId) throws LibrisException {
		if (singleRecordMode) {
			for (int i= 0; i < openRecords.size(); ++i) {
				RecordWindow<DatabaseRecord> r = openRecords.get(i);
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

	public void addLayouts(Layouts<DatabaseRecord> layouts) {
		layoutIds = layouts.getLayoutIds();
		String lo = "";
		try {
			lo = myDatabase.getLayouts().getLayoutByUsage(LibrisXMLConstants.XML_LAYOUT_USER_NEWRECORD).getId();
		} catch (DatabaseException e) {
			mainGui.alert("exception "+e+" "+e.getMessage()+" reading layouts");
		}
		for (String s: layoutIds) {
			layoutSelector.addItem(layouts.getSwingLayout(s).getTitle());
			if (s.equals(lo)) {
				layoutSelector.setSelectedIndex(layoutSelector.getItemCount()-1);
			}
		}
	}

	void setRecLayout(LibrisSwingLayout<DatabaseRecord> theLayout) throws DatabaseException, LibrisException {
		if (theLayout == recLayout) {
			return;
		}
		DatabaseRecordWindow currentRecordWindow = mainGui.getCurrentRecordWindow();
		if (nonNull(currentRecordWindow) && currentRecordWindow.isModified()) {
			mainGui.alert("Current record has been modified. Enter or close record and try again");
			return;
		}
		recLayout = theLayout;
		boolean oldMode = singleRecordMode;
		singleRecordMode = theLayout.isSingleRecord();
		LibrisMenu theMenu = mainGui.getMenu();
		if (singleRecordMode != oldMode) {
			if (singleRecordMode) {
				remove(multipleRecordView);
				add(singleRecordView);
				theMenu.enableFieldValueOperations(nonNull(currentRecordWindow) && currentRecordWindow.isEditable());
			} else {
				remove(singleRecordView);
				multipleRecordView.removeAll();
				theLayout.layOutFields(mainGui.getResultRecords(), mainGui,multipleRecordView, null);
				add(multipleRecordView);
				theMenu.enableFieldValueOperations(false);
			}
		}
		theMenu.enableRecordEdit(theLayout.isEditable() && !mainGui.getDatabase().isDatabaseReadOnly());
		mainGui.mainFrame.pack();
		mainGui.repaint();
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

		private int oldLayout = 0;

		@Override
		public void actionPerformed(ActionEvent e) {
			int selectedLayout = layoutSelector.getSelectedIndex();
			if (selectedLayout >= 0) {
				RecordWindow<DatabaseRecord> rw = getCurrentRecordWindow();
				if ((selectedLayout != oldLayout) && nonNull(rw)) {
rw.checkClose();
				}
				try {
					LibrisSwingLayout<DatabaseRecord> theLayout = myDatabase.getLayouts().getSwingLayout(layoutIds[selectedLayout]);
					setRecLayout(theLayout);
					boolean singleRecordLayout = theLayout.isSingleRecord();
					if (singleRecordLayout && nonNull(rw)) {
						rw.setRecordLayout(recLayout);
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
		RecordWindow<DatabaseRecord> w = getCurrentRecordWindow();
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


	public boolean close(boolean allRecords) {
		boolean canClose = true;
		if (allRecords) {
			for (int i = openRecords.size()-1; i >= 0 && canClose; --i) {
				canClose &= removeRecord(i);
			}
		} else {
			int currentRecordIndex = singleRecordView.getSelectedIndex();
			canClose &= removeRecord(currentRecordIndex);
		}
		if (canClose) {
			adjustMenusForWindowClose();
		}
		return canClose;
	}

	/**
	 * @param index specifies a record window
	 * @return true if window is removable
	 */
	private boolean removeRecord(int index) {
		boolean canClose = true;
		if (index >= 0) {
			RecordWindow<DatabaseRecord> rw = openRecords.get(index);
			if (null != rw) {
				int result = rw.checkClose();
				switch (result) {
				case Dialogue.CANCEL_OPTION: canClose = false; break;
				case Dialogue.YES_OPTION: try {
						myDatabase.putRecord(rw.getRecord());
					} catch (LibrisException e) {
						mainGui.alert("Error saving record", e);
					}
				break;
				case Dialogue.NO_OPTION: break;
				default: break;
				}
				if (canClose) {
					rw.close();
				}
			}
			if (canClose) {
				openRecords.remove(index);
				singleRecordView.remove(index);
			}
		}
		return canClose;
	}
}
