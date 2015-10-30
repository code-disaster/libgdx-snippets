package com.badlogic.gdx.json;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonWriter;

import java.io.IOException;
import java.util.function.Consumer;

public class AnnotatedJson {

	public static <T> T read(FileHandle path, Class<T> clazz, Consumer<Json> setupJson) throws IOException {

		Json json = new Json();

		json.setSerializer(clazz, new AnnotatedJsonSerializer<>(json, clazz));

		setupJson.accept(json);

		try {
			return json.fromJson(clazz, path);
		} catch (RuntimeException e) {
			throw new IOException(e);
		}
	}

	public static <T> void write(FileHandle path, T object, Class<T> clazz, Consumer<Json> setupJson) {

		Json json = new Json(JsonWriter.OutputType.json);
		json.setSerializer(clazz, new AnnotatedJsonSerializer<>(json, clazz));

		setupJson.accept(json);

		String output = json.prettyPrint(object);
		path.writeString(output, false, "UTF-8");
	}

}
