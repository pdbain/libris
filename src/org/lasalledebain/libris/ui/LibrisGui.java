package org.lasalledebain.libris.ui;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;

import org.lasalledebain.libris.ArtifactParameters;
import org.lasalledebain.libris.DatabaseRecord;
import org.lasalledebain.libris.Field;
import org.lasalledebain.libris.LibrisConstants;
import org.lasalledebain.libris.LibrisDatabase;
import org.lasalledebain.libris.NamedRecordList;
import org.lasalledebain.libris.Record;
import org.lasalledebain.libris.RecordId;
import org.lasalledebain.libris.RecordList;
import org.lasalledebain.libris.exception.DatabaseError;
import org.lasalledebain.libris.exception.DatabaseException;
import org.lasalledebain.libris.exception.FieldDataException;
import org.lasalledebain.libris.exception.InputException;
import org.lasalledebain.libris.exception.LibrisException;

import com.apple.eawt.AppEvent.QuitEvent;
import com.apple.eawt.QuitHandler;
import com.apple.eawt.QuitResponse;

public class LibrisGui extends LibrisWindowedUi {
	private static final String CONTENT_PANE_HEIGHT = "CONTENT_PANE_HEIGHT";
	private static final String CONTENT_PANE_WIDTH = "CONTENT_PANE_WIDTH";
	private static final String CONTENT_PANE_DIVIDER = "CONTENT_PANE_DIVIDER_LOCATION";
	LibrisMenu menu;
	private JMenuBar menuBar;
	protected BrowserWindow resultsPanel;
	protected RecordDisplayPanel displayPanel;
	private JSplitPane contentPane;
	private JSplitPane mainWindow;
	private JPanel layoutEditPane;
	private Clipboard systemClipboard;
	private Component mainframeContents;
	public LibrisGui(File databaseFile, boolean readOnly) throws LibrisException {
		super(databaseFile, readOnly);
		System.setProperty("apple.laf.useScreenMenuBar","true");
		System.setProperty("apple.eawt.quitStrategy system property", "CLOSE_ALL_WINDOWS");
		initializeGui();
	}

	protected void initializeGui() throws DatabaseException {
		com.apple.eawt.Application.getApplication().setQuitHandler(new QuitHandler() {
			@Override
			public void handleQuitRequestWith(QuitEvent quitEvt, QuitResponse quitResp) {
				try {
					if ((null == currentDatabase) || currentDatabase.closeDatabase(false)) {
						quitResp.performQuit();
					} else {
						quitResp.cancelQuit();
					}
				} catch (DatabaseException e) {
					throw new DatabaseError(e);
				}
			}});
		databaseSelected = isDatabaseSelected();
		systemClipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		System.setProperty("Title", "Libris");
		menu = new LibrisMenu(this);
		menuBar = menu.createMenus();
		boolean readOnly = isDatabaseReadOnly();
		recordsAccessible(databaseSelected && !readOnly);
		databaseModifiable(databaseSelected && !readOnly);
		createPanes(!databaseSelected);
		mainFrame.setJMenuBar(menuBar);
		mainFrame.addWindowListener(new WindowCloseListener());
		mainFrame.setVisible(true);
	}

	public boolean chooseDatabase() {
		return getMenu().openDatabaseDialogue();
	}

	public LibrisDatabase openDatabase() throws DatabaseException {
		super.openDatabase();
		menu.setDatabase(currentDatabase);
		getMainFrame().toFront();
		boolean readOnly = currentDatabase.isReadOnly();
		menu.editMenuEnableModify(readOnly);
		destroyWindow(true);
		createPanes(false);
		updateUITitle(false);

		displayPanel.addLayouts(currentDatabase.getLayouts());
		RecordList<DatabaseRecord> list = currentDatabase.getRecords();
		resultsPanel.initialize(list);
		recordsAccessible(!readOnly);
		databaseModifiable(!readOnly);
		return currentDatabase;
	}

	public LibrisMenu getMenu() {
		return menu;
	}

