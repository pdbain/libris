/*
 * Created on Dec 25, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package Libris;

import java.util.ArrayList;
import java.util.StringTokenizer;

public class LibrisFieldDatatype {
	public enum FieldType {
		T_FIELD_UNKNOWN(-1,"UNKNOWN"),
		T_FIELD_STRING(0,"string"),
		T_FIELD_BOOLEAN(1, "boolean"),
		T_FIELD_INTEGER(2, "INTEGER"),
		T_FIELD_INTEGERS(3, "INTEGERS"), 
		T_FIELD_INDEXENTRY(4, "INDEXENTRY"), 
		T_FIELD_ENUM(5, "ENUM");
		private final int value;
		private final String name;

		public int getValue() {
			return value;
		}
		
		public static Object stringToObject(String s, FieldType t) throws Exception {
			Object o = null;
			switch (t) {
			case T_FIELD_BOOLEAN: o = Boolean.getBoolean(s); break;
			case T_FIELD_ENUM: throw new Exception("T_FIELD_ENUM not supported");
			case T_FIELD_INDEXENTRY: throw new Exception("T_FIELD_INDEXENTRY not supported");
			case T_FIELD_INTEGER: o = Integer.parseInt(s);
			case T_FIELD_INTEGERS: 
				StringTokenizer st = new StringTokenizer(s);
				int n = st.countTokens();
				ArrayList<Integer> l = new ArrayList <Integer> (n);
				o = l;
				while (st.hasMoreTokens()) {	
					l.add(Integer.getInteger(st.nextToken()));
				}
				break;
			case T_FIELD_STRING: o = s.toString(); break;
			case T_FIELD_UNKNOWN: throw new Exception("Unknown field type");
			}
			return o;
		}
		
		public static Object addValue(Object o, FieldType t, String s) throws Exception {
			if (t.equals(FieldType.T_FIELD_INTEGERS)) {
				((ArrayList <Integer>) o).add(Integer.getInteger(s));
			} else {
				o = stringToObject(s, t);
			}
			return o;
		}

		FieldType(int value, String name){
			this.value = value;
			this.name = name;
		}

	}
	
	public LibrisFieldDatatype() {
				
	}

	/**
	 * @param value
	 * @return
	 * @throws Exception 
	 */
	public static FieldType getDatatype(String value) throws Exception {
		for (FieldType t: FieldType.values()) {
			if (t.name.equalsIgnoreCase(value))  return t;
		}
		throw new Exception("field type "+value+" not recognized");
	}

	public static LibrisRecordField createField(LibrisSchemaField sf) {
		LibrisRecordField object = new LibrisRecordField(sf);
		return object;
	}
}
