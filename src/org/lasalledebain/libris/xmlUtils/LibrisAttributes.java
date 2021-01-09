/**
 * 
 */
package org.lasalledebain.libris.xmlUtils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.TreeMap;
import java.util.logging.Level;

import org.lasalledebain.libris.LibrisConstants;
import org.lasalledebain.libris.LibrisDatabase;
import org.lasalledebain.libris.LibrisMetadata;
import org.lasalledebain.libris.exception.DatabaseException;
import org.lasalledebain.libris.exception.InputException;

public class LibrisAttributes implements Iterable<Entry<String, String>> {
	private final TreeMap<String, String> attributeList;
	private final HashSet<String> defaultValues;
	private LibrisAttributes next;
	protected DateFormat timeInstance;

	public LibrisAttributes() {
		this.defaultValues = new HashSet<String>();
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

	public LibrisAttributes(LibrisAttributes attrs) {
		this();
		for (Entry<String, String> keyValue: attrs.attributeList.entrySet()) {
			setAttribute(keyValue.getKey(), keyValue.getValue());
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

	public void setDefaultAttribute(String key, String value) {
		defaultValues.add(key);
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
		if (null != dbDateString) {
			try {
				Date modDate = LibrisMetadata.parseDateString(dbDateString);
				if (null != modDate) {
					return modDate;
				}
			} catch (ParseException e) {
				LibrisDatabase.log(Level.WARNING, "DatabaseAttributes: Invalid date string: "+dbDateString);
			}
		}
		return null;
	}

	private class AttributeIterator implements Iterator<Entry<String, String>> {
		Iterator<Entry<String, String>> iter;
		public AttributeIterator() {
			iter = attributeList.entrySet().iterator();
		}
		@Override
		public boolean hasNext() {
			return iter.hasNext();
		}

		@Override
		public Entry<String, String> next() {
			Entry<String, String> result = iter.next();
			return result;
		}

		@Override
		public void remove() {
			return;
		}
	}
	@Override
	public Iterator<Entry<String, String>> iterator() {
		return new AttributeIterator();
	}

	public String get(String attrName) {
		return attributeList.get(attrName);
	}
	
	public int getInt(String attrName) throws InputException {
		String attrString = attributeList.get(attrName);
		try {
			return Integer.parseInt(attrString);
		} catch (NumberFormatException e) {
			throw new InputException("malformed integer: "+attrString, e);
		}
	}
	
	public int getInt(String attrName, int defaultValue) throws InputException {
		if (contains(attrName)) {
			return getInt(attrName);
		} else {
			return defaultValue;
		}
	}
	
	public boolean contains(String attrName) {
		String value = attributeList.get(attrName);
		return Objects.nonNull(value) && !value.isEmpty();
	}
	
	public boolean isDefault(String key) {
		return defaultValues.contains(key);
	}
}