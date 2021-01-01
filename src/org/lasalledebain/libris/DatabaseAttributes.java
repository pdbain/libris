package org.lasalledebain.libris;

import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.lasalledebain.libris.exception.DatabaseException;
import org.lasalledebain.libris.xmlUtils.LibrisAttributes;
import org.lasalledebain.libris.xmlUtils.LibrisXMLConstants;

public class DatabaseAttributes extends LibrisAttributes implements LibrisXMLConstants {
	private Date modificationDate;
	private final String databaseName;
	private boolean locked;
	private final String schemaName;
	private final String metadataLocation;

	@Override
	public void setAttribute(String key, String value) {
		super.setAttribute(key, value);
	}
	
	public DatabaseAttributes(LibrisAttributes attrs) throws DatabaseException {
		super(attrs);
		String schemaversion = attrs.get(XML_SCHEMA_VERSION_ATTR);
		schemaName = attrs.get(XML_DATABASE_SCHEMA_NAME_ATTR);
		metadataLocation = attrs.get(XML_DATABASE_METADATA_LOCATION_ATTR);
		databaseName = attrs.get(XML_DATABASE_NAME_ATTR);
		if ( schemaversion.isEmpty() || schemaName.isEmpty()) {
			throw new DatabaseException("Missing required attributes in the "+XML_LIBRIS_TAG+" element");
		}
		String dbDateString = attrs.get(XML_DATABASE_DATE_ATTR);
		modificationDate = parseDate(dbDateString);
		String lockedString = attrs.get(XML_DATABASE_LOCKED_ATTR);
		locked = Boolean.parseBoolean(lockedString);
	}

	public String getDatabaseName() {
		return databaseName;
	}
	public String setDatabaseName(String databaseName) {
		return databaseName;
	}
	public void setModificationDate() {
		modificationDate = new Date();
	}
	
	@Override
	public Iterator<Entry<String, String>> iterator() {	
		if (null != modificationDate) {
			super.setAttribute(XML_DATABASE_DATE_ATTR, timeInstance.format(modificationDate));		
		}
		return super.iterator();
	}
	public boolean isLocked() {
		return locked;
	}
	public void setLocked(boolean locked) {
		this.locked = locked;
		setAttribute(XML_DATABASE_LOCKED_ATTR, Boolean.toString(locked));
	}

	public String getSchemaName() {
		return schemaName;
	}

	/**
	 * @return the metadataLocation
	 */
	public String getMetadataLocation() {
		return metadataLocation;
	}

}
