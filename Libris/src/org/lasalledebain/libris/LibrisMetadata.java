package org.lasalledebain.libris;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.logging.Level;

import org.lasalledebain.libris.exception.DatabaseException;
import org.lasalledebain.libris.exception.InputException;
import org.lasalledebain.libris.exception.LibrisException;
import org.lasalledebain.libris.ui.Layouts;
import org.lasalledebain.libris.xmlUtils.ElementManager;
import org.lasalledebain.libris.xmlUtils.ElementWriter;
import org.lasalledebain.libris.xmlUtils.LibrisAttributes;
import org.lasalledebain.libris.xmlUtils.LibrisXMLConstants;

public class LibrisMetadata implements LibrisXMLConstants, XmlExportable {

	private LibrisDatabase database;
	private Layouts myLayouts;
	private RecordId lastRecordId;
	private int savedRecords;
	private int modifiedRecords;
	private boolean schemaInline;
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

	public LibrisMetadata(LibrisDatabase database) {
		this.database = database;
		usageProperties = new Properties();
	}
	
	public void readMetadata(ElementManager metadataMgr) throws InputException, DatabaseException {
		ElementManager schemaMgr;
		
		metadataMgr.parseOpenTag();
		schemaMgr = metadataMgr.nextElement();
		Schema schem = new Schema();
		schem.fromXml(schemaMgr);
		database.setSchema(schem);
		myLayouts = new Layouts(database);
		ElementManager layoutsMgr = metadataMgr.nextElement();
		myLayouts.fromXml(schem, layoutsMgr);
		metadataMgr.parseClosingTag();
	}
	public Layouts getLayouts() {
		return myLayouts;
	}

	public void saveProperties(FileOutputStream propertiesFile) throws IOException {
			usageProperties.setProperty(LibrisConstants.PROPERTY_LAST_SAVED, getCurrentDateString());
			String lastId = lastRecordId.toString();
			usageProperties.setProperty(LibrisConstants.PROPERTY_LAST_RECORD_ID, lastId);
			usageProperties.setProperty(LibrisConstants.PROPERTY_RECORD_COUNT, Integer.toString(savedRecords));
			usageProperties.setProperty(LibrisConstants.PROPERTY_DATABASE_BRANCH, database.getBranchString());
			final String lastChild = database.getLastChildString();
				usageProperties.setProperty(LibrisConstants.PROPERTY_DATABASE_LAST_CHILD, lastChild.toString());
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
			database.log(Level.WARNING, "Error reading "+LibrisConstants.PROPERTY_RECORD_COUNT+" value = "+recCount, exc);
			savedRecords = 0;
		}
		int branchNum = 0;
		String branchString = usageProperties.getProperty(LibrisConstants.PROPERTY_DATABASE_BRANCH);
		branchNum = DatabaseAttributes.parseBranchString(branchString);
		if (null != recordIdString) {
			try {
				lastRecordId = new RecordId(recordIdString);
				lastRecOkay = true;
			} catch (LibrisException e) {
				return e;
			}
		}
		database.setBranch(branchNum);
		String lastChildString = usageProperties.getProperty(LibrisConstants.PROPERTY_DATABASE_LAST_CHILD);

		int lastChild = DatabaseAttributes.parseBranchString(lastChildString);
		database.setLastChild(lastChild);
		return null;
	}

	public static String getCurrentDateString() {
		return (DateFormat.getDateTimeInstance()).format(new Date());
	}

	public synchronized RecordId getLastRecordId() {
		return lastRecordId;
	}
	public synchronized void setLastRecordId(RecordId recordId) {
		lastRecordId = recordId;
		lastRecOkay = true;
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

	@Override
	public void toXml(ElementWriter output) throws LibrisException {
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
	
	
}
