package org.lasalledebain.libris.records;

import java.io.IOException;
import java.io.Reader;
import java.util.Iterator;

import org.lasalledebain.libris.field.FieldValueStringList;

import au.com.bytecode.opencsv.CSVReader;

public class CsvRecords implements Iterable<FieldValueStringList[]>, Iterator<FieldValueStringList[]> {

	private static final String VERTICAL_TAB = "\u000B";
	CSVReader recordReader;
	private Reader inputReader;
	private char separator;
	private FieldValueStringList[] currentRecordFieldList;
	private Exception lastException;
	
	public Exception getLastException() {
		return lastException;
	}
	public CsvRecords(Reader rdr, char separator) {
		inputReader = rdr;
		this.separator = separator;
	}
	@Override
	public Iterator<FieldValueStringList[]> iterator() {
		recordReader = new CSVReader(inputReader, separator);
		return this;
	}
	@Override
	public boolean hasNext() {
		boolean result = false;
		if (null != recordReader) {
			try {
				if (null == currentRecordFieldList) {
					String[] nextLine = null;
					try {
						nextLine  = recordReader.readNext();
					} catch (IOException e) {
						recordReader.close();
					}
					if (null == nextLine) {
						recordReader.close();
					} else {
						currentRecordFieldList = new FieldValueStringList[nextLine.length];
						for (int i = 0; i < nextLine.length; ++i) {
							currentRecordFieldList[i] = new FieldValueStringList(nextLine[i].split(VERTICAL_TAB));
						}
						result = true;
					}
				} else {
					result = true;
				}
			} catch (Exception e) {
				lastException = e;
			}
		}
		return result;
	}
	@Override
	public FieldValueStringList[] next() {
		FieldValueStringList[] result = null;
		if (hasNext()) {
			result = currentRecordFieldList;
			currentRecordFieldList = null;
		}
		return result;
	}
	@Override
	public void remove() {
		return;
	}

}
