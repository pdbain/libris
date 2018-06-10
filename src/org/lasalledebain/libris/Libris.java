package org.lasalledebain.libris;

import java.io.File;

import org.lasalledebain.libris.exception.DatabaseException;
import org.lasalledebain.libris.exception.InputException;
import org.lasalledebain.libris.exception.LibrisException;
import org.lasalledebain.libris.exception.UserErrorException;
import org.lasalledebain.libris.ui.HeadlessUi;
import org.lasalledebain.libris.ui.LibrisGui;
import org.lasalledebain.libris.ui.LibrisUi;
import org.lasalledebain.libris.ui.LibrisUiGeneric;

public class Libris {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		boolean readOnly = false;
		boolean isGui = true;
		String auxDirpath = null;
		String databaseFilePath = null;

		int i = 0; 
		File databaseFile = null;
		while (i < args.length) {
			String arg = args[i];
			if (arg.equals("-r")) {
				readOnly = true;
			} else if (arg.equals("-g")) {
				isGui = true;
			} else if (arg.equals("-c")) {
				isGui = false;
			} else if (arg.equals("-x")) {
				if ((i + 1) < args.length) {
					auxDirpath = args[i+1];
				}
			} else if (!arg.startsWith("-")) {
				if (null == databaseFile) {
					databaseFilePath = arg;
				} else {
					LibrisUiGeneric.cmdlineError("only one database name can be specified");
				}
			}
			++i;
		}

		if (isGui) {
			try {
				File dbFile = (null == databaseFilePath) ? null : new File(databaseFilePath);
				File auxDir = null;
				if ((null != dbFile) && (!dbFile.isFile())) {
					LibrisUiGeneric.cmdlineError(databaseFilePath+" is not a file");
				} else {
					if (null != auxDirpath) {
						auxDir = new File(auxDirpath);
					}
				}
				LibrisGui ui = new LibrisGui(dbFile, auxDir, readOnly);
				if (true)
				if (null != dbFile) {
					ui.openDatabase();
				} else {
					ui.sendChooseDatabase();
				}
			} catch (LibrisException e) {
				LibrisUiGeneric.cmdlineError("Cannot open Libris: "+e.getMessage());

			}
		}

	}

	public static LibrisDatabase buildAndOpenDatabase(File databaseFile) throws LibrisException {
		HeadlessUi ui = new HeadlessUi(databaseFile);
		buildIndexes(databaseFile, ui);
		
		LibrisDatabase result = new LibrisDatabase(databaseFile, null, ui, false);
		return result;
	}

	public static LibrisDatabase openDatabase(File databaseFile, LibrisUi ui) throws LibrisException {
		if (null == ui) {
			ui = new HeadlessUi(databaseFile);
		}
		LibrisDatabase db = new LibrisDatabase(databaseFile, null, ui, false);
		if (db.isDatabaseReserved() || !db.reserveDatabase()) {
			db = null;
		}
		return db;
	}

	public static boolean buildIndexes(File databaseFile, LibrisUi ui)
			throws UserErrorException, DatabaseException, InputException,
			LibrisException {
		LibrisDatabase db = new LibrisDatabase(databaseFile, null, ui, false);
		if (!db.isDatabaseReserved()) {
			if (!db.buildIndexes()) {
				return false;
			};
			return db.closeDatabase(false);
		} else {
			return false;
		}
	}

}
