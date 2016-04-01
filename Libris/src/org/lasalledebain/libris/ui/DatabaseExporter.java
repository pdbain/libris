package org.lasalledebain.libris.ui;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import org.lasalledebain.libris.LibrisDatabase;
import org.lasalledebain.libris.exception.InputException;
import org.lasalledebain.libris.exception.LibrisException;

public class DatabaseExporter {
	private static final String DATABASE_EXPORT_FILE = "DATABASE_EXPORT_FILE";
	private static final String DATABASE_EXPORT_FORMAT = "DATABASE_EXPORT_FORMAT";
	enum ExportFormat {
		EXPORT_XML, EXPORT_CSV, EXPORT_TEXT
	};
	LibrisDatabase db;
	private JFrame exportFrame;
	private Preferences librisPrefs;
	File exportFile;
	private ExportFormat fmt;
	private boolean includeSchema;
	private boolean includeRecords;

	public static void guiExportFile(LibrisDatabase db) throws LibrisException  {
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
					try {
						exportStream = new FileOutputStream(exportFile);
						db.exportDatabaseXml(exportStream, includeSchema, includeRecords);
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
		librisPrefs = LibrisUiGeneric.getLibrisPrefs();
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
		JRadioButton xmlButton = new JRadioButton("XML");
		JRadioButton csvButton = new JRadioButton("CSV");
		JRadioButton textButton = new JRadioButton("Formatted Text");
		if (lastExportFormat.equals(ExportFormat.EXPORT_XML.toString())) {
			xmlButton.setSelected(true);
		} else if 	(lastExportFormat.equals(ExportFormat.EXPORT_CSV.toString())) {
			csvButton.setSelected(false);
		} else if 	(lastExportFormat.equals(ExportFormat.EXPORT_TEXT.toString())) {
			textButton.setSelected(false);
		}
		
		ButtonGroup formatButton = new ButtonGroup();
		formatButton.add(xmlButton);
		formatButton.add(csvButton);
		formatButton.add(textButton);
		GridBagLayout buttonLayout = new GridBagLayout();
		GridBagConstraints buttonConstraints = new GridBagConstraints();
		buttonConstraints.gridwidth = GridBagConstraints.REMAINDER;
		buttonConstraints.anchor = GridBagConstraints.WEST;
		final JPanel sepPanel = new JPanel(buttonLayout);
		sepPanel.setLayout(new BoxLayout(sepPanel, BoxLayout.Y_AXIS));
		JLabel sepLabel = new JLabel("Select output format");
		buttonLayout.setConstraints(sepLabel, buttonConstraints);
		sepPanel.add(sepLabel);
	
		buttonLayout.setConstraints(xmlButton, buttonConstraints);
		sepPanel.add(xmlButton);
		
		buttonLayout.setConstraints(csvButton, buttonConstraints);
		sepPanel.add(csvButton);
		
		buttonLayout.setConstraints(textButton, buttonConstraints);
		sepPanel.add(textButton);
				
		JCheckBox includeSchemaControl = new JCheckBox("Include schema");
		JCheckBox includeRecordsControl = new JCheckBox("Include records");
		sepPanel.add(includeSchemaControl);
		sepPanel.add(includeRecordsControl);
		includeSchemaControl.setSelected(true);
		includeRecordsControl.setSelected(true);
		JFileChooser exportFileChooser = new JFileChooser(lastExportFile);
		if (lastExportFile.isFile()) {
			exportFileChooser.setSelectedFile(lastExportFile);
		}
		exportFileChooser.setApproveButtonText("Export");
		exportFileChooser.setDialogTitle("Export to...");
		exportFileChooser.setAccessory(sepPanel);

		int response = exportFileChooser.showSaveDialog(sepPanel);
		exportFile = null;
		if (JFileChooser.CANCEL_OPTION != response) {
			Preferences librisPrefs = LibrisUiGeneric.getLibrisPrefs();
			if (xmlButton.isSelected()) {
				fmt = ExportFormat.EXPORT_XML;
			} else if (csvButton.isSelected()) {
				fmt = ExportFormat.EXPORT_CSV;
			} else if (textButton.isSelected()) {
				fmt = ExportFormat.EXPORT_TEXT;
			} else {
				throw new InputException("No format specified");
			}
			librisPrefs.put(DATABASE_EXPORT_FORMAT, fmt.toString());
			exportFile = exportFileChooser.getSelectedFile();
			librisPrefs.put(DATABASE_EXPORT_FILE, exportFile.getAbsolutePath());
			includeSchema = includeSchemaControl.isSelected();
			includeRecords = includeRecordsControl.isSelected();
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