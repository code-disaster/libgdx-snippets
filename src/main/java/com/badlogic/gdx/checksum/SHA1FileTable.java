package com.badlogic.gdx.checksum;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.*;

import java.io.*;
import java.security.NoSuchAlgorithmException;

public class SHA1FileTable {

	public enum CheckFileResult {
		Unmodified, // file not changed
		Modified, // file content has been modified
		NoSHA1SumFound, // file not yet known (may be new)
		FileNotFound // file not found (may be deleted)
	}

	private static class Entry {
		String filePath;
		SHA1 sha1;

		Entry(String filePath, SHA1 sha1) {
			this.filePath = filePath;
			this.sha1 = sha1;
		}
	}

	private final ObjectMap<String, Entry> entries = new ObjectMap<>();

	public SHA1FileTable() throws IOException {
		this(null);
	}

	public int size() {
		return entries.size;
	}

	/**
	 * Initializes the file/hash table from a .sha1sum text file.
	 */
	public SHA1FileTable(FileHandle sha1sumFile) throws IOException {

		if (sha1sumFile == null || !sha1sumFile.exists()) {
			return;
		}

		TextFileLineReader.readLines(
				sha1sumFile,
				new String[] { "^[0-9a-fA-F]+\\s+[\\S]+$" },
				line -> {

					String[] split = line.split(" ");

					String path = split[split.length - 1];
					String digest = split[0];

					entries.put(path, new Entry(path, SHA1.valueOf(digest)));

				});
	}

	/**
	 * Saves the file/hash table as .sha1sum text file. The output format is compatible to the *NIX 'sha1sum'
	 * command line tool.
	 */
	public void save(FileHandle sha1sumFile) throws IOException {

		BufferedWriter writer = new BufferedWriter(new FileWriter(sha1sumFile.file()));

		Array<Entry> sortedValues = entries.values().toArray();
		sortedValues.sort((value1, value2) -> value1.filePath.compareTo(value2.filePath));

		for (Entry entry : sortedValues) {

			writer.write(entry.sha1.toString());
			writer.write("  ");
			writer.write(entry.filePath);
			writer.write("\n");

		}

		writer.flush();
		writer.close();
	}

	/**
	 * Checks the SHA1 hash of a file against the SHA1 stored in the table.
	 */
	public CheckFileResult checkFile(File file) throws NoSuchAlgorithmException {

		if (!isKnownFile(file)) {
			return CheckFileResult.NoSHA1SumFound;
		}

		try {

			Entry entry = entries.get(file.getPath());

			SHA1 sha1 = processFile(file);

			if (!sha1.equals(entry.sha1)) {
				return CheckFileResult.Modified;
			}

		} catch (IOException e) {
			return CheckFileResult.FileNotFound;
		}

		return CheckFileResult.Unmodified;
	}

	/**
	 * Adds a file and its hash to the SHA1 table.
	 */
	public void registerFile(File file) throws IOException, NoSuchAlgorithmException {

		SHA1 sha1 = processFile(file);

		if (isKnownFile(file)) {
			Entry entry = entries.get(file.getPath());
			entry.sha1 = sha1;
		} else {
			Entry entry = new Entry(file.getPath(), sha1);
			entries.put(file.getPath(), entry);
		}
	}

	/**
	 * Removes a table entry.
	 */
	public void unregisterFile(File file) {
		if (isKnownFile(file)) {
			entries.remove(file.getPath());
		}
	}

	private boolean isKnownFile(File file) {
		return entries.containsKey(file.getPath());
	}

	private SHA1 processFile(File file) throws IOException, NoSuchAlgorithmException {

		BufferedInputStream input = new BufferedInputStream(new FileInputStream(file));

		SHA1 sha1 = SHA1.create();

		int n = 0;
		byte[] buffer = new byte[8192];
		while (n != -1) {
			n = input.read(buffer);
			if (n > 0) {
				SHA1.update(sha1, buffer, 0, n);
			}
		}

		SHA1.submit(sha1);

		return sha1;
	}

}
