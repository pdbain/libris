package org.lasalledebain.libris;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Objects;
import java.util.Properties;
import java.util.logging.Level;

import org.lasalledebain.libris.exception.LibrisException;
import org.lasalledebain.libris.ui.LastFilterSettings;
import org.lasalledebain.libris.ui.Layouts;
import org.lasalledebain.libris.xmlUtils.ElementWriter;
import org.lasalledebain.libris.xmlUtils.LibrisAttributes;

public abstract class LibrisMetadata extends GenericDatabaseMetadata implements XMLElement, LibrisConstants {

	protected LibrisDatabase database;
	protected Layouts uiLayouts;
	private int modifiedRecords;
	private DatabaseInstance instanceInfo;

	private LastFilterSettings lastFiltSettings;
	public LibrisMetadata() {
		usageProperties = new Properties();
		lastFiltSettings = new LastFilterSettings();
		signatureLevels = 0;
	}

	public LibrisMetadata(LibrisDatabase database) {
		this();
		this.database = database;
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
			lastRecordId = RecordId.NULL_RECORD_ID;
		}
		String sigLevelsString = usageProperties.getProperty(PROPERTY_SIGNATURE_LEVELS);
		if (!Objects.isNull(sigLevelsString)) {
			signatureLevels = Integer.parseInt(sigLevelsString);
		} else {
			signatureLevels = 1;
		}
		return null;
	}

	public synchronized int getRecordIdBase() {
		int result =  (null == instanceInfo) ? 0 : instanceInfo.getRecordIdBase();
		return result;
	}

	public void setInstanceInfo(DatabaseInstance instanceInfo) {
		this.instanceInfo = instanceInfo;
		lastRecordId = Math.max(lastRecordId, instanceInfo.getRecordIdBase());
	}

	public DatabaseInstance getInstanceInfo() {
		return instanceInfo;
	}

	public boolean isMetadataOkay() {
		return lastRecOkay;
	}

	public LastFilterSettings getLastFilterSettings() {
		return lastFiltSettings;
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
		toXml(output, false);
	}
	public void toXml(ElementWriter output, boolean addInstanceInfo) throws LibrisException {
		output.writeStartElement(XML_METADATA_TAG, getAttributes(), false);
		writeContents(output);
		output.writeEndElement();
	}

	protected void writeContents(ElementWriter output) throws LibrisException {
		database.getSchema().toXml(output);
		uiLayouts.toXml(output);
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
			GenericDatabaseMetadata otherMetadat = (GenericDatabaseMetadata) comparand;
			return (lastRecordId == otherMetadat.lastRecordId)
					&& (signatureLevels == otherMetadat.signatureLevels);
		}
	}

	public int getFieldNum() {
		return 0;
	}	
}
