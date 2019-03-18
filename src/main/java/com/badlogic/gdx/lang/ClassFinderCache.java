package com.badlogic.gdx.lang;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.files.TextFileUtils;
import com.badlogic.gdx.function.Supplier;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;

import java.io.IOException;
import java.io.Writer;

public class ClassFinderCache {

	private final ObjectMap<String, Array<String>> classNames = new ObjectMap<>();

	public ClassFinderCache() {

	}

	public ClassFinderCache(FileHandle file) throws IOException {
		readFromFile(file);
	}

	public Array<String> get(String groupName) {
		return classNames.get(groupName);
	}

	public void add(String groupName, Supplier<Array<String>> supplier) {
		classNames.put(groupName, new Array<>());
		classNames.get(groupName).addAll(supplier.get());
	}

	public void writeToFile(FileHandle file) throws IOException {

		try (Writer writer = file.writer(false, "UTF-8")) {

			for (ObjectMap.Entry<String, Array<String>> group : classNames) {

				writer.append(':').append(group.key).append('\n');

				for (String name : group.value) {
					writer.append(name).append('\n');
				}
			}

			writer.flush();
		}
	}

	private void readFromFile(FileHandle file) throws IOException {

		if (!file.exists()) {
			return;
		}

		Box.Reference<String> groupName = new Box.Reference<>(null);

		TextFileUtils.readLines(file, line -> {
			if (line.startsWith(":")) {
				groupName.set(line.substring(1));
				classNames.put(groupName.get(), new Array<>());
			} else if (!groupName.isNull()) {
				classNames.get(groupName.get()).add(line);
			}
		});
	}

}
