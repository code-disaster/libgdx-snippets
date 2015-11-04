package com.badlogic.gdx.json;

import com.badlogic.gdx.json.annotations.JsonMap;
import com.badlogic.gdx.utils.*;

import java.util.Map;
import java.util.function.*;

/**
 * Implementation of {@link com.badlogic.gdx.utils.Json.Serializer} to serialize {@link Map} containers.
 *
 * This is used internally by {@link AnnotatedJsonSerializer}.
 *
 * @see {@link AnnotatedJsonSerializer}
 */
class JsonMapSerializer<K, V> implements Json.Serializer<Iterable<?>> {

	private String name;
	private JsonMap map;

	JsonMapSerializer(String name, JsonMap map) {
		this.name = name;
		this.map = map;
	}

	@Override
	public void write(Json json, Iterable<?> object, Class knownType) {
		throw new GdxRuntimeException("Not implemented!");
	}

	public <E> void write(Json json, Iterable<E> entries,
						  Function<E, ?> getKey, Function<E, ?> getValue) {

		json.writeArrayStart(name);

		entries.forEach(entry -> {

			json.writeObjectStart();

			json.writeValue("key", getKey.apply(entry), map.key());
			json.writeValue("value", getValue.apply(entry), map.value());

			json.writeObjectEnd();

		});

		json.writeArrayEnd();
	}

	@Override
	public Iterable<?> read(Json json, JsonValue jsonData, Class type) {
		throw new GdxRuntimeException("Not implemented!");
	}

	@SuppressWarnings("unchecked")
	public Map<?, ?> read(Json json, JsonValue jsonData) {

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

	public interface KeyValueConsumer<M, K, V> {
		void accept(M map, K key, V value);
	}

	@SuppressWarnings("unchecked")
	public <M> M read(Json json, JsonValue jsonData,
					  Supplier<M> newInstance, KeyValueConsumer<M, K, V> put) {

		M values = newInstance.get();

		JsonValue entry = jsonData.getChild(name);

		while (entry != null) {

			JsonValue keyValue = entry.get("key");
			K key = json.readValue((Class<K>) map.key(), keyValue);

			JsonValue valueValue = entry.get("value");
			V value = json.readValue((Class<V>) map.value(), valueValue);

			put.accept(values, key, value);

			entry = entry.next;
		}

		return values;
	}
}
