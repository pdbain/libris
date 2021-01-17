package org.lasalledebain.libris.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentListener;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

import org.lasalledebain.libris.LibrisDatabase;
import org.lasalledebain.libris.exception.DatabaseError;
import org.lasalledebain.libris.exception.DatabaseException;

public abstract class AbstractLibrisMenu {
	static final String OPEN_DATABASE = "Open database...";
	static final String BUILD_DATABASE = "Build database...";

	protected LibrisDatabase database;
	protected LibrisGui guiMain;
	protected JMenu fileMenu;
	protected JMenu editMenu;
	protected JMenuItem openDatabase;
	protected JMenuItem buildDatabase;
	protected JMenuItem closeDatabase;

	protected abstract JMenuBar createMenus();

	protected abstract JMenu createFileMenu();

	protected abstract JMenu createEditMenu();

	protected boolean isEditable() {
		return (null != database)  && !database.isDatabaseReadOnly();
	}

	public void sendChooseDatabase() {
		for (ComponentListener a: openDatabase.getComponentListeners()) {
			ActionEvent theEvent = new ActionEvent(a, ActionEvent.ACTION_PERFORMED, OPEN_DATABASE);
			java.awt.Toolkit.getDefaultToolkit().getSystemEventQueue().postEvent(
					theEvent);	
		}
	}

	public void setDatabase(LibrisDatabase database) {
		this.database = database;
	}

	abstract boolean openDatabaseDialogue();

	abstract boolean buildDatabaseDialogue();

	public class BuildOpenDatabaseListener implements ActionListener {
		private final boolean buildDatabaseSelected;

		public BuildOpenDatabaseListener(boolean doBuild) {
			buildDatabaseSelected = doBuild;
		}

		public void actionPerformed(ActionEvent arg0) {
			if (buildDatabaseSelected)
				buildDatabaseDialogue();
			else
				openDatabaseDialogue();
		}
	}

	public class CloseWindowListener implements ActionListener {
		boolean closeAllWindows;
		public CloseWindowListener(boolean allWindows) {
			closeAllWindows = allWindows;
		}
		@Override
		public void actionPerformed(ActionEvent arg0) {
			guiMain.closeWindow(closeAllWindows);
		}

	}

	public class CloseDatabaseListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			if (null != database) {
				try {
					guiMain.closeDatabase(false);
				} catch (DatabaseException e) {
					throw new DatabaseError(e);
				}
			}
		}		
	}
	public class QuitListener implements ActionListener {		
		@Override
		public void actionPerformed(ActionEvent arg0) {

			try {
				if (guiMain.quit(false)) System.exit(0);
			} catch (DatabaseException e) {
				throw new DatabaseError(e);
			}
		}
	}
}

