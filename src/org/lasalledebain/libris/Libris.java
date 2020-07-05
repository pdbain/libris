package org.lasalledebain.libris;

import java.io.File;
import java.net.URL;
import java.util.Objects;

import org.lasalledebain.libris.exception.LibrisException;
import org.lasalledebain.libris.indexes.LibrisIndexConfiguration;
import org.lasalledebain.libris.records.PdfRecordImporter;
import org.lasalledebain.libris.ui.HeadlessUi;
import org.lasalledebain.libris.ui.LibrisGui;
import org.lasalledebain.libris.ui.LibrisUi;
import org.lasalledebain.libris.ui.LibrisUiGeneric;

public class Libris {
	enum IfType {UI_HTML, UI_CMDLINE, UI_WEB, UI_GUI} ;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		IfType myUiType = IfType.UI_GUI;
		boolean readOnly = false;
		String auxDirpath = null;
		String databaseFilePath = null;

		int i = 0; 
		File databaseFile = null;
		while (i < args.length) {
			String arg = args[i];
			if (arg.equals("-r")) {
				readOnly = true;
			} else if (arg.equals("-g")) {
				myUiType = IfType.UI_GUI;
			} else if (arg.equals("-c")) {
				myUiType = IfType.UI_CMDLINE;
			} else if (arg.equals("-h")) {
				printHelpString();
				System.exit(0);
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
			// TODO configurable aux dir
			LibrisGui ui = null;
			if (IfType.UI_GUI == myUiType) {
				ui = new LibrisGui(dbFile, readOnly);
			}
			if (null != dbFile) {
				LibrisDatabase db = ui.openDatabase();
			} else {
				ui.sendChooseDatabase();
			}
		} catch (LibrisException e) {
			LibrisUiGeneric.cmdlineError("Cannot open Libris: "+e.getMessage());

		}
	}

	private static void printHelpString() {
		String helpString = "Libris: a record management system.\n"
				+ "Syntax:]\n"
				+ "libris -[c|g|w] -x <path> -r <database file>\n"
				+ "-c: command-line\n"
				+ "-g: graphical user interface\n"
				+ "-w: start web server\n"
				+ "-r: open database read-only"
				+ "-x: specify auxiliary directory";
		System.out.println(helpString);

	}

	public static LibrisDatabase buildAndOpenDatabase(File databaseFile) throws LibrisException {
		HeadlessUi ui = new HeadlessUi(databaseFile, false);
		buildIndexes(databaseFile, ui);

		LibrisDatabase result = ui.openDatabase();
		return result;
	}

	public static LibrisDatabase buildAndOpenDatabase(LibrisIndexConfiguration config) throws LibrisException {
		buildIndexes(config);

		LibrisDatabase result = config.getDatabaseUi().openDatabase();
		return result;
	}

	public static LibrisDatabase openDatabase(File databaseFile, LibrisUi ui) throws LibrisException {
		if (null == ui) {
			ui = new HeadlessUi(databaseFile, false);
		}
		LibrisDatabase db = ui.openDatabase();
		return db;
	}

	public static boolean buildIndexes(File databaseFile, LibrisUi ui) throws LibrisException {
		LibrisIndexConfiguration config = new LibrisIndexConfiguration(databaseFile, ui);
		return buildIndexes(config);
	}

	public static boolean buildIndexes(LibrisIndexConfiguration config) throws LibrisException {
		LibrisDatabase db = new LibrisDatabase(config.getDatabaseFile(), false, config.getDatabaseUi());
		config.setLoadMetadata(true);
		if (!db.isDatabaseReserved()) {
			boolean buildResult = db.buildDatabase(config);
			if (!buildResult) {
				return false;
			}
			return db.closeDatabase(true);
		} else {
			return false;
		}
	}
	static {
		URL props = PdfRecordImporter.class.getClassLoader().getResource("commons-logging.properties");
		if (Objects.nonNull(props)) {
			String path = props.getPath();
			System.setProperty("java.util.logging.config.file", path);
		}
	}
}
