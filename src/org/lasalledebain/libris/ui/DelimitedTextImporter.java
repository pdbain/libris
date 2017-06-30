package org.lasalledebain.libris.ui;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.TreeSet;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButton;
import javax.swing.JTable;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

import org.lasalledebain.libris.FileAccessManager;
import org.lasalledebain.libris.LibrisDatabase;
import org.lasalledebain.libris.LibrisFileManager;
import org.lasalledebain.libris.exception.DatabaseException;
import org.lasalledebain.libris.exception.LibrisException;
import org.lasalledebain.libris.records.DelimitedTextRecordsReader;
import org.lasalledebain.libris.records.DirectRecordImporter;
import org.lasalledebain.libris.records.FilteringRecordImporter;
import org.lasalledebain.libris.records.RecordImporter;
import org.lasalledebain.libris.xmlUtils.LibrisXmlFactory;

public class DelimitedTextImporter {
	/**
	 * 
	 */
	private static final long serialVersionUID = -711158397627820062L;
	private static final String DELIMITED_FILE = "IMPORT_DELIMITED_TEXT_FILE";
	private static final String UNKNOWN_FIELD_NAME = "<unknown>";
	private static final String IGNORE_FIELD_ID = "<ignore>";
	private static final String FIELD_SEPARATOR_TAB = "FIELD_SEPARATOR_TAB";
	private static final String IMPORT_FILTER_FILE = "CSV_IMPORT_FILTER_FILE";
	public String[] fieldIds;
	LibrisDatabase db;
	TreeSet<String> availableFieldIds;
	private char separatorChar;
	private JFrame importFrame;
	int unknownFields = 0;
	private Preferences librisPrefs;
	private JTable importData;
	File dataFile;
	File importFilterFile = null;

	public static void guiImportFile(LibrisDatabase db) throws DatabaseException {
		DelimitedTextImporter importer = new DelimitedTextImporter(db);
		if (importer.chooseImportFile()) {
			importer.doImport();
		}
	}
	
	private void doImport() throws DatabaseException {
		DelimitedTextRecordsReader dtfReader = null;
		FileAccessManager mgr = null;
		try {
			dtfReader = new DelimitedTextRecordsReader(db, 
					dataFile, separatorChar);
			RecordImporter imp;
			if  (null == importFilterFile) {
				imp = new DirectRecordImporter(db);
			} else {
				mgr = db.getFileMgr().makeAccessManager(LibrisFileManager.CSV_FILE, importFilterFile);
				imp = new FilteringRecordImporter(db, new LibrisXmlFactory(),	mgr);
			}

			dtfReader.importRecordsToDatabase(
					new InputStreamReader(new FileInputStream(dataFile)), imp);
		} catch (FileNotFoundException e) {
			throw new DatabaseException(e);
		} catch (LibrisException e) {
			if ((null != dtfReader) && (dtfReader.rowCount > 0)) {
				throw new DatabaseException("Error reading line "+dtfReader.rowCount+" file "+importFilterFile+"\n", e);
			} else {
				throw new DatabaseException(e);

			}
		} finally {
			if (null != mgr) {
				db.getFileMgr().releaseAccessManager(mgr);
			}
		}
	}

	public void setDatafile(File dFile) {
		dataFile = dFile;
	}

	public DelimitedTextImporter(LibrisDatabase db) {
		this.db = db;
		librisPrefs = LibrisUiGeneric.getLibrisPrefs();
		importFrame = new JFrame("Import CSV Data");
		importFrame.setLayout(new BorderLayout());
	}


	class ImportButtonListener implements ActionListener {

		private boolean doImport;

		public ImportButtonListener(boolean doImport) {
			this.doImport = doImport;
		}

