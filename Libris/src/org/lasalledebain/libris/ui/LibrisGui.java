package org.lasalledebain.libris.ui;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.logging.Level;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;

import org.lasalledebain.libris.DatabaseAttributes;
import org.lasalledebain.libris.DatabaseRecordList;
import org.lasalledebain.libris.Field;
import org.lasalledebain.libris.LibrisDatabase;
import org.lasalledebain.libris.Record;
import org.lasalledebain.libris.RecordId;
import org.lasalledebain.libris.exception.DatabaseException;
import org.lasalledebain.libris.exception.FieldDataException;
import org.lasalledebain.libris.exception.InputException;
import org.lasalledebain.libris.exception.LibrisException;

import com.apple.eawt.QuitHandler;
import com.apple.eawt.QuitResponse;
import com.apple.eawt.AppEvent.QuitEvent;

public class LibrisGui extends LibrisUiGeneric {

	private static final String CONTENT_PANE_HEIGHT = "CONTENT_PANE_HEIGHT";
	private static final String CONTENT_PANE_WIDTH = "CONTENT_PANE_WIDTH";
	private static final String CONTENT_PANE_DIVIDER = "CONTENT_PANE_DIVIDER_LOCATION";
	private static final long serialVersionUID = -6063661235767540726L;
	LibrisMenu menu;
	private JMenuBar menuBar;
	private JTabbedPane selectorPanel;
	private JPanel filterView;
	private JPanel navigateView;
	protected BrowserWindow resultsPanel;
	protected RecordDisplayPanel displayPanel;
	private JSplitPane contentPane;
	private JSplitPane mainWindow;
	private JPanel layoutEditPane;
	private Clipboard systemClipboard;
	private boolean databaseSelected = false;
	boolean readOnly = false;
	private JFrame mainFrame;
	private Component mainframeContents;
	private ImageIcon searchLogo;
	public JFrame getMainFrame() {
		return mainFrame;
	}

	public LibrisGui(String[] args) throws DatabaseException {
		super(args);
		if (isParameterError()) {
			System.exit(1);
		}
		initializeGui();
	}

	public LibrisGui(LibrisParameters parameters) throws DatabaseException {
		this.parameters = parameters;
		parameters.setUi(this);
		parameters.isGui(true);
		if (isParameterError()) {
			System.exit(1);
		}
		initializeGui();
	}

