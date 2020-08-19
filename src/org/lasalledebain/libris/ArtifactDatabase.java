package org.lasalledebain.libris;

import static java.util.Objects.nonNull;
import static org.lasalledebain.libris.Field.FieldType.T_FIELD_LOCATION;
import static org.lasalledebain.libris.Field.FieldType.T_FIELD_STRING;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Objects;
import java.util.logging.Level;

import org.lasalledebain.libris.exception.DatabaseError;
import org.lasalledebain.libris.exception.DatabaseException;
import org.lasalledebain.libris.exception.FieldDataException;
import org.lasalledebain.libris.exception.InputException;
import org.lasalledebain.libris.exception.LibrisException;
import org.lasalledebain.libris.field.FieldValue;
import org.lasalledebain.libris.index.GroupDef;
import org.lasalledebain.libris.index.GroupDefs;
import org.lasalledebain.libris.records.Records;
import org.lasalledebain.libris.ui.DatabaseUi;
import org.lasalledebain.libris.xmlUtils.ElementManager;
import org.lasalledebain.libris.xmlUtils.ElementWriter;
import org.lasalledebain.libris.xmlUtils.LibrisAttributes;
import org.lasalledebain.libris.xmlUtils.LibrisXMLConstants;

public class ArtifactDatabase extends GenericDatabase<ArtifactRecord> implements LibrisXMLConstants, RecordFactory<ArtifactRecord> {

	static final DynamicSchema artifactsSchema = makeSchema();
	private final DatabaseMetadata myMetadata;
	
	/* field IDs */
	static final String ID_COMMENTS = "ID_comments";
	static final String ID_TITLE = "ID_title";
	static final String ID_KEYWORDS = "ID_keywords";
	static final String ID_DOI = "ID_doi";
	static final String ID_DATE = "ID_date";
	static final String ID_GROUPS = "ID_groups";
	static final String ID_SOURCE = "ID_source";
	static final String ID_ARCHIVEPATH = "ID_archivepath";
	
	/* field numbers */
	public static int COMMENTS_FIELD;
	public static int KEYWORDS_FIELD;
	public static int DATE_FIELD;
	public static int DOI_FIELD;
	public static int GROUP_FIELD;
	public static int SOURCE_FIELD;
	public static int TITLE_FIELD;
	public static int ARCHIVEPATH_FIELD;
	public static int numFields;
	public ArtifactDatabase(DatabaseUi<ArtifactRecord> theUi, FileManager theFileManager) throws DatabaseException {
		super(theUi, theFileManager);
		myMetadata = new DatabaseMetadata();
	}
	
