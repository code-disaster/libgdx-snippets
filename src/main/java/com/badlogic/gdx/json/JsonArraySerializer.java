package com.badlogic.gdx.json;

import com.badlogic.gdx.json.annotations.JsonArray;
import com.badlogic.gdx.utils.*;

/**
 * Implementation of {@link com.badlogic.gdx.utils.Json.Serializer} to serialize {@link Array} containers.
 *
 * This is used internally by {@link AnnotatedJsonSerializer}.
 *
 * @see {@link AnnotatedJsonSerializer}
 */
class JsonArraySerializer<V> implements Json.Serializer<Array<?>> {

	private String name;
	private JsonArray array;

	JsonArraySerializer(String name, JsonArray array) {
		this.name = name;
		this.array = array;
	}

	@Override
	public void write(Json json, Array<?> object, Class knownType) {

		json.writeArrayStart(name);

		object.forEach(item -> json.writeValue(item, array.value()));

		json.writeArrayEnd();
	}

	@Override
	@SuppressWarnings("unchecked")
	public Array<?> read(Json json, JsonValue jsonData, Class type) {

		JsonValue entry = jsonData.getChild(name);
		JsonValue entry2 = entry;

		// pre-scan size of array

		int size = 0;
		while (entry2 != null) {
			size++;
			entry2 = entry2.next;
		}

		// create array, read data

		Array<V> values;

		try {
			values = (Array<V>) array.array().newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			throw new GdxRuntimeException(e);
		}

		values.ensureCapacity(size);
		values.ordered = array.ordered();

		while (entry != null) {

			V value = json.readValue((Class<V>) array.value(), entry);

			values.add(value);

			entry = entry.next;
		}

		return values;
	}

}
