package com.badlogic.gdx.json;

import com.badlogic.gdx.json.annotations.JsonMap;
import com.badlogic.gdx.utils.*;

import java.util.Map;

/**
 * Implementation of {@link com.badlogic.gdx.utils.Json.Serializer} to serialize {@link Map} containers.
 *
 * This is used internally by {@link AnnotatedJsonSerializer}.
 *
 * @see {@link AnnotatedJsonSerializer}
 */
class JsonMapSerializer<K, V> implements Json.Serializer<Map<?, ?>> {

	private String name;
	private JsonMap map;

	JsonMapSerializer(String name, JsonMap map) {
		this.name = name;
		this.map = map;
	}

	@Override
	public void write(Json json, Map<?, ?> object, Class knownType) {

		json.writeArrayStart(name);

		object.entrySet().forEach(entry -> {

			json.writeObjectStart();

			json.writeValue("key", entry.getKey(), map.key());
			json.writeValue("value", entry.getValue(), map.value());

			json.writeObjectEnd();

		});

		json.writeArrayEnd();
	}

	@Override
	@SuppressWarnings("unchecked")
	public Map<?, ?> read(Json json, JsonValue jsonData, Class type) {

		Map<K, V> values;

		try {
			values = (Map<K, V>) map.map().newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			throw new GdxRuntimeException(e);
		}

		JsonValue entry = jsonData.getChild(name);

		while (entry != null) {

			JsonValue keyValue = entry.get("key");
			K key = json.readValue((Class<K>) map.key(), keyValue);

			JsonValue valueValue = entry.get("value");
			V value = json.readValue((Class<V>) map.value(), valueValue);

			values.put(key, value);

			entry = entry.next;
		}

		return values;
	}
}
