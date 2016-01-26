package com.badlogic.gdx.json;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonWriter;

import java.io.IOException;
import java.util.function.Consumer;

/**
 * Utility functions to serialize a hierarchy of objects annotated with
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

	public static <T> void register(Json json, Class<T> clazz) {
		json.setSerializer(clazz, new AnnotatedJsonSerializer<>(json, clazz));
	}

	public static <T> void register(Json json, Class<T> clazz, Json.Serializer<T> serializer) {
		json.setSerializer(clazz, serializer);
	}

}
