package org.lasalledebain.libris;

import org.lasalledebain.libris.exception.InputException;
import org.lasalledebain.libris.exception.LibrisException;
import org.lasalledebain.libris.exception.XmlException;
import org.lasalledebain.libris.indexes.LibrisJournalFileManager;
import org.lasalledebain.libris.ui.LibrisUi;
import org.lasalledebain.libris.xmlUtils.ElementManager;
import org.lasalledebain.libris.xmlUtils.ElementWriter;
import org.lasalledebain.libris.xmlUtils.LibrisAttributes;
import org.lasalledebain.libris.xmlUtils.LibrisXMLConstants;

public class ArtifactDatabase extends GenericDatabase<ArtifactRecord> implements LibrisXMLConstants {

	public ArtifactDatabase(LibrisUi theUi, LibrisFileManager theFileManager) {
		super(theUi, theFileManager);
	}
	@Override
	public String getElementTag() {
		return XML_ARTIFACTS_TAG;
	}

	@Override
	public void fromXml(ElementManager mgr) throws LibrisException {
		// TODO Auto-generated method stub

	}

	@Override
	public void toXml(ElementWriter outWriter) throws LibrisException {
		LibrisAttributes recordsAttrs = new LibrisAttributes();
		Iterable<ArtifactRecord> recordSource = getRecordReader();
		outWriter.writeStartElement(getElementTag(), recordsAttrs, false);
		for (Record r: recordSource) {
			r.toXml(outWriter);
		}
		outWriter.writeEndElement(); /* records */
	}

	@Override
	public LibrisAttributes getAttributes() throws XmlException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Schema getSchema() {
		return Repository.mySchema;
	}

	@Override
	public LibrisJournalFileManager<ArtifactRecord> getJournalFileMgr() throws LibrisException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public GenericDatabaseMetadata getMetadata() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ArtifactRecord newRecord() throws InputException {
		return new ArtifactRecord(getSchema());
	}

	@Override
	public int putRecord(ArtifactRecord rec) throws LibrisException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public RecordFactory<ArtifactRecord> getRecordFactory() {
		// TODO Auto-generated method stub
		return null;
	}
	public boolean isRecordReadOnly(int recordId) {
		return false;
	}

}
