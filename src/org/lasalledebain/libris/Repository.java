package org.lasalledebain.libris;

import static org.lasalledebain.libris.Field.FieldType.T_FIELD_LOCATION;
import static org.lasalledebain.libris.Field.FieldType.T_FIELD_STRING;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Objects;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLStreamException;

import org.lasalledebain.libris.exception.InputException;
import org.lasalledebain.libris.exception.LibrisException;
import org.lasalledebain.libris.exception.UserErrorException;
import org.lasalledebain.libris.field.FieldValue;
import org.lasalledebain.libris.index.GroupDef;
import org.lasalledebain.libris.index.GroupDefs;
import org.lasalledebain.libris.ui.HeadlessUi;
import org.lasalledebain.libris.ui.Layouts;
import static org.lasalledebain.libris.util.StringUtils.stringEquals;

public class Repository extends Libris {
// TODO 1 Add Repository field to LibrisDatabase and add repository field to record
private static final String LEVEL_PREFIX = "_L";

private static final String DIRECTORY_PREFIX = "D_";

private static final String ARTIFACT_PREFIX = "A_";

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
static final int FANOUT = 100;

	private static final DynamicSchema mySchema = makeSchema();
	File root;
	public Repository(LibrisDatabase db, File theRoot) {
		database = db;
		root = theRoot;
	}

	private static DynamicSchema makeSchema() {
		DynamicSchema theSchema = new DynamicSchema();
		GroupDef grp = new GroupDef(theSchema, ID_GROUPS,"", 0);
		GROUP_FIELD = theSchema.addField(grp);
		GroupDefs defs = theSchema.getGroupDefs();
		defs.addGroup(grp);
		TITLE_FIELD = theSchema.addField(new FieldTemplate(theSchema, ID_TITLE, "", T_FIELD_STRING));
		SOURCE_FIELD = theSchema.addField(new FieldTemplate(theSchema, ID_SOURCE, "", T_FIELD_LOCATION));
		DOI_FIELD = theSchema.addField(new FieldTemplate(theSchema, ID_DOI, "", T_FIELD_STRING));
		DATE_FIELD = theSchema.addField(new FieldTemplate(theSchema, ID_DATE, "", T_FIELD_STRING));
		KEYWORDS_FIELD = theSchema.addField(new FieldTemplate(theSchema, ID_KEYWORDS, "", T_FIELD_STRING));
		COMMENTS_FIELD = theSchema.addField(new FieldTemplate(theSchema, ID_COMMENTS, "", T_FIELD_STRING));
		return theSchema;
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

	public static boolean initialize(File databaseFile) throws LibrisException, XMLStreamException, IOException {
		HeadlessUi theUi = new HeadlessUi(databaseFile, false);
		LibrisDatabaseParameter params = new LibrisDatabaseParameter(theUi);
		params.setDatabaseSchema(mySchema);
		params.setSchemaName(REPOSITORY);
		Layouts theLayouts = new Layouts(mySchema);
		MetadataHolder metadata = new MetadataHolder(mySchema, theLayouts);
		return LibrisDatabase.newDatabase(params, metadata);
	}

	public static Repository open(File databaseFile, File theRoot, boolean readOnly) throws FactoryConfigurationError, LibrisException {
		HeadlessUi ui = new HeadlessUi(databaseFile, readOnly);
		Repository result = new Repository(ui.openDatabase(), theRoot);
		return result;
	}

	public File idToDirectoryPath(File root, int id) {
		int levels = 1;
		int count = id;
		while (count > FANOUT) {
			++levels;
			count /= FANOUT;
		}
		String dirNames[] = new String[levels];
		dirNames[0] = "r_"+levels;
		count = id;
		for (int l = levels - 1; l > 0; l--) {
			count /= FANOUT;
			dirNames[l] = DIRECTORY_PREFIX+count+LEVEL_PREFIX+l;
		}
		String path = String.join(File.separator, Arrays.asList(dirNames));
		return new File(root, path);
	}
	
	public int importFile(ArtifactParameters params) throws LibrisException, IOException {
		final URI sourceUri = params.source;
		File sourceFile = new File(sourceUri);
		if (!sourceFile.isFile()) {
			throw new UserErrorException(sourceUri.toString()+" is not a file");
		}
		int id = putArtifactInfo(params);
		copyFileToRepo(sourceFile, id);
		return id;
	}
	
	public URI copyFileToRepo(File original, int id) throws InputException, IOException {
		File dir = idToDirectoryPath(root, id);
		if (!dir.exists()) {
			if (!dir.mkdirs()) {
				throw new InputException("Cannot create directory "+dir.getAbsolutePath()+" to hold artifact "+id+" "+original.getName());
			};
		}
		if (!dir.isDirectory()) {
			throw new InputException(dir.getAbsolutePath()+" is not a directory");
		}
		String originalName = original.getName();
		String nameWithId = ARTIFACT_PREFIX+Integer.toString(id)+"_"+originalName;
		Path destinationPath = Paths.get(dir.getAbsolutePath(), nameWithId);
		Files.copy(original.toPath(), destinationPath);
		return destinationPath.toUri();
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
	
	public int putArtifactInfo(ArtifactParameters params) throws LibrisException {
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
	
	public int putArtifactInfo(URI sourceLocation) throws LibrisException {
		return putArtifactInfo(new ArtifactParameters(sourceLocation));	
	}
	
	public static class ArtifactParameters {
		public ArtifactParameters(URI source) {
			super();
			this.source = source;
			recordName = "";
			recordParentName = "";
			date = LibrisMetadata.getCurrentDateAndTimeString();
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
