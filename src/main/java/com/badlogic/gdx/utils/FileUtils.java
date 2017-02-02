package com.badlogic.gdx.utils;

import com.badlogic.gdx.Files;
import com.badlogic.gdx.checksum.SHA1;
import com.badlogic.gdx.files.FileHandle;

import java.io.*;

public final class FileUtils {

	private FileUtils() {

	}

	@FunctionalInterface
	public interface StreamConsumer {
		void read(byte[] bytes, int length) throws IOException;
	}

	/**
	 * Utility function to consume a buffered {@link InputStream}.
	 */
	public static void readStream(InputStream stream,
								  int bufferSize,
								  StreamConsumer consumer) throws IOException {

		try (BufferedInputStream bis = new BufferedInputStream(stream)) {

			int n = 0;
			byte[] buffer = new byte[bufferSize];
			while (n != -1) {
				n = bis.read(buffer);
				if (n > 0) {
					consumer.read(buffer, n);
				}
			}

		}

	}

	/**
	 * Calculates the SHA-1 hash on a {@link InputStream}.
	 */
	public static SHA1 hashStream(InputStream stream) throws IOException {

		SHA1 sha1 = SHA1.create();

		FileUtils.readStream(stream, 1024, (bytes, n) -> {
			SHA1.update(sha1, bytes, 0, n);
		});

		SHA1.submit(sha1);

		return sha1;
	}

	public static FileHandle newFileHandle(File file, Files.FileType type) {
		return new FileHandleHelper(file, type);
	}

	/**
	 * Normalizes the path of a file handle.
	 *
	 * This does some hoops to work around some restrictions of the {@link FileHandle} class.
	 */
	public static FileHandle normalize(FileHandle file) {
		return new FileHandleHelper(file).normalize();
	}

	private static class FileHandleHelper extends FileHandle {

		FileHandleHelper(FileHandle file) {
			super(file.file(), file.type());
		}

		FileHandleHelper(File file, Files.FileType type) {
			super(file, type);
		}

		FileHandleHelper normalize() {
			file = file.toPath().normalize().toFile();
			return this;
		}
	}

}
