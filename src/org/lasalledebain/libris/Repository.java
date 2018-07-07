package org.lasalledebain.libris;

import static org.lasalledebain.libris.Field.FieldType.T_FIELD_LOCATION;
import static org.lasalledebain.libris.Field.FieldType.T_FIELD_STRING;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Objects;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLStreamException;

import org.lasalledebain.libris.exception.InputException;
import org.lasalledebain.libris.exception.LibrisException;
import org.lasalledebain.libris.field.FieldValue;
import org.lasalledebain.libris.index.GroupDef;
import org.lasalledebain.libris.index.GroupDefs;
import org.lasalledebain.libris.ui.HeadlessUi;
import org.lasalledebain.libris.ui.Layouts;
import static org.lasalledebain.libris.util.StringUtils.stringEquals;

public class Repository extends Libris {

private static final String ID_GROUPS = "ID_groups";

private static final String REPOSITORY = "Repository";

public static final String ID_DATE = "ID_date";
public static final String ID_DOI = "ID_doi";

public static final String ID_KEYWORDS = "ID_keywords";

private static final String ID_SOURCE = "ID_source";

private static final String ID_TITLE = "ID_title";
private static final String ID_COMMENTS = "ID_comments";

LibrisDatabase database;
static public int GROUP_FIELD;
static public  int TITLE_FIELD;
static public int SOURCE_FIELD;
static public int DOI_FIELD;
static public int DATE_FIELD;
static public int KEYWORDS_FIELD;
static public int COMMENTS_FIELD;

	private static final DynamicSchema mySchema = makeSchema();

	public Repository(LibrisDatabase db) {
		database = db;
	}

