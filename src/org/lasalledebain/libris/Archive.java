package org.lasalledebain.libris;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.lasalledebain.libris.exception.DatabaseError;

import static java.util.Objects.nonNull;

public class Archive {

	public static void addFilesToArchive(File archiveFile, Stream<Path> fileList, File rootDir) throws IOException {
		Path rootPath = rootDir.toPath();
		try (TarArchiveOutputStream archiveOutputStream = getTarArchiveOutputStream(archiveFile)){
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

	private static TarArchiveOutputStream getTarArchiveOutputStream(File archiveFile) throws IOException {
		TarArchiveOutputStream archiveOutputStream = new TarArchiveOutputStream(new BufferedOutputStream(new FileOutputStream(archiveFile)));
		archiveOutputStream.setBigNumberMode(TarArchiveOutputStream.BIGNUMBER_STAR);
		archiveOutputStream.setLongFileMode(TarArchiveOutputStream.LONGFILE_GNU);
		return archiveOutputStream;
	}
}
