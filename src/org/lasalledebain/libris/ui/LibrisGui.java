package org.lasalledebain.libris.ui;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.logging.Level;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;

import org.lasalledebain.libris.DatabaseAttributes;
import org.lasalledebain.libris.Field;
import org.lasalledebain.libris.LibrisDatabase;
import org.lasalledebain.libris.NamedRecordList;
import org.lasalledebain.libris.Record;
import org.lasalledebain.libris.RecordId;
import org.lasalledebain.libris.RecordList;
import org.lasalledebain.libris.exception.DatabaseException;
import org.lasalledebain.libris.exception.FieldDataException;
import org.lasalledebain.libris.exception.InputException;
import org.lasalledebain.libris.exception.LibrisException;

import com.apple.eawt.QuitHandler;
import com.apple.eawt.QuitResponse;
import com.apple.eawt.AppEvent.QuitEvent;

public class LibrisGui extends LibrisWindowedUi {
	private static final String CONTENT_PANE_HEIGHT = "CONTENT_PANE_HEIGHT";
	private static final String CONTENT_PANE_WIDTH = "CONTENT_PANE_WIDTH";
	private static final String CONTENT_PANE_DIVIDER = "CONTENT_PANE_DIVIDER_LOCATION";
	private static final long serialVersionUID = -6063661235767540726L;
	LibrisMenu menu;
	private JMenuBar menuBar;
	protected BrowserWindow resultsPanel;
	protected RecordDisplayPanel displayPanel;
	private JSplitPane contentPane;
	private JSplitPane mainWindow;
	private JPanel layoutEditPane;
	private Clipboard systemClipboard;
	private boolean databaseSelected = false;
	boolean readOnly = false;
	private Component mainframeContents;
	public LibrisGui(File databaseFile, File auxDirectory, boolean readOnly) throws LibrisException {
		super(databaseFile, auxDirectory, readOnly);
		System.setProperty("apple.laf.useScreenMenuBar","true");
		System.setProperty("apple.eawt.quitStrategy system property", "CLOSE_ALL_WINDOWS");
		initializeGui();
	}

	private void initializeGui() throws DatabaseException {
		com.apple.eawt.Application.getApplication().setQuitHandler(new QuitHandler() {
			@Override
			public void handleQuitRequestWith(QuitEvent quitEvt, QuitResponse quitResp) {
				if((null == currentDatabase) || currentDatabase.close()) {
					close(true, true);
					quitResp.performQuit();
				} else {
					quitResp.cancelQuit();
				}
			}});
		databaseSelected = isDatabaseSelected();
		systemClipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		System.setProperty("Title", "Libris");
		menu = new LibrisMenu(this);
		menuBar = menu.createMenus();
		recordsAccessible(databaseSelected && !readOnly);
		databaseModifiable(databaseSelected && !readOnly);
		createPanes(!databaseSelected);
		mainFrame.setJMenuBar(menuBar);
		mainFrame.addWindowListener(new closeListener());
		mainFrame.setVisible(true);
	}

	@Override
	public void exit() {
		destroyWindow(false);
	}

	public void updateUITitle(LibrisDatabase db, boolean isModified) {
		String databaseName = "no database open";
		if (null != db) {
			DatabaseAttributes databaseAttributes = db.getAttributes();
			databaseName = databaseAttributes.getDatabaseName();
			if (isModified) {
				databaseName = databaseName+"*";
			}
		}
		setTitle(databaseName);
	}

	@Override
	public void indicateModified(boolean isModified) {
		String title = uiTitle;
		if (isModified && !title.endsWith("*")) {
			title = title+"*";
		}
		setTitle(title);
	}

	public void setTitle(String title) {
		super.setTitle(title);
		mainFrame.setTitle(title);
	}
	
	public boolean chooseDatabase() {
		return getMenu().openDatabaseDialogue();
	}

