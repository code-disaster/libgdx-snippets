package com.badlogic.gdx.files;

import com.badlogic.gdx.function.ThrowableConsumer;
import com.badlogic.gdx.utils.*;
import com.badlogic.gdx.utils.StringBuilder;

import java.io.*;
import java.util.regex.Pattern;

public class TextFileUtils {

	/**
	 * Reads the file as text, passing its content to the consumer function, line by line.
	 */
	public static void readLines(FileHandle file,
								 ThrowableConsumer<String, IOException> consumer) throws IOException {

		readLines(file, null, consumer);
	}

	/**
	 * Reads the file as text, filtering each line by a user-defined set of RegEx patterns.
	 * The line is passed to the consumer function only if (at least) one of the patterns matches.
	 */
	public static void readLines(FileHandle file, String[] patterns,
								 ThrowableConsumer<String, IOException> consumer) throws IOException {

		Pattern[] p = null;
		int lineNo = 0;

		if (!ArrayUtils.isNullOrEmpty(patterns)) {
			p = new Pattern[patterns.length];
			for (int i = 0; i < patterns.length; i++) {
				p[i] = Pattern.compile(patterns[i]);
			}
		}

		try (BufferedReader reader = new BufferedReader(file.reader())) {

			String line;
			while ((line = reader.readLine()) != null) {

				lineNo++;

				if (p == null) {
					consumer.accept(line);
				} else {
					for (int i = 0; i < patterns.length; i++) {
						if (p[i].matcher(line).matches()) {
							consumer.accept(line);
							break;
						}
					}
				}
			}

		} catch (IOException e) {
			throw new IOException("Error reading " + file.path() + " at line #" + lineNo, e);
		}
	}

	public static String readString(FileHandle file) throws IOException {

		StringBuilder builder = new StringBuilder((int) file.length());

		readLines(file, line -> {

			if (builder.length() > 0) {
				builder.append('\n');
			}

			builder.append(line);
		});

		return builder.toString();
	}

	public static void writeString(FileHandle file, String text) throws IOException {

		StringBuilder builder = new StringBuilder(text.length());
		String newLine = Host.os != Host.OS.Windows ? "\n" : "\r\n";

		try (BufferedReader reader = new BufferedReader(new StringReader(text))) {

			String line;
			while ((line = reader.readLine()) != null) {

				if (builder.length() > 0) {
					builder.append(newLine);
				}

				builder.append(line);
			}
		}

		file.writeString(builder.toString(), false);
	}

}
