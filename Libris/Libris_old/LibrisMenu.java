/*
 * Created on Jun 25, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package Libris;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.prefs.BackingStoreException;

import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

import org.lasalledebain.libris.gui.BrowserWindow;

import com.sun.media.sound.Toolkit;

import Libris.LibrisException.ErrorIds;

/**
 * @author pdbain
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class LibrisMenu {
	/**
	 * @author pdbain
	 *
	 * TODO To change the template for this generated type comment go to
	 * Window - Preferences - Java - Code Style - Code Templates
	 */
	private LibrisDatabase database;
	public LibrisMenu(LibrisDatabase database) {
		this.database = database;
	}
	public LibrisMenu() {
		this.database = null;
	}
	
	protected JMenuBar createMenus() {
		JMenuBar menu = new JMenuBar();
		JMenu fileMenu = new JMenu("File");
		JMenuItem openDatabase = new JMenuItem("Open database...");
		openDatabase.addActionListener(new OpenDatabaseListener(database));
		openDatabase.setAccelerator(setAccelerator('O'));
		fileMenu.add(openDatabase);
		fileMenu.add("Save as ...");
		fileMenu.add("Save");
		fileMenu.add("Print ...");
		fileMenu.add("Quit");
		menu.add(fileMenu);

		JMenu editMenu = new JMenu("Edit");
		editMenu.add("Cut");
		editMenu.add("Copy");
		editMenu.add("Paste");
		editMenu.add("Delete");
		editMenu.addSeparator();
		JMenuItem newRecord = new JMenuItem("New record");
		newRecord.setAccelerator(setAccelerator('R'));
		newRecord.addActionListener(new NewRecordListener(database));
		editMenu.add(newRecord);
		editMenu.add("Duplicate record");
		editMenu.add("Link record");
		menu.add(editMenu);

		JMenu searchMenu = new JMenu("Search");
		searchMenu.add("Find ...");
		searchMenu.add("Search ...");
		searchMenu.add("Quick search ...");
		
		JMenuItem browseRecords = new JMenuItem("Browse records");
		browseRecords.addActionListener(new BrowseRecordListener(database));
		searchMenu.add(browseRecords);
		menu.add(searchMenu);

		JMenu organizeMenu = new JMenu("Organize");
		organizeMenu.add("Define fields ...");
		JMenuItem rebuildIndex = new JMenuItem("Rebuild index");
		rebuildIndex.addActionListener(new RebuildIndexListener(database));
		organizeMenu.add(rebuildIndex);
		organizeMenu.add("Quick search ...");
		menu.add(organizeMenu);
		
		return(menu);
	}
	private KeyStroke setAccelerator(char key) {
		return KeyStroke.getKeyStroke(key, 
				java.awt.Toolkit.getDefaultToolkit().getMenuShortcutKeyMask(), false);
	}
	
	public class OpenDatabaseListener implements ActionListener {

		private static final String DATABASE_DIR = "DATABASE_DIR";
		private LibrisDatabase database;
		public OpenDatabaseListener(LibrisDatabase database) {
			this.database = database;
 
		}

		public void actionPerformed(ActionEvent arg0) {
			JFileChooser chooser;
			String dbDirName = System.getProperty("user.home");
			if (dbDirName == null) {
				dbDirName = "/";
			}
			dbDirName = LibrisMain.librisPrefs.get(DATABASE_DIR, dbDirName);
			File dbDir = new File(dbDirName);
			if (dbDir == null) {
				chooser = new JFileChooser();
			} else {
				chooser = new JFileChooser(dbDir);

			}
			int option = chooser.showOpenDialog(database.getDbFrame());
			if (option == JFileChooser.APPROVE_OPTION) {
				File sf = chooser.getSelectedFile();
				String dbFileName = "null";
				if (sf != null) {
					dbFileName = sf.getAbsolutePath();
					database.setCurrentDBName(dbFileName);
					database.openDatabase(true);
				}
				LibrisMain.librisPrefs.put(DATABASE_DIR, chooser.getCurrentDirectory().getPath());
				try {
					LibrisMain.librisPrefs.sync();
				} catch (BackingStoreException e) {
					new LibrisException(ErrorIds.ERR_CANT_SAVE_PREFS);
				}
			}
		}

	}
	class NewRecordListener implements ActionListener {

		private LibrisDatabase database;
		private RecordWindow newRecWindow = null;
		public NewRecordListener(LibrisDatabase database) {
			this.database = database;
		}
		public void actionPerformed(ActionEvent arg0) {
			LibrisRecord record = null;
			try {
				record = database.newRecord();
			} catch (NullPointerException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			this.newRecWindow  = record.display(newRecWindow, true);

		}
		
	}
	
	class RebuildIndexListener implements ActionListener {
		private LibrisDatabase database;
		public RebuildIndexListener(LibrisDatabase database) {
			this.database = database;
			
		}

		public void actionPerformed(ActionEvent arg0) {
			try {
				database.RebuildIndex();
			} catch (LibrisException e) {
				// TODO Put up an alert if rebuildIndex fails
				e.printStackTrace();
			}
		}
	}
	public class BrowseRecordListener implements ActionListener {

		private LibrisDatabase database;
		public BrowseRecordListener(LibrisDatabase database) {
			this.database = database;
			new BrowserWindow(database);
		}

		/* (non-Javadoc)
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		 */
		public void actionPerformed(ActionEvent arg0) {
			RecordList list = new RecordList(database);
			list.readRecords();
		}

	}
}

