package com.badlogic.gdx.json;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.lang.ClassFinder;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonWriter;

import java.io.IOException;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Utility functions to read or write a hierarchy of objects annotated with
 * {@link com.badlogic.gdx.json.annotations.JsonSerializable} and {@link com.badlogic.gdx.json.annotations.JsonSerialize}.
 */
public class AnnotatedJson {

	public static <T> T read(FileHandle path, Class<T> clazz, Consumer<Json> setupJson) throws IOException {

		Json json = new Json();

		json.setSerializer(clazz, new AnnotatedJsonSerializer<>(json, clazz));

		if (setupJson != null) {
			setupJson.accept(json);
		}

		try {
			return json.fromJson(clazz, path);
		} catch (RuntimeException e) {
			throw new IOException(e);
		}
	}

	public static <T> void write(FileHandle path, T object, Class<T> clazz, Consumer<Json> setupJson) {

		Json json = new Json(JsonWriter.OutputType.json);
		json.setSerializer(clazz, new AnnotatedJsonSerializer<>(json, clazz));

		if (setupJson != null) {
			setupJson.accept(json);
		}

		String output = json.toJson(object);
		String prettyOutput = json.prettyPrint(output);

		path.writeString(prettyOutput, false, "UTF-8");
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