	private void initializeGui() throws DatabaseException {
		com.apple.eawt.Application.getApplication().setQuitHandler(new QuitHandler() {
			@Override
			public void handleQuitRequestWith(QuitEvent quitEvt, QuitResponse quitResp) {
                if((null == database) || database.close()) {
                	close(true, true);
                    quitResp.performQuit();
                } else {
               	quitResp.cancelQuit();
                }
			}});
		mainFrame = new JFrame();
		readOnly = parameters.isReadOnly();
		databaseSelected = isDatabaseSelected();
		systemClipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		System.setProperty("Title", "Libris");
		menu = new LibrisMenu(this);
		menuBar = menu.createMenus();
		recordsAccessible(databaseSelected && !readOnly);
		databaseModifiable(databaseSelected && !readOnly);
			createPanes(!databaseSelected);
		mainFrame.setVisible(true);
		mainFrame.setJMenuBar(menuBar);
		mainFrame.addWindowListener(new closeListener());
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

	public LibrisDatabase openDatabase(LibrisParameters params) {
		super.openDatabase(params);
		menu.setDatabase(database);
		getMainFrame().toFront();
		return database;
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
		 if (null == searchLogo) {
			ClassLoader l = this.getClass().getClassLoader();
			 URL iconUrl = l.getResource("magnifier12.png");
			searchLogo = new ImageIcon(iconUrl);
		}
		if (!empty) {
			selectorPanel = new JTabbedPane();
			filterView = new JPanel();
			JLabel icon = new JLabel(searchLogo);
			filterView.add(icon);
			filterView.add(new JTextArea("Filter"));
			selectorPanel.add("Filter", filterView);
			navigateView = new JPanel();
			selectorPanel.add("Navigate", navigateView);
			navigateView.add(new JTextArea("selectorPanel"));

			resultsPanel = new BrowserWindow(database, this);

			displayPanel = new RecordDisplayPanel(database, this);

			contentPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, resultsPanel, displayPanel);
			contentPane.setOneTouchExpandable(true);
			Preferences prefs = getLibrisPrefs();
			int compwidth = prefs.getInt(CONTENT_PANE_WIDTH, 1200);
			int compheight = prefs.getInt(CONTENT_PANE_HEIGHT, 650);
			contentPane.setPreferredSize(new Dimension(compwidth, compheight));
			int divloc = prefs.getInt(CONTENT_PANE_DIVIDER, 285);
			contentPane.setDividerLocation(divloc);

			mainWindow = new JSplitPane(JSplitPane.VERTICAL_SPLIT, selectorPanel, contentPane);
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
		Record record = null;
		try {
			record = database.newRecord();
			record.setEditable(true);
		} catch (LibrisException e1) {
			fatalError(e1);
		}
		try {
			displayPanel.addRecord(record, true);
		} catch (Exception e) {
			alert("error creating new record", e);
		}
		return record;
	}
	
	@Override
	public void addRecord(Record newRecord) throws DatabaseException {
		if (null != database) {
			resultsPanel.addRecord(newRecord);
		}
	}
	
	@Override
	public void put(Record newRecord) throws DatabaseException {
		resultsPanel.addRecord(newRecord);
	}

	public void displayRecord(RecordId recId) throws LibrisException {
		displayPanel.displayRecord(recId);
	}

	@Override
	public void alert(String msg, Exception e) {
		String errorString = msg;
		String emessage = "";
		if (null != e) {
			emessage = e.getMessage();
			if (null != emessage) {
				errorString += ": "+emessage;
			} else {
				StackTraceElement location = e.getStackTrace()[0];
				errorString += " at "+location.toString();
			}
		}
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
	public void databaseOpened (LibrisDatabase db) throws DatabaseException {
		this.database = db;
		menu.editMenuEnableModify(readOnly);
		destroyWindow(true);
		createPanes(false);
		updateUITitle(db, false);

		displayPanel.addLayouts(db.getLayouts());
		DatabaseRecordList list = new DatabaseRecordList(db);
		resultsPanel.initialize(list);
		recordsAccessible(!readOnly);
		databaseModifiable(!readOnly);
	}

	@Override
	public void databaseClosed () {
		destroyWindow(true);
		recordsAccessible(false);
		databaseModifiable(false);
	}

	public boolean isEditable() {
		RecordWindow rw = displayPanel.getCurrentRecordWindow();
		boolean editable = false;
		if (null != rw) {
			editable = rw.isEditable();
		}
		return editable;
	}
	
	public void setEditable(boolean editable) throws LibrisException {
		RecordWindow rw = displayPanel.getCurrentRecordWindow();
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

	public JTabbedPane getSelectorPanel() {
		return selectorPanel;
	}

	public JPanel getFilterView() {
		return filterView;
	}

	public JPanel getNavigateView() {
		return navigateView;
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
			if (null != database) {
				database.quit();
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

	public static void  main(String[] args) {
		System.setProperty("apple.laf.useScreenMenuBar","true");
		System.setProperty("apple.eawt.quitStrategy system property", "CLOSE_ALL_WINDOWS");
		try {
			new LibrisGui(args);			
		} catch (DatabaseException e) {
			System.exit(1);
		}		
	}

	public static LibrisUi  launchGui(LibrisParameters parameters) {
		System.setProperty("apple.laf.useScreenMenuBar","true");
		System.setProperty("apple.eawt.quitStrategy system property", "CLOSE_ALL_WINDOWS");
		LibrisUiGeneric gui = null;
		try {
			gui = new LibrisGui(parameters);			
		} catch (DatabaseException e) {
			System.exit(1);
		}
		return gui;
	}

	@Override
	public void recordsAccessible(boolean accessible) {
		boolean enable = accessible && (null != database);
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
			RecordWindow crw = displayPanel.getCurrentRecordWindow();
			if ((null != crw) && crw.isEditable()) {
				menu.enableFieldValueOperations(selected);
			}
		}
	}

	@Override
	public void newFieldValue() {
		RecordWindow currentRecordWindow = displayPanel.getCurrentRecordWindow();
		final UiField selectedField = getSelectedField();
		if (null != selectedField) {
			GuiControl ctrl;
			try {
				ctrl = selectedField.addControl(true);
				ctrl.requestFocusInWindow();
				repaint();
			} catch (FieldDataException e) {
				database.alert("Error displaying record", e);
			}
			currentRecordWindow.setModified(true);
		}
	}

	public void repaint() {
		displayPanel.repaint();
	}

	@Override
	public void removeFieldValue() {
		RecordWindow currentRecordWindow = displayPanel.getCurrentRecordWindow();
		UiField f = getSelectedField();
		Field recordField = f.getRecordField();
		try {
			currentRecordWindow.enter();
			recordField.removeValue();
			currentRecordWindow.refresh();
			currentRecordWindow.setModified(true);
		} catch (LibrisException e) {
			database.alert("Error removing field", e);
		}
	}

	@Override
	public void arrangeValues() {
		RecordWindow currentRecordWindow = displayPanel.getCurrentRecordWindow();
		UiField f = getSelectedField();
		final JFrame frame = new JFrame("JDialog Demo");

		FieldValueArranger arrng = new FieldValueArranger(frame, f);
		arrng.setVisible(true);
		currentRecordWindow.setModified(arrng.isFieldUpdated());
	}

	public void setRecordDuplicateRecordEnabled(boolean enabled) {
		menu.setRecordDuplicateRecordEnabled(enabled);
	}

	public void duplicateRecord() {
		RecordId rid = resultsPanel.getSelectedRecordId();
		Record newRecord = null;
		try {
			Record originalRecord = database.getRecord(rid);
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

}
