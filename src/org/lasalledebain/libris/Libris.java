package org.lasalledebain.libris;

import static org.lasalledebain.libris.ui.LibrisUi.cmdlineError;

import java.io.File;
import java.net.URL;
import java.util.Objects;
import java.util.logging.Level;

import org.lasalledebain.libris.exception.Assertion;
import org.lasalledebain.libris.exception.LibrisException;
import org.lasalledebain.libris.indexes.LibrisDatabaseConfiguration;
import org.lasalledebain.libris.records.PdfRecordImporter;
import org.lasalledebain.libris.ui.CmdlineUi;
import org.lasalledebain.libris.ui.ConsoleUi;
import org.lasalledebain.libris.ui.DatabaseUi;
import org.lasalledebain.libris.ui.HeadlessUi;
import org.lasalledebain.libris.ui.LibrisGui;
import org.lasalledebain.libris.ui.LibrisHttpServer;
import org.lasalledebain.libris.ui.LibrisUi;

public class Libris {
	private static final String OPTION_AUXDIR = "-x";
	public static final String OPTION_GUI = "-g";
	public static final String OPTION_ARCHIVEDIR = "-a";
	public static final String OPTION_REBUILD = "-b";
	public static final String OPTION_CMDLINEUI = "-c";
	public static final String OPTION_WEBUI = "-w";
	public static final String OPTION_PORT = "-p";

	enum IfType {
		UI_HTML, UI_CMDLINE, UI_WEB, UI_GUI
	};

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		LibrisUi<DatabaseRecord> result = mainImpl(args);

