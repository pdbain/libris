package org.lasalledebain.libris.ui;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Objects;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.lasalledebain.libris.DatabaseArchive;
import org.lasalledebain.libris.DatabaseRecord;
import org.lasalledebain.libris.Libris;
import org.lasalledebain.libris.LibrisConstants;
import org.lasalledebain.libris.LibrisDatabase;
import org.lasalledebain.libris.Record;
import org.lasalledebain.libris.exception.DatabaseError;
import org.lasalledebain.libris.exception.InputException;
import org.lasalledebain.libris.exception.LibrisException;
import org.lasalledebain.libris.index.GroupDef;
import static java.util.Objects.nonNull;
import static org.junit.Assert.assertTrue;

public class LibrisMenu extends AbstractLibrisMenu implements LibrisConstants {

	private static final String ERROR_BUILDING_INDEXES_MESSAGE = "Error building indexes";

	public class DuplicateRecordListener implements ActionListener {

		public void actionPerformed(ActionEvent arg0) {
			guiMain.duplicateRecord();
		}

	}

	public class PasteListener implements ActionListener {

		public PasteListener(boolean pasteToField) {
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			database.getUi().pasteToField();
		}

	}
	public static final int BROWSE_RECORD_POSITION = 3;
	private DatabaseMenu dbMenu;
	private JMenu recordMenu;
	private JMenu searchMenu;
	private JMenu organizeMenu;
	private NewRecordListener newRecordHandler;
	private ViewRecordListener viewRecordHandler;
	private JMenuItem enterRecord, viewRecord;
	private JCheckBoxMenuItem editRecord;
	private HashSet <JMenuItem> fileMenuModifyCommands;
	private final ArrayList<JMenuItem> editMenuFieldValueCommands;
	private final ArrayList<JMenuItem> editMenuRecordCommands;
	private final ArrayList<JMenuItem> editMenuArtifactCommands;
	private JMenuItem duplicateRecord;
	private JMenuItem searchRecords;
	private final HashSet<JMenuItem> databaseAccessibleCommands, databaseNotAccessibleCommands;
	private JMenuItem openArtifactInfoMenuItem;

	public LibrisMenu(LibrisGui gui) {
		this();
		guiMain = gui;
	}

	public LibrisMenu() {
		this.database = null;
		databaseAccessibleCommands = new HashSet<JMenuItem>();
		databaseNotAccessibleCommands = new HashSet<JMenuItem>();

		editMenuFieldValueCommands = new ArrayList<JMenuItem>(2);
		editMenuRecordCommands = new ArrayList<JMenuItem>(2);
		editMenuArtifactCommands = new ArrayList<JMenuItem>(2);
	}

	@Override
	protected JMenuBar createMenus() {
		JMenuBar menu = new JMenuBar();

		fileMenu = createFileMenu();
		menu.add(fileMenu);
		editMenu = createEditMenu();
		menu.add(editMenu);
		dbMenu = new DatabaseMenu(guiMain);
		menu.add(dbMenu);
		recordMenu = createRecordMenu();
		menu.add(recordMenu);
		searchMenu = createSearchMenu();
		menu.add(searchMenu);

		organizeMenu = createOrganizeMenu();
		menu.add(organizeMenu);

		return(menu);
	}

