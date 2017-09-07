/*
 * Created on Dec 25, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package Libris;

import java.util.ArrayList;
import java.util.HashMap;

import org.xml.sax.Attributes;


/**
 * @author pdbain
 *
 */
public class LibrisSchema {
	private HashMap<String, LibrisSchemaField> fields;
	private StringArrayList fieldIDs;
	private ArrayList <Integer> summaryFields;
	private HashMap <String, librisEnumSet> enumSets;
	private LibrisRecordField fieldFactory;
	private librisEnumSet currentEnumSet;
	
	public LibrisSchema() {
		fields = new HashMap<String, LibrisSchemaField>();
		fieldIDs = new StringArrayList();
		summaryFields = new ArrayList<Integer>();
		this.fieldFactory = new LibrisRecordField();
		enumSets = new HashMap<String, librisEnumSet>();
		this.currentEnumSet = null;
	}
	protected void AddField(Attributes attrs) {
		LibrisSchemaField field = new LibrisSchemaField(this, attrs);
		String id = attrs.getValue("id");
		fields.put(id, field);
		fieldIDs.add(id);
		String sum = attrs.getValue("summary");
		int fieldNum = fieldIDs.size()-1;
		if (sum.equals("true")) {
			summaryFields.add(fieldNum);
			field.setSummary(true);
		}
		field.setFieldNum(fieldNum);
		sum = attrs.getValue("indexable");
		if (sum.equals("true")) {
			field.setIndexable(true);
		}
		String enumSet = attrs.getValue("enumset");
	}
	/**
	 * @return Returns the fieldIDs.
	 */
	public StringArrayList getFieldIDs() {
		return(fieldIDs);
	}
	/**
	 * @return Returns the fields.
	 */
	public LibrisSchemaField getField(String ID) {
		return fields.get(ID);
	}
	/**
	 * @return
	 */
	public int numberOfFields() {
		// TODO Auto-generated method stub
		return(fieldIDs.size());
	}
	/**
	 * @return
	 * @throws Exception 
	 */
	public LibrisRecordField createRecordField(String fieldID) throws Exception {
		LibrisSchemaField sf = getField(fieldID);
		LibrisRecordField field = this.fieldFactory.createField(sf);
		return(field);
	}
	
	public ArrayList <Integer> getSummaryFields() {
		return(summaryFields);
	}
	public void newEnumSet(Attributes attrs) throws LibrisException {
		String setName = attrs.getValue("id");
		if ((setName == null) || (setName.equals("")) || (enumSets.get(setName) != null)) {
			throw new LibrisException(LibrisException.ErrorIds.ERR_INVALID_ENUMSET_ID);
		}
		this.currentEnumSet = new librisEnumSet(setName);
	}

	public void endEnumSet() throws LibrisException {
		currentEnumSet.endOfChoices();
		enumSets.put(currentEnumSet.getEnumSetName(), currentEnumSet);
	}

	public void addEnumChoice(Attributes attrs) throws LibrisException {
		if (null == currentEnumSet) {
			throw new LibrisException(LibrisException.ErrorIds.ERR_ENUMCHOICE_NOT_IN_ENUMSET);
		}
		String choiceName = attrs.getValue("id");
		if ((choiceName == null) || (choiceName.equals("")) || (currentEnumSet.get(choiceName) != null)) {
			throw new LibrisException(LibrisException.ErrorIds.ERR_INVALID_ENUMCHOICE_ID, " in enumset "+currentEnumSet.enumSetName);
		}
		String value = attrs.getValue("value");
		if (value == null) {
			throw new LibrisException(LibrisException.ErrorIds.ERR_INVALID_ENUMCHOICE_VALUE, choiceName);
		}
		this.currentEnumSet.put(choiceName, value);
	}

	private ArrayList <String> valuesBuffer;
	private ArrayList <String> idsBuffer;
	public class librisEnumSet extends HashMap<String,Integer> {
		 String enumSetName;
		 String[] values;
		 String[] ids;
		 int numValues;

		 public int getNumValues() {
			return numValues;
		}

		public void put(String id, String value) {
			 idsBuffer.add(id);
			 valuesBuffer.add(value);
		 }
		 
		public void endOfChoices() {
			values = new String[idsBuffer.size()];
			ids = idsBuffer.toArray(new String[idsBuffer.size()]);
			values = valuesBuffer.toArray(new String[idsBuffer.size()]);
			numValues = idsBuffer.size();
			idsBuffer = null;
			valuesBuffer = null;
		}

		public String getEnumSetName() {
			return enumSetName;
		}

		public librisEnumSet(String enumSetName) {
			super();
			this.enumSetName = enumSetName;
			numValues = 0;
			valuesBuffer = new ArrayList<String>();
			idsBuffer = new ArrayList<String>();
		}

		public String getValue(int i) {
			return values[i];
		}
	}

	public librisEnumSet getEnumSet(String id) {
		librisEnumSet e = enumSets.get(id);
		return e;
	}

}