		@Override
		public void actionPerformed(ActionEvent evt) {
			if (doImport) {
				try {
					TableModel colMod = importData.getModel();
					int colCount = colMod.getColumnCount();
					ArrayList<String> colIds = new ArrayList<String>(colCount);
					for (int i = 0; i < colCount; ++i) {
						String fid = colMod.getColumnName(i);
						colIds.add(fid.equals(IGNORE_FIELD_ID)? "": fid);
					}
					fieldIds = colIds.toArray(new String[colCount]);
					DelimitedTextRecordsReader dtfReader = new DelimitedTextRecordsReader(db, 
							dataFile, separatorChar);
					dtfReader.importRecordsToDatabase();
				} catch (FileNotFoundException exc) {
					db.alert("Cannot find data file "+dataFile.getAbsolutePath(), exc);
				} catch (DatabaseException exc) {
					db.alert("Error reading data file "+dataFile.getAbsolutePath(), exc);
				} catch (LibrisException exc) {
					db.alert("Error adding record", exc);
				}

			}
			importFrame.setVisible(false);
			importFrame.dispose();
		}
	}
	private boolean chooseImportFile() {
		String userDir = System.getProperty("user.dir");
		String lastDelimFileName = librisPrefs.get(DELIMITED_FILE, userDir);
		File lastDelimFile = new File(lastDelimFileName);
		if (!lastDelimFile.exists()) {
			lastDelimFile = null;
		}
		JPanel importPanel = new JPanel(new GridLayout(1,0));
		importFrame.setContentPane(importPanel);
		JRadioButton commaButton = new JRadioButton("Comma");
		JRadioButton tabButton = new JRadioButton("Tab");
		final boolean useTab = librisPrefs.getBoolean(FIELD_SEPARATOR_TAB, false);
		commaButton.setSelected(!useTab);
		tabButton.setSelected(useTab);
		ButtonGroup separatorButtons = new ButtonGroup();
		separatorButtons.add(commaButton);
		separatorButtons.add(tabButton);
		GridBagLayout buttonLayout = new GridBagLayout();
		GridBagConstraints buttonConstraints = new GridBagConstraints();
		buttonConstraints.gridwidth = GridBagConstraints.REMAINDER;
		buttonConstraints.anchor = GridBagConstraints.WEST;
		final JPanel sepPanel = new JPanel(buttonLayout);
		JLabel sepLabel = new JLabel("Select field separator");
		buttonLayout.setConstraints(sepLabel, buttonConstraints);
		sepPanel.add(sepLabel);
		buttonLayout.setConstraints(commaButton, buttonConstraints);
		sepPanel.add(commaButton);
		
		buttonLayout.setConstraints(tabButton, buttonConstraints);
		sepPanel.add(tabButton);
		
		JButton useImportFilter = new JButton("Use import filter...");
		buttonLayout.setConstraints(useImportFilter, buttonConstraints);
		sepPanel.add(useImportFilter);
		useImportFilter.addActionListener(new ActionListener () {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				final String importFilterFilePref = librisPrefs.get(IMPORT_FILTER_FILE, System.getProperty("user.dir"));
				JFileChooser filterChooser = new JFileChooser(importFilterFilePref);
				filterChooser.addChoosableFileFilter(new FileFilter() {
					@Override
					public boolean accept(File f) {
						String fName = f.getName();
						return fName.endsWith(".xml");
					}
					@Override
					public String getDescription() {
						return "Delimited text filter files";
					}
				});
				int response = filterChooser.showOpenDialog(sepPanel);
				if (JFileChooser.CANCEL_OPTION != response) {
					final File selectedFile = filterChooser.getSelectedFile();
					librisPrefs.put(IMPORT_FILTER_FILE, selectedFile.getPath());
					importFilterFile = selectedFile;
					try {
						librisPrefs.sync();
					} catch (BackingStoreException e) {
						db.alert("cannot save preferences: ", e);
					}
				} else {
					importFilterFile = null;
				}

			}
			
		});
		
		JFileChooser dataFileChooser = new JFileChooser(lastDelimFile);
		dataFileChooser.setApproveButtonText("Import");
		dataFileChooser.setAccessory(sepPanel);
		dataFileChooser.addChoosableFileFilter(new FileFilter() {

			@Override
			public boolean accept(File f) {
				String fName = f.getName();
				return fName.endsWith(".csv") || fName.endsWith(".tdf") || fName.endsWith(".txt");
			}

			@Override
			public String getDescription() {
				return "Delimited text files";
			}
			
		});

		int response = dataFileChooser.showOpenDialog(sepPanel);
		dataFile = null;
		if (JFileChooser.CANCEL_OPTION != response) {
			Preferences librisPrefs = LibrisUiGeneric.getLibrisPrefs();
			separatorChar = (tabButton.isSelected()) ? '\t' : ',';				
			librisPrefs.putBoolean(FIELD_SEPARATOR_TAB, tabButton.isSelected());
			dataFile = dataFileChooser.getSelectedFile();
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

	class ColumnMouseHandler extends MouseAdapter {

		private String[] fieldIdChoices;
		String selectedFieldId = null;
		private TableColumn selectedColumn;

		public ColumnMouseHandler(DelimitedTextImporter delimitedTextImportGui, String[] fieldIds) {
			this.fieldIdChoices = new String[fieldIds.length+2];
			System.arraycopy(fieldIds, 0, fieldIdChoices, 0, fieldIds.length);
			fieldIdChoices[fieldIds.length] = IGNORE_FIELD_ID;
			fieldIdChoices[fieldIds.length+1] = UNKNOWN_FIELD_NAME;
		}

		@Override
		public void mouseClicked(MouseEvent e) {
			JTableHeader hdr = (JTableHeader) e.getSource();
			TableColumnModel colMod = hdr.getColumnModel();
			int clickedColumn = hdr.columnAtPoint(e.getPoint());
			selectedColumn = colMod.getColumn(clickedColumn);
			JPopupMenu fidSelector = new JPopupMenu("Select field");
			for (String s: fieldIdChoices) {
				JMenuItem fieldOption = new JMenuItem(s);
				fieldOption.setEnabled(isGenericMenuItem(s) ||
						availableFieldIds.contains(s)) ;
				fieldOption.addActionListener(new ActionListener() {

					@Override
					public void actionPerformed(ActionEvent e) {
						String oldValue = selectedColumn.getHeaderValue().toString();
						JMenuItem mi = (JMenuItem) e.getSource();
						selectedFieldId = mi.getText();
						selectedColumn.setHeaderValue(selectedFieldId);
						if (UNKNOWN_FIELD_NAME == selectedFieldId) {
							++unknownFields;
						}
						availableFieldIds.add(oldValue);
						if (UNKNOWN_FIELD_NAME == oldValue) {
							++unknownFields;
						}
						availableFieldIds.remove(selectedFieldId);
					}

				});
				fidSelector.add(fieldOption);
			}
			fidSelector.show(e.getComponent(), e.getX(), e.getY());
		}

		private boolean isGenericMenuItem(String s) {
			return ((IGNORE_FIELD_ID == s) ||
					(UNKNOWN_FIELD_NAME ==s));
		}
	}
}