	private static DynamicSchema makeSchema() {
		DynamicSchema theSchema = new DynamicSchema();
		GroupDef grp = new GroupDef(theSchema, ID_GROUPS,"", 0);
		theSchema.addField(grp);
		GroupDefs defs = theSchema.getGroupDefs();
		defs.addGroup(grp);
		theSchema.addField(new FieldTemplate(theSchema, ID_TITLE, "", T_FIELD_STRING));
		theSchema.addField(new FieldTemplate(theSchema, ID_SOURCE, "", T_FIELD_LOCATION));
		theSchema.addField(new FieldTemplate(theSchema, ID_DOI, "", T_FIELD_STRING));
		theSchema.addField(new FieldTemplate(theSchema, ID_DATE, "", T_FIELD_STRING));
		theSchema.addField(new FieldTemplate(theSchema, ID_KEYWORDS, "", T_FIELD_STRING));
		theSchema.addField(new FieldTemplate(theSchema, ID_COMMENTS, "", T_FIELD_STRING));
		GROUP_FIELD = theSchema.getFieldNum(ID_GROUPS);
		TITLE_FIELD = theSchema.getFieldNum(ID_TITLE);
		SOURCE_FIELD = theSchema.getFieldNum(ID_SOURCE);
		DOI_FIELD = theSchema.getFieldNum(ID_DOI);
		DATE_FIELD = theSchema.getFieldNum(ID_DATE);
		KEYWORDS_FIELD = theSchema.getFieldNum(ID_KEYWORDS);
		COMMENTS_FIELD = theSchema.getFieldNum(ID_COMMENTS);
		return theSchema;
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

	public static boolean initialize(File databaseFile) throws LibrisException, XMLStreamException, IOException {
		HeadlessUi theUi = new HeadlessUi();
		LibrisDatabaseParameter params = new LibrisDatabaseParameter(theUi, databaseFile);
		params.databaseSchema = mySchema;
		params.schemaName = REPOSITORY;
		Layouts theLayouts = new Layouts(mySchema);
		MetadataHolder metadata = new MetadataHolder(mySchema, theLayouts);
		return LibrisDatabase.newDatabase(params, metadata);
	}

	public static Repository open(File databaseFile, boolean readOnly) throws FactoryConfigurationError, LibrisException {
		HeadlessUi ui = new HeadlessUi(databaseFile, readOnly);
		Repository result = new Repository(ui.openDatabase());
		return result;
	}

	public File getArtifact(int artifactId) throws InputException, URISyntaxException {
		final Record record = database.getRecord(artifactId);
		final FieldValue sourceField = record.getFieldValue(SOURCE_FIELD);
		String uriString = sourceField.getMainValueAsString();
		File result = new File(new URI(uriString));
		return result;
		
	}
	
	public ArtifactParameters getArtifactInfo(int artifactId) throws InputException, URISyntaxException {
		final Record record = database.getRecord(artifactId);
		String uriString = record.getFieldValue(SOURCE_FIELD).getMainValueAsString();
		ArtifactParameters result = new ArtifactParameters(new URI(uriString));
		result.date = record.getFieldValue(ID_DATE).getMainValueAsString();
		result.comments = record.getFieldValue(ID_COMMENTS).getMainValueAsString();
		result.doi = record.getFieldValue(ID_DOI).getMainValueAsString();
		result.keywords = record.getFieldValue(ID_KEYWORDS).getMainValueAsString();
		result.title = record.getFieldValue(ID_TITLE).getMainValueAsString();
		result.recordName = record.getName();
		if (record.hasAffiliations()) {
			result.recordParentName = database.getRecordName(record.getParent(0));
		}
		return result;
		
	}
	
	public int putArtifact(ArtifactParameters params) throws LibrisException {
		Record rec = database.newRecord();
		rec.addFieldValue(ID_SOURCE, params.getSourceString());
		rec.addFieldValue(ID_DATE, params.date);
		rec.addFieldValue(ID_DOI, params.doi);
		rec.addFieldValue(ID_KEYWORDS, params.keywords);
		rec.addFieldValue(ID_COMMENTS, params.comments);
		rec.addFieldValue(ID_TITLE, params.title);
		rec.addFieldValue(ID_SOURCE, params.source.toString());
		if (!params.recordName.isEmpty()) {
			rec.setName(params.recordName);
		}
		if (!params.recordParentName.isEmpty()) {
			Record parent = database.getRecord(params.recordParentName);
			if (Objects.isNull(parent)) {
				throw new InputException("Cannot locate record "+params.recordParentName);
			}
			rec.setParent(0, parent.getRecordId());
		}
		int id = database.put(rec);
		return id;	
	}
	
	public int putArtifact(URI location) throws LibrisException {
		return putArtifact(new ArtifactParameters(location));	
	}
	
	public static class ArtifactParameters {
		public ArtifactParameters(URI source) {
			super();
			this.source = source;
			recordName = "";
			recordParentName = "";
		}
		public String getSourceString() {
			return source.toASCIIString();
		}
		private URI source;
		public String date;
		public String title;
		public String comments;
		public String keywords;
		public String doi;
		public String recordParentName;
		public String recordName;
		/* (non-Javadoc)
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object obj) {
			if (Objects.nonNull(obj) && getClass().isAssignableFrom(obj.getClass())) {
				ArtifactParameters other = (ArtifactParameters) obj;
				return
						stringEquals(other.comments, comments)
						&& stringEquals(other.date, date)
						&& stringEquals(other.doi, doi)
						&& stringEquals(other.keywords, keywords)
						&& other.source.equals(source)
						&& stringEquals(other.recordName, recordName)
						&& stringEquals(other.recordParentName, recordParentName)
						&& stringEquals(other.title, title);
			} else return false;
		}
		/* (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			StringBuilder buff = new StringBuilder(100);
			buff.append("comments=\""); buff.append(comments); buff.append("\",");
			buff.append("date=\""); buff.append(date); buff.append("\",");
			buff.append("doi=\""); buff.append(doi); buff.append("\",");
			buff.append("keywords=\""); buff.append(keywords); buff.append("\",");
			buff.append("recordName=\""); buff.append(recordName); buff.append("\",");
			buff.append("recordParentName=\""); buff.append(recordParentName); buff.append("\",");
			buff.append("source=\""); buff.append(getSourceString()); buff.append("\",");
			buff.append("title=\""); buff.append(title); buff.append("\"");
			return buff.toString();
		}
	}
}
