package org.lasalledebain.libris;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.logging.Level;

import org.lasalledebain.libris.exception.DatabaseException;
import org.lasalledebain.libris.exception.InputException;
import org.lasalledebain.libris.exception.LibrisException;
import org.lasalledebain.libris.ui.LastFilterSettings;
import org.lasalledebain.libris.ui.Layouts;
import org.lasalledebain.libris.xmlUtils.ElementManager;
import org.lasalledebain.libris.xmlUtils.ElementWriter;
import org.lasalledebain.libris.xmlUtils.LibrisAttributes;
import org.lasalledebain.libris.xmlUtils.LibrisXMLConstants;

public class LibrisMetadata implements LibrisXMLConstants, XMLElement {

	private LibrisDatabase database;
	private Layouts myLayouts;
	private int lastRecordId;
	private int savedRecords;
	private int modifiedRecords;
	private boolean schemaInline;
	DatabaseInstance instanceInfo;
	public void setInstanceInfo(DatabaseInstance instanceInfo) {
		this.instanceInfo = instanceInfo;
	}

	public boolean isSchemaInline() {
		return schemaInline;
	}

	public void setSchemaInline(boolean schemaInline) {
		this.schemaInline = schemaInline;
	}

	/**
	 * Dynamic attributes of the database
	 */
	private Properties usageProperties;

	private boolean lastRecOkay;
	private LastFilterSettings lastFiltSettings;
	private static SimpleDateFormat dateFormatter = new SimpleDateFormat(LibrisConstants.YMD_TIME_TZ);

	public LastFilterSettings getLastFilterSettings() {
		return lastFiltSettings;
	}

	public LibrisMetadata(LibrisDatabase database) {
		this.database = database;
		usageProperties = new Properties();
		lastFiltSettings = new LastFilterSettings();
	}

	public void fromXml(ElementManager metadataMgr) throws InputException, DatabaseException {
		ElementManager schemaMgr;

		metadataMgr.parseOpenTag();
		schemaMgr = metadataMgr.nextElement();
		Schema schem = new Schema();
		schem.fromXml(schemaMgr);
		database.setSchema(schem);
		myLayouts = new Layouts(database);
		ElementManager layoutsMgr = metadataMgr.nextElement();
		myLayouts.fromXml(layoutsMgr);
		metadataMgr.parseClosingTag();
	}
	public Layouts getLayouts() {
		return myLayouts;
	}

	public void saveProperties(FileOutputStream propertiesFile) throws IOException {
		usageProperties.setProperty(LibrisConstants.PROPERTY_LAST_SAVED, getCurrentDateString());
		String lastId = RecordId.toString(lastRecordId);
		usageProperties.setProperty(LibrisConstants.PROPERTY_LAST_RECORD_ID, lastId);
		usageProperties.setProperty(LibrisConstants.PROPERTY_RECORD_COUNT, (0 == savedRecords)? "0": Integer.toString(savedRecords));
		usageProperties.store(propertiesFile, "");
	}

	LibrisException readProperties(LibrisDatabase librisDatabase, FileInputStream ipFile) throws LibrisException, IOException {
		Properties props = new Properties();
		props.load(ipFile);
		usageProperties = props;
		usageProperties.setProperty(LibrisConstants.PROPERTY_LAST_OPENED, LibrisMetadata.getCurrentDateString());
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
		return null;
	}

	public static String getCurrentDateString() {
		return formatDate(getCurrentDate());
	}

	public static Date getCurrentDate() {
		return new Date();
	}

	public static String formatDate(Date theDate) {
		return dateFormatter.format(theDate);
	}

	public synchronized int getLastRecordId() {
		return lastRecordId;
	}
	public synchronized int getRecordIdBase() {
		int result =  (null == instanceInfo) ? 0 : instanceInfo.getRecordIdBase();
		return result;
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
		if (!comparand.getClass().isAssignableFrom(LibrisMetadata.class)) {
			return false;
		} else {
			LibrisMetadata otherMetadat = (LibrisMetadata) comparand;
			return myLayouts.equals(otherMetadat.myLayouts);
		}
	}

	public int getFieldNum() {
		return 0;
	}	
}
