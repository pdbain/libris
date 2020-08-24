package org.lasalledebain.libris;

import java.io.File;
import java.net.URL;
import java.util.Objects;

import org.lasalledebain.libris.exception.Assertion;
import org.lasalledebain.libris.exception.LibrisException;
import org.lasalledebain.libris.indexes.LibrisDatabaseConfiguration;
import org.lasalledebain.libris.records.PdfRecordImporter;
import org.lasalledebain.libris.ui.HeadlessUi;
import org.lasalledebain.libris.ui.LibrisGui;
import org.lasalledebain.libris.ui.LibrisHttpServer;
import org.lasalledebain.libris.ui.DatabaseUi;
import org.lasalledebain.libris.ui.LibrisUi;

public class Libris {
	enum IfType {UI_HTML, UI_CMDLINE, UI_WEB, UI_GUI} ;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		IfType myUiType = IfType.UI_GUI;
		boolean readOnly = false;
		boolean doRebuild = false;
		String auxDirPath = null;
		String artifactDirpath = null;
		String databaseFilePath = null;
		boolean status = true; /* success */

		int i = 0; 
		File databaseFile = null;
		while (i < args.length) {
			String arg = args[i];
			if (arg.equals("-r")) {
				readOnly = true;
			} else if (arg.equals("-b")) {
				doRebuild = true;
			} else if (arg.equals("-g")) {
				myUiType = IfType.UI_GUI;
			} else if (arg.equals("-c")) {
				myUiType = IfType.UI_CMDLINE;
			} else if (arg.equals("-w")) {
				myUiType = IfType.UI_WEB;
				// TODO add port and context for web
			} else if (arg.equals("-h")) {
				printHelpString();
				System.exit(0);
			} else if (arg.equals("-a")) {
				notImplemented(arg);
			} else if (arg.equals("-x")) {
				if ((i + 1) < args.length) {
					auxDirPath = args[i+1];
				}
				notImplemented(arg);
			} else if (!arg.startsWith("-")) {
				if (null == databaseFile) {
					databaseFilePath = arg;
				} else {
					LibrisUi.cmdlineError("only one database name can be specified");
				}
			}
			++i;
		}

		try {
			File dbFile = (null == databaseFilePath) ? null : new File(databaseFilePath);
			File auxDir = null;
			if ((null != dbFile) && (!dbFile.isFile())) {
				LibrisUi.cmdlineError(databaseFilePath+" is not a file");
			} else {
				if (null != auxDirPath) {
					auxDir = new File(auxDirPath);
				}
			}
			// TODO configurable aux dir & artifact dir
			LibrisUi<DatabaseRecord> ui = null;
			if (doRebuild) {
				Assertion.assertNotNullError("Database file not set", databaseFile);
				ui = new HeadlessUi(databaseFile, false);
				status = ui.rebuildDatabase();
			} else switch (myUiType) {
			case UI_GUI:
				ui = new LibrisGui(dbFile, readOnly);
				break;
			case UI_WEB:
				ui = new LibrisHttpServer(LibrisHttpServer.default_port, LibrisHttpServer.DEFAULT_CONTEXT);
				break;
			default:
				LibrisUi.cmdlineError(myUiType.toString()+" not implemented");
				break;
			}
			LibrisDatabaseConfiguration config = new LibrisDatabaseConfiguration(dbFile);
			if (null != dbFile) {
				ui.openDatabase(config);
			} else {
				ui.sendChooseDatabase();
			}
		} catch (LibrisException e) {
			LibrisUi.cmdlineError("Cannot open Libris: "+e.getMessage());
			status = false;
		}
		
		if (!status) {
			System.exit(1);
		}
	}

	private static void notImplemented(String arg) {
		System.err.println(arg+" not implemented");
		System.exit(1);
	}

	private static void printHelpString() {
		String helpString = "Libris: a record management system.\n"
				+ "Syntax:]\n"
				+ "libris -[c|g|w] [-x <path>] [-r] [-a <path>] <database file>\n"
				+ "-c: command-line\n"
				+ "-g: graphical user interface\n"
				+ "-w: start web server\n"
				+ "-r: open database read-only\n"
				+ "-x: specify auxiliary directory\n"
				+ "-a: specify arifact repository location";
		System.out.println(helpString);

	}

	public static LibrisDatabase buildAndOpenDatabase(File databaseFile) throws LibrisException {
		HeadlessUi ui = new HeadlessUi(databaseFile, false);
		buildIndexes(databaseFile, ui);

		LibrisDatabase result = ui.openDatabase();
		return result;
	}

	public static LibrisDatabase buildAndOpenDatabase(LibrisDatabaseConfiguration config) throws LibrisException {
		HeadlessUi ui = new HeadlessUi();
		buildIndexes(config, ui);
		LibrisDatabase result = ui.openDatabase(config);
		return result;
	}

	public static LibrisDatabase openDatabase(File databaseFile, LibrisUi ui) throws LibrisException {
		if (null == ui) {
			ui = new HeadlessUi(databaseFile, false);
		}
		LibrisDatabase db = ui.openDatabase(new LibrisDatabaseConfiguration(databaseFile));
		return db;
	}

	public static boolean buildIndexes(File databaseFile, DatabaseUi ui) throws LibrisException {
		LibrisDatabaseConfiguration config = new LibrisDatabaseConfiguration(databaseFile);
		return buildIndexes(config, ui);
	}

	public static boolean buildIndexes(LibrisDatabaseConfiguration config, DatabaseUi databaseUi) throws LibrisException {
		if (config.isReadOnly()) {
			databaseUi.alert("Cannot build indexes if read-only set");
			return false;
		}
		LibrisDatabase db = new LibrisDatabase(config, databaseUi);
		config.setLoadMetadata(true);
		if (!db.isDatabaseReserved()) {
			boolean buildResult = db.buildDatabase();
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
