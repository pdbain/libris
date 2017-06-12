package org.lasalledebain.libris.ui;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
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

import org.lasalledebain.libris.Libris;
import org.lasalledebain.libris.LibrisConstants;
import org.lasalledebain.libris.LibrisDatabase;
import org.lasalledebain.libris.Record;
import org.lasalledebain.libris.exception.DatabaseException;
import org.lasalledebain.libris.exception.InputException;
import org.lasalledebain.libris.exception.InternalError;
import org.lasalledebain.libris.exception.LibrisException;

/**
 * @author pdbain
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class LibrisMenu {
	public class DuplicateRecordListener implements ActionListener {

		public void actionPerformed(ActionEvent arg0) {
			guiMain.duplicateRecord();
		}
		
	}

	public void setDatabase(LibrisDatabase database) {
		this.database = database;
	}

	public class PasteListener implements ActionListener {

		private boolean pasteToField;

		public PasteListener(boolean pasteToField) {
			this.pasteToField = pasteToField;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			database.getUi().pasteToField();
		}

	}
	public static final int BROWSE_RECORD_POSITION = 3;
	private LibrisDatabase database;
	private LibrisGui guiMain;
	private JMenu fileMenu;
	private JMenu editMenu;
	private JMenu searchMenu;
	private JMenu recordMenu;
	private JMenu organizeMenu;
	private NewRecordListener newRecordHandler;
	private ViewRecordListener viewRecordHandler;
	private JMenuItem viewRecord;
	private JMenuItem enterRecord;
	private JCheckBoxMenuItem editRecord;
	private HashSet <JMenuItem> recordMenuEditCommands;
	private HashSet <JMenuItem> fileMenuModifyCommands;
	private HashSet <JMenuItem> fileMenuAccessCommands;
	private final ArrayList<JMenuItem> editMenuFieldValueCommands;
	private final ArrayList<JMenuItem> editMenuRecordCommands;
	private JMenuItem duplicateRecord;
	private JMenuItem childRecord;

	public LibrisMenu(LibrisGui gui) {
		this();
		guiMain = gui;
	}
	
	public LibrisMenu() {
		this.database = null;
		editMenuFieldValueCommands = new ArrayList<JMenuItem>(2);
		editMenuRecordCommands = new ArrayList<JMenuItem>(2);
	}
	
	protected JMenuBar createMenus() {
		JMenuBar menu = new JMenuBar();
		
		fileMenu = createFileMenu();
		menu.add(fileMenu);
		editMenu = createEditMenu();
		menu.add(editMenu);
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
	private JMenu createFileMenu() {
		JMenu fileMenu = new JMenu("File");
		fileMenuModifyCommands = new HashSet<JMenuItem>();
		fileMenuAccessCommands = new HashSet<JMenuItem>();
		JMenuItem openDatabase = new JMenuItem("Open database...");
		openDatabase.addActionListener(new OpenDatabaseListener());
		openDatabase.setAccelerator(getAcceleratorKeystroke('O'));
		fileMenu.add(openDatabase);
		
		JMenuItem saveDatabase = new JMenuItem("Save");
		saveDatabase.setAccelerator(getAcceleratorKeystroke('S'));
		saveDatabase.addActionListener(new SaveListener(false));
		JMenuItem saveDatabaseAs = new JMenuItem("Save as ...");
		saveDatabaseAs.setAccelerator(getAcceleratorKeystroke('S', java.awt.event.InputEvent.CTRL_DOWN_MASK));
		saveDatabaseAs.addActionListener(new SaveListener(true));
		fileMenu.add(saveDatabase);
		fileMenuModifyCommands.add(saveDatabase);
		fileMenu.add(saveDatabaseAs);
		fileMenuModifyCommands.add(saveDatabaseAs);
		
		JMenuItem importData = new JMenuItem("Import...");
		importData.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					DelimitedTextImporter.guiImportFile(database);
				} catch (DatabaseException exc) {
					guiMain.alert("Problem importing data", exc);
				}
			}
		});
		fileMenu.add(importData);
		fileMenuAccessCommands.add(importData);
		
		JMenuItem exportData = new JMenuItem("Export...");
		exportData.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					guiMain.exportData(database);
				} catch (LibrisException exc) {
					guiMain.alert("Problem exporting data", exc);
				}
			}
		});
		fileMenu.add(exportData);
		fileMenuAccessCommands.add(exportData);
		
		JMenuItem print = new JMenuItem("Print...");
		print.setAccelerator(getAcceleratorKeystroke('P'));
		fileMenu.add(print);
		fileMenuAccessCommands.add(print);
		
		JMenuItem closeWindow = new JMenuItem("Close window");
		closeWindow.addActionListener(new CloseWindowListener(false));
		closeWindow.setAccelerator(getAcceleratorKeystroke('W'));
		fileMenu.add(closeWindow);
		fileMenuAccessCommands.add(closeWindow);
		
		JMenuItem closeAllWindows = new JMenuItem("Close all windows");
		closeAllWindows.addActionListener(new CloseWindowListener(true));
		closeAllWindows.setAccelerator(getAcceleratorKeystroke('W', java.awt.event.InputEvent.SHIFT_DOWN_MASK));
		fileMenu.add(closeAllWindows);
		fileMenuAccessCommands.add(closeAllWindows);
		
		JMenuItem closeDatabase = new JMenuItem("Close database");
		closeDatabase.addActionListener(new CloseDatabaseListener());
		closeDatabase.setAccelerator(getAcceleratorKeystroke('W', java.awt.event.InputEvent.CTRL_DOWN_MASK));
		fileMenu.add(closeDatabase);
		fileMenuAccessCommands.add(closeDatabase);

		JMenuItem quit = new JMenuItem("Quit");
		quit.addActionListener(new QuitListener());
		quit.setAccelerator(getAcceleratorKeystroke('Q'));
		fileMenu.add(quit);
		for (JMenuItem m: fileMenuModifyCommands) {
			fileMenuAccessCommands.add(m);
		}
		return fileMenu;
	}
	
	/**
	 * Enable or disable File menu choices related to open files.
	 * @param accessible if database is opened
	 */
	public void fileMenuDatabaseAccessible(boolean accessible) {
		for (JMenuItem m: fileMenuAccessCommands) {
			m.setEnabled(accessible);
		}
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
	private JMenu createEditMenu() {
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
		recordName.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					database.getUi().setRecordName(database.getNamedRecords());
				} catch (InputException exc) {
					throw new InternalError("Error setting name", exc);
				}
			}
			
		});
		edMenu.add(recordName);
		recordName.setAccelerator(getAcceleratorKeystroke('N', java.awt.event.InputEvent.CTRL_DOWN_MASK));

		editMenuRecordCommands.add(recordName);
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
		removeValue.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {	
				database.getUi().removeFieldValue();
			}
		});
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
		
		edMenu.add("Delete");
		enableFieldValueOperations(false);
		return edMenu;
	}

	/**
	 * @param enabled
	 */
	void editMenuEnableModify(boolean enabled) {
		for (Component i: editMenuRecordCommands) {
			i.setEnabled(enabled);
		}
		editMenu.setEnabled(enabled);
	}

	private JMenu createRecordMenu() {
		recordMenuEditCommands = new HashSet<JMenuItem>();
		/* edit menu */
		JMenu recMenu = new JMenu("Record");
		JMenuItem newRecord = new JMenuItem("New record");
		newRecord.setAccelerator(getAcceleratorKeystroke('R'));
		newRecordHandler = new NewRecordListener();
		newRecord.addActionListener(newRecordHandler);
		recMenu.add(newRecord);
		recordMenuEditCommands.add(newRecord);
		
		viewRecord = new JMenuItem("View record");
		viewRecordHandler = new ViewRecordListener();
		recMenu.add(viewRecord);
		viewRecord.setEnabled(false);
		
		editRecord = new JCheckBoxMenuItem("Edit record");
		editRecord.setAccelerator(getAcceleratorKeystroke('E', java.awt.event.InputEvent.SHIFT_DOWN_MASK));
		editRecordListener = new EditRecordListenerImpl();
		editRecord.addActionListener(editRecordListener);
		recMenu.add(editRecord);
		editRecord.setEnabled(!guiMain.isReadOnly());
		editRecord.setState(false);
		
		enterRecord = new JMenuItem("Enter record");
		enterRecord.setAccelerator(getAcceleratorKeystroke('E'));
		enterRecord.addActionListener(new EnterRecordListener());
		recMenu.add(enterRecord);
		
		duplicateRecord = new JMenuItem("Duplicate record");
		recMenu.add(duplicateRecord);
		recordMenuEditCommands.add(duplicateRecord);
		duplicateRecord.setEnabled(false);
		duplicateRecord.addActionListener(new DuplicateRecordListener());
		
		childRecord = new JMenuItem("New child record");
		recMenu.add(childRecord);
		recordMenuEditCommands.add(childRecord);
		childRecord.setEnabled(false);
		childRecord.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				RecordWindow rw = guiMain.getCurrentRecordWindow();
				if (null != rw) {
					Record rec = rw.getRecord();
					guiMain.newChildRecord(rec, guiMain.getSelectedGroup().getGroupNum());
				}
			}
		});
		
		recMenu.add("Link record");
		return recMenu;
	}
	
	void setRecordEditEnabled(boolean enabled) {
		for (Component i: recordMenuEditCommands) {
			i.setEnabled(enabled);
		}
		recordMenu.setEnabled(enabled);
		setRecordDuplicateRecordEnabled(false);
		setRecordEnterRecordEnabled(false);
		setEditRecordState();
	}

	public void setEditRecordState() {
		editRecordListener.setEditingState();
	}

	private JMenu createSearchMenu() {
		JMenu srchMenu = new JMenu("Search");
		srchMenu.add("Find ...");
		srchMenu.add("Search ...");
		srchMenu.add("Quick search ...");
		return srchMenu;
	}

	void setSearchMenuEnabled(boolean enabled) {
		for (Component i: searchMenu.getMenuComponents()) {
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
				java.awt.Toolkit.getDefaultToolkit().getMenuShortcutKeyMask(), false);
	}
	
	private KeyStroke getAcceleratorKeystroke(char key, int modifier) {
		return KeyStroke.getKeyStroke(key, 
				java.awt.Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() | modifier, false);
	}
	
	public class OpenDatabaseListener implements ActionListener {

		public OpenDatabaseListener() {
		}

		public void actionPerformed(ActionEvent arg0) {
			JFileChooser chooser;
			File dbLocation = null;
			String userDir = System.getProperty("user.dir");
			Preferences librisPrefs = LibrisUiGeneric.getLibrisPrefs();
			String lastDbFileName = librisPrefs.get(LibrisDatabase.DATABASE_FILE, userDir);
			 dbLocation = new File(lastDbFileName);
			if (!dbLocation.exists()) {
				dbLocation = new File(userDir);
			}
			chooser = new JFileChooser(dbLocation);
			Box buttonPanel = new Box(BoxLayout.Y_AXIS);
			JCheckBox roCheckbox = new JCheckBox("Read-only", false);
			buttonPanel.add(roCheckbox);
			JCheckBox reIndexCheckbox = new JCheckBox("Build indexes", false);
			buttonPanel.add(reIndexCheckbox);
			chooser.setAccessory(buttonPanel);
			chooser.setSelectedFile(dbLocation);
			FileNameExtensionFilter librisFileFilter = new FileNameExtensionFilter(
					"Libris files",
					LibrisConstants.FILENAME_XML_FILES_SUFFIX, "xml");
					
			chooser.setFileFilter(librisFileFilter);
			if (dbLocation.isFile()) {
				chooser.setSelectedFile(dbLocation);
			}
			int option = chooser.showOpenDialog(guiMain.getMainFrame());
			if (option == JFileChooser.APPROVE_OPTION) {
				roCheckbox.isSelected();
				File sf = chooser.getSelectedFile();
				if (sf != null) {
					if (reIndexCheckbox.isSelected()) {
						try {
							Libris.buildIndexes(sf, guiMain);
						} catch (Exception e) {
							guiMain.alert("Error building indexes", e);
							return;
						}
						guiMain.alert("Database successfully indexed");
					} else {
						openDatabase(sf);
					}
				}
				try {
					librisPrefs.sync();
				} catch (BackingStoreException e) {
					guiMain.alert("cannot save preferences: "+e.getMessage());
				}
			}
		}

		private void openDatabase(File dbFile) {
			
			guiMain.setDatabaseFile(dbFile);
			try {
				database = guiMain.openDatabase();
			} catch (Exception e) {
				guiMain.alert("Error opening database", e);
				// TODO rebuild index
				return;
			}
			Preferences librisPrefs = LibrisUiGeneric.getLibrisPrefs();
			librisPrefs.put(LibrisDatabase.DATABASE_FILE, dbFile.getAbsolutePath());
			guiMain.getMainFrame().toFront();
		}
	}

	private class SaveListener implements ActionListener {
	// TODO 1 set database unmodified when saving
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
				}
			}
		}
	}

	public class CloseWindowListener implements ActionListener {
		boolean closeAllWindows;
		public CloseWindowListener(boolean allWindows) {
			closeAllWindows = allWindows;
		}

		@Override
		public void actionPerformed(ActionEvent arg0) {
			guiMain.close(closeAllWindows, false);
		}

	}

	public class CloseDatabaseListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent arg0) {
			if (null != database) {
				database.close();
			}
		}
		
	}

	public class QuitListener implements ActionListener {
	
		@Override
		public void actionPerformed(ActionEvent arg0) {
			if (null != database) {
				database.quit();
				// TODO check if there are new, modified records
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
			boolean wasEditable = guiMain.isEditable();
			try {
				guiMain.setEditable(!wasEditable);
				editRecord.setState(!wasEditable);
			} catch (LibrisException e) {
				guiMain.alert("Problem toggling editable", e);
			}
		}
		
		public void setEditingState() {
			boolean isEditable = false;
			if (null != guiMain) {
				isEditable = guiMain.isEditable();
			}
			editRecord.setState(isEditable);
		}
	}
	
	class RebuildIndexListener implements ActionListener {
		public void actionPerformed(ActionEvent arg0) {
			if (null != database) {
				File dbFile = database.getDatabaseFile();

				if (!database.close()) {
					guiMain.alert("Please save database before rebuildiing");
				}
				try {
					guiMain.rebuildDatabase();
				} catch (LibrisException e) {
					guiMain.alert("error rebuilding database", e);
				}
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

	private static final String DATABASE_FILE = "DATABASE_FILE";
	private EditRecordListenerImpl editRecordListener;
	public void enableFieldValueOperations(boolean selected) {
		for (JMenuItem m: editMenuFieldValueCommands) {
			m.setEnabled(selected);
		}
	}

	public void setRecordDuplicateRecordEnabled(boolean enabled) {
		duplicateRecord.setEnabled(enabled);
	}

}