		if (null == result) {
			System.exit(1);
		}
	}

	protected static LibrisUi<DatabaseRecord> mainImpl(String[] args) {
		IfType myUiType = IfType.UI_GUI;
		boolean readOnly = false;
		boolean doRebuild = false;
		String auxDirPath = null;
		String artifactDirpath = null;
		String databaseFilePath = null;
		int webPort = LibrisHttpServer.default_port;
		boolean batch = false;
		boolean status = true; /* success */

		int i = 0;
		while (status && (i < args.length)) {
			String arg = args[i];
			if (arg.equals("-r")) {
				readOnly = true;
			} else if (arg.equals(OPTION_REBUILD)) {
				doRebuild = true;
				batch = true;
			} else if (arg.equals(OPTION_GUI)) {
				myUiType = IfType.UI_GUI;
			} else if (arg.equals(OPTION_CMDLINEUI)) {
				myUiType = IfType.UI_CMDLINE;
			} else if (arg.equals(OPTION_WEBUI)) {
				myUiType = IfType.UI_WEB;
				// TODO add port and context for web
			} else if (arg.equals(OPTION_PORT)) {
				if ((i + 1) < args.length) {
					++i;
					try {
						webPort = Integer.parseInt(args[i]);
					} catch (NumberFormatException e) {
						cmdlineError("Invalid argument for " + OPTION_PORT + ": " + args[i]);
						status = false;
					}
				} else {
					cmdlineError("Missing argument for " + OPTION_PORT);
					status = false;
				}
			} else if (arg.equals("-h")) {
				printHelpString();
				System.exit(0);
			} else if (arg.equals(OPTION_ARCHIVEDIR)) {
				notImplemented(arg);
			} else if (arg.equals(OPTION_AUXDIR)) {
				++i;
				if (i < args.length) {
					auxDirPath = args[i + 1];
				} else {
					cmdlineError("Missing argument for " + OPTION_AUXDIR);
				}
				status = false;
			} else if (!arg.startsWith("-")) {
				if (null == databaseFilePath) {
					databaseFilePath = arg;
				} else {
					cmdlineError("only one database name can be specified");
					status = false;
				}
			}
			++i;
		}

		LibrisUi<DatabaseRecord> ui = null;
		try {
			File dbFile = (null == databaseFilePath) ? null : new File(databaseFilePath);
			LibrisDatabaseConfiguration config = new LibrisDatabaseConfiguration(dbFile);
			File auxDir = null;
			if ((null != dbFile) && (!dbFile.isFile())) {
				cmdlineError(databaseFilePath + " is not a file");
				status = false;
			}
			if (null != auxDirPath) {
				auxDir = new File(auxDirPath);
				if (!auxDir.exists()) {
					cmdlineError("Auxiliary directory "+auxDirPath+" does not exist");
					status = false;
				}
				config.setAuxiliaryDirectory(auxDir);
			}
			// TODO configurable aux dir & artifact dir
			if (status) {
				if (doRebuild) {
					Assertion.assertNotNullError("Database file not set", dbFile);
					ui = new HeadlessUi<DatabaseRecord>(false);
					status = ui.rebuildDatabase(config);
				} else {
					switch (myUiType) {
					case UI_GUI:
						ui = new LibrisGui(dbFile, readOnly);
						break;
					case UI_WEB:
						LibrisHttpServer<DatabaseRecord> htmlUi = new LibrisHttpServer<DatabaseRecord>(webPort,
								LibrisHttpServer.DEFAULT_CONTEXT);
						ui = htmlUi;
						break;
					case UI_CMDLINE:
						ui = new CmdlineUi<>(false);
						break;
					default:
						LibrisUi.cmdlineError(myUiType.toString() + " not implemented");
						break;
					}
					if (null != dbFile) {
						ui.openDatabase(config);
						ui.start();
					} else {
						ui.sendChooseDatabase();
					}
				}
			}
		} catch (LibrisException e) {
			LibrisDatabase.log(Level.SEVERE, "Error opening database", e);
			LibrisUi.cmdlineError("Cannot open Libris: " + e.getMessage());
			status = false;
		}

		LibrisUi<DatabaseRecord> result = null;
		if (status && !batch) {
			if (IfType.UI_CMDLINE != myUiType) {
				ConsoleUi<DatabaseRecord> console = new ConsoleUi<DatabaseRecord>(ui);
				console.start();
				result = console;
			} else {
				result = ui;
			}
		} else {
			result = ui;
		}
		return result;
	}

	private static void notImplemented(String arg) {
		System.err.println(arg + " not implemented");
		System.exit(1);
	}

	private static void printHelpString() {
		String helpString = "Libris: a record management system.\n" + "Syntax:]\n" 
				+ OPTION_REBUILD
				+ ": rebuild database\n" + "libris -[c|g|w] [-p <port>] [-x <path>] [-r] [-a <path>] <database file>\n"
				+ OPTION_CMDLINEUI + ": command-line\n" 
				+ OPTION_GUI + ": graphical user interface\n" 
				+ OPTION_WEBUI + ": start web server\n"
				+ OPTION_PORT + ": specify port (web server only)"
				+ OPTION_AUXDIR + ": specify auxiliary directory\n" 
				+ "-r: open database read-only\n"
				+ OPTION_ARCHIVEDIR + ": specify arifact repository location";
		System.out.println(helpString);

	}

	public static LibrisDatabase buildAndOpenDatabase(File databaseFile) throws LibrisException {
		HeadlessUi<DatabaseRecord> ui = new HeadlessUi<DatabaseRecord>(databaseFile, false);
		buildIndexes(databaseFile, ui);

		LibrisDatabase result = ui.openDatabase();
		return result;
	}

	public static LibrisDatabase buildAndOpenDatabase(LibrisDatabaseConfiguration config) throws LibrisException {
		HeadlessUi<DatabaseRecord> ui = new HeadlessUi<DatabaseRecord>();
		buildIndexes(config, ui);
		LibrisDatabase result = ui.openDatabase(config);
		return result;
	}

	public static LibrisDatabase openDatabase(File databaseFile, LibrisUi<DatabaseRecord> ui) throws LibrisException {
		if (null == ui) {
			ui = new HeadlessUi<DatabaseRecord>(false);
		}
		LibrisDatabase db = ui.openDatabase(new LibrisDatabaseConfiguration(databaseFile));
		return db;
	}

	public static boolean buildIndexes(File databaseFile, DatabaseUi<?> ui) throws LibrisException {
		LibrisDatabaseConfiguration config = new LibrisDatabaseConfiguration(databaseFile);
		return buildIndexes(config, ui);
	}

	public static boolean buildIndexes(LibrisDatabaseConfiguration config, DatabaseUi<?> databaseUi)
			throws LibrisException {
		if (config.isReadOnly()) {
			databaseUi.alert("Cannot build indexes if read-only set");
			return false;
		}
		LibrisDatabase db = new LibrisDatabase(config, databaseUi);
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
