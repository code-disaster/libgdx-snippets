package com.badlogic.gdx.lang;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Utility class to filter and iterate classes registered to a {@link ClassLoader}.
 */
public class ClassFinder {

	private final URLClassLoader classLoader;
	private Array<URL> urls = new Array<>();

	public ClassFinder() {
		this(Thread.currentThread().getContextClassLoader());
	}

	public ClassFinder(ClassLoader classLoader) {
		if (classLoader == null || !(classLoader instanceof URLClassLoader)) {
			throw new GdxRuntimeException("Error obtaining class loader.");
		}
		this.classLoader = (URLClassLoader) classLoader;
	}

	/**
	 * Enumerates all URLs registered to the {@link ClassLoader}.
	 *
	 * Must be called before {@link ClassFinder#process(Predicate, Consumer)}.
	 */
	public ClassFinder filter(Predicate<URL> filter) {
		urls.clear();
		Arrays.stream(classLoader.getURLs()).filter(filter::test).forEach(urls::add);
		return this;
	}

	/**
	 * Only uses the same URL as the given class was loaded from.
	 *
	 * Must be called before {@link ClassFinder#process(Predicate, Consumer)}.
	 */
	public ClassFinder filterURLforClass(Class<?> clazz) {

		// todo: this may not work because of SecurityManager
		URL url = clazz.getProtectionDomain().getCodeSource().getLocation();
		urls.add(url);

		return this;
	}

	/**
	 * Iterates classes of all URLs filtered by a previous call of {@link ClassFinder#filter(Predicate)}.
	 */
	public ClassFinder process(Predicate<String> filter, Consumer<Class<?>> processor) {

		for (URL url : urls) {

			FileHandle file = new FileHandle(url.getFile());

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
				.replace("/", ".").replaceAll("\\$.", "");

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
