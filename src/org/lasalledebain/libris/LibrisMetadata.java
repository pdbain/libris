package org.lasalledebain.libris;

import java.io.File;
import java.util.Objects;

import org.lasalledebain.libris.exception.LibrisException;
import org.lasalledebain.libris.ui.LastFilterSettings;
import org.lasalledebain.libris.ui.Layouts;
import org.lasalledebain.libris.xmlUtils.ElementWriter;
import org.lasalledebain.libris.xmlUtils.LibrisAttributes;

public abstract class LibrisMetadata<RecordType extends Record> extends DatabaseMetadata implements XMLElement {

	protected LibrisDatabase database;
	protected Layouts<RecordType> uiLayouts;
	private DatabaseInstance instanceInfo;

	private LastFilterSettings lastFiltSettings;
	public LibrisMetadata() {
		super();
		lastFiltSettings = new LastFilterSettings();
		signatureLevels = 0;
	}

	public LibrisMetadata(LibrisDatabase database) {
		this();
		this.database = database;
	}

	public LibrisMetadata(LibrisDatabase database, Layouts<RecordType> myLayouts) {
		this();
		this.database = database;
		if (Objects.isNull(myLayouts)) {
			uiLayouts = new Layouts<RecordType>(database.getSchema());
		}
	}

	public Layouts<RecordType> getLayouts() {
		return uiLayouts;
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

	public LastFilterSettings getLastFilterSettings() {
		return lastFiltSettings;
	}

	public boolean hasDocumentRepository() {
		return Boolean.parseBoolean(usageProperties.getProperty(LibrisConstants.PROPERTY_HAS_REPOSITORY));
	}

	public void hasDocumentRepository(boolean hasRepo) {
		usageProperties.setProperty(LibrisConstants.PROPERTY_HAS_REPOSITORY, Boolean.toString(hasRepo));
	}
	
	public void setRepositoryRoot(File repositoryRoot) {
		usageProperties.setProperty(LibrisConstants.PROPERTY_REPOSITORY_ROOT, repositoryRoot.getAbsolutePath());
	}

	public File  getRepositoryRoot() {
		return new File(usageProperties.getProperty(LibrisConstants.PROPERTY_REPOSITORY_ROOT));
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
			DatabaseMetadata otherMetadat = (DatabaseMetadata) comparand;
			return (lastRecordId == otherMetadat.lastRecordId)
					&& (signatureLevels == otherMetadat.signatureLevels);
		}
	}

	public int getFieldNum() {
		return 0;
	}	
}
