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
import java.util.logging.Level;

import org.lasalledebain.libris.exception.DatabaseError;
import org.lasalledebain.libris.exception.DatabaseException;
import org.lasalledebain.libris.exception.InputException;
import org.lasalledebain.libris.exception.LibrisException;
import org.lasalledebain.libris.exception.UserErrorException;
import org.lasalledebain.libris.field.FieldValue;
import org.lasalledebain.libris.indexes.LibrisDatabaseConfiguration;
import org.lasalledebain.libris.ui.DatabaseUi;
import org.lasalledebain.libris.xmlUtils.ElementManager;
import org.lasalledebain.libris.xmlUtils.ElementWriter;

public class ArtifactManager implements LibrisConstants {
	private static final String LEVEL_PREFIX = "_L";
	private static final String DIRECTORY_PREFIX = "D_";
	private static final String ARTIFACT_PREFIX = "A_";
	static final int FANOUT = 100;
	private final FileManager myFileMgr;
	private final File artifactDirectory;
	private final File repositoryDirectory;
	public File getRepositoryDirectory() {
		return repositoryDirectory;
	}

	public File getArtifactDatabaseDirectory() {
		return artifactDirectory;
	}

	private final Path artifactDirectoryPath;
	private ArtifactDatabase myDb;
	private ReservationManager reservationMgr;

	public ArtifactManager(DatabaseUi<ArtifactRecord> theUi, File theArtifactDirectory, FileManager theFileMgr, boolean readOnly) throws DatabaseException {
		artifactDirectory = theArtifactDirectory;
		repositoryDirectory = new File(artifactDirectory, ARTIFACTS_REPOSITORY_DIRECTORY);
		artifactDirectoryPath = artifactDirectory.toPath();
		myFileMgr = theFileMgr;
		myDb = new ArtifactDatabase(theUi, theFileMgr, readOnly);
	}

	public boolean initialize() throws LibrisException {
		boolean result = true;
		if (!artifactDirectory.exists() && !artifactDirectory.mkdir()) {
			LibrisDatabase.log(Level.SEVERE, "Cannot create " + artifactDirectory.getAbsolutePath());
			result = false;
		}
		if (!repositoryDirectory.exists() && !repositoryDirectory.mkdir()) {
			LibrisDatabase.log(Level.SEVERE, "Cannot create " + repositoryDirectory.getAbsolutePath());
			result = false;
		}
		myDb.initialize();
		return result;
	}

	public void open() throws LibrisException {
		reservationMgr = new ReservationManager(
				myFileMgr.makeAuxiliaryFileAccessManager(LibrisConstants.LOCK_FILENAME));
		reservationMgr.reserveDatabase();
		myDb.openDatabase();
	}

	public boolean isDatabaseReserved() {
		return Objects.nonNull(reservationMgr) && reservationMgr.isDatabaseReserved();
	}
	
	public void close(boolean force) throws DatabaseException {
		if (isDatabaseReserved()) {
			reservationMgr.freeDatabase();
		}
		myDb.closeDatabase(force);
	}
	
	public synchronized int importFile(ArtifactParameters artifactParameters)
			throws LibrisException, IOException {
		int id = myDb.newRecordId();
		final URI sourceUri = artifactParameters.getSourcePath();
		File sourceFile = new File(sourceUri);
		if (!sourceFile.isFile()) {
			throw new UserErrorException(sourceUri.toString() + " is not a file");
		}
		String archivePath = copyFileToRepo(sourceFile, id);
		artifactParameters.setArchivepPath(archivePath);
		putArtifactInfo(artifactParameters, id);
		return id;
	}

	public int importFile(File sourceFile)
			throws LibrisException, IOException {
		if (!sourceFile.isFile()) {
			throw new UserErrorException(sourceFile.getAbsolutePath() + " is not a file");
		}
		ArtifactParameters params = new ArtifactParameters(sourceFile.toURI());
		return importFile(params);
	}

	private String copyFileToRepo(File original, int id) throws InputException, IOException {
		File dir = idToDirectoryPath(repositoryDirectory, id);
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
		originalName.replaceAll("\\s", "_");
		String nameWithId = ARTIFACT_PREFIX + Integer.toString(id) + "_" + originalName;
		Path destinationPath = Paths.get(dir.getAbsolutePath(), nameWithId);
		Files.copy(original.toPath(), destinationPath);
		return artifactDirectoryPath.relativize(destinationPath).toString();
	}

	private static File idToDirectoryPath(File root, int id) {
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

	public void putArtifactInfo(ArtifactParameters artifactParameters, int recId) throws LibrisException {
		ArtifactRecord rec = myDb.newRecord(artifactParameters);
		rec.setRecordId(recId);
		myDb.putRecord(rec);
	}
	
	public int putArtifactInfo(ArtifactParameters params) throws LibrisException {
		ArtifactRecord rec = myDb.newRecord(params);
		return myDb.putRecord(rec);
	}
	
	public void updateArtifactInfo(int artifactId, ArtifactParameters params) throws LibrisException {
		ArtifactRecord artRecord = myDb.getRecord(artifactId);
		myDb.setMutableFields(params, artRecord);
		myDb.putRecord(artRecord);
	}
	
	public int putArtifactInfo(URI sourceLocation) throws LibrisException {
		return putArtifactInfo(new ArtifactParameters(sourceLocation));
	}

	public File getArtifactSourceFile(int artifactId) {
		try {
			Record record = myDb.getRecord(artifactId);
			final FieldValue sourceField = record.getFieldValue(ArtifactDatabase.ID_SOURCE);
			String uriString = sourceField.getMainValueAsString();
			File result = new File(new URI(uriString));
			return result;
		} catch (InputException | URISyntaxException e) {
			throw new DatabaseError("Error retrieving artifact " + artifactId, e);
		}
	}

	public File getArtifactArchiveFile(int artifactId) {
		try {
			Record record = myDb.getRecord(artifactId);
			final FieldValue archivePathField = record.getFieldValue(ArtifactDatabase.ID_ARCHIVEPATH);
			String relativePath = archivePathField.getMainValueAsString();
			File result = new File(artifactDirectory, relativePath);
			return result;
		} catch (InputException e) {
			throw new DatabaseError("Error retrieving artifact " + artifactId, e);
		}
	}

	public int getNumArtifacts() {
		return myDb.getNumRecords();
	}
	public ArtifactParameters getArtifactInfo(int artifactId) {
		return myDb.getArtifactInfo(artifactId);
	}

	public void fromXml(ElementManager artifactsMgr) throws LibrisException {
		myDb.fromXml(artifactsMgr);
	}

	public void toXml(ElementWriter outWriter) throws LibrisException {
		myDb.toXml(outWriter);
	}

	public void buildIndexes(LibrisDatabaseConfiguration config) throws LibrisException {
		myDb.buildIndexes(config);
	}

	public void save() {
		myDb.save();
	}
}
