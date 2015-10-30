package com.badlogic.gdx.json;

import com.badlogic.gdx.utils.*;
import com.badlogic.gdx.utils.reflect.ArrayReflection;

/**
 * Implementation of {@link com.badlogic.gdx.utils.Json.Serializer} to serialize {@link Array} containers.
 *
 * This is automatically injected by {@link AnnotatedJsonSerializer} if needed.
 *
 * @see {@link AnnotatedJsonSerializer}
 */
public class ArraySerializer<T> implements Json.Serializer<Array<T>> {

	private Class<T> clazz;

	public ArraySerializer(Class<T> clazz) {
		this.clazz = clazz;
	}

	@Override
	public void write(Json json, Array<T> object, Class knownType) {

		json.writeArrayStart();

		object.forEach(item -> {
			json.writeValue(item, clazz);
		});

		json.writeArrayEnd();
	}

	@Override
	@SuppressWarnings("unchecked")
	public Array<T> read(Json json, JsonValue jsonData, Class type) {

		T[] empty = (T[]) ArrayReflection.newInstance(clazz, 0);
		T[] array = (T[]) json.readValue(empty.getClass(), clazz, jsonData);

		return new Array<>(array);
	}

}
