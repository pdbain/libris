package org.lasalledebain.libris;

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

import org.lasalledebain.libris.exception.DatabaseError;
import org.lasalledebain.libris.exception.DatabaseException;
import org.lasalledebain.libris.exception.InputException;
import org.lasalledebain.libris.exception.LibrisException;
import org.lasalledebain.libris.exception.UserErrorException;
import org.lasalledebain.libris.field.FieldValue;
import org.lasalledebain.libris.ui.HeadlessUi;
import org.lasalledebain.libris.ui.Layouts;
import org.lasalledebain.libris.ui.DatabaseUi;

public class Repository extends Libris {
	private static final String LEVEL_PREFIX = "_L";

	private static final String DIRECTORY_PREFIX = "D_";

	private static final String ARTIFACT_PREFIX = "A_";

	private static final String REPOSITORY = "Repository";

	GenericDatabase<DatabaseRecord> repoDatabase;
	static final int FANOUT = 100;

	File root;

	static FieldTemplate[] templateList;

	public Repository(GenericDatabase<DatabaseRecord> db, File theRoot) {
		repoDatabase = db;
		root = theRoot;
	}

	/**
	 * Create a database file in the repository directory
	 * 
	 * @param repoRoot repository directory
	 * @return database file on success, null on failure
	 * @throws LibrisException
	 * @throws XMLStreamException
	 * @throws IOException
	 */
	@Deprecated
	public static File initialize(File repoRoot) throws LibrisException, XMLStreamException, IOException {
		File databaseFile = getDatabaseFileFromRoot(repoRoot);
		HeadlessUi<ArtifactRecord> theUi = new HeadlessUi<ArtifactRecord>(databaseFile, false);
		Layouts<ArtifactRecord> theLayouts = new Layouts<ArtifactRecord>(ArtifactDatabase.artifactsSchema);
		MetadataHolder metadata = new MetadataHolder(ArtifactDatabase.artifactsSchema, theLayouts);
		boolean success = LibrisDatabase.newDatabase(databaseFile, REPOSITORY, false, theUi, metadata);
		return success ? databaseFile : null;
	}

	public static File getDatabaseFileFromRoot(File repoRoot) throws DatabaseException {
		if (!repoRoot.exists() || !repoRoot.isDirectory() || !repoRoot.canWrite()) {
			throw new DatabaseException("Cannot access repository directory " + repoRoot.getPath());
		}
		File databaseFile = new File(repoRoot, LibrisConstants.REPO_DB);
		return databaseFile;
	}

	public static Repository open(DatabaseUi parent, File repoRoot) throws FactoryConfigurationError, LibrisException {
		HeadlessUi myUi = new HeadlessUi(getDatabaseFileFromRoot(repoRoot), parent.isDatabaseReadOnly());
		GenericDatabase<DatabaseRecord> db = myUi.openDatabase();
		Repository result = new Repository(db, repoRoot);
		return result;
	}

	public static File idToDirectoryPath(File root, int id) {
		int levels = 1;
		int count = id;
		while (count > FANOUT) {
			++levels;
			count /= FANOUT;
		}
		String dirNames[] = new String[levels];
		dirNames[0] = "r_" + levels;
		count = id;
		for (int l = levels - 1; l > 0; l--) {
			count /= FANOUT;
			dirNames[l] = DIRECTORY_PREFIX + count + LEVEL_PREFIX + l;
		}
		String path = String.join(File.separator, Arrays.asList(dirNames));
		return new File(root, path);
	}

	public int importFile(org.lasalledebain.libris.ArtifactParameters artifactParameters)
			throws LibrisException, IOException {
		final URI sourceUri = artifactParameters.getSourcePath();
		File sourceFile = new File(sourceUri);
		if (!sourceFile.isFile()) {
			throw new UserErrorException(sourceUri.toString() + " is not a file");
		}
		int id = putArtifactInfo(artifactParameters);
		copyFileToRepo(sourceFile, id);
		return id;
	}

