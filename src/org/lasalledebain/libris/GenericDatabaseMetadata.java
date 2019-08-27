package org.lasalledebain.libris;

import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

import org.lasalledebain.libris.xmlUtils.LibrisXMLConstants;

public abstract class GenericDatabaseMetadata implements LibrisXMLConstants {

	protected int lastRecordId;
	private Date databaseDate;
	protected boolean lastRecOkay;
	protected int signatureLevels;
	protected int savedRecords;
	private static SimpleDateFormat dateAndTimeFormatter = new SimpleDateFormat(LibrisConstants.YMD_TIME_TZ);
	private static SimpleDateFormat compactDateFormatter = new SimpleDateFormat(LibrisConstants.YMD);
	/**
	 * Dynamic attributes of the database
	 */
	protected Properties usageProperties;

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

	public static String formatDateAndTime(Date theDate) {
		return dateAndTimeFormatter.format(theDate);
	}

	public static String formatCompactDate(Date theDate) {
		return compactDateFormatter.format(theDate);
	}

	public synchronized int getLastRecordId() {
		return lastRecordId;
	}

	public synchronized void setLastRecordId(final int recId) {
		if ((RecordId.isNull(lastRecordId)) || ((recId > lastRecordId))) {
			lastRecordId = recId;
		}
		lastRecOkay = true;
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
