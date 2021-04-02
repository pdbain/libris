package org.lasalledebain.libris;

import java.io.File;
import java.net.URL;
import java.util.Objects;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;

import org.lasalledebain.libris.exception.Assertion;
import org.lasalledebain.libris.exception.LibrisException;
import org.lasalledebain.libris.indexes.LibrisDatabaseConfiguration;
import org.lasalledebain.libris.records.PdfRecordImporter;
import org.lasalledebain.libris.ui.AbstractUi;
import org.lasalledebain.libris.ui.CmdlineUi;
import org.lasalledebain.libris.ui.ConsoleUi;
import org.lasalledebain.libris.ui.HeadlessUi;
import org.lasalledebain.libris.ui.LibrisGui;
import org.lasalledebain.libris.ui.LibrisHttpServer;

public class Libris {
	public static final String OPTION_HELP = "-h";
	public static final String OPTION_AUXDIR = "-u";
	public static final String OPTION_GUI = "-g";
	public static final String OPTION_REPODIR = "-a";
	public static final String OPTION_REBUILD = "-b";
	public static final String OPTION_ARCHIVE = "-a";
	public static final String OPTION_CMDLINEUI = "-c";
	public static final String OPTION_WEBUI = "-w";
	public static final String OPTION_PORT = "-p";
	public static final String OPTION_EXPORT = "-e";
	public static final String OPTION_INCLUDE_ARTIFACTS = "-f";
	public static final String OPTION_READONLY = "-r";

	enum IfType {
		UI_HTML, UI_CMDLINE, UI_WEB, UI_GUI, UI_DEFAULT
	};

	private static Object prefsSync = new Object();
	private static Preferences librisPrefs;
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		AbstractUi<DatabaseRecord> result = mainImpl(args);

