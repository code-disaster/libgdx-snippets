package com.badlogic.gdx.utils;

import com.badlogic.gdx.checksum.SHA1;

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

}