	/**
	 * @param menu
	 * @return 
	 */
	@Override
	protected JMenu createFileMenu() {
		JMenu menu = new JMenu("File");
		fileMenuModifyCommands = new HashSet<JMenuItem>();
		
		openDatabase = new JMenuItem(OPEN_DATABASE);
		openDatabase.addActionListener(new BuildOpenDatabaseListener(false));
		openDatabase.setAccelerator(getAcceleratorKeystroke('O'));
		menu.add(openDatabase);
		databaseNotAccessibleCommands.add(openDatabase);

		buildDatabase = new JMenuItem(BUILD_DATABASE);
		buildDatabase.addActionListener(new BuildOpenDatabaseListener(true));
		menu.add(buildDatabase);
		databaseNotAccessibleCommands.add(buildDatabase);

		JMenuItem saveDatabase = new JMenuItem("Save");
		saveDatabase.setAccelerator(getAcceleratorKeystroke('S'));
		saveDatabase.addActionListener(new SaveListener(false));
		JMenuItem saveDatabaseAs = new JMenuItem("Save as ...");
		saveDatabaseAs.setAccelerator(getAcceleratorKeystroke('S', java.awt.event.InputEvent.CTRL_DOWN_MASK));
		saveDatabaseAs.addActionListener(new SaveListener(true));
		menu.add(saveDatabase);
		fileMenuModifyCommands.add(saveDatabase);
		menu.add(saveDatabaseAs);
		fileMenuModifyCommands.add(saveDatabaseAs);

		JMenuItem print = new JMenuItem("Print...");
		print.setAccelerator(getAcceleratorKeystroke('P'));
		menu.add(print);
		databaseAccessibleCommands.add(print);

		JMenuItem closeWindow = new JMenuItem("Close window");
		closeWindow.addActionListener(new CloseWindowListener(false));
		closeWindow.setAccelerator(getAcceleratorKeystroke('W'));
		menu.add(closeWindow);
		databaseAccessibleCommands.add(closeWindow);

		JMenuItem closeAllWindows = new JMenuItem("Close all windows");
		closeAllWindows.addActionListener(new CloseWindowListener(true));
		closeAllWindows.setAccelerator(getAcceleratorKeystroke('W', java.awt.event.InputEvent.SHIFT_DOWN_MASK));
		menu.add(closeAllWindows);
		databaseAccessibleCommands.add(closeAllWindows);

		closeDatabase = new JMenuItem("Close database");
		closeDatabase.addActionListener(new CloseDatabaseListener());
		closeDatabase.setAccelerator(getAcceleratorKeystroke('W', java.awt.event.InputEvent.CTRL_DOWN_MASK));
		menu.add(closeDatabase);
		databaseAccessibleCommands.add(closeDatabase);

		JMenuItem quit = new JMenuItem("Quit");
		quit.addActionListener(new QuitListener());
		quit.setAccelerator(getAcceleratorKeystroke('Q'));
		menu.add(quit);
		for (JMenuItem m: fileMenuModifyCommands) {
			databaseAccessibleCommands.add(m);
		}
		return menu;
	}

	/**
	 * Enable or disable File menu choices related to open files.
	 * @param accessible if database is opened
	 */
	public void databaseAccessible(boolean accessible) {
		for (JMenuItem m: databaseAccessibleCommands) {
			m.setEnabled(accessible);
		}
		for (JMenuItem m: databaseNotAccessibleCommands) {
			m.setEnabled(!accessible);
		}
		dbMenu.databaseAccessible(database, accessible);
	}

	public void fileMenuEnableModify(boolean enable) {
		for (JMenuItem m: fileMenuModifyCommands) {
			m.setEnabled(enable);
		}
	}

