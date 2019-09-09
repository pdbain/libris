package org.lasalledebain.libris;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.logging.Level;

import org.lasalledebain.libris.exception.DatabaseError;
import org.lasalledebain.libris.exception.DatabaseException;
import org.lasalledebain.libris.exception.InputException;
import org.lasalledebain.libris.exception.LibrisException;
import org.lasalledebain.libris.exception.UserErrorException;
import org.lasalledebain.libris.field.FieldValue;
import org.lasalledebain.libris.ui.Dialogue;
import org.lasalledebain.libris.ui.LibrisUi;
import org.lasalledebain.libris.xmlUtils.ElementManager;

public class ArtifactManager {
	private static final String LEVEL_PREFIX = "_L";
	private static final String DIRECTORY_PREFIX = "D_";
	private static final String ARTIFACT_PREFIX = "A_";
	static final int FANOUT = 100;
	private final FileManager myFileMgr;
	private final File artifactDirectory;
	private final LibrisUi myUi;
	private ArtifactDatabase myDb;
	private ReservationManager reservationMgr;

	public ArtifactManager(LibrisUi theUi, File artifactDirectory, FileManager theFileMgr) throws DatabaseException {
		this.artifactDirectory = artifactDirectory;
		myFileMgr = theFileMgr;
		myUi = theUi;
		myDb = new ArtifactDatabase(theUi, theFileMgr);
	}

	public boolean initialize(boolean force) throws LibrisException {
		boolean result = true;
		if (artifactDirectory.exists()) {
			if (force || (Dialogue.YES_OPTION == myUi
					.confirm("Delete directory " + artifactDirectory.getAbsolutePath()))) {
				artifactDirectory.delete();
			} else {
				result= false;
			}
		}
		if (!artifactDirectory.mkdir()) {
			LibrisDatabase.log(Level.SEVERE, "Cannot create " + artifactDirectory.getAbsolutePath());
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

	public void close(boolean force) throws DatabaseException {
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
		URI archivePath = copyFileToRepo(sourceFile, id);
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

	// TODO make this private
	private URI copyFileToRepo(File original, int id) throws InputException, IOException {
		File dir = idToDirectoryPath(artifactDirectory, id);
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
	
	public int putArtifactInfo(ArtifactParameters artifactParameters) throws LibrisException {
		ArtifactRecord rec = myDb.newRecord(artifactParameters);
		return myDb.putRecord(rec);
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
			String uriString = archivePathField.getMainValueAsString();
			File result = new File(new URI(uriString));
			return result;
		} catch (InputException | URISyntaxException e) {
			throw new DatabaseError("Error retrieving artifact " + artifactId, e);
		}
	}

	public ArtifactParameters getArtifactInfo(int artifactId) {
		return myDb.getArtifactInfo(artifactId);
	}

	public void fromXml(ElementManager artifactsMgr) throws LibrisException {
		myDb.fromXml(artifactsMgr);
	}

}
