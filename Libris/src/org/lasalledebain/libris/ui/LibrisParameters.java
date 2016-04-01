package org.lasalledebain.libris.ui;

import java.io.File;

import org.lasalledebain.libris.exception.UserErrorException;

public class LibrisParameters {
	private boolean doIndexing;
	private boolean readOnly = false;
	private LibrisUi ui;
	private File databaseFile;
	private File auxiliaryDirectory;
	private UserErrorException errorException;
	private boolean gui;
	/* TODO 2 add cmdline args
	 * add export argument
	 * add reindex argument
	 */
	public UserErrorException getErrorException() {
		return errorException;
	}
	public LibrisParameters(String[] args) {
		parseArguments(args);
	}
	public LibrisParameters(LibrisUi ui, File dbFile) {
		this(ui, false, false, dbFile);
	}
	public LibrisParameters(LibrisUi ui, boolean readOnly, boolean doIndx,
			File dbFile) {
		this.ui = ui;
		this.readOnly = readOnly;
		this.doIndexing = doIndx;
		setDatabaseFile(dbFile);
	}
	
	public LibrisParameters(LibrisUiGeneric guiMain, boolean ro, File dbFile) {
		this(guiMain, ro, false, dbFile);
	}
	private void parseArguments(String[] args) {
		int i = 0; 
		boolean skip = false;
		errorException = null;
		while (i < args.length) {
			String arg = args[i];
			if (arg.equals("-r")) {
				readOnly = true;
			} else if (arg.equals("-i")) {
				doIndexing = true;
			} else if (arg.equals("-g")) {
				gui = true;
			} else if (arg.equals("-x")) {
				if ((i + 1) < args.length) {
					setAuxiliaryDirectory(args[i+1]);
				}
			} else if (!arg.startsWith("-")) {
				if (null == databaseFile) {
					setDatabaseFilePath(arg);
				} else {
					errorException = new UserErrorException("only one database name can be specified");
				}
			}
			++i;
		}
	}
	public LibrisUi getUi() {
		if (null == ui) {
			ui = new HeadlessUi();
		}
		return ui;
	}
	public void setUi(LibrisUi ui) {
		this.ui = ui;
	}
	public File setDatabaseFilePath(String dbFileName) {
		File dbFile = new File(dbFileName);
		return setDatabaseFile(dbFile);
	}
	private File setDatabaseFile(File dbFile) {
		databaseFile = dbFile;
		if (null == auxiliaryDirectory) {
			auxiliaryDirectory = databaseFile.getParentFile();
		}
		return databaseFile;
	}
	private File setAuxiliaryDirectory(String path) {
		return auxiliaryDirectory;
	}
	public File getAuxiliaryDirectory() {
		return auxiliaryDirectory;
	}
	public void setAuxiliaryDirectory(File auxiliaryDirectory) {
		this.auxiliaryDirectory = auxiliaryDirectory;
	}
	public boolean isDoIndexing() {
		return doIndexing;
	}
	public void setDoIndexing(boolean doIndexing) {
		this.doIndexing = doIndexing;
	}
	public File getDatabaseFile() {
		return databaseFile;
	}
	public boolean isReadOnly() {
		return readOnly;
	}
	public boolean isGui() {
		return gui;
	}
	public void isGui(boolean uiIsGui) {
		gui = uiIsGui;
	}
}
