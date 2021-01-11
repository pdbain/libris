package org.lasalledebain.libris;

import static java.util.Objects.nonNull;

import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.lasalledebain.libris.exception.DatabaseError;

public class ArchiveWriter implements Closeable {

	private final TarArchiveOutputStream archiveOutputStream;

	public ArchiveWriter(File archiveFile) throws IOException {
		this(new FileOutputStream(archiveFile, false));
	}

	public ArchiveWriter(OutputStream archiveStream) throws IOException {
		archiveOutputStream = new TarArchiveOutputStream(new BufferedOutputStream(archiveStream));
		archiveOutputStream.setBigNumberMode(TarArchiveOutputStream.BIGNUMBER_STAR);
		archiveOutputStream.setLongFileMode(TarArchiveOutputStream.LONGFILE_GNU);
	}

	public void addFilesToArchive(Stream<Path> fileList, File rootDir) throws IOException {
		Path rootPath = rootDir.toPath();
		fileList.forEach(f -> {
			String entryPath = rootPath.relativize(f).toString();
			TarArchiveEntry tarEntry = new TarArchiveEntry(f.toFile(),entryPath);
			try {
				archiveOutputStream.putArchiveEntry(tarEntry);
				Files.copy(f, archiveOutputStream);
				archiveOutputStream.closeArchiveEntry();
			} catch (IOException e) {
				throw new DatabaseError(e);
			}
		});
	}
	
	public void addDirectoryToArchive(File sourceDir, File rootDir) throws IOException {
		Stream<Path> fileList = Files.walk(sourceDir.toPath()).filter(Files::isRegularFile);
		addFilesToArchive(fileList, rootDir);
	}
	
	public void addFileToArchive(File sourceFile, String entryPath) throws IOException {
		TarArchiveEntry tarEntry = new TarArchiveEntry(sourceFile, entryPath);
		archiveOutputStream.putArchiveEntry(tarEntry);
		Files.copy(sourceFile.toPath(), archiveOutputStream);
		archiveOutputStream.closeArchiveEntry();
	}

	@Override
	public void close() throws IOException {
		archiveOutputStream.close();
	}
	
	public static void getFilesFromArchive(File archiveFile, File rootDir) throws IOException {
		try (TarArchiveInputStream archiveInputStream = new TarArchiveInputStream(new FileInputStream(archiveFile))){
				TarArchiveEntry tarEntry;
				while (nonNull(tarEntry = archiveInputStream.getNextTarEntry())) {
					if (tarEntry.isDirectory()) continue;
					File tarContent = new File(rootDir, tarEntry.getName());
					File parentDirectory = tarContent.getParentFile();
					if (!parentDirectory.exists())
						parentDirectory.mkdirs();
					Files.copy(archiveInputStream, tarContent.toPath());
				}
		}
	}

}