	/**
	 * @throws DatabaseException 
	 * 	Icon made by http://www.simpleicon.com from www.flaticon.com
	 *  licensed under Creative Commons BY 3.0
	 */
	private void createPanes(boolean empty) throws DatabaseException {
		if (null != mainframeContents) {
			mainFrame.remove(mainframeContents);
			mainframeContents = null;
		}
		if (!empty) {
			resultsPanel = new BrowserWindow(currentDatabase, this);

			displayPanel = new RecordDisplayPanel(currentDatabase, this);

			contentPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, resultsPanel, displayPanel);
			contentPane.setOneTouchExpandable(true);
			Preferences prefs = getLibrisPrefs();
			int compwidth = prefs.getInt(CONTENT_PANE_WIDTH, 1200);
			int compheight = prefs.getInt(CONTENT_PANE_HEIGHT, 650);
			contentPane.setPreferredSize(new Dimension(compwidth, compheight));
			int divloc = prefs.getInt(CONTENT_PANE_DIVIDER, 285);
			contentPane.setDividerLocation(divloc);

			mainWindow = contentPane;
			mainWindow.setOneTouchExpandable(true);
			mainFrame.add(mainWindow);
			mainframeContents = mainWindow;
		} else {
			createDummyWindow();
		}
		mainFrame.pack();
	}

	@Override
	public Dimension getDisplayPanelSize() {
		Dimension s = displayPanel.getSize();
		return s;
	}

	private void createDummyWindow() {
		JLabel dummyMessage = new JLabel("No database selected");
		mainFrame.setPreferredSize(new Dimension(1000, 600));
		JPanel dummyWindow = new JPanel(new FlowLayout());
		dummyWindow.add(dummyMessage);
		mainframeContents = dummyWindow;
		mainFrame.add(mainframeContents);
	}
	
	void goToPrevOrNextRecord(boolean ifNext) {
		int currentIndex = resultsPanel.getSelectedRecordIndex();
		int increment = 0;
		if (ifNext && (currentIndex < (resultsPanel.getNumRecords()-1))) {
			increment = 1;
		} else if (!ifNext && (currentIndex > 0)) {
			increment = -1;
		}
		if (0 != increment) {
			displayPanel.doClose(null, false);
			resultsPanel.setSelectedRecordIndex(currentIndex+increment);
			resultsPanel.displaySelectedRecord();
		}
	}
	
	@Override
	 protected void destroyWindow(boolean retain) {
		if (null != mainWindow) {
			if (!isDatabaseReadOnly()) {
				Preferences prefs = getLibrisPrefs();
				int temp = contentPane.getWidth();
				prefs.putInt(CONTENT_PANE_WIDTH, temp);
				temp = contentPane.getHeight();
				prefs.putInt(CONTENT_PANE_HEIGHT, temp);
				temp = contentPane.getDividerLocation();
				prefs.putInt(CONTENT_PANE_DIVIDER, temp);
				try {
					prefs.flush();
				} catch (BackingStoreException e) {
					LibrisDatabase.log(Level.WARNING, "exception in destroyWindow", e);
				}
			}
			for (Component c: mainWindow.getComponents()) {
				mainWindow.remove(c);
			}
			if (retain) {
				mainWindow.setVisible(false);
				mainFrame.remove(mainframeContents);
				createDummyWindow();
				mainFrame.pack();
			} else {
				mainFrame.setVisible(false);
			}
		}
	}

	/**
	 * edit the record layouts.
	 * Hides the displayPanel and replaces it with an editor panel comprising:
	 * - a field selector
	 * - control type,size, and position controls
	 * - preview of the layout.
	 * @param b 
	 */
	public void editLayouts(boolean enabled) {
		// TODO editLayouts
		if (enabled) {
			contentPane.setBottomComponent(getLayoutEditPane());
		} else {
			contentPane.setBottomComponent(displayPanel);
		}
		
	}

	private Component getLayoutEditPane() {
		if (null == layoutEditPane) {
			layoutEditPane = new JPanel();
			// TODO Write layout editing functions
			layoutEditPane.add(new JTextArea("edit layouts not implemented"));
		}
		return layoutEditPane;
	}

	@Override
	public DatabaseRecord newRecord() {
		DatabaseRecord rec = null;
		RecordWindow rw = newRecordWindow();
		if (null != rw) {
			rec = rw.getRecord();
		}
		return rec;
	}
	
	public RecordWindow newRecordWindow() {
		DatabaseRecord record = null;
		RecordWindow rw = null;
		try {
			record = currentDatabase.newRecord();
			record.setEditable(!currentDatabase.isReadOnly());
		} catch (LibrisException e1) {
			fatalError(e1);
		}
		try {
			rw = displayPanel.addRecord(record, true);
		} catch (Exception e) {
			alert("error creating new record", e);
		}
		return rw;
	}
	
	@Override
	public void addRecord(Record newRecord) throws DatabaseException {
		if (null != currentDatabase) {
			resultsPanel.addRecord(newRecord);
		}
	}
	
	@Override
	public void put(Record newRecord) throws DatabaseException {
		resultsPanel.addRecord(newRecord);
	}

	public void displayRecord(int recId) throws LibrisException {
		displayPanel.displayRecord(recId);
	}

	FilterDialogue createSearchDialogue() {
		return new FilterDialogue(currentDatabase, getMainFrame(), resultsPanel);
	}
	public void fatalError(Exception e) {
		String msg = "Fatal error: ";
		fatalError(e, msg);
	}

	@Override
	public void closeWindow(boolean allWindows) {
		if (null != displayPanel) {
			displayPanel.close(allWindows);
		}
	}

	public boolean isEditable() {
		RecordWindow rw = null;
		if (null != displayPanel) {
			rw = displayPanel.getCurrentRecordWindow();
		}
		boolean editable = false;
		if (null != rw) {
			editable = rw.isEditable();
		}
		return editable;
	}
	
	/**
	 * Sets the editability of the current record window.  The request may not be obeyed if the database or the record are read-only.
	 * @param editable new state of record window
	 * @return new state of record window
	 * @throws LibrisException
	 */
	public boolean setRecordWindowEditable(boolean editable) throws LibrisException {
		RecordWindow rw = getCurrentRecordWindow();
		if (editable) {
			if (currentDatabase.isReadOnly()
					|| currentDatabase.isRecordReadOnly(rw.getRecordId())) 
				return false;
		}
		boolean wasEditable = rw.isEditable();
		rw.setEditable(!wasEditable);
		rw.refresh();
		repaint();
		rw.setModified(false);
		return true;
	}

	public boolean isDatabaseReadOnly() {
		return (null == currentDatabase) || currentDatabase.isReadOnly();
	}

	@Override
	public void alert(String msg) {
		JOptionPane.showMessageDialog(mainFrame, msg);
	}

	public void displaySelectedRecord() {
		resultsPanel.displaySelectedRecord();
	}

	public void setViewViewSelectedRecordEnabled(boolean enabled) {
		menu.setViewViewSelectedRecordEnabled(enabled);
	}

	@Override
	public String SelectSchemaFile(String schemaName) throws DatabaseException {
		// TODO Auto-generated method stub SelectSchemaFile(String schemaName)
		return null;
	}

	public String promptAndReadReply(String prompt) throws DatabaseException {
		return null;
	}

	@Override
	public void pasteToField() {
		if (systemClipboard.isDataFlavorAvailable(DataFlavor.stringFlavor)) {
			try {
				String contents = (String) systemClipboard.getData(DataFlavor.stringFlavor);
			} catch (UnsupportedFlavorException e) {
				/* shouldn't happen */
				System.err.println(e.getMessage());
			} catch (IOException e) {
				/* shouldn't happen */
				System.err.println(e.getMessage());
			}
		}
	}

	@Override
	public void recordsAccessible(boolean accessible) {
		boolean enable = accessible && (null != currentDatabase);
		menu.setDatabase(currentDatabase);
		menu.databaseAccessible(enable);
		menu.editMenuEnableModify(enable);
		menu.setSearchMenuEnabled(enable);
		if (!accessible && (null != resultsPanel)) {
			resultsPanel.clearSelection();
		}
	}

	private void databaseModifiable(boolean modifiable) {
		menu.fileMenuEnableModify(modifiable);
		menu.editMenuEnableModify(modifiable);
		menu.setOrganizeMenuEnableModify(modifiable);		
	}

	public void exportData(LibrisDatabase db) throws LibrisException {
		try {
			DatabaseExporter.guiExportFile(this, db);
		} catch (InputException e) {
			db.alert("Error exporting database", e);
		}
	}

	@Override
	public void fieldSelected(boolean selected) {
		if (null != menu) {
			RecordWindow crw = getCurrentRecordWindow();
			if ((null != crw) && crw.isEditable()) {
				menu.enableFieldValueOperations(selected);
			}
		}
	}

	@Override
	public void newFieldValue() {
		RecordWindow currentRecordWindow = getCurrentRecordWindow();
		final UiField selectedField = getSelectedField();
		if ((null != selectedField) &&selectedField.isMultiControl()) {
			GuiControl ctrl;
			try {
				ctrl = ((MultipleValueUiField) selectedField).addControl(true);
				ctrl.requestFocusInWindow();
				repaint();
			} catch (FieldDataException e) {
				currentDatabase.alert("Error displaying record", e);
			}
			currentRecordWindow.setModified(true);
		}
	}

	public void repaint() {
		resultsPanel.repaint();
		displayPanel.repaint();
	}

	@Override
	public void removeFieldValue() {
		RecordWindow currentRecordWindow = getCurrentRecordWindow();
		UiField f = getSelectedField();
		Field recordField = f.getRecordField();
		try {
			currentRecordWindow.enter();
			recordField.removeValue();
			currentRecordWindow.refresh();
			currentRecordWindow.setModified(true);
		} catch (LibrisException e) {
			currentDatabase.alert("Error removing field", e);
		}
	}

	@Override
	public void arrangeValues() {
		RecordWindow currentRecordWindow = getCurrentRecordWindow();
		UiField f = getSelectedField();
		final JFrame frame = new JFrame("Arrange values");
		if (f.isMultiControl()) {
			FieldValueArranger arrng = new FieldValueArranger(frame, (MultipleValueUiField) f);
			arrng.setVisible(true);
			currentRecordWindow.setModified(arrng.isFieldUpdated());
		}
	}

	public void setRecordDuplicateRecordEnabled(boolean enabled) {
		menu.setRecordDuplicateRecordEnabled(enabled);
	}

	public boolean duplicateRecord() {
		if (currentDatabase.isReadOnly()) {
			return false;
		}
		int rid = resultsPanel.getSelectedRecordId();
		DatabaseRecord newRecord = null;
		try {
			DatabaseRecord originalRecord = currentDatabase.getRecord(rid);
			newRecord = originalRecord.duplicate();
			newRecord.setEditable(true);
		} catch (LibrisException e1) {
			fatalError(e1);
		}
		try {
			displayPanel.addRecord(newRecord, true);
		} catch (Exception e) {
			alert("error creating new record", e);
			return false;
		}
		return true;
	}

	public void enterRecord() {
		displayPanel.doClose(null, true);
	}

	public void setRecordEnterRecordEnabled(boolean enabled) {
		menu.setRecordEnterRecordEnabled(enabled);
		
	}

	@Override
	public void setRecordName(NamedRecordList<DatabaseRecord> namedRecs) throws InputException {
		RecordWindow currentRecordWindow = getCurrentRecordWindow();
		if (null != currentRecordWindow){
			Record rec = currentRecordWindow.getRecord();
			String newName = JOptionPane.showInputDialog("New record name", rec.getName());
			if ((null != newName) && !newName.isEmpty()) {
				if (!Record.validateRecordName(newName)) {
					alert(newName+": invalid record name");
				} else {
					int recId = namedRecs.getId(newName);
					if (!RecordId.isNull(recId)) {
						alert(newName+" already used by record "+recId);
					} else {
						String oldName = rec.getName();
						if (!newName.equals(oldName)) {
							if ((null != oldName) && !oldName.isEmpty()) {
								namedRecs.remove(oldName);
							}
							currentRecordWindow.setModified(true);
							rec.setName(newName);
							resultsPanel.removeRecord(rec);
						}
						displayPanel.setCurrentRecordName(newName);
						repaint();
					}
				}
			}
		}
	}

	@Override
	public void setRecordArtifact() {
		RecordWindow currentRecordWindow = getCurrentRecordWindow();
		if (null != currentRecordWindow){
			DatabaseRecord rec = currentRecordWindow.getRecord();
			int artifactId = rec.getArtifactId();
			if (!RecordId.isNull(artifactId)) {
				int choice = confirm("Record has an artifact.  Replace it?");
				if (Dialogue.YES_OPTION != choice) {
					return;
				}
			}

			File artifactSourceFile;
			try {
				artifactSourceFile = selectArtifactFile();
				if (null != artifactSourceFile) {
					currentDatabase.addArtifact(rec, artifactSourceFile);
					artifactId = rec.getArtifactId();
				}
				currentRecordWindow.setModified(true);
			} catch (BackingStoreException | LibrisException | IOException e1) {
				throw new DatabaseError(e1);
			}
			ArtifactParameters artifactInfo = currentDatabase.getArtifactInfo(artifactId);
			currentRecordWindow.addArtiFactButton(artifactInfo);
		}
	}

