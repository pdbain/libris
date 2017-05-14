/*
 * Created on Dec 23, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package Libris;

import java.util.ArrayList;
import java.util.Iterator;

public class LibrisRecord {
	/**
	 * @author pdbain
	 *
	 */
	LibrisDatabase database;
	int recNum;
	LibrisSchema schema;
	RecordWindow window;
	StringArrayList fieldIDs;
	LibrisRecordField[] fieldList;
	
	public LibrisRecord(LibrisDatabase database, LibrisSchema schema) {
		this.schema = schema;
		this.database = database;
		if (schema == null) return;
		LibrisSchemaField f;
		fieldIDs = schema.getFieldIDs();
		int numFields = schema.numberOfFields();
		fieldList = new LibrisRecordField[numFields];
	}
	
	public void createBlankRecord() throws Exception {
		int numFields = schema.numberOfFields();
		for (int i = 0; i < numFields; i++ ) {
			addField(i);
		}
	}
	
	
	protected LibrisRecordField addField(int position) throws Exception {
		LibrisRecordField dataField = 
			schema.createRecordField(fieldIDs.getString(position));
		fieldList[position] =  dataField;
		return(dataField);
	}

	protected LibrisRecordField addField(String fieldID) throws Exception {
		LibrisRecordField dataField = schema.createRecordField(fieldID);
		int position = dataField.getSchemaField().getFieldNum();
		fieldList[position] =  dataField;
		return(dataField);
	}
	
	public LibrisRecordField getField(String fieldID) {
		LibrisRecordField dataField;
		int position = schema.getField(fieldID).getFieldNum();
		dataField = fieldList[position];
		return(dataField);	
	}
	public LibrisRecordField getField(int fieldNum) {
		return(fieldList[fieldNum]);
	}
	/**
	 * @return Returns the recNum.
	 */
	public int getRecNum() {
		return recNum;
	}
	/**
	 * @param recNum The recNum to set.
	 */
	public void setRecNum(int recNum) {
		this.recNum = recNum;
	}
	public String[] getKeywords() {
		ArrayList<String> keyTemp = new ArrayList();
		LibrisRecordField tempField;
		
		for (int i = 0; i < fieldList.length; ++i) {
			 tempField = (LibrisRecordField) fieldList[i];
			 if (tempField != null)
				 tempField.getKeywords(keyTemp);
		}
		String[] k = new String[keyTemp.size()];
		k = keyTemp.toArray(k);
		return(k);
	}
	
	public void show () {
		
	}
	
	public RecordWindow display() {
		return display(null, false);
	}
	public RecordWindow display(RecordWindow oldWindow, boolean editable) {
		if (oldWindow == null) {
			window = new RecordWindow(this, database, editable);
		} else {
			window = oldWindow;
			window.setContent(this);
		}
		return (window);
	}

	/**
	 * @return
	 */
	public LibrisRecordField[] getFieldList() {
		return(fieldList);
	}
	
	public String getSummary() {
		String summary = "";
		ArrayList <Integer> s = schema.getSummaryFields();
		Iterator <Integer> i = s.iterator();
		LibrisRecordField f = null;
		String sep = "";
		int n;
		while (i.hasNext()) {
			n = i.next();
			f = getField(n);
			if (f != null) {
				summary = summary + sep + f.getValue();
				sep = ", ";
			}
		}
		return summary;
	}
	
	/**
	 * Get the field data back into the variables
	 * 
	 */
	void closeRecord() {
		for (LibrisRecordField f: fieldList) {
			f.getDataFromControl();
		}
	}
}
