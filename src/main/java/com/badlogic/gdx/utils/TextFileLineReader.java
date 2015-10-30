package com.badlogic.gdx.utils;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.function.ThrowableConsumer;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.regex.Pattern;

/**
 * Text file reader utility.
 */
public class TextFileLineReader {

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

}
