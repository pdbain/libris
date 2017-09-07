/*
 * Created on Jan 3, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package Libris;

import java.awt.Color;
import java.util.ArrayList;
import java.util.StringTokenizer;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTextArea;

import Libris.LibrisException.ErrorIds;
import Libris.LibrisFieldDatatype.FieldType;
import Libris.LibrisSchema.librisEnumSet;

public class LibrisRecordField {
	public LibrisRecordField createField(LibrisSchemaField schemaField) throws Exception {
		LibrisRecordField field;
		LibrisFieldDatatype.FieldType type;
		type = schemaField.getDatatype();
		switch (type) {
		case T_FIELD_STRING: 
			field = new LibrisStringField(schemaField);
			break;
		case T_FIELD_BOOLEAN: field = new LibrisBooleanField(schemaField); break;
		case T_FIELD_INTEGER: field = new LibrisIntegerField(schemaField); break;
		case T_FIELD_INTEGERS: field = new LibrisIntegersField(schemaField); break;
		case T_FIELD_INDEXENTRY: field = new LibrisIndexEntryField(schemaField); break;
		case T_FIELD_ENUM: field = new LibrisEnumField(schemaField); break;
		default: field = null;
		}
		return field;
	}


		public JComponent createControl() {
		// TODO Auto-generated method stub
		return null;
	}
		public void getDataFromControl() {
			/* null method, must be overridden */
			
		}


		public void setEditable(boolean editable) {
		// TODO Auto-generated method stub
		return;
	}


		public LibrisRecordField() {
			// TODO Auto-generated constructor stub
		}
		private static final int MIN_KEYWORD_LENGTH = 1;
		LibrisSchemaField schemaField;
		private int verbosity = 0;

		public LibrisRecordField(LibrisSchemaField schemaField) {
			this.schemaField = schemaField;
		}

		public void setSchemaField(LibrisSchemaField schemaField) {
			this.schemaField = schemaField;
		}
		public LibrisSchemaField getSchemaField() {
			return this.schemaField;
		}

		public ArrayList <String> getKeywords() {
			ArrayList<String> keywords = new ArrayList<String>();
			return getKeywords(keywords);
		}
		/**
		 * @param keywords: ArrayList
		 * @return: keywords with keywords added
		 */
		protected ArrayList<String> getKeywords(ArrayList<String> keywords) {
			if (schemaField.isIndexable()) {
				String stringValue = getValue();
				if (stringValue != null) {
					String keyStrings[] = stringValue.split("\\s+");
					for (int i=0; i < keyStrings.length; ++i) {
						if (keyStrings[i].length() >= MIN_KEYWORD_LENGTH) {
							keywords.add(keyStrings[i]);
						}
					}
				}
			}
			return keywords;
		}

		public String getValue() {
			return null;
		}
		public void setValue(String valuebuffer) throws Exception {
			return;
		}
		public void addValue(String valuebuffer) throws Exception {
			// Dummy 			
		}
		public FieldType getDatatype() {
			// TODO Auto-generated method stub
			return null;
		}


		protected void logMsg(String msg) {
			LibrisMain.logMsg(verbosity, msg);
		}
		
		protected JTextArea createTextControl() {
			String value = getValue();
			JTextArea fieldControl;
			if ((value == null) || (value.length() == 0)) {
				fieldControl = new JTextArea( 1, 16);
			} else {
				fieldControl = new JTextArea( value);
				fieldControl.setLineWrap(true);
				fieldControl.setWrapStyleWord(true);
			}
			fieldControl.setBorder(BorderFactory.createLineBorder(Color.black));
			fieldControl.setLineWrap(true);
			fieldControl.setWrapStyleWord(true);
			return fieldControl;

		}

		public class LibrisStringField extends LibrisRecordField{
			String fieldData;
			protected JTextArea control;
			public LibrisStringField(LibrisSchemaField schemaField) {
				this.schemaField = schemaField;
			}

			public FieldType getDatatype() {
				return FieldType.T_FIELD_STRING;
			}
			public  String getValue() {
				return (fieldData == null) ? "": fieldData;
			}

			public void setValue(String s) throws Exception {
				this.fieldData = s;
			}
			public void addValue(String s) throws Exception {
				if (this.fieldData == null) {
					this.fieldData = s;
				} else if (this.fieldData.endsWith(" ") || this.fieldData.endsWith("\n")) {
					this.fieldData += s;
				} else {
					this.fieldData += " "+s;
				}
			}
			public JTextArea createControl() {
				return (this.control = createTextControl());
			}
			public void setEditable(boolean editable) {
				this.control.setEditable(editable);
			}

			@Override
			public void getDataFromControl() {
				if (null != control) {
					try {
						setValue(control.getText());
					} catch (Exception e) {
						
					}
				}
			}
			
			
		}

		public class LibrisIntegerField extends LibrisRecordField{
			int fieldData;
			protected JTextArea control;
			public LibrisIntegerField(LibrisSchemaField schemaField) throws Exception {
				this.schemaField = schemaField;
				fieldData = 0;
			}

			public FieldType getDatatype() {
				return FieldType.T_FIELD_STRING;
			}
			public  String getValue() {
				return Integer.toString(fieldData);
			}

			public void setValue(String s) throws Exception {
				this.fieldData = Integer.parseInt(s);
			}
			public void addValue(String s) throws Exception {
				throw new LibrisException(ErrorIds.ERR_ADD_TO_INT);
			}
			public JTextArea createControl() {
				return (control = createTextControl());
			}

			@Override
			public void setEditable(boolean editable) {
				// TODO Auto-generated method stub
				
			}
		}

		public class LibrisIntegersField extends LibrisRecordField{
			private ArrayList <Integer> values;
			protected JTextArea control;

			public LibrisIntegersField(LibrisSchemaField schemaField) throws Exception {
				this.schemaField = schemaField;
				values = new ArrayList<Integer>(0);
			}

			public FieldType getDatatype() {
				return FieldType.T_FIELD_INTEGERS;
			}
			public void setValue(String s) {
				values.clear();
				addValue(s);
			}

			public void addValue(String s) {
				StringTokenizer st = new StringTokenizer(s);
				while (st.hasMoreTokens()) {
					values.add(Integer.getInteger(st.nextToken()));
				}
			}

			public String getValue() {
				// TODO Auto-generated method stub
				return(values.toString());
			}
			public JTextArea createControl() {
				return (this.control = createTextControl());
			}

			@Override
			public void setEditable(boolean editable) {
				// TODO Auto-generated method stub
				
			}
		}

		public class LibrisBooleanField extends LibrisRecordField {

			private boolean value;
			public LibrisBooleanField(LibrisSchemaField schemaField) {
				super(schemaField);
				value = false;
			}

			public void addValue(String s) throws Exception {
				throw new LibrisException(ErrorIds.ERR_ADD_TO_BOOLEAN);
			}
			public void setValue(String s) {
				value = Boolean.getBoolean(s);

			}
			public final FieldType getDatatype() {
				return(FieldType.T_FIELD_BOOLEAN);
			}

			public String getValue() {
				return(Boolean.toString(value));
			}

			public JComponent createControl() {
				String value = getValue();
				JCheckBox fieldControl;
				String name = getSchemaField().getName();
				boolean selected = Boolean.parseBoolean(value);

				fieldControl = new JCheckBox();
				fieldControl.setSelected(selected);
				return fieldControl;
			}

			@Override
			public void setEditable(boolean editable) {
				// TODO Auto-generated method stub
				
			}
		}
		public class LibrisIndexEntryField extends LibrisRecordField {

			private String value;
			public LibrisIndexEntryField(LibrisSchemaField schemaField) throws Exception {
				super(schemaField);
				value = "";
			}

			public void addValue(String s) throws Exception {
				throw new LibrisException(ErrorIds.ERR_ADD_TO_INDEX);
			}
			public void setValue(String s) {
				value = s;

			}
			public final FieldType getDatatype() {
				return(FieldType.T_FIELD_BOOLEAN);
			}

			public String getValue() {
				return(value);
			}

			@Override
			public JComponent createControl() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public void setEditable(boolean editable) {
				// TODO Auto-generated method stub
				
			}
		}
		public class LibrisEnumField extends LibrisRecordField {

			private String value;
			private JComboBox ctrl;
			
			public LibrisEnumField(LibrisSchemaField schemaField) throws Exception {
				super(schemaField);
				value = "";
			}

			public void addValue(String s) throws Exception {
				throw new LibrisException(ErrorIds.ERR_ADD_TO_ENUM);
			}
			
			/**
			 * @param valueOrId is an enumchoice ID if the field is restricted, or the value if not
			 * @throws LibrisException 
			 */
			public void setValue(String valueOrId) throws LibrisException {
				value = valueOrId;
				if (schemaField.isRestricted()) {
					if (null == schemaField.getEnumChoice(valueOrId)) {
						throw new LibrisException(ErrorIds.ERR_INVALID_ENUMCHOICE_ID, " in field "+schemaField.getName());
					}
				}

			}
			public final FieldType getDatatype() {
				return(FieldType.T_FIELD_BOOLEAN);
			}

			public String getValue() {
				return(value);
			}

			@Override
			public JComponent createControl() {
				ctrl = new JComboBox();
				librisEnumSet enumSet = schemaField.getEnumset();
				int numValues = enumSet.getNumValues();
				String enumItem;
				for (int i = 0; i < numValues; ++i) {
					enumItem = enumSet.getValue(i);
					ctrl.addItem(enumItem);
				}
				ctrl.setEditable(!schemaField.isRestricted());
				return ctrl;
			}

			@Override
			public void setEditable(boolean editable) {
				// TODO Auto-generated method stub
				
			}
		}


}