private File selectArtifactFile() throws BackingStoreException {
	File result = null;
	Preferences librisPrefs = LibrisUiGeneric.getLibrisPrefs();
	String userDir = System.getProperty("user.dir");
	String lastArtifactDirectory = librisPrefs.get(LibrisConstants.LAST_ARTIFACT_SOURCE_DIR, userDir);
	JFileChooser chooser = new JFileChooser(lastArtifactDirectory);
	int option = chooser.showOpenDialog(this.mainframeContents);
	if (JFileChooser.APPROVE_OPTION == option) {
		result = chooser.getSelectedFile();
		lastArtifactDirectory = result.getParent();
		librisPrefs.put(LibrisConstants.LAST_ARTIFACT_SOURCE_DIR, lastArtifactDirectory);
		librisPrefs.flush();
	}
	return result;
}

public RecordWindow getCurrentRecordWindow() {
		RecordWindow currentRecordWindow = (null == displayPanel) ? null: displayPanel.getCurrentRecordWindow();
		return currentRecordWindow;
	} 
	
	public Record newChildRecord(Record currentRecord, int groupNum) {
		RecordWindow rw = newRecordWindow();
		 Record newRec = rw.getRecord();
		try {
			newRec.setParent(groupNum, currentRecord.getRecordId());
			rw.refresh();
		} catch (LibrisException e) {
			alert("Error creating record");
		}
		return newRec;
	}

	public void sendChooseDatabase() {
		menu.sendChooseDatabase();
	}

}
