package org.lasalledebain.libris;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;
import java.util.Properties;
import java.util.logging.Level;

import org.lasalledebain.libris.exception.DatabaseException;
import org.lasalledebain.libris.xmlUtils.LibrisXMLConstants;

public class DatabaseMetadata implements LibrisXMLConstants {

	protected int lastRecordId;
	private int modifiedRecords;
	private Date databaseDate;
	protected boolean lastRecOkay;
	protected int signatureLevels;
	protected int savedRecords;
	private static SimpleDateFormat dateAndTimeFormatter = new SimpleDateFormat(LibrisConstants.YMD_TIME_TZ);
	private static SimpleDateFormat compactDateFormatter = new SimpleDateFormat(LibrisConstants.YMD);
	/**
	 * Dynamic attributes of the database
	 */
	protected final Properties usageProperties;
	
	public DatabaseMetadata() {
		usageProperties = new Properties();
	}

	public boolean readProperties(FileInputStream ipFile) {
		try {
			usageProperties.load(ipFile);
		} catch (IOException e) {
			LibrisDatabase.log(Level.SEVERE, "Error reading properties file", e);
			return false;
		}
		usageProperties.setProperty(LibrisConstants.PROPERTY_LAST_OPENED, LibrisMetadata.getCurrentDateAndTimeString());
		String recordIdString = usageProperties.getProperty(LibrisConstants.PROPERTY_LAST_RECORD_ID);
		String recCount = usageProperties.getProperty(LibrisConstants.PROPERTY_RECORD_COUNT);
		if (null != recCount) try {
			savedRecords = Integer.parseInt(recCount);
		} catch (NumberFormatException exc) {
			LibrisDatabase.log(Level.SEVERE, "Error reading "+LibrisConstants.PROPERTY_RECORD_COUNT+" value = "+recCount, exc);
			return false;
		}
		if ((null != recordIdString) && !recordIdString.isEmpty()) {
			try {
				lastRecordId = RecordId.toId(recordIdString);
			} catch (DatabaseException e) {
				LibrisDatabase.log(Level.SEVERE, "Invalid last record ID "+lastRecordId, e);
				return false;
			}
			lastRecOkay = true;
		} else {
			lastRecordId = RecordId.NULL_RECORD_ID;
		}
		String sigLevelsString = usageProperties.getProperty(LibrisConstants.PROPERTY_SIGNATURE_LEVELS);
		if (!Objects.isNull(sigLevelsString)) {
			signatureLevels = Integer.parseInt(sigLevelsString);
		} else {
			signatureLevels = 1;
		}
		return true;
	}

	public void saveProperties(FileOutputStream propertiesFile) throws IOException {
		usageProperties.setProperty(LibrisConstants.PROPERTY_LAST_SAVED, getCurrentDateAndTimeString());
		String lastId = RecordId.toString(lastRecordId);
		usageProperties.setProperty(LibrisConstants.PROPERTY_LAST_RECORD_ID, lastId);
		usageProperties.setProperty(LibrisConstants.PROPERTY_RECORD_COUNT, (0 == savedRecords)? "0": Integer.toString(savedRecords));
		usageProperties.setProperty(LibrisConstants.PROPERTY_SIGNATURE_LEVELS, String.valueOf(signatureLevels));
		usageProperties.store(propertiesFile, "");
	}

	public static Date parseDateString(String dateString) throws ParseException {
		return dateAndTimeFormatter.parse(dateString);
	}

	public static String getCurrentDateAndTimeString() {
		return formatDateAndTime(getCurrentDate());
	}

	public static String getCompactDateString() {
		return formatCompactDate(getCurrentDate());
	}

	public static Date getCurrentDate() {
		return new Date();
	}

	public boolean isMetadataOkay() {
		return lastRecOkay;
	}

	public static String formatDateAndTime(Date theDate) {
		return dateAndTimeFormatter.format(theDate);
	}

	public static String formatCompactDate(Date theDate) {
		return compactDateFormatter.format(theDate);
	}

	public synchronized int getLastRecordId() {
		return lastRecordId;
	}

	public int getNumRecords() {
		return lastRecordId;
	}
	
	public synchronized void setLastRecordId(final int recId) {
		if ((RecordId.isNull(lastRecordId)) || ((recId > lastRecordId))) {
			lastRecordId = recId;
		}
		lastRecOkay = true;
	}

	public int getModifiedRecords() {
		return modifiedRecords;
	}

	public void setModifiedRecords(int modifiedRecords) {
		this.modifiedRecords = modifiedRecords;
	}
	void adjustModifiedRecords(int numAdded) {
		this.modifiedRecords += numAdded;
	}

	public synchronized int newRecordId() {
		int newId = ++lastRecordId;
		return newId;
	}

	public static String getXmlTag() {
		return XML_METADATA_TAG;
	}

	public String getElementTag() {
		return getXmlTag();
	}

	public void setSignatureLevels(int sigLevels) {
		signatureLevels = sigLevels;
	}

	public int getSignatureLevels() {
		return signatureLevels;
	}

	public Date getDatabaseDate() {
		return databaseDate;
	}

	public void setDatabaseDate(Date databaseDate) {
		this.databaseDate = databaseDate;
	}

	public int getSavedRecords() {
		return savedRecords;
	}

	public void setSavedRecords(int savedRecords) {
		this.savedRecords = savedRecords;
	}

	public void adjustSavedRecords(int numAdded) {
		this.savedRecords += numAdded;
	}

}
