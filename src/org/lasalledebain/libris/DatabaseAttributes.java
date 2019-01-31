package org.lasalledebain.libris;

import java.util.Date;
import java.util.Iterator;
import java.util.Map;

import org.lasalledebain.libris.exception.DatabaseException;
import org.lasalledebain.libris.xmlUtils.LibrisAttributes;
import org.lasalledebain.libris.xmlUtils.LibrisXMLConstants;

public class DatabaseAttributes extends LibrisAttributes implements LibrisXMLConstants {
	private Date modificationDate;
	private final LibrisDatabase db;
	private final String databaseName;
	private boolean locked;
	private final boolean hasRepo;
	private final String schemaName;
	private final String schemaLocation;
	private final String repositoryLocation;

	public LibrisDatabase getDatabase() {
		return db;
	}
	
	@Override
	public void setAttribute(String key, String value) {
		super.setAttribute(key, value);
	}
	
	public DatabaseAttributes(LibrisDatabase db, Map<String, String> attrs) throws DatabaseException {
		super(attrs);
		this.db = db;
		String schemaversion = attrs.get(XML_SCHEMA_VERSION_ATTR);
		schemaName = attrs.get(XML_DATABASE_SCHEMA_NAME_ATTR);
		schemaLocation = attrs.get(XML_DATABASE_SCHEMA_LOCATION_ATTR);
		databaseName = attrs.get(XML_DATABASE_NAME_ATTR);
		if ( schemaversion.isEmpty() || schemaName.isEmpty()) {
			throw new DatabaseException("Missing required attributes in the "+XML_LIBRIS_TAG+" element");
		}
		String dbDateString = attrs.get(XML_DATABASE_DATE_ATTR);
		modificationDate = parseDate(dbDateString);
		String lockedString = attrs.get(XML_DATABASE_LOCKED_ATTR);
		locked = Boolean.parseBoolean(lockedString);
		hasRepo = attrs.containsKey(XML_DATABASE_REPOSITORY_LOCATION_ATTR);
		if (hasRepo) {
			repositoryLocation = attrs.get(XML_DATABASE_REPOSITORY_LOCATION_ATTR);
		} else {
			repositoryLocation = null;
		}
	}
	public String getDatabaseName() {
		return databaseName;
	}
	public void setModificationDate() {
		modificationDate = new Date();
	}
	
	@Override
	public Iterator<String[]> iterator() {	
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
	 * @return the db
	 */
	public LibrisDatabase getDb() {
		return db;
	}

	/**
	 * @return the schemaLocation
	 */
	public String getSchemaLocation() {
		return schemaLocation;
	}

	public boolean hasRepository() {
		return hasRepo;
	}

	public String getRepositoryLocation() {
		return repositoryLocation;
	}

}
