package org.lasalledebain.libris.ui;

import java.awt.Component;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Objects;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import org.lasalledebain.libris.LibrisDatabase;
import org.lasalledebain.libris.LibrisMetadata;
import org.lasalledebain.libris.exception.DatabaseException;
import org.lasalledebain.libris.exception.LibrisException;
import org.lasalledebain.libris.exception.OutputException;
import org.lasalledebain.libris.util.StringUtils;

@SuppressWarnings("serial")
public class DatabaseMenu extends JMenu {
	private LibrisDatabase database;
	private LibrisGui guiMain;

	private JMenuItem importItem;
	private JMenuItem exportItem;
	private JMenuItem createForkItem;
	private JMenuItem closeForkItem;
	private JMenuItem joinItem;

	public DatabaseMenu(LibrisGui gui) {
		super("Database");
		guiMain = gui;
		initialize();
		disableMenu();
	}

	public void setDatabase(LibrisDatabase database) {
		this.database = database;
	}

	private void initialize() {

		importItem = addMenuItem("Import...", e -> {
			try {
				DelimitedTextImporter.guiImportFile(database);
			} catch (DatabaseException exc) {
				guiMain.alert("Problem importing data", exc);
			}
		});
		add(importItem);

		exportItem = addMenuItem("Export...", e -> {
			try {
				guiMain.exportData(database);
			} catch (LibrisException exc) {
				guiMain.alert("Problem exporting data", exc);
			}
		});
		add(exportItem);


		createForkItem = addMenuItem("Create fork...", e -> {
			forkDialogue(database);
		});
		add(createForkItem);

		closeForkItem = addMenuItem("Close fork...", e -> {
			closeForkDialogue(database);
		});
		add(closeForkItem);

		joinItem = addMenuItem("Join fork...", e -> {
			mergeDialogue(database);
		});
		add(joinItem);
	}

	private JMenuItem addMenuItem(String itemTitle, ActionListener actListener) {
		JMenuItem mItem = new JMenuItem(itemTitle);
		mItem.addActionListener(actListener);
		return mItem;
	}

	private void mergeDialogue(LibrisDatabase db) {
		File incrementFile = (new LibrisFileChooser(guiMain, "Merge from increment file...")).chooseInputFile();
		try {
			db.importIncrement(incrementFile);
		} catch (FileNotFoundException | LibrisException e) {
			guiMain.alert("Cannot merge increment file "+incrementFile.getAbsolutePath(), e);
		}		
	}

	private void closeForkDialogue(LibrisDatabase db) {
		String incrementName = StringUtils.stripSuffix(db.getDatabaseFile().getAbsolutePath())+"_increment_"+LibrisMetadata.getCompactDateString()+".libi";
		LibrisFileChooser chooser = new LibrisFileChooser(guiMain, "Export increment file to ...");
		File incrementFile = chooser.chooseOutputFile(incrementName);
		if (Objects.isNull(incrementFile)) {
			return;
		}
		try {
			OutputStream incrementStream = new FileOutputStream(incrementFile);
			db.exportIncrement(incrementStream );
		} catch (OutputException | FileNotFoundException e) {
			guiMain.alert("Cannot export increment file "+incrementFile.getAbsolutePath(), e);
		}		
	}

	private void forkDialogue(LibrisDatabase db) {
		String forkName = StringUtils.stripSuffix(db.getDatabaseFile().getAbsolutePath())+"_export_"+LibrisMetadata.getCompactDateString()+".libr";
		LibrisFileChooser chooser = new LibrisFileChooser(guiMain, "Create database fork...");
		File forkFile = chooser.chooseOutputFile(forkName);
		if (Objects.isNull(forkFile)) {
			return;
		}
		try {
			FileOutputStream forkStream = new FileOutputStream(forkFile);
			db.exportFork(forkStream );
		} catch (FileNotFoundException | LibrisException e) {
			guiMain.alert("Cannot merge increment file "+forkFile.getAbsolutePath(), e);
		}		
	}

	public void databaseAccessible(LibrisDatabase db, boolean accessible) {
		if (accessible) {
			database = db;
			if (db.isFork()) {
				closeForkItem.setEnabled(true);
			} else {
				createForkItem.setEnabled(true);
				joinItem.setEnabled(db.isDatabaseReadOnly());				
			}
			exportItem.setEnabled(true);
			importItem.setEnabled(db.isDatabaseReadOnly());
		} else {
			disableMenu();
		}
	}

	private void disableMenu() {
		for ( Component i: getMenuComponents()) {
			i.setEnabled(false);
		}
	}
}
