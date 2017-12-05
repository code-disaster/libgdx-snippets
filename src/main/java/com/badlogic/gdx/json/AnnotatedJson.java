package com.badlogic.gdx.json;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.lang.ClassFinder;
import com.badlogic.gdx.utils.*;

import java.io.*;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Utility functions to read or write a hierarchy of objects annotated with
 * {@link com.badlogic.gdx.json.annotations.JsonSerializable} and {@link com.badlogic.gdx.json.annotations.JsonSerialize}.
 */
public class AnnotatedJson {

	public static <T> Json newReader(Class<T> clazz, Consumer<Json> setupJson) {

		Json json = new Json();
		json.setSerializer(clazz, new AnnotatedJsonSerializer<>(json, clazz));

		if (setupJson != null) {
			setupJson.accept(json);
		}

		return json;
	}

	public static <T> T read(FileHandle path, Class<T> clazz, Consumer<Json> setupJson) throws IOException {
		Json json = newReader(clazz, setupJson);
		return read(path, clazz, json);
	}

	public static <T> T read(FileHandle path, Class<T> clazz, Json json) throws IOException {
		try {
			InputStream fileStream = path.read();
			BufferedInputStream stream = new BufferedInputStream(fileStream);
			Reader reader = new InputStreamReader(stream, "UTF-8");
			return json.fromJson(clazz, reader);
		} catch (SerializationException e) {
			GdxSnippets.log.error("Error while serializing class " + clazz.getName(), e);
			throw new IOException(e.getCause());
		} catch (RuntimeException e) {
			throw new IOException(e);
		}
	}

	public static <T> Json newWriter(Class<T> clazz, Consumer<Json> setupJson) {

		Json json = new Json(JsonWriter.OutputType.json);
		json.setSerializer(clazz, new AnnotatedJsonSerializer<>(json, clazz));

		if (setupJson != null) {
			setupJson.accept(json);
		}

		return json;
	}

	public static <T> void write(FileHandle path, T object, Class<T> clazz, Consumer<Json> setupJson) throws IOException {
		Json json = newWriter(clazz, setupJson);
		write(path, false, object, json);
	}

	public static <T> void write(FileHandle path, boolean compact, T object, Class<T> clazz, Consumer<Json> setupJson) throws IOException {
		Json json = newWriter(clazz, setupJson);
		write(path, compact, object, json);
	}

	public static <T> void write(FileHandle path, T object, Json json) throws IOException {
		write(path, false, object, json);
	}

	public static <T> void write(FileHandle path, boolean compact, T object, Json json) throws IOException {

		String output = json.toJson(object);
		String prettyOutput = compact ? output : json.prettyPrint(output);

		try (FileOutputStream fos = new FileOutputStream(path.file(), false)) {
			try (Writer writer = new OutputStreamWriter(fos, "UTF-8")) {
				writer.write(prettyOutput);
				writer.flush();
			}
		}
	}

	/**
	 * Convenience function to register another annotated Json serializer.
	 */
	public static <T> void register(Json json, Class<T> clazz) {
		json.setSerializer(clazz, new AnnotatedJsonSerializer<>(json, clazz));
	}

	/**
	 * Convenience function to register another Json serializer.
	 */
	public static <T> void register(Json json, Class<T> clazz, Json.Serializer<T> serializer) {
		json.setSerializer(clazz, serializer);
	}

	/**
	 * Scans for subclasses of the given class, and adds annotated serializers for each
	 * of them. Only classes located the same URL as the given class are searched.
	 */
	public static <T> void registerSubclasses(Json json, Class<T> clazz,
											  Predicate<String> clazzNameFilter) {

		if (clazzNameFilter == null) {
			clazzNameFilter = (name) -> true;
		}

		new ClassFinder()
				.filterURLforClass(clazz)
				.process(clazzNameFilter::test, aClazz -> {
					if (!clazz.equals(aClazz) && clazz.isAssignableFrom(aClazz)) {
						//System.out.println("register: " + aClazz.getName());
						register(json, aClazz.asSubclass(clazz));
					}
				});

	}

}