		if (null == result) {
			System.exit(1);
		}
	}

	protected static AbstractUi<DatabaseRecord> mainImpl(String[] args) {
		Thread.currentThread().setName("Console");
		IfType myUiType = IfType.UI_DEFAULT;
		boolean readOnly = false;
		boolean doRebuild = false;
		boolean doExport = false;
		boolean includeArtifacts = false;
		boolean archive = false;
		String auxDirPath = null;
		String repoDirPath = null;
		String databaseFilePath = null;
		int webPort = LibrisHttpServer.default_port;
		boolean batch = false;
		boolean status = true; /* success */

		int i = 0;
		while (status && (i < args.length)) {
			String arg = args[i];
			switch (arg) {
			case OPTION_READONLY: 
				readOnly = true;
				break;
			case OPTION_INCLUDE_ARTIFACTS: 
				includeArtifacts = true;
				notImplemented(arg);
				break;
			case OPTION_REBUILD:
				doRebuild = true;
				batch = true;
				break;
			case OPTION_EXPORT:
				doExport = true;
				batch = true;
				notImplemented(arg);
				break;
			case OPTION_GUI:
				myUiType = IfType.UI_GUI;
				break;
			case OPTION_CMDLINEUI:
				myUiType = IfType.UI_CMDLINE;
				break;
			case OPTION_WEBUI:
				myUiType = IfType.UI_WEB;
				// TODO add port and context for web
				break;
			case OPTION_PORT:
				++i;
				if (status = assertOptionParameter(args, i, OPTION_PORT)) {
					try {
						webPort = Integer.parseInt(args[i]);
					} catch (NumberFormatException e) {
						cmdlineError("Invalid argument for " + OPTION_PORT + ": " + args[i]);
						status = false;
					}
				}
				break;
			case OPTION_HELP:
				printHelpString();
				System.exit(0);
				break;
			case OPTION_AUXDIR:
				++i;
				if (status = assertOptionParameter(args, i, OPTION_AUXDIR))	auxDirPath = args[i];
				break;
			case OPTION_REPODIR:
				++i;
				if (status = assertOptionParameter(args, i, OPTION_REPODIR)) repoDirPath = args[i];
				break;
			default: 
				if (!arg.startsWith("-")) {
					if (null == databaseFilePath) {
						databaseFilePath = arg;
					} else {
						cmdlineError("only one database name can be specified");
						status = false;
					}
				}
			}

			++i;
		}

		AbstractUi<DatabaseRecord> ui = null;
		try {
			File dbFile = (null == databaseFilePath) ? null : new File(databaseFilePath);
			LibrisDatabaseConfiguration config = new LibrisDatabaseConfiguration(dbFile);
			if ((null != dbFile) && (!dbFile.isFile())) {
				Libris.cmdlineError(databaseFilePath + " is not a file");
				status = false;
			}
			if (null != auxDirPath) {
				File auxDir = null;
				auxDir = new File(auxDirPath);
				if (!auxDir.exists()) {
					Libris.cmdlineError("Auxiliary directory "+auxDirPath+" does not exist");
					status = false;
				}
				config.setAuxiliaryDirectory(auxDir);
			}
			if (null != repoDirPath) {
				File repoDirFile = new File(repoDirPath);
				if (!repoDirFile.exists()) {
					Libris.cmdlineError("Repository directory "+repoDirPath+" does not exist");
					status = false;
				}
				config.setRepositoryDirectory(repoDirFile);
			}
			if (status) {
				{
					Assertion.assertTrue(ui, "cannot specify both "+OPTION_EXPORT+" and "+OPTION_REBUILD, !(doExport && doRebuild));
					Assertion.assertTrue(ui, "cannot specify both "+OPTION_INCLUDE_ARTIFACTS+" without "+OPTION_EXPORT, !doExport || includeArtifacts);
					Assertion.assertTrue(ui, "cannot specify "+OPTION_ARCHIVE+" without "+OPTION_EXPORT+" or "+OPTION_REBUILD, 
							!(archive  && !(doRebuild || doExport)));
				}
				if (doRebuild) {
					Assertion.assertEquals(ui, "cannot specify UI type for batch operations", IfType.UI_DEFAULT, myUiType);
					Assertion.assertNotNullError("Database file not set", dbFile);
					ui = new HeadlessUi<DatabaseRecord>(false);
					status = ui.buildDatabase(config);
				} else {
					switch (myUiType) {
					case UI_GUI:
					case UI_DEFAULT:
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
						Libris.cmdlineError(myUiType.toString() + " not implemented");
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
			Libris.cmdlineError("Cannot open Libris: " + e.getMessage());
			status = false;
		}

		AbstractUi<DatabaseRecord> result = null;
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

	protected static boolean assertOptionParameter(String[] args, int i, final String optionKey) {
		boolean status;
		if (i >= args.length) {
			Libris.cmdlineError("Missing argument for " + optionKey);
			status = false;
		}
		status = true;
		return status;
	}

	private static void notImplemented(String arg) {
		System.err.println(arg + " not implemented");
		System.exit(1);
	}

	private static void printHelpString() {
		String helpString = "Libris: a record management system.\n" + "Syntax:]\n" 
				+ "libris ["+OPTION_CMDLINEUI+ "|"+OPTION_GUI+ "|"+OPTION_WEBUI+"|+"+OPTION_REBUILD+"]"
				+ "["+ OPTION_PORT+" <port>]"+ "["+ OPTION_INCLUDE_ARTIFACTS+"]"
				+ "["
				+ OPTION_AUXDIR+" <path>]"
				+ "["+ OPTION_READONLY+"] ["+OPTION_REPODIR + " <path>] "
				+ "<file>\n"
				+ OPTION_CMDLINEUI + ": command-line\n" 
				+ OPTION_GUI + ": graphical user interface\n" 
				+ OPTION_WEBUI + ": start web server\n"
				+ OPTION_PORT + ": specify port (web server only)"
				+ OPTION_AUXDIR + ": specify auxiliary directory\n" 
				+ OPTION_READONLY+": open database read-only\n"
				+ OPTION_REPODIR + ": specify artifact repository location"
				+ OPTION_REBUILD + ": rebuild database\n";
		System.out.println(helpString);

	}

	public static LibrisDatabase openDatabase(File databaseFile, AbstractUi<DatabaseRecord> ui) throws LibrisException {
		if (null == ui) {
			ui = new HeadlessUi<DatabaseRecord>(false);
		}
		LibrisDatabase db = ui.openDatabase(new LibrisDatabaseConfiguration(databaseFile));
		return db;
	}

	static {
		URL props = PdfRecordImporter.class.getClassLoader().getResource("commons-logging.properties");
		if (Objects.nonNull(props)) {
			String path = props.getPath();
			System.setProperty("java.util.logging.config.file", path);
		}
	}

	public static String formatConciseStackTrace(Throwable e, StringBuilder buff) {
		String emessage;
		emessage = e.getMessage();
		if (null != emessage) {
			buff.append(": "); buff.append(emessage);
		} else {
			buff.append(" at ");
			String sep = "";
			for (StackTraceElement t: e.getStackTrace()) {
				buff.append(sep);
				String className = t.getClassName();
				int lastDot = className.lastIndexOf('.');
				if (lastDot > 0) {
					buff.append(className.substring(lastDot + 1, className.length()));
				} else {
					buff.append(className);
				}
				buff.append(".");
				buff.append(t.getMethodName());
				buff.append("() line ");
				buff.append(t.getLineNumber());
				sep = "\n";
			}
		}
		return emessage;
	}

	public static Preferences getLibrisPrefs() {
		synchronized (prefsSync) {
			if (null == librisPrefs) {
				librisPrefs = Preferences.userRoot();
			}
		}
		return librisPrefs;
	}

	public static void setLoggingLevel(Logger myLogger) {
		String logLevelString = System.getProperty(LibrisConstants.LIBRIS_LOGGING_LEVEL);
		if (null != logLevelString) {
			Level logLevel = Level.parse(logLevelString);
			myLogger.setLevel(logLevel);
			for (Handler handler : Logger.getLogger("").getHandlers()) {
				handler.setLevel(logLevel);
			}
		}
	}

	public static void cmdlineError(String msg) {
		System.err.println(msg);
	}
}
