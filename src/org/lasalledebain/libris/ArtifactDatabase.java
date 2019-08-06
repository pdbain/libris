package org.lasalledebain.libris;

import static org.lasalledebain.libris.Field.FieldType.T_FIELD_LOCATION;
import static org.lasalledebain.libris.Field.FieldType.T_FIELD_STRING;

import org.lasalledebain.libris.exception.InputException;
import org.lasalledebain.libris.exception.LibrisException;
import org.lasalledebain.libris.exception.XmlException;
import org.lasalledebain.libris.index.GroupDef;
import org.lasalledebain.libris.index.GroupDefs;
import org.lasalledebain.libris.indexes.LibrisJournalFileManager;
import org.lasalledebain.libris.ui.LibrisUi;
import org.lasalledebain.libris.xmlUtils.ElementManager;
import org.lasalledebain.libris.xmlUtils.ElementWriter;
import org.lasalledebain.libris.xmlUtils.LibrisAttributes;
import org.lasalledebain.libris.xmlUtils.LibrisXMLConstants;

public class ArtifactDatabase extends GenericDatabase<ArtifactRecord> implements LibrisXMLConstants {

	static final DynamicSchema artifactsSchema = ArtifactDatabase.makeSchema();
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
		return ArtifactDatabase.artifactsSchema;
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
	public static Field newField(int fieldNum, int fieldData) throws InputException {
		return Repository.templateList[fieldNum].newField(fieldData);
	}
	public static Field newField(int fieldNum, String mainValue, String extraValue) throws InputException {
		return Repository.templateList[fieldNum].newField(mainValue, extraValue);
	}
	public static Field newField(int fieldNum, int mainValue, String extraValue) throws InputException {
		return Repository.templateList[fieldNum].newField(mainValue, extraValue);
	}
	public static Field newField(int fieldNum, String fieldData) throws InputException {
		return Repository.templateList[fieldNum].newField(fieldData);
	}
	public static Field newField(int fieldNum) {
		return Repository.templateList[fieldNum].newField();
	}
	static DynamicSchema makeSchema() {
		DynamicSchema theSchema = new DynamicSchema();
		GroupDef grp = new GroupDef(theSchema, Repository.ID_GROUPS, "", 0);
		Repository.GROUP_FIELD = theSchema.addField(grp);
		GroupDefs defs = theSchema.getGroupDefs();
		defs.addGroup(grp);
		Repository.TITLE_FIELD = theSchema.addField(new FieldTemplate(theSchema, Repository.ID_TITLE, "", T_FIELD_STRING));
		Repository.SOURCE_FIELD = theSchema.addField(new FieldTemplate(theSchema, Repository.ID_SOURCE, "", T_FIELD_LOCATION));
		Repository.DOI_FIELD = theSchema.addField(new FieldTemplate(theSchema, Repository.ID_DOI, "", T_FIELD_STRING));
		Repository.DATE_FIELD = theSchema.addField(new FieldTemplate(theSchema, Repository.ID_DATE, "", T_FIELD_STRING));
		Repository.KEYWORDS_FIELD = theSchema.addField(new FieldTemplate(theSchema, Repository.ID_KEYWORDS, "", T_FIELD_STRING));
		Repository.COMMENTS_FIELD = theSchema.addField(new FieldTemplate(theSchema, Repository.ID_COMMENTS, "", T_FIELD_STRING));
		int numFields = Repository.COMMENTS_FIELD + 1;
		Repository.templateList = new FieldTemplate[numFields];
		for (int i = 1; i < numFields; ++i) {
			Repository.templateList[i] = theSchema.getFieldTemplate(i);
		}
		return theSchema;
	}

}
