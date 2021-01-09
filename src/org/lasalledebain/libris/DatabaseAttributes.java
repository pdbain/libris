package org.lasalledebain.libris;

import java.util.Date;

import org.lasalledebain.libris.exception.DatabaseException;
import org.lasalledebain.libris.xmlUtils.LibrisAttributes;
import org.lasalledebain.libris.xmlUtils.LibrisXMLConstants;

public class DatabaseAttributes extends LibrisAttributes implements LibrisXMLConstants {
	@Override
	public void setAttribute(String key, String value) {
		super.setAttribute(key, value);
	}
	
	public DatabaseAttributes()  {
		super();
	}
		public DatabaseAttributes(LibrisAttributes attrs) throws DatabaseException {
		super(attrs);

	}

	public String getDatabaseName() {
		return get(XML_DATABASE_NAME_ATTR);
	}
	public void setDatabaseName(String databaseName) {
		setAttribute(XML_DATABASE_NAME_ATTR, databaseName);
	}
	public void setModificationDate() {
		Date modificationDate = new Date();
		setAttribute(XML_DATABASE_DATE_ATTR, timeInstance.format(modificationDate));
	}
	public Date getModificationDate() {
		String dbDateString = get(XML_DATABASE_DATE_ATTR);
		Date modificationDate = parseDate(dbDateString);
		return modificationDate;
	}
	
	public boolean isLocked() {
		return Boolean.getBoolean(get(XML_DATABASE_LOCKED_ATTR));
	}
	public void setLocked(boolean locked) {
		setAttribute(XML_DATABASE_LOCKED_ATTR, Boolean.toString(locked));
	}

	public String getSchemaName() {
		return get(XML_DATABASE_SCHEMA_NAME_ATTR);
	}

	public String getMetadataLocation() {
		return get(XML_DATABASE_METADATA_LOCATION_ATTR);
	}

}
