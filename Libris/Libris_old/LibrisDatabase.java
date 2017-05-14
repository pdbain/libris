/*
 * Created on Dec 9, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */

/**
 * @author pdbain
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package Libris;

import java.awt.HeadlessException;
import java.io.File;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JMenuBar;


public class LibrisDatabase {
	private int databaseSize;
	protected BloomFilter keywordFilter;
	private DatabaseFile reader;
	private RecordList newRecordList;
	private LibrisSchema schema;
	private JFrame dbFrame;
	private LibrisMenu menus;
	public DatabaseFileIndex fileIndex;
	boolean verbose = true;
	public LibrisDatabase() {
		schema = new LibrisSchema();
		// keywordFilter = new BloomFilter(databaseSize);
		
		dbFrame = new JFrame();
		dbFrame.setVisible(true);
		setCurrentDBName(null);
	}

	/**
	 * @return Returns the keywordFilter.
	 */
	public void openDatabase(String DBName, boolean schemaOnly) {
		setCurrentDBName(DBName);
		openDatabase(schemaOnly);
	}

	public void openDatabase(boolean schemaOnly) {
		reader = new DatabaseFileSchema(getCurrentDBFile(), this);
		if (schemaOnly) return;
		fileIndex = new DatabaseFileIndex(dbFile, this);
		try {
			fileIndex.readRecordPositions();
		} catch (LibrisException e) {
			JDialog dlog = new JDialog();
		}
		int l = BloomFilterSection.getLevels(fileIndex.getRecCount());
		keywordFilter = new BloomFilterSection(this, l);
	}
	public BloomFilter getKeywordFilter() {
		return keywordFilter;
	}
	
	public LibrisRecord newRecord() throws Exception {
		LibrisRecord record = new LibrisRecord(this, schema);
		record.createBlankRecord();
		return(record);
	}

	/**
	 * @param dbFileName 
	 * 
	 */

	/**
	 * @return
	 */
	public LibrisSchema getSchema() {
		// TODO Auto-generated method stub
		return(schema);
	}

	String DBName;
	private File dbFile;
	public String getCurrentDBName() {
		return(DBName);
	}
	public File getCurrentDBFile() {
		return(dbFile);
	}
	public void setCurrentDBName(String DBName) {
		this.DBName = DBName;
		if (DBName != null) {
			this.dbFile = new File(DBName);
		} else {
			this.dbFile = null;
		}
	}

	public void RebuildIndex() throws LibrisException {
		try {
			fileIndex = new DatabaseFileIndex(dbFile, this);
			fileIndex.readDatabase(schema);
			keywordFilter = new BloomFilter(this, fileIndex.getRecCount());
			keywordFilter.readDatabase();
		} catch (LibrisException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void logMsg(String msg) {
		if (verbose)
			System.out.println(msg);
	}

	public void error(String string) {
		System.out.println(string);
		
	}

	public JFrame getDbFrame() {
		return dbFrame;
	}

	public int getRecordStartPosition(int recNum) throws LibrisException {
		return fileIndex.getRecordStartPosition(recNum);
	}

	public void addNewRecord(LibrisRecord record) {
		if (newRecordList == null) {
			this.newRecordList = new RecordList(null);
		}
		newRecordList.addrecord(record);
	}
}