	public ArtifactDatabase(DatabaseUi<ArtifactRecord> theUi, File workingDirectory) throws DatabaseException {
		this(theUi, new FileManager(new File(workingDirectory, LibrisConstants.REPOSITORY_AUX_DIRECTORY_NAME)));
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
			Records<ArtifactRecord> recs = getDatabaseRecordsUnchecked();
			ElementManager recsMgr = mgr.nextElement();
			recs.fromXml(recsMgr);
		}
	}

	@Override
	public void toXml(ElementWriter outWriter) throws LibrisException {
		LibrisAttributes recordsAttrs = new LibrisAttributes();
		outWriter.writeStartElement(getElementTag(), recordsAttrs, false);
		databaseRecords.toXml(outWriter);
		outWriter.writeEndElement(); /* records */
	}

	@Override
	public Schema getSchema() {
		return artifactsSchema;
	}

	@Override
	public DatabaseMetadata getMetadata() {
		return myMetadata;
	}

	@Override
	public ArtifactRecord newRecord() {
		assertDatabaseWritable("new artiact record");
		return newRecordUnchecked();
	}

	@Override
	public ArtifactRecord newRecordUnchecked() {
		return new ArtifactRecord(getSchema());
	}

	public ArtifactRecord newRecordUnchecked(ArtifactParameters artifactParameters) throws LibrisException {
		ArtifactRecord rec = newRecord();
		setRecordFields(artifactParameters, rec);
		return rec;
	}

	public ArtifactRecord newRecord(ArtifactParameters artifactParameters) throws LibrisException {
		assertDatabaseWritable("new artiact record");
		return newRecordUnchecked(artifactParameters);
	}

		protected void setRecordFields(ArtifactParameters artifactParameters, ArtifactRecord rec)
			throws InputException, FieldDataException, DatabaseException {
		rec.addFieldValue(ID_SOURCE, artifactParameters.getSourceString());
		rec.addFieldValue(ID_ARCHIVEPATH, artifactParameters.getArchivePathString());
		rec.addFieldValue(ID_DATE, artifactParameters.getDate());
		if (!artifactParameters.recordName.isEmpty()) {
			rec.setName(artifactParameters.recordName);
		}
		int parentId = artifactParameters.getParentId();
		if (parentId != RecordId.NULL_RECORD_ID) {
			Record parent = getRecord(parentId);
			if (Objects.isNull(parent)) {
				throw new InputException("Cannot locate record " + parentId);
			}
			rec.setParent(0, parent.getRecordId());
		} else {
			String recordParentName = artifactParameters.getRecordParentName();
			if (!recordParentName.isEmpty()) {
				Record parent = getRecord(recordParentName);
				if (Objects.isNull(parent)) {
					throw new InputException("Cannot locate record " + recordParentName);
				}
				rec.setParent(0, parent.getRecordId());
			}
		}
		setMutableFields(artifactParameters, rec);
	}

	protected void setMutableFields(ArtifactParameters artifactParameters, ArtifactRecord rec) throws InputException, DatabaseException {
		String title = artifactParameters.getTitle();
		if (Objects.isNull(title) || title.isEmpty()) {
			File sourceFile = new File(artifactParameters.getSourcePath());
			title = sourceFile.getName();
		}
		rec.setFieldValue(ID_TITLE, title);
		rec.setFieldValue(ID_DOI, artifactParameters.getDoi());
		rec.setFieldValue(ID_KEYWORDS, artifactParameters.getKeywords());
		rec.setFieldValue(ID_COMMENTS, artifactParameters.getComments());
	}

	@Override
	public int putRecord(ArtifactRecord rec) throws LibrisException {
		if (!isDatabaseOpen()) {
			throw new DatabaseException("putRecord: database closed");
		}
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
			if (Objects.isNull(record)) {
				throw new DatabaseError("Cannot find artifact record "+artifactId);
			}
			String uriString = record.getFieldValue(ID_SOURCE).getMainValueAsString();
			ArtifactParameters result = new ArtifactParameters(new URI(uriString));
			FieldValue fldValue = record.getFieldValue(ID_ARCHIVEPATH);
			if (nonNull(fldValue)) {
				String artifactPath = fldValue.getMainValueAsString();
				if (nonNull(artifactPath)) {
					result.setArchivepPath(new URI(artifactPath));
				}
			}
			result.setDate(record.getFieldValue(ID_DATE).getMainValueAsString());
			result.setComments(record.getFieldValue(ID_COMMENTS));
			result.setDoi(record.getFieldValue(ID_DOI));
			result.setKeywords(record.getFieldValue(ID_KEYWORDS));
			result.setTitle(record.getFieldValue(ID_TITLE).getMainValueAsString());
			result.recordName = record.getName();
			if (record.hasAffiliations()) {
				int parent = record.getParent(0);
				result.setRecordParentName(getRecordName(parent));
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
		GroupDef grp = new GroupDef(theSchema, ID_GROUPS, "", 0);
		GROUP_FIELD = theSchema.addField(grp);
		GroupDefs defs = theSchema.getGroupDefs();
		defs.addGroup(grp);
		TITLE_FIELD = theSchema.addField(new FieldTemplate(theSchema, ID_TITLE, "", T_FIELD_STRING));
		SOURCE_FIELD = theSchema.addField(new FieldTemplate(theSchema, ID_SOURCE, "", T_FIELD_LOCATION));
		ARCHIVEPATH_FIELD = theSchema.addField(new FieldTemplate(theSchema, ID_ARCHIVEPATH, "", T_FIELD_LOCATION));
		DOI_FIELD = theSchema.addField(new FieldTemplate(theSchema, ID_DOI, "", T_FIELD_STRING));
		DATE_FIELD = theSchema.addField(new FieldTemplate(theSchema, ID_DATE, "", T_FIELD_STRING));
		KEYWORDS_FIELD = theSchema.addField(new FieldTemplate(theSchema, ID_KEYWORDS, "", T_FIELD_STRING));
		COMMENTS_FIELD = theSchema.addField(new FieldTemplate(theSchema, ID_COMMENTS, "", T_FIELD_STRING));
		numFields = COMMENTS_FIELD + 1;
		Repository.templateList = new FieldTemplate[numFields];
		for (int i = 1; i < numFields; ++i) {
			Repository.templateList[i] = theSchema.getFieldTemplate(i);
		}
		theSchema.setIndexFields(LibrisXMLConstants.XML_INDEX_NAME_KEYWORDS, new int[] {TITLE_FIELD, KEYWORDS_FIELD});
		return theSchema;
	}
	// TODO add unchecked version
	@Override
	public ArtifactRecord makeRecord(boolean editable) {
		ArtifactRecord rec = newRecord();
		rec.setEditable(editable);
		return rec;
	}

	@Override
	public ArtifactRecord makeRecordUnchecked(boolean editable) {
		ArtifactRecord rec = newRecordUnchecked();
		rec.setEditable(editable);
		return rec;
	}
}
