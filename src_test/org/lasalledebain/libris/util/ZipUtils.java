package org.lasalledebain.libris.util;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.CopyOption;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import org.lasalledebain.Utilities;

public class ZipUtils {
	static final Map<String, String> env;
	static {
		env = new HashMap<>(); 
		env.put("create", "true");
	}

	public static boolean unzipZipFile(URI zipFile, File destDir) {
		if (destDir.exists()) {
			Utilities.deleteRecursively(destDir);
		}
		if (!destDir.mkdirs()) {
			return false;
		}
		try (FileSystem zipFs = FileSystems.newFileSystem(zipFile, env)) {
			for (Path rootFs: zipFs.getRootDirectories()) {
				copyFiles(Files.list(rootFs), destDir);
			}
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	private static void copyFiles(Stream<Path> list, File destDir) {
		list.forEach(p -> {
			final File destFile = new File(destDir, p.getFileName().toString());
			try {
				if (Files.isDirectory(p, LinkOption.NOFOLLOW_LINKS)) {
					copyFiles(Files.list(p), destFile);
				} else {
					Files.copy(p, destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
				}
			} catch (IOException e) {
				throw new Error(e);
			}
		});
	}
}
