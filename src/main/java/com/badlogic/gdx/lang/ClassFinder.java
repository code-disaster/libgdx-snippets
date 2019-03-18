package com.badlogic.gdx.lang;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.function.Consumer;
import com.badlogic.gdx.function.Predicate;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Utility class to filter and iterate classes registered to a {@link ClassLoader}.
 */
public class ClassFinder {

	private Array<URL> urls = new Array<>();

	/**
	 * Only uses the same URL as the given class was loaded from.
	 * <p>
	 * Must be called before {@link ClassFinder#process(Predicate, Consumer)}.
	 */
	public ClassFinder filterURLforClass(Class<?> clazz) {

		// todo: this may not work because of SecurityManager
		URL url = clazz.getProtectionDomain().getCodeSource().getLocation();
		urls.add(url);

		return this;
	}

	/**
	 * Iterates classes of all URLs filtered by a previous call of {@link ClassFinder#filterURLforClass(Class)}.
	 */
	public ClassFinder process(Predicate<String> filter, Consumer<Class<?>> processor) {

		for (URL url : urls) {

			Path path;

			// required for Windows paths with spaces
			try {
				path = Paths.get(url.toURI());
			} catch (URISyntaxException e) {
				throw new GdxRuntimeException(e);
			}

			FileHandle file = new FileHandle(path.toFile());

			if (file.isDirectory()) {

				processDirectory(file, file, filter, processor);

			} else if (file.extension().equals("jar")) {

				try (JarFile jar = new JarFile(file.file())) {

					Enumeration<JarEntry> entries = jar.entries();

					while (entries.hasMoreElements()) {

						JarEntry entry = entries.nextElement();

						if (entry.isDirectory()) {
							continue;
						}

						FileHandle entryFile = new FileHandle(entry.getName());

						process(null, entryFile, filter, processor);
					}

				} catch (IOException ignored) {

				}
			}

		}

		return this;
	}

	/**
	 * Iterates classes from a {@link ClassFinderCache}.
	 * <p>
	 * This is faster than walking URLs/directories. It's also more secure/portable.
	 */
	public ClassFinder process(ClassFinderCache cache, String groupName,
							   Predicate<String> filter, Consumer<Class<?>> processor) {

		Array<String> classNames = cache.get(groupName);

		if (classNames == null) {
			return this;
		}

		for (String className : classNames) {
			processClass(className, filter, processor);
		}

		return this;
	}

	private void processDirectory(FileHandle root, FileHandle directory,
								  Predicate<String> filter, Consumer<Class<?>> processor) {

		FileHandle[] files = directory.list();

		for (FileHandle file : files) {

			if (file.isDirectory()) {
				processDirectory(root, new FileHandle(new File(directory.file(), file.name())), filter, processor);
			} else {
				process(root, file, filter, processor);
			}

		}
	}

	private void process(FileHandle root, FileHandle file, Predicate<String> filter, Consumer<Class<?>> processor) {

		if (!file.extension().equals("class")) {
			return;
		}

		FileHandle relative = relativeTo(file, root);

		String className = relative.pathWithoutExtension()
				.replace("/", ".");//.replaceAll("\\$.", "");

		processClass(className, filter, processor);
	}

	private void processClass(String className, Predicate<String> filter, Consumer<Class<?>> processor) {

		if (!filter.test(className)) {
			return;
		}

		try {

			Class<?> clazz = Class.forName(className);
			processClass(clazz, filter, processor);

		} catch (ClassNotFoundException ignored) {

		}

	}

	private void processClass(Class<?> clazz, Predicate<String> filter, Consumer<Class<?>> processor) {

		if (!filter.test(clazz.getName())) {
			return;
		}

		processor.accept(clazz);

		for (Class<?> inner : clazz.getDeclaredClasses()) {
			processClass(inner, filter, processor);
		}

	}

	private FileHandle relativeTo(FileHandle file, FileHandle root) {

		if (root == null) {
			return file;
		}

		String r = root.path();
		String f = file.path();

		if (!f.startsWith(r)) {
			return file;
		}

		return new FileHandle(f.substring(r.length() + 1));
	}
}