	public URI copyFileToRepo(File original, int id) throws InputException, IOException {
		File dir = idToDirectoryPath(root, id);
		if (!dir.exists()) {
			if (!dir.mkdirs()) {
				throw new InputException("Cannot create directory " + dir.getAbsolutePath() + " to hold artifact " + id
						+ " " + original.getName());
			}
			;
		}
		if (!dir.isDirectory()) {
			throw new InputException(dir.getAbsolutePath() + " is not a directory");
		}
		String originalName = original.getName();
		String nameWithId = ARTIFACT_PREFIX + Integer.toString(id) + "_" + originalName;
		Path destinationPath = Paths.get(dir.getAbsolutePath(), nameWithId);
		Files.copy(original.toPath(), destinationPath);
		return destinationPath.toUri();
	}

	public File getArtifactFile(int artifactId) {
		try {
			Record record = repoDatabase.getRecord(artifactId);
			final FieldValue sourceField = record.getFieldValue(ArtifactDatabase.SOURCE_FIELD);
			String uriString = sourceField.getMainValueAsString();
			File result = new File(new URI(uriString));
			return result;
		} catch (InputException | URISyntaxException e) {
			throw new DatabaseError("Error retrieving artifact " + artifactId, e);
		}
	}

	public ArtifactParameters getArtifactInfo(int artifactId) {
		try {
			final Record record = repoDatabase.getRecord(artifactId);
			String uriString = record.getFieldValue(ArtifactDatabase.SOURCE_FIELD).getMainValueAsString();
			ArtifactParameters result = new ArtifactParameters(new URI(uriString));
			result.setDate(record.getFieldValue(ArtifactDatabase.ID_DATE).getMainValueAsString());
			result.setComments(record.getFieldValue(ArtifactDatabase.ID_COMMENTS).getMainValueAsString());
			result.setDoi(record.getFieldValue(ArtifactDatabase.ID_DOI).getMainValueAsString());
			result.setKeywords(record.getFieldValue(ArtifactDatabase.ID_KEYWORDS).getMainValueAsString());
			result.setTitle(record.getFieldValue(ArtifactDatabase.ID_TITLE).getMainValueAsString());
			result.recordName = record.getName();
			if (record.hasAffiliations()) {
				result.setRecordParentName(repoDatabase.getRecordName(record.getParent(0)));
			}
			return result;
		} catch (InputException | URISyntaxException e) {
			throw new DatabaseError("Error retrieving artifact " + artifactId, e);
		}

	}

	public int putArtifactInfo(ArtifactParameters artifactParameters) throws LibrisException {
		repoDatabase.assertDatabaseWritable("put artifact information");
		DatabaseRecord rec = repoDatabase.newRecordUnchecked();
		rec.addFieldValue(ArtifactDatabase.ID_SOURCE, artifactParameters.getSourceString());
		rec.addFieldValue(ArtifactDatabase.ID_DATE, artifactParameters.getDate());
		rec.addFieldValue(ArtifactDatabase.ID_DOI, artifactParameters.getDoi());
		rec.addFieldValue(ArtifactDatabase.ID_KEYWORDS, artifactParameters.getKeywords());
		rec.addFieldValue(ArtifactDatabase.ID_COMMENTS, artifactParameters.getComments());
		String title = artifactParameters.getTitle();
		rec.addFieldValue(ArtifactDatabase.ID_SOURCE, artifactParameters.getSourceString());
		if (!artifactParameters.recordName.isEmpty()) {
			rec.setName(artifactParameters.recordName);
		}
		if (Objects.isNull(title) || title.isEmpty()) {
			File sourceFile = new File(artifactParameters.getSourcePath());
			title = sourceFile.getName();
		}
		rec.addFieldValue(ArtifactDatabase.ID_TITLE, title);
		String recordParentName = artifactParameters.getRecordParentName();
		if (!recordParentName.isEmpty()) {
			Record parent = repoDatabase.getRecord(recordParentName);
			if (Objects.isNull(parent)) {
				throw new InputException("Cannot locate record " + recordParentName);
			}
			rec.setParent(0, parent.getRecordId());
		}
		int id = repoDatabase.putRecord(rec);
		return id;
	}

	public int putArtifactInfo(URI sourceLocation) throws LibrisException {
		return putArtifactInfo(new ArtifactParameters(sourceLocation));
	}

}
