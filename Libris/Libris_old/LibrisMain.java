/*
 * Created on Apr 24, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package Libris;

import java.util.HashMap;
import java.util.prefs.Preferences;
import java.util.prefs.PreferencesFactory;

import javax.swing.JFrame;
import javax.swing.JMenuBar;


/**
 * @author pdbain
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class LibrisMain {

	static int verbosity = 0;

	private static JFrame mainFrame;

	private static LibrisMenu menus;

	public static Preferences librisPrefs;
	public static JMenuBar getMenubar() {
		return menuBar;
	}

	private static JMenuBar menuBar; 
	public static void main(String[] args) {
		System.setProperty("apple.laf.useScreenMenuBar","true");
		LibrisRecord record;
		LibrisDatabase database;
		librisPrefs = Preferences.userRoot();
				
		database = new LibrisDatabase();
		menus = new LibrisMenu(database);
		menuBar = menus.createMenus();
		mainFrame = database.getDbFrame();
		mainFrame.setJMenuBar(menuBar);
		mainFrame.setVisible(true);
		LibrisOptions options = new LibrisOptions();
		options.parseArgs(args);
		if (options.getCurrentDBName() != null) {
			database.openDatabase(options.getCurrentDBName(), true);
		}
	}
	
	public static void logMsg(int msgVerbosity, String msg) {
		if (msgVerbosity < verbosity)
			System.out.println(msg);
	}
	// TODO add scrollbars to the record window
}
