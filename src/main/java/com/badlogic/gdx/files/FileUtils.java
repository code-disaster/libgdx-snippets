package com.badlogic.gdx.files;

import com.badlogic.gdx.Files.FileType;
import com.badlogic.gdx.utils.Host;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Comparator;

import static com.badlogic.gdx.utils.Host.OS.Linux;

public final class FileUtils {

	/**
	 * Default comparator to sort {@link FileHandle}s:
	 * - directories first
	 * - then by name, case insensitive
	 */
	public static final Comparator<FileHandle> DEFAULT_COMPARATOR = (file1, file2) -> {

		boolean d1 = file1.isDirectory();
		boolean d2 = file2.isDirectory();

		if (d1 != d2) {
			return d1 ? -1 : 1;
		}
		
		return file1.name().compareToIgnoreCase(file2.name());
	};

	/**
	 * Wrapper to {@link FileHandle#list()} which also sorts the result.
	 */
	public static FileHandle[] list(FileHandle folder) {

		FileHandle[] files = folder.list();
		Arrays.sort(files, DEFAULT_COMPARATOR);

		return files;
	}

	/**
	 * Wrapper to {@link FileHandle#list(String)} which also sorts the result.
	 */
	public static FileHandle[] list(FileHandle folder, String suffix) {

		FileHandle[] files = folder.list(suffix);
		Arrays.sort(files, DEFAULT_COMPARATOR);

		return files;
	}

	/**
	 * Queries (and creates, if necessary) a path to store user files.
	 * <p>
	 * Linux:
	 * - $XDG_DATA_HOME
	 * - user.home/.local/share
	 * - $HOME/.local/share
	 * MacOS:
	 * - $XDG_DATA_HOME
	 * - user.home/Library/Application Support
	 * - $HOME/Library/Application Support
	 * Windows:
	 * - %LOCALAPPDATA%
	 * - %APPDATA%
	 */
	public static FileHandle getUserFolder(String companyIdentifier,
										   String... productFolders) throws IOException {

		Path path = null;

		switch (Host.os) {

			case Linux: {

				// $XDG_DATA_HOME

				String dataDir = System.getenv("XDG_DATA_HOME");

				if (dataDir != null && !dataDir.isEmpty()) {

					path = Paths.get(dataDir);

					if (path.toFile().isDirectory()) {
						break;
					}

					path = null;
				}

				// [user.home | $HOME]/.local/share

				String homeDir = System.getProperty("user.home");

				if (homeDir == null || homeDir.isEmpty()) {
					homeDir = System.getenv("HOME");
				}

				if (homeDir != null && !homeDir.isEmpty()) {

					path = Paths.get(homeDir, ".local", "share");

					if (path.toFile().isDirectory()) {
						break;
					}

					path = null;
				}

				break;
			}

			case MacOS: {

				// $XDG_DATA_HOME

				String dataDir = System.getenv("XDG_DATA_HOME");

				if (dataDir != null && !dataDir.isEmpty()) {

					path = Paths.get(dataDir);

					if (path.toFile().isDirectory()) {
						break;
					}

					path = null;
				}

				// [user.home | $HOME]/Library/"Application Support"

				String homeDir = System.getProperty("user.home");

				if (homeDir == null || homeDir.isEmpty()) {
					homeDir = System.getenv("HOME");
				}

				if (homeDir != null && !homeDir.isEmpty()) {

					path = Paths.get(homeDir, "Library", "Application Support");

					if (path.toFile().isDirectory()) {
						break;
					}

					path = null;
				}

				break;
			}

			case Windows:

				// [%LOCALAPPDATA% | %APPDATA%]

				String appData = System.getenv("LOCALAPPDATA");

				if (appData == null || appData.isEmpty()) {
					appData = System.getenv("APPDATA");
				}

				if (appData != null && !appData.isEmpty()) {

					path = Paths.get(appData);

					if (path.toFile().isDirectory()) {
						break;
					}

					path = null;
				}

				break;
		}

		if (path == null) {
			throw new IOException("no valid user data folder found");
		}

		// company sub-folder

		path = path.resolve(Host.os == Linux ? companyIdentifier.toLowerCase() : companyIdentifier);

		// append remaining user-defined folders

		for (String folder : productFolders) {
			path = path.resolve(folder);
		}

		// create if necessary

		FileHandle fileHandle = newFileHandle(path.toFile(), FileType.Absolute);

		if (!fileHandle.exists()) {
			fileHandle.mkdirs();
		}

		return fileHandle;
	}

	public static FileHandle newFileHandle(File file, FileType type) {
		return new FileHandleHelper(file, type);
	}

	/**
	 * Normalizes the path of a file handle.
	 * <p>
	 * This does some hoops to work around some restrictions of the {@link FileHandle} class.
	 */
	public static FileHandle normalize(FileHandle file) {
		return new FileHandleHelper(file).normalize();
	}

	private static class FileHandleHelper extends FileHandle {

		FileHandleHelper(FileHandle file) {
			super(file.file(), file.type());
		}

		FileHandleHelper(File file, FileType type) {
			super(file, type);
		}

		FileHandleHelper normalize() {
			file = file.toPath().normalize().toFile();
			return this;
		}
	}

}
