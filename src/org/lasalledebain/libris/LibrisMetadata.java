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

import org.lasalledebain.libris.exception.LibrisException;
import org.lasalledebain.libris.ui.LastFilterSettings;
import org.lasalledebain.libris.ui.Layouts;
import org.lasalledebain.libris.xmlUtils.ElementWriter;
import org.lasalledebain.libris.xmlUtils.LibrisAttributes;
import org.lasalledebain.libris.xmlUtils.LibrisXMLConstants;

public abstract class LibrisMetadata implements LibrisXMLConstants, XMLElement {

	protected LibrisDatabase database;
	protected Layouts uiLayouts;
	private int lastRecordId;
	private int savedRecords;
	private int modifiedRecords;
	private DatabaseInstance instanceInfo;
	/**
	 * Dynamic attributes of the database
	 */
	private Properties usageProperties;

	private boolean lastRecOkay;
	private LastFilterSettings lastFiltSettings;
	private int signatureLevels;
	private static SimpleDateFormat dateAndTimeFormatter = new SimpleDateFormat(LibrisConstants.YMD_TIME_TZ);
	private static SimpleDateFormat compactDateFormatter = new SimpleDateFormat(LibrisConstants.YMD);

	public LibrisMetadata(LibrisDatabase database) {
		this.database = database;
		usageProperties = new Properties();
		lastFiltSettings = new LastFilterSettings();
		signatureLevels = 0;
	}

	public LibrisMetadata(LibrisDatabase database, Layouts myLayouts) {
		this.database = database;
		usageProperties = new Properties();
		lastFiltSettings = new LastFilterSettings();
		signatureLevels = 0;
		if (Objects.isNull(myLayouts)) {
			uiLayouts = new Layouts(database.getSchema());
		}
	}

	public Layouts getLayouts() {
		return uiLayouts;
	}

	public void saveProperties(FileOutputStream propertiesFile) throws IOException {
		usageProperties.setProperty(LibrisConstants.PROPERTY_LAST_SAVED, getCurrentDateAndTimeString());
		String lastId = RecordId.toString(lastRecordId);
		usageProperties.setProperty(LibrisConstants.PROPERTY_LAST_RECORD_ID, lastId);
		usageProperties.setProperty(LibrisConstants.PROPERTY_RECORD_COUNT, (0 == savedRecords)? "0": Integer.toString(savedRecords));
		usageProperties.setProperty(LibrisConstants.PROPERTY_SIGNATURE_LEVELS, String.valueOf(signatureLevels));
		usageProperties.store(propertiesFile, "");
	}

	LibrisException readProperties(FileInputStream ipFile) throws LibrisException, IOException {
		Properties props = new Properties();
		props.load(ipFile);
		usageProperties = props;
		usageProperties.setProperty(LibrisConstants.PROPERTY_LAST_OPENED, LibrisMetadata.getCurrentDateAndTimeString());
		String recordIdString = usageProperties.getProperty(LibrisConstants.PROPERTY_LAST_RECORD_ID);
		String recCount = usageProperties.getProperty(LibrisConstants.PROPERTY_RECORD_COUNT);
		if (null != recCount) try {
			savedRecords = Integer.parseInt(recCount);
		} catch (NumberFormatException exc) {
			LibrisDatabase.log(Level.WARNING, "Error reading "+LibrisConstants.PROPERTY_RECORD_COUNT+" value = "+recCount, exc);
			savedRecords = 0;
		}
		if ((null != recordIdString) && !recordIdString.isEmpty()) {
			try {
				lastRecordId = RecordId.toId(recordIdString);
				lastRecOkay = true;
			} catch (LibrisException e) {
				return e;
			}
		} else {
			lastRecordId = RecordId.getNullId();
		}
		String sigLevelsString = usageProperties.getProperty(LibrisConstants.PROPERTY_SIGNATURE_LEVELS);
		if (!Objects.isNull(sigLevelsString)) {
			signatureLevels = Integer.parseInt(sigLevelsString);
		} else {
			signatureLevels = 1;
		}
		return null;
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
	public synchronized int getRecordIdBase() {
		int result =  (null == instanceInfo) ? 0 : instanceInfo.getRecordIdBase();
		return result;
	}

	public void setInstanceInfo(DatabaseInstance instanceInfo) {
		this.instanceInfo = instanceInfo;
		lastRecordId = Math.max(lastRecordId, instanceInfo.getRecordIdBase());
	}

	public synchronized void setLastRecordId(final int recId) {
		if ((RecordId.isNull(lastRecordId)) || ((recId > lastRecordId))) {
			lastRecordId = recId;
		}
		lastRecOkay = true;
	}

	public DatabaseInstance getInstanceInfo() {
		return instanceInfo;
	}

	public synchronized int newRecordId() {
		int newId = ++lastRecordId;
		return newId;
	}
	public boolean isMetadataOkay() {
		return lastRecOkay;
	}

	public LastFilterSettings getLastFilterSettings() {
		return lastFiltSettings;
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

	public int getModifiedRecords() {
		return modifiedRecords;
	}

	public void setModifiedRecords(int modifiedRecords) {
		this.modifiedRecords = modifiedRecords;
	}
	public void adjustModifiedRecords(int numAdded) {
		this.modifiedRecords += numAdded;
	}

	public static String getXmlTag() {
		return XML_METADATA_TAG;
	}


	@Override
	public String getElementTag() {
		return getElementTag();
	}

	@Override
	public void toXml(ElementWriter output) throws LibrisException {
		toXml(output, false);
	}
	public void toXml(ElementWriter output, boolean addInstanceInfo) throws LibrisException {
		output.writeStartElement(XML_METADATA_TAG, getAttributes(), false);
		database.getSchema().toXml(output);
		database.getLayouts().toXml(output);
		output.writeEndElement();
	}

	@Override
	public LibrisAttributes getAttributes() {
		return LibrisAttributes.getLibrisEmptyAttributes();
	}

	@Override
	public boolean equals(Object comparand) {
		if (!LibrisMetadata.class.isAssignableFrom(comparand.getClass())) {
			return false;
		} else {
			LibrisMetadata otherMetadat = (LibrisMetadata) comparand;
			return uiLayouts.equals(otherMetadat.uiLayouts);
		}
	}

	public int getFieldNum() {
		return 0;
	}

	public void setSignatureLevels(int sigLevels) {
		signatureLevels = sigLevels;
	}

	public int getSignatureLevels() {
		return signatureLevels;
	}	
}
