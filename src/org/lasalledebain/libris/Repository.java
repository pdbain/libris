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

import org.lasalledebain.libris.exception.DatabaseException;
import org.lasalledebain.libris.exception.InputException;
import org.lasalledebain.libris.exception.InternalError;
import org.lasalledebain.libris.exception.LibrisException;
import org.lasalledebain.libris.exception.UserErrorException;
import org.lasalledebain.libris.field.FieldValue;
import org.lasalledebain.libris.index.GroupDef;
import org.lasalledebain.libris.index.GroupDefs;
import org.lasalledebain.libris.ui.ChildUi;
import org.lasalledebain.libris.ui.HeadlessUi;
import org.lasalledebain.libris.ui.Layouts;
import org.lasalledebain.libris.ui.LibrisUi;

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

LibrisDatabase repoDatabase;
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

	public Repository(ChildUi ui, LibrisDatabase db, File theRoot) {
		repoDatabase = db;
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

	/**
	 * Create a database file in the repository directory
	 * @param repoRoot repository directory
	 * @return database file on success, null on failure
	 * @throws LibrisException
	 * @throws XMLStreamException
	 * @throws IOException
	 */
	public static File initialize(File repoRoot) throws LibrisException, XMLStreamException, IOException {
		File databaseFile = getDatabaseFileFromRoot(repoRoot);
		HeadlessUi theUi = new HeadlessUi(databaseFile, false);
		Layouts theLayouts = new Layouts(mySchema);
		MetadataHolder metadata = new MetadataHolder(mySchema, theLayouts);
		boolean success = LibrisDatabase.newDatabase(databaseFile, REPOSITORY, false, theUi, metadata);
		return success? databaseFile: null;
	}
	
	public static File getDatabaseFileFromRoot(File repoRoot) throws DatabaseException {
		if (!repoRoot.exists() || !repoRoot.isDirectory() || !repoRoot.canWrite()) {
			throw new DatabaseException("Cannot access repository directory "+repoRoot.getPath());
		}
		File databaseFile = new File(repoRoot, LibrisConstants.REPO_DB);
		return databaseFile;
	}

	public static Repository open(LibrisUi parent, File repoRoot) throws FactoryConfigurationError, LibrisException {
		ChildUi myUi = new ChildUi(getDatabaseFileFromRoot(repoRoot), parent.isDatabaseReadOnly(), parent);
		Repository result = new Repository(myUi, myUi.openDatabase(), repoRoot);
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

	public File getArtifactFile(int artifactId) {
		try {
			Record record = repoDatabase.getRecord(artifactId);
			final FieldValue sourceField = record.getFieldValue(SOURCE_FIELD);
			String uriString = sourceField.getMainValueAsString();
			File result = new File(new URI(uriString));
			return result;	
		} catch (InputException | URISyntaxException e) {
			throw new InternalError("Error retrieving artifact " + artifactId, e);
		}
	}
	
	public ArtifactParameters getArtifactInfo(int artifactId) {
		try {
			final Record record = repoDatabase.getRecord(artifactId);
			String uriString = record.getFieldValue(SOURCE_FIELD).getMainValueAsString();
			ArtifactParameters result = new ArtifactParameters(new URI(uriString));
			result.date = record.getFieldValue(ID_DATE).getMainValueAsString();
			result.comments = record.getFieldValue(ID_COMMENTS).getMainValueAsString();
			result.doi = record.getFieldValue(ID_DOI).getMainValueAsString();
			result.keywords = record.getFieldValue(ID_KEYWORDS).getMainValueAsString();
			result.title = record.getFieldValue(ID_TITLE).getMainValueAsString();
			result.recordName = record.getName();
			if (record.hasAffiliations()) {
				result.recordParentName = repoDatabase.getRecordName(record.getParent(0));
			}
			return result;
		} catch (InputException | URISyntaxException e) {
			throw new InternalError("Error retrieving artifact " + artifactId, e);
		}

	}
	
	public int putArtifactInfo(ArtifactParameters params) throws LibrisException {
		Record rec = repoDatabase.newRecord();
		rec.addFieldValue(ID_SOURCE, params.getSourceString());
		rec.addFieldValue(ID_DATE, params.date);
		rec.addFieldValue(ID_DOI, params.doi);
		rec.addFieldValue(ID_KEYWORDS, params.keywords);
		rec.addFieldValue(ID_COMMENTS, params.comments);
		String title = params.title;
		rec.addFieldValue(ID_SOURCE, params.source.toString());
		if (!params.recordName.isEmpty()) {
			rec.setName(params.recordName);
		}
		if (Objects.isNull(title) || title.isEmpty()) {
			File sourceFile = new File(params.source);
			title = sourceFile.getName();
		}
		rec.addFieldValue(ID_TITLE, title);
		if (!params.recordParentName.isEmpty()) {
			Record parent = repoDatabase.getRecord(params.recordParentName);
			if (Objects.isNull(parent)) {
				throw new InputException("Cannot locate record "+params.recordParentName);
			}
			rec.setParent(0, parent.getRecordId());
		}
		int id = repoDatabase.put(rec);
		return id;	
	}
	
	public int putArtifactInfo(URI sourceLocation) throws LibrisException {
		return putArtifactInfo(new ArtifactParameters(sourceLocation));	
	}
}
