package org.lasalledebain.libris.ui;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import javax.swing.JFrame;
import javax.swing.JPanel;

import org.lasalledebain.libris.LibrisDatabase;
import org.lasalledebain.libris.exception.InputException;
import org.lasalledebain.libris.exception.LibrisException;

public class DatabaseExporter {
	private static final String DATABASE_EXPORT_FILE = "DATABASE_EXPORT_FILE";
	private static final String DATABASE_EXPORT_FORMAT = "DATABASE_EXPORT_FORMAT";
	enum ExportFormat {
		EXPORT_XML, EXPORT_CSV, EXPORT_TEXT
	}
	private static LibrisWindowedUi gui;
	LibrisDatabase db;
	private JFrame exportFrame;
	private Preferences librisPrefs;
	File exportFile;
	private ExportFormat fmt;
	private boolean includeSchema;
	private boolean includeRecords;

	public static void guiExportFile(LibrisWindowedUi ui, LibrisDatabase db) throws LibrisException  {
		gui = ui;
		DatabaseExporter exporter = new DatabaseExporter(db);
		if (exporter.chooseExportFile()) {
			exporter.doExport();
		}
	}
	
	private void doExport() throws LibrisException {
		if (null == fmt) {
			db.alert("format not specified");
		} else {
			switch (fmt) {
			case EXPORT_XML:
				FileOutputStream exportStream;
				boolean addInstanceInfo = false;
					try {
						exportStream = new FileOutputStream(exportFile);
						db.exportDatabaseXml(exportStream, includeSchema, includeRecords, addInstanceInfo);
					} catch (FileNotFoundException e) {
						db.alert("error exporting database to "+exportFile.getPath(), e);
					}
				break;
				default: db.alert(fmt.toString() + " not implemented");
			}
		}
	}

	public void setDatafile(File dFile) {
		exportFile = dFile;
	}

	public DatabaseExporter(LibrisDatabase db) {
		this.db = db;
		librisPrefs = LibrisUi.getLibrisPrefs();
		exportFrame = new JFrame("Export database");
		exportFrame.setLayout(new BorderLayout());
	}


	private boolean chooseExportFile() throws InputException {
		String userDir = System.getProperty("user.dir");
		String lastExportFileName = librisPrefs.get(DATABASE_EXPORT_FILE, userDir);
		String lastExportFormat = librisPrefs.get(DATABASE_EXPORT_FORMAT, ExportFormat.EXPORT_XML.toString());
		File lastExportFile = new File(lastExportFileName);
		if (!lastExportFile.exists()) {
			lastExportFile = null;
		}
		JPanel importPanel = new JPanel(new GridLayout(1,0));
		exportFrame.setContentPane(importPanel);
		LibrisFileChooser exportFileChooser = new LibrisFileChooser(gui, "Export to...");
		final DatabaseExporterAccessoryPanel sepPanel = 
				new DatabaseExporterAccessoryPanel(exportFileChooser, lastExportFormat);
		if ((null != lastExportFile) && lastExportFile.isFile()) {
			exportFileChooser.setSelectedFile(lastExportFile);
		}
		exportFileChooser.setApproveButtonText("Export");
		exportFileChooser.setAccessory(sepPanel);

		exportFile = exportFileChooser.chooseOutputFile(lastExportFileName);
		if (null != exportFile) {
			Preferences librisPrefs = LibrisUi.getLibrisPrefs();
			fmt = sepPanel.getFormat();
			librisPrefs.put(DATABASE_EXPORT_FORMAT, fmt.toString());
			librisPrefs.put(DATABASE_EXPORT_FILE, exportFile.getAbsolutePath());
			includeSchema = sepPanel.isIncludeSchema();
			includeRecords = sepPanel.isIncludeRecords();
			try {
				librisPrefs.sync();
			} catch (BackingStoreException e) {
				db.alert("cannot save preferences: ", e);
			}
			return true;
		} else {
			return false;
		}
	}
}