package org.lasalledebain.libris;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;

import org.lasalledebain.libris.exception.DatabaseException;
import org.lasalledebain.libris.xmlUtils.LibrisAttributes;
import org.lasalledebain.libris.xmlUtils.LibrisXMLConstants;

public class DatabaseAttributes extends LibrisAttributes implements LibrisXMLConstants {
	private static final String HH_MM_SS = "HH:mm:ss";
	private static final String YMD = "yyyy-MM-dd";
	private static final String YMD_TIME = YMD+" "+HH_MM_SS;
	private static final String YMD_TIME_TZ = YMD_TIME+" z";
	private Date modificationDate;
	private LibrisDatabase db;
	private String databaseName;
	public static int  parseBranchString(String branchString) throws DatabaseException {
		int branchId = 0;
		if (null != branchString) {
			try {
				branchId = Integer.parseInt(branchString);
			} catch (NumberFormatException e) {
				throw new DatabaseException("error parsing branch ID "+branchString, e);
			}
		}
		return branchId;
	}
	public LibrisDatabase getDatabase() {
		return db;
	}
	@Override
	public void setAttribute(String key, String value) {
		db.alert("setAttribute unimplemented for "+getClass().getName());
	}
	
	public DatabaseAttributes(LibrisDatabase db, Map<String, String> attrs) throws DatabaseException {
		super(attrs);
		this.db = db;
		String schemaversion = attrs.get(XML_SCHEMA_VERSION_ATTR);
		String schemaname = attrs.get(XML_SCHEMA_NAME_ATTR);
		databaseName = attrs.get(XML_DATABASE_NAME_ATTR);
		if ( schemaversion.isEmpty() || schemaname.isEmpty()) {
			throw new DatabaseException("Missing required attributes in the "+XML_LIBRIS_TAG+" element");
		}
		String dbDateString = attrs.get(XML_DATABASE_DATE_ATTR);
		if (null != dbDateString) {
			for (String fmt: new String[] {YMD_TIME_TZ, YMD_TIME, YMD}) {
				try {
					timeInstance = new SimpleDateFormat(fmt);
					timeInstance.setLenient(true);
					modificationDate = timeInstance.parse(dbDateString);
					break;
				} catch (ParseException e) {
					db.log(Level.WARNING, "DatabaseAttributes: Invalid date string: "+dbDateString+" for "+fmt);
				}
			}
		}
		String branchString = attrs.get(XML_DATABASE_BRANCH_ATTR);
		parseBranchString(branchString);
		String lastChildString = attrs.get(XML_DATABASE_BRANCH_ATTR);
		parseBranchString(lastChildString);
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

}