	public LibrisDatabase openDatabase() {
		super.openDatabase();
		menu.setDatabase(currentDatabase);
		getMainFrame().toFront();
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
	
	private void destroyWindow(boolean retain) {
		if (null != mainWindow) {
			if (!isReadOnly()) {
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
					uiLogger.log(Level.WARNING, "exception in destroyWindow", e);
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
	public Record newRecord() {
		Record rec = null;
		RecordWindow rw = newRecordWindow();
		if (null != rw) {
			rec = rw.getRecord();
		}
		return rec;
	}
	
	public RecordWindow newRecordWindow() {
		Record record = null;
		RecordWindow rw = null;
		try {
			record = currentDatabase.newRecord();
			record.setEditable(true);
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
		menu.setRecordEditEnabled(true);
	}

	FilterDialogue createSearchDialogue() {
		return new FilterDialogue(currentDatabase, getMainFrame(), resultsPanel);
	}
	@Override
	public void alert(String msg, Exception e) {
		StringBuilder buff = new StringBuilder(msg);
		LibrisDatabase.log(Level.WARNING, e.getMessage(), e);
		String emessage = "";
		if (null != e) {
			emessage = LibrisUiGeneric.formatConciseStackTrace(e, buff);
		}
		String errorString = buff.toString();
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		e.printStackTrace(new PrintStream(bos));
		try {
			uiLogger.log(Level.WARNING, bos.toString(Charset.defaultCharset().name()));
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		}
		Throwable c = e;
		while (null != ( c = c.getCause())) {
			errorString += '\n'+c.getMessage();
		}
		LibrisDatabase.librisLogger.log(Level.FINE, emessage, e);
		alert(errorString);
	}

	public void fatalError(Exception e) {
		String msg = "Fatal error: ";
		fatalError(e, msg);
	}

	@Override
	public void close(boolean allWindows, boolean closeGui) {
		if (null != displayPanel) {
			displayPanel.close(allWindows);
		}
		if (closeGui) {
			destroyWindow(true);
		}
	}

	@Override
	public void databaseOpened(LibrisDatabase db) throws DatabaseException {
		super.databaseOpened(db);
		menu.editMenuEnableModify(readOnly);
		destroyWindow(true);
		createPanes(false);
		updateUITitle(db, false);

		displayPanel.addLayouts(db.getLayouts());
		RecordList list = db.getRecords();
		resultsPanel.initialize(list);
		recordsAccessible(!readOnly);
		databaseModifiable(!readOnly);
	}

	@Override
	public void databaseClosed () {
		super.databaseClosed();
		destroyWindow(true);
		recordsAccessible(false);
		databaseModifiable(false);
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
	
	public void setEditable(boolean editable) throws LibrisException {
		RecordWindow rw = getCurrentRecordWindow();
		boolean wasEditable = rw.isEditable();
		rw.setEditable(!wasEditable);
		rw.refresh();
		repaint();
		rw.setModified(false);
	}

	public boolean isReadOnly() {
		return readOnly;
	}

	public void setReadOnly(boolean readOnly) {
		this.readOnly = readOnly;
	}

	@Override
	public void alert(String msg) {
		JOptionPane.showMessageDialog(mainFrame, msg);
	}

	class closeListener implements WindowListener {

		
		@Override
		public void windowActivated(WindowEvent e) {
			return;
		}

		@Override
		public void windowClosed(WindowEvent e) {
			return;
			}

		@Override
		public void windowClosing(WindowEvent e) {
			if (null != currentDatabase) {
				currentDatabase.quit();
			}
		}

		@Override
		public void windowDeactivated(WindowEvent e) {
			return;
			}

		@Override
		public void windowDeiconified(WindowEvent e) {
			return;
			}

		@Override
		public void windowIconified(WindowEvent e) {
			return;
			}

		@Override
		public void windowOpened(WindowEvent e) {
			return;
			}
		
	}

	public void displaySelectedRecord() {
		resultsPanel.displaySelectedRecord();
	}

	public void setViewViewSelectedRecordEnabled(boolean enabled) {
		menu.setViewViewSelectedRecordEnabled(enabled);
	}

	@Override
	public int confirm(String message) {
		return Dialogue.yesNoDialog(mainFrame, message);
	}

	public int confirmWithCancel(String msg) {
		return Dialogue.yesNoCancelDialog(mainFrame, msg);
	}

	@Override
	public String SelectSchemaFile(String schemaName) throws DatabaseException {
		// TODO Auto-generated method stub
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
		menu.fileMenuDatabaseAccessible(accessible);
		menu.editMenuEnableModify(enable);
		menu.setRecordEditEnabled(enable);
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
			DatabaseExporter.guiExportFile(db);
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

	public void duplicateRecord() {
		int rid = resultsPanel.getSelectedRecordId();
		Record newRecord = null;
		try {
			Record originalRecord = currentDatabase.getRecord(rid);
			newRecord = originalRecord.duplicate();
			newRecord.setEditable(true);
		} catch (LibrisException e1) {
			fatalError(e1);
		}
		try {
			displayPanel.addRecord(newRecord, true);
		} catch (Exception e) {
			alert("error creating new record", e);
		}
	}

	public void enterRecord() {
		displayPanel.doClose(null, true);
	}

	public void setRecordEnterRecordEnabled(boolean enabled) {
		menu.setRecordEnterRecordEnabled(enabled);
		
	}

	@Override
	public void setAuxiliaryDirectory(File auxDir) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setRecordName(NamedRecordList namedRecs) throws InputException {
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
						// TODO change name in record browser and tab
					}
				}
			}
		}
	}

	public RecordWindow getCurrentRecordWindow() {
		RecordWindow currentRecordWindow = displayPanel.getCurrentRecordWindow();
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

	@Deprecated
	class RecordNameDialogue implements ActionListener {
		final LibrisDatabase dBase;
		public RecordNameDialogue(LibrisDatabase db) {
			dBase = db;
		}
		@Override
		public void actionPerformed(ActionEvent e) {
			dBase.getUi().getSelectedField();
		}
		
	}

	@Override
	void enableNewChild() {
		// TODO Auto-generated method stub
		
	}

	public static void makeLabelledControl(JPanel parentPanel, Component theControl, String labelText, boolean vertical) {
		JPanel controlPanel = new JPanel();
		if (vertical) {
			BoxLayout layout = new BoxLayout(controlPanel, BoxLayout.Y_AXIS);
			controlPanel.setLayout(layout);
		}
		JLabel l = new JLabel(labelText);
		l.setLabelFor(theControl);
		controlPanel.add(l);
		controlPanel.add(theControl);
		parentPanel.add(controlPanel);
	}

}