	/**
	 * @param menu
	 * @return 
	 */
	@Override
	protected JMenu createEditMenu() {
		/* edit menu */
		JMenu edMenu = new JMenu("Edit");
		edMenu.add("Cut");
		edMenu.add("Copy");

		JMenuItem paste = new JMenuItem("Paste");
		paste.addActionListener(new PasteListener(false));
		edMenu.add(paste);
		paste.setAccelerator(getAcceleratorKeystroke('V'));
		editMenuRecordCommands.add(paste);

		JMenuItem recordName = new JMenuItem("Record name...");
		editMenuFieldValueCommands.add(recordName);
		recordName.addActionListener(e ->  {
			try {
				database.getUi().setRecordName();
			} catch (InputException exc) {
				throw new DatabaseError("Error setting name", exc);
			}
		});
		edMenu.add(recordName);
		recordName.setAccelerator(getAcceleratorKeystroke('N', java.awt.event.InputEvent.CTRL_DOWN_MASK));
		recordName.setEnabled(false);

		JMenuItem newValue = new JMenuItem("New field value");
		newValue.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {	
				database.getUi().newFieldValue();
			}
		});
		edMenu.add(newValue);
		newValue.setAccelerator(getAcceleratorKeystroke('N', java.awt.event.InputEvent.SHIFT_DOWN_MASK));
		editMenuFieldValueCommands.add(newValue);
		newValue.setEnabled(false);

		JMenuItem removeValue = new JMenuItem("Remove field value");
		removeValue.addActionListener(e -> database.getUi().removeFieldValue());
		edMenu.add(removeValue);
		removeValue.setAccelerator(getAcceleratorKeystroke('X', java.awt.event.InputEvent.SHIFT_DOWN_MASK));
		editMenuFieldValueCommands.add(removeValue);

		JMenuItem arrangeValues = new JMenuItem("Arrange field values...");
		arrangeValues.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {	
				database.getUi().arrangeValues();
			}
		});
		edMenu.add(arrangeValues);
		editMenuFieldValueCommands.add(arrangeValues);

		JMenuItem pasteToField = new JMenuItem("Paste to field...");
		pasteToField.setAccelerator(getAcceleratorKeystroke('V', java.awt.event.InputEvent.SHIFT_DOWN_MASK));
		pasteToField.addActionListener(new PasteListener(true));
		edMenu.add(pasteToField);
		editMenuFieldValueCommands.add(pasteToField);


		addArtifactMenuItem = new JMenuItem("Set artifact...");
		addArtifactMenuItem.addActionListener(e -> guiMain.setRecordArtifact());
		edMenu.add(addArtifactMenuItem);
		editMenuArtifactCommands.add(addArtifactMenuItem);
		
		openArtifactInfoMenuItem = new JMenuItem("Open artifact information...");
		openArtifactInfoMenuItem.addActionListener(e -> guiMain.openArtifactInfo(getCurrentRecord()));
		edMenu.add(openArtifactInfoMenuItem);
		editMenuArtifactCommands.add(openArtifactInfoMenuItem);
		
		edMenu.add("Delete");
		enableFieldValueOperations(false);
		return edMenu;
	}

	/**
	 * @param enabled
	 */
	void editMenuEnableModify(boolean enabled) {
		boolean enableRepoCommands = hasRepo(enabled);
		for (Component i: editMenuRecordCommands) {
			i.setEnabled(enabled);
		}
		for (Component i: editMenuArtifactCommands) {
			i.setEnabled(enabled && enableRepoCommands);
		}
		editMenu.setEnabled(enabled);
	}

	public boolean hasRepo(boolean enabled) {
		LibrisDatabase currentDatabase = guiMain.currentDatabase;
		boolean hasRepo = enabled && Objects.nonNull(currentDatabase) && currentDatabase.hasDocumentRepository();
		return hasRepo;
	}

	public void enableFieldValueOperations(boolean enabled) {
		for (JMenuItem m: editMenuFieldValueCommands) {
			m.setEnabled(enabled);
		}
		boolean enableRepoCommands = hasRepo(enabled);
		for (Component i: editMenuArtifactCommands) {
			i.setEnabled(enabled && enableRepoCommands);
		}
	}

	public void enableRecordEdit(boolean enabled) {
		editRecord.setEnabled(enabled);
	}

	private JMenu createRecordMenu() {
		/* edit menu */
		JMenu recMenu = new JMenu("Record");
		JMenuItem newRecord = new JMenuItem("New record");
		newRecord.setAccelerator(getAcceleratorKeystroke('R'));
		newRecordHandler = new NewRecordListener();
		newRecord.addActionListener(newRecordHandler);
		recMenu.add(newRecord);

		viewRecord = new JMenuItem("View record");
		viewRecordHandler = new ViewRecordListener();
		viewRecord.addActionListener(viewRecordHandler);
		recMenu.add(viewRecord);
		viewRecord.setEnabled(false);

		editRecord = new JCheckBoxMenuItem("Edit record");
		editRecord.setAccelerator(getAcceleratorKeystroke('E', java.awt.event.InputEvent.SHIFT_DOWN_MASK));
		editRecordListener = new EditRecordListenerImpl();
		editRecord.addActionListener(editRecordListener);
		recMenu.add(editRecord);
		editRecord.setEnabled(!guiMain.isDatabaseReadOnly());
		editRecord.setState(false);

		enterRecord = new JMenuItem("Enter record");
		enterRecord.setAccelerator(getAcceleratorKeystroke('E'));
		enterRecord.addActionListener(new EnterRecordListener());
		recMenu.add(enterRecord);
		enterRecord.setEnabled(false);

		duplicateRecord = new JMenuItem("Duplicate record");
		recMenu.add(duplicateRecord);
		duplicateRecord.setEnabled(false);
		duplicateRecord.addActionListener(new DuplicateRecordListener());

		JMenuItem childRecord = new JMenuItem("New child record");
		recMenu.add(childRecord);
		childRecord.setEnabled(false);
		childRecord.addActionListener(e -> {
			boolean success = false;
			Record rec = getCurrentRecord();
			if (null != rec) {
				GroupDef selectedGroup = guiMain.getSelectedGroup();
				if (nonNull(selectedGroup)) {
					guiMain.newChildRecord(rec, selectedGroup.getGroupNum());
					success = true;
				}
			}
			if (!success) {
				guiMain.alert("Select record and group");
			}
		}
				);

		recordWindowItems = new JMenuItem[] {duplicateRecord, childRecord, editRecord};
		return recMenu;
	}

	public DatabaseRecord getCurrentRecord() {
		DatabaseRecordWindow rw = guiMain.getCurrentRecordWindow();
		DatabaseRecord rec = null;
		if (null != rw) {
			rec = rw.getRecord();
		}
		return rec;
	}

	void recordWindowOpened(boolean recordEditable) {
		if (isEditable()) {
			for (JMenuItem i: recordWindowItems) {
				i.setEnabled(true);
			}
			editRecord.setState(recordEditable);
		}
	}

	void recordWindowClosed() {
		for (JMenuItem i: recordWindowItems) {
			i.setEnabled(false);
		}
		editRecord.setState(false);
	}

	private JMenu createSearchMenu() {
		JMenu srchMenu = new JMenu("Search");
		srchMenu.add("Find in record...");
		searchRecords = srchMenu.add("Search records...");
		searchRecords.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				guiMain.createSearchDialogue();
			}
		});
		srchMenu.add("Quick search ...");
		return srchMenu;
	}

	void setSearchMenuEnabled(boolean enabled) {
		for (Component i: searchMenu.getMenuComponents()) {
			i.setEnabled(enabled);
		}
		searchMenu.setEnabled(enabled);
	}

	void setDatabaseMenuEnabled(boolean enabled) {
		for (Component i: dbMenu.getMenuComponents()) {
			i.setEnabled(enabled);
		}
		searchMenu.setEnabled(enabled);
	}

	private JMenu createOrganizeMenu() {
		JMenu orgMenu = new JMenu("Organize");
		orgMenu.add("Define fields ...");
		JCheckBoxMenuItem editLayouts = new JCheckBoxMenuItem("Edit layouts");
		editLayouts.addActionListener(new editLayoutListener(editLayouts));
		orgMenu.add(editLayouts);
		orgMenu.add("Quick search ...");
		return orgMenu;
	}

	void setOrganizeMenuEnableModify(boolean enabled) {
		for (Component i: organizeMenu.getMenuComponents()) {
			i.setEnabled(enabled);
		}
		organizeMenu.setEnabled(enabled);
	}

	private KeyStroke getAcceleratorKeystroke(char key) {
		return KeyStroke.getKeyStroke(key, 
				java.awt.Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx(), false);
	}

	private KeyStroke getAcceleratorKeystroke(char key, int modifier) {
		return KeyStroke.getKeyStroke(key, 
				java.awt.Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx() | modifier, false);
	}

	@Override
	boolean openDatabaseDialogue() {
		boolean result = false;
		JFileChooser chooser;
		File dbLocation = null;
		String userDir = System.getProperty("user.dir");
		Preferences librisPrefs = Libris.getLibrisPrefs();
		String lastDbFileName = librisPrefs.get(DATABASE_FILE, userDir);
		dbLocation = new File(lastDbFileName);
		if (!dbLocation.exists()) {
			dbLocation = new File(userDir);
		}
		chooser = new JFileChooser(dbLocation);
		Box buttonPanel = new Box(BoxLayout.Y_AXIS);
		JCheckBox roCheckbox = new JCheckBox("Read-only", false);
		buttonPanel.add(roCheckbox);
		chooser.setAccessory(buttonPanel);
		chooser.setFileFilter(librisFileFilter);
		if (dbLocation.isFile()) {
			chooser.setSelectedFile(dbLocation);
		}
		int option = chooser.showOpenDialog(guiMain.getMainFrame());
		if (option == JFileChooser.APPROVE_OPTION) {
			boolean readOnlySelected = roCheckbox.isSelected(); // TODO check read-only but no action
			File sf = chooser.getSelectedFile();
			if (sf != null) {
					result = openDatabaseImpl(sf, readOnlySelected);
			}
			try {
				librisPrefs.sync();
			} catch (BackingStoreException e) {
				guiMain.alert("cannot save preferences: "+e.getMessage());
			}
		}
		return result;
	}

	@Override
	boolean buildDatabaseDialogue() {
		boolean result = false;
		JFileChooser chooser;
		File archiveLocation = null;
		String userDir = System.getProperty("user.dir");
		archiveLocation = new File(userDir);
		if (!archiveLocation.exists()) {
			archiveLocation = new File(userDir);
		}
		chooser = new JFileChooser(archiveLocation);
		Box buttonPanel = new Box(BoxLayout.Y_AXIS);
		JCheckBox buildFromArchiveCheckbox = new JCheckBox("Build from archive", false);
		buttonPanel.add(buildFromArchiveCheckbox);
		chooser.setAccessory(buttonPanel);
		chooser.setFileFilter(librisFileFilter);
		if (archiveLocation.isFile()) {
			chooser.setSelectedFile(archiveLocation);
		}
		int option = chooser.showOpenDialog(guiMain.getMainFrame());
		if (option == JFileChooser.APPROVE_OPTION) {
			File selectedFile =  chooser.getSelectedFile();
			if (selectedFile != null) {
				String errorMessage = "Error reading TAR archive "+selectedFile.getPath();
				File databaseFile;
				try {
					if (buildFromArchiveCheckbox.isSelected()) {
						ArrayList<File> fileList = DatabaseArchive.getFilesFromArchive(selectedFile, selectedFile.getParentFile());
						errorMessage = ERROR_BUILDING_INDEXES_MESSAGE;
						assertTrue("Archive file is empty", fileList.size() > 0);
						databaseFile = fileList.get(0);
					} else {
						databaseFile = selectedFile;
					}
					Libris.buildIndexes(databaseFile, guiMain);
				} catch (Exception e) {
					guiMain.alert(errorMessage, e);
					return false;
				}
			}
			boolean doOpenDatabase = (Dialogue.yesNoDialog(guiMain.getMainFrame(), "Database built successfully\nOpen now?") == Dialogue.YES_OPTION);
			if (doOpenDatabase)
				result = openDatabaseImpl(selectedFile, false);
		}
		return result;
	}

	private boolean openDatabaseImpl(File dbFile, boolean readOnlySelected) {
		guiMain.setDatabaseFile(dbFile);
		try {
			guiMain.setOpenReadOnly(readOnlySelected);
			database = guiMain.openDatabase();
		} catch (Exception e) {
			guiMain.alert("Error opening database\n", e);
			return false;
		}
		if (null == database) return false;
		Preferences librisPrefs = Libris.getLibrisPrefs();
		librisPrefs.put(LibrisConstants.DATABASE_FILE, dbFile.getAbsolutePath());
		guiMain.getMainFrame().toFront();
		return true;
	}

	private class SaveListener implements ActionListener {
		boolean saveToOtherFile;
		SaveListener(boolean saveAs) {
			saveToOtherFile = saveAs;
		}
		@Override
		public void actionPerformed(ActionEvent arg0) {
			if (null != database) {
				if (saveToOtherFile) {
					database.getUi().alert("save as not implemented");
				} else {
					database.save();
					guiMain.updateUITitle();
				}
			}
		}
	}

	class NewRecordListener implements ActionListener {
		public void actionPerformed(ActionEvent arg0) {
			if (!database.isIndexed()) {
				guiMain.alert("Please index database");
			} else {
				guiMain.newRecord();
			}
		}
	}

	class EnterRecordListener implements ActionListener {
		public void actionPerformed(ActionEvent arg0) {
			guiMain.enterRecord();
		}
	}

	public void setViewViewSelectedRecordEnabled(boolean enabled) {
		viewRecord.setEnabled(enabled);
	}

	public void setRecordEnterRecordEnabled(boolean enabled) {
		enterRecord.setEnabled(enabled);
	}

	class ViewRecordListener implements ActionListener {

		public void actionPerformed(ActionEvent arg0) {
			guiMain.displaySelectedRecord();
		}

	}

	class EditRecordListenerImpl implements ActionListener {

		public void actionPerformed(ActionEvent evt) {
			boolean wasEditable = guiMain.isCurrentRecordWindowEditable();
			try {
				boolean result = guiMain.setRecordWindowEditable(!wasEditable);
				editRecord.setState(!result);
				if (!wasEditable && !result) {
					guiMain.alert(LibrisConstants.DATABASE_OR_RECORD_ARE_READ_ONLY);
				} else 
				enableFieldValueOperations(!wasEditable);
			} catch (LibrisException e) {
				guiMain.alert("Problem toggling editable", e);
			}
		}
	}

	class editLayoutListener implements ActionListener {
		private JCheckBoxMenuItem menuItem;

		public editLayoutListener(JCheckBoxMenuItem editLayouts) {
			menuItem = editLayouts;
		}

		public void actionPerformed(ActionEvent arg0) {
			guiMain.editLayouts(menuItem.isSelected());
		}
	}
	public JMenu getSearchMenu() {
		return searchMenu;
	}
	public NewRecordListener getNewRecordHandler() {
		return newRecordHandler;
	}

	private EditRecordListenerImpl editRecordListener;
	private JMenuItem recordWindowItems[];
	private JMenuItem addArtifactMenuItem;
	protected static final FileNameExtensionFilter librisFileFilter = new FileNameExtensionFilter(
			"Libris files",
			FILENAME_LIBRIS_FILES_SUFFIX, FILENAME_XML_FILES_SUFFIX, FILENAME_ARCHIVE_FILES_SUFFIX);
	public void setRecordDuplicateRecordEnabled(boolean enabled) {
		duplicateRecord.setEnabled(enabled);
	}

	public void setOpenArtifactInfoEnabled(boolean enabled) {
		openArtifactInfoMenuItem.setEnabled(enabled);
	}
}

