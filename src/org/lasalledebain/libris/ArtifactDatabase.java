package org.lasalledebain.libris;

import static org.lasalledebain.libris.Field.FieldType.T_FIELD_LOCATION;
import static org.lasalledebain.libris.Field.FieldType.T_FIELD_STRING;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Objects;
import java.util.logging.Level;

import org.lasalledebain.libris.exception.DatabaseError;
import org.lasalledebain.libris.exception.DatabaseException;
import org.lasalledebain.libris.exception.InputException;
import org.lasalledebain.libris.exception.LibrisException;
import org.lasalledebain.libris.exception.XmlException;
import org.lasalledebain.libris.index.GroupDef;
import org.lasalledebain.libris.index.GroupDefs;
import org.lasalledebain.libris.indexes.LibrisRecordsFileManager;
import org.lasalledebain.libris.records.Records;
import org.lasalledebain.libris.ui.LibrisUi;
import org.lasalledebain.libris.xmlUtils.ElementManager;
import org.lasalledebain.libris.xmlUtils.ElementWriter;
import org.lasalledebain.libris.xmlUtils.LibrisAttributes;
import org.lasalledebain.libris.xmlUtils.LibrisXMLConstants;

public class ArtifactDatabase extends GenericDatabase<ArtifactRecord> implements LibrisXMLConstants, RecordFactory<ArtifactRecord> {

	static final DynamicSchema artifactsSchema = ArtifactDatabase.makeSchema();
	private final DatabaseMetadata myMetadata;
	public ArtifactDatabase(LibrisUi theUi, LibrisFileManager theFileManager) throws DatabaseException {
		super(theUi, theFileManager);
		myMetadata = new DatabaseMetadata();
	}
	
	public ArtifactDatabase(LibrisUi theUi, File workingDirectory) throws DatabaseException {
		this(theUi, new LibrisFileManager(new File(workingDirectory, LibrisConstants.REPOSITORY_AUX_DIRECTORY_NAME)));
	}
	@Override
	public String getElementTag() {
		return XML_ARTIFACTS_TAG;
	}

	@Override
	public void fromXml(ElementManager mgr) throws LibrisException {
		mgr.parseOpenTag();
		String nextId = mgr.getNextId();
		if (LibrisXMLConstants.XML_RECORDS_TAG == nextId) {
			Records<ArtifactRecord> recs = makeDatabaseRecords();
			ElementManager recsMgr = mgr.nextElement();
			recs.fromXml(recsMgr);
		}
	}

	@Override
	public void toXml(ElementWriter outWriter) throws LibrisException {
		LibrisAttributes recordsAttrs = new LibrisAttributes();
		Iterable<ArtifactRecord> recordSource = getRecordReader();
		outWriter.writeStartElement(getElementTag(), recordsAttrs, false);
		databaseRecords.toXml(outWriter);
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
	public DatabaseMetadata getMetadata() {
		return myMetadata;
	}

	@Override
	public ArtifactRecord newRecord() {
		return new ArtifactRecord(getSchema());
	}
	
	public ArtifactRecord newRecord(ArtifactParameters artifactParameters) throws LibrisException {
		ArtifactRecord rec = newRecord();
		rec.addFieldValue(Repository.ID_SOURCE, artifactParameters.getSourceString());
		rec.addFieldValue(Repository.ID_DATE, artifactParameters.date);
		rec.addFieldValue(Repository.ID_DOI, artifactParameters.doi);
		rec.addFieldValue(Repository.ID_KEYWORDS, artifactParameters.keywords);
		rec.addFieldValue(Repository.ID_COMMENTS, artifactParameters.comments);
		String title = artifactParameters.title;
		if (!artifactParameters.recordName.isEmpty()) {
			rec.setName(artifactParameters.recordName);
		}
		if (Objects.isNull(title) || title.isEmpty()) {
			File sourceFile = new File(artifactParameters.source);
			title = sourceFile.getName();
		}
		rec.addFieldValue(Repository.ID_TITLE, title);
		int parentId = artifactParameters.parentId;
		if (parentId != RecordId.NULL_RECORD_ID) {
			Record parent = getRecord(parentId);
			if (Objects.isNull(parent)) {
				throw new InputException("Cannot locate record " + parentId);
			}
			rec.setParent(0, parent.getRecordId());
		} else if (!artifactParameters.recordParentName.isEmpty()) {
			Record parent = getRecord(artifactParameters.recordParentName);
			if (Objects.isNull(parent)) {
				throw new InputException("Cannot locate record " + artifactParameters.recordParentName);
			}
			rec.setParent(0, parent.getRecordId());
		}
		return rec;
	}

	@Override
	public int putRecord(ArtifactRecord rec) throws LibrisException {
		int id = genericPutRecord(myMetadata, rec);
		int[] affiliations = rec.getAffiliates(0);
		if (affiliations.length != 0) {
			if (affiliations[0] != LibrisConstants.NULL_RECORD_ID) {
				indexMgr.addChild(0, affiliations[0], id);
			}
			for (int i = 1; i < affiliations.length; ++i) {
				indexMgr.addAffiliate(0, affiliations[i], id);
			}
		}
		LibrisDatabase.log(Level.FINE, "ArtifactDatabase.put "+rec.getRecordId()); //$NON-NLS-1$
		return id;
	}

	public ArtifactParameters getArtifactInfo(int artifactId) {
		try {
			final ArtifactRecord record = getRecord(artifactId);
			String uriString = record.getFieldValue(Repository.SOURCE_FIELD).getMainValueAsString();
			ArtifactParameters result = new ArtifactParameters(new URI(uriString));
			result.date = record.getFieldValue(Repository.ID_DATE).getMainValueAsString();
			result.comments = record.getFieldValue(Repository.ID_COMMENTS).getMainValueAsString();
			result.doi = record.getFieldValue(Repository.ID_DOI).getMainValueAsString();
			result.keywords = record.getFieldValue(Repository.ID_KEYWORDS).getMainValueAsString();
			result.title = record.getFieldValue(Repository.ID_TITLE).getMainValueAsString();
			result.recordName = record.getName();
			if (record.hasAffiliations()) {
				int parent = record.getParent(0);
				result.recordParentName = getRecordName(parent);
				result.setParentId(parent);
			}
			return result;
		} catch (InputException | URISyntaxException e) {
			throw new DatabaseError("Error retrieving artifact " + artifactId, e);
		}

	}

	@Override
	public RecordFactory<ArtifactRecord> getRecordFactory() {
		return this;
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
	@Override
	public ArtifactRecord makeRecord(boolean editable) {
		ArtifactRecord rec = newRecord();
		rec.setEditable(editable);
		return rec;
	}

}
