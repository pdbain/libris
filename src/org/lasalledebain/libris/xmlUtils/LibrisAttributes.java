/**
 * 
 */
package org.lasalledebain.libris.xmlUtils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;

import org.lasalledebain.libris.LibrisConstants;
import org.lasalledebain.libris.LibrisDatabase;
import org.lasalledebain.libris.exception.DatabaseException;

public class LibrisAttributes implements Iterable<String[]> {
	private TreeMap<String, String> attributeList;
	private LibrisAttributes next;
	protected DateFormat timeInstance;

	public LibrisAttributes() {
		attributeList = new TreeMap<String, String>();
		timeInstance = new SimpleDateFormat(LibrisConstants.YMD_TIME_TZ);
		next = null;
	}
	
	public LibrisAttributes(Map<String, String> attrs) throws DatabaseException {
		this();
		putAll(attrs);
	}

	public LibrisAttributes(String[][] attrs) {
		this();
		for (String[] keyValue: attrs) {
			setAttribute(keyValue[0], keyValue[1]);
		}
	}

	public LibrisAttributes getNext() {
		return null;
	}

	public void setNext(LibrisAttributes next) {
		this.next = next;
	}

	public void setAttribute(String key, String value) {
		attributeList.put(key, value);
	}

	public void setAttribute(String key, int value) {
		attributeList.put(key, Integer.toString(value));
	}


	public void addAttribute(String key, boolean value) {
		attributeList.put(key, Boolean.toString(value));
		}

	@Override
	public boolean equals(Object obj) {
		try {
			LibrisAttributes comparand = (LibrisAttributes) obj;
			if (!attributeList.equals(comparand.attributeList)) {
				return false;
			} else if (null != next) {
				LibrisAttributes cursor = next;
				LibrisAttributes compCursor = comparand.next;
				while ((null != compCursor) && (null != cursor)) {
					if (!compCursor.attributeList.equals(cursor.attributeList)) {
						return false;
					} else {
						compCursor = compCursor.next;
						cursor = cursor.next;
					}
				}
				if ((null != compCursor) || (null != cursor)) {
					return false;
				}
			}
			return true;
		} catch (ClassCastException e) {
			return false;
		}
	}

	public void putAll(Map<String, String> attribs) throws DatabaseException {
		 attributeList.putAll(attribs);
	}
	public static LibrisAttributes getLibrisEmptyAttributes() {
		return LibrisEmptyAttributes.getLibrisEmptyAttributes();
	}
	
	public static Date parseDate(String dbDateString) {
		DateFormat dfmt = null;
		if (null != dbDateString) {
			for (String fmt: new String[] {LibrisConstants.YMD_TIME_TZ, LibrisConstants.YMD_TIME, LibrisConstants.YMD}) {
				try {
					dfmt = new SimpleDateFormat(fmt);
					dfmt.setLenient(true);
					Date modDate = dfmt.parse(dbDateString);
					if (null != modDate) {
						return modDate;
					}
				} catch (ParseException e) {
					LibrisDatabase.log(Level.WARNING, "DatabaseAttributes: Invalid date string: "+dbDateString+" for "+fmt);
				}
			}
		}
		return null;
	}

	private class AttributeIterator implements Iterator<String[]> {
		Iterator<String> iter;
		public AttributeIterator() {
			iter = attributeList.navigableKeySet().iterator();
		}
		@Override
		public boolean hasNext() {
			return iter.hasNext();
		}

		@Override
		public String[] next() {
			String key = iter.next();
			String value = attributeList.get(key);
			String result[] = {key, value};
			return result;
		}

		@Override
		public void remove() {
			return;
		}
		
	}
	@Override
	public Iterator<String[]> iterator() {
		return new AttributeIterator();
	}

	public String get(String attrName) {
		return attributeList.get(attrName);
	}
}