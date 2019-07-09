package org.lasalledebain.libris.records;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Iterator;

import org.lasalledebain.libris.DatabaseRecord;
import org.lasalledebain.libris.LibrisDatabase;
import org.lasalledebain.libris.Record;
import org.lasalledebain.libris.exception.LibrisException;
import org.lasalledebain.libris.field.FieldValueStringList;
import org.lasalledebain.libris.ui.LibrisUiGeneric;

public class DelimitedTextRecordsReader {
	/**
	 * 
	 */
	public String[] fieldIds;
	private LibrisDatabase db;
	private char separatorChar;
	int unknownFields = 0;
	boolean fieldIdsInFirstRow;
	private File dataFile;
	public int 		rowCount;

	public void setDatafile(File dFile) {
		dataFile = dFile;
	}

	public DelimitedTextRecordsReader(LibrisDatabase db, File dFile, char separatorChar) {
		this.db = db;
		dataFile = dFile;
		this.separatorChar = separatorChar;
	}
	
	public DelimitedTextRecordsReader(LibrisDatabase db, char separatorChar) {
		this.db = db;
		LibrisUiGeneric.getLibrisPrefs();
		this.fieldIdsInFirstRow = false;
		this.separatorChar = separatorChar;
	}

	public DelimitedTextRecordsReader(LibrisDatabase db, Reader source) {
		super();
		this.db = db;
	}

	public Record[] importRecordsToDatabase() throws LibrisException, FileNotFoundException {
		ArrayList<Record> newRecords = new ArrayList<Record>();
		// TODO handle extra data fields
		Iterator<FieldValueStringList[]> recRdr = createRecordReader(dataFile);
		if (null == fieldIds) {
			FieldValueStringList[] row = recRdr.next();
			if (null == row) {
				return null;
			}
			fieldIds = new String[row.length];
			for (int i = 0; i < row.length; ++i) {
				fieldIds[i]=row[i].toString().trim();
			}
		}
		while (recRdr.hasNext()) {
			FieldValueStringList[] recFields = recRdr.next();
			Record rec = db.newRecord();
			for (int i = 0; i < fieldIds.length; ++i) {
				if ((null == fieldIds[i]) || (fieldIds[i].length() == 0)) {
					continue;
				}
				FieldValueStringList values = recFields[i];
				for (String val: values) {
					rec.addFieldValue(fieldIds[i], val);
				}
			}
			newRecords.add(rec);
		}
		return newRecords.toArray(new Record[newRecords.size()]);
	}
	

	public Record[] importRecordsToDatabase(Reader importReader, RecordImporter<DatabaseRecord> recImporter) throws LibrisException  {
		Iterator<FieldValueStringList[]> recRdr = (new CsvRecords(importReader, separatorChar)).iterator();
		ArrayList<Record> newRecords = new ArrayList<Record>();
		rowCount = 0;
		while (recRdr.hasNext()) {
			FieldValueStringList[] recFields = recRdr.next();
			++rowCount;
			DatabaseRecord rec = recImporter.importRecord(recFields);
			db.putRecord(rec);
			newRecords.add(rec);
		}
		return newRecords.toArray(new Record[newRecords.size()]);
	}

	private Iterator<FieldValueStringList[]> createRecordReader(File dataFile)
			throws FileNotFoundException {
		FileReader importFileReader = new FileReader(dataFile);
		Iterator<FieldValueStringList[]> recRdr = (new CsvRecords(importFileReader, separatorChar)).iterator();
		return recRdr;
	}


	public void setSeparatorChar(char separatorChar) {
		this.separatorChar = separatorChar;
	}
	
}
