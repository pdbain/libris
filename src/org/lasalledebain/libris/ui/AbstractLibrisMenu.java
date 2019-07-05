package org.lasalledebain.libris.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentListener;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

import org.lasalledebain.libris.GenericDatabase;
import org.lasalledebain.libris.LibrisDatabase;

public abstract class AbstractLibrisMenu {
	static final String OPEN_DATABASE = "Open database...";

	protected LibrisDatabase database;
	protected LibrisGui guiMain;
	protected JMenu fileMenu;
	protected JMenu editMenu;
	protected JMenuItem openDatabase;

	protected abstract JMenuBar createMenus();

	protected abstract JMenu createFileMenu();

	protected abstract JMenu createEditMenu();

	protected boolean isEditable() {
		return (null != database)  && !database.isReadOnly();
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

	public abstract boolean openDatabaseDialogue();

	public class OpenDatabaseListener implements ActionListener {
		public void actionPerformed(ActionEvent arg0) {
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
				guiMain.closeDatabase(false);
			}
		}		
	}
	public class QuitListener implements ActionListener {		
		@Override
		public void actionPerformed(ActionEvent arg0) {
			if (null != database) {
				guiMain.quit(false);
				// TODO check if there are new, modified records
			}
		}	
	}
}

