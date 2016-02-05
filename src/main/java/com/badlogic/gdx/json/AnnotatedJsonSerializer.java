package com.badlogic.gdx.json;

import com.badlogic.gdx.json.annotations.*;
import com.badlogic.gdx.utils.*;
import com.badlogic.gdx.utils.reflect.*;

import java.util.Map;
import java.util.function.Consumer;

/**
 * Generic implementation of {@link com.badlogic.gdx.utils.Json.Serializer} which uses reflection information
 * to decide which fields to serialize.
 *
 * @see {@link com.badlogic.gdx.json.annotations.JsonSerializable}
 * @see {@link com.badlogic.gdx.json.annotations.JsonSerialize}
 */
public class AnnotatedJsonSerializer<T> implements Json.Serializer<T> {

	private static class FieldAdapter {

		Field field;
		JsonSerialize annotation;
		Json.Serializer<?> serializer;

		FieldAdapter(Field field) {
			this.field = field;
			annotation = field.getDeclaredAnnotation(JsonSerialize.class).getAnnotation(JsonSerialize.class);
		}

		void ensureAccess(Consumer<FieldAdapter> consumer) {
			boolean accessible = field.isAccessible();
			try {
				field.setAccessible(true);
				consumer.accept(this);
			} finally {
				field.setAccessible(accessible);
			}
		}

		@SuppressWarnings("unchecked")
		<T, V> V get(T instance) {
			try {
				return (V) field.get(instance);
			} catch (ReflectionException e) {
				throw new GdxRuntimeException(e);
			}
		}

		<T, V> void set(T instance, V value) {
			try {
				field.set(instance, value);
			} catch (ReflectionException e) {
				throw new GdxRuntimeException(e);
			}
		}

		String getName() {
			String name = annotation.name();
			return name.isEmpty() ? field.getName() : name;
		}

	}

	private Class<T> clazz;
	private JsonSerializable annotation;
	private Array<FieldAdapter> fieldAdapters = new Array<>();

	public AnnotatedJsonSerializer(Json json, Class<T> clazz) {
		this.clazz = clazz;
		createSerializer(json);
	}

	@Override
	public void write(Json json, T object, Class knownType) {

		json.writeObjectStart();

		if (annotation.dynamic()) {
			json.writeValue("type", object.getClass().getName(), String.class);
		}

		fieldAdapters.forEach(adapter -> {

			if (isKnownContainerType(adapter)) {

				if (isArray(adapter)) {
					writeArray(json, object, adapter);
				} else if (isMap(adapter)) {
					writeMap(json, object, adapter);
				}

			} else {

				writeObject(json, object, adapter);

			}
		});

		json.writeObjectEnd();
	}

	private void writeObject(Json json, T object, FieldAdapter adapter) {

		adapter.ensureAccess(accessible -> {

			Object value = accessible.get(object);

			Class<?> fieldType = accessible.field.getType();

			if (value != null) {

				Class<?> valueType = value.getClass();
				if (!valueType.equals(fieldType)) {
					// todo: check for @JsonSerializable.dynamic() and warn/error
				}

			} else {

				if (!annotation.writeNull()) {
					return;
				}

			}

			if (fieldType.isArray()) {
				json.writeValue(accessible.getName(), value, fieldType, fieldType.getComponentType());
			} else if (annotation.encodeFP() && fieldType.equals(float.class)) {
				json.writeValue(accessible.getName(), JsonFloatSerializer.encodeFloatBits((float) value), fieldType);
			} else if (annotation.encodeFP() && fieldType.equals(double.class)) {
				json.writeValue(accessible.getName(), JsonFloatSerializer.encodeDoubleBits((double) value), fieldType);
			} else {
				json.writeValue(accessible.getName(), value, fieldType);
			}

		});
	}

	private void writeArray(Json json, T object, FieldAdapter adapter) {

		adapter.ensureAccess(accessible -> {

			Array<?> array = accessible.get(object);
			JsonArraySerializer<?> serializer = (JsonArraySerializer<?>) accessible.serializer;
			serializer.write(json, array, Array.class);

		});
	}

	private void writeMap(Json json, T object, FieldAdapter adapter) {

		adapter.ensureAccess(accessible -> {

			JsonMap map = accessible.annotation.map()[0];

			Class<?> clazz = map.map();
			JsonMapSerializer<?, ?> serializer = (JsonMapSerializer<?, ?>) accessible.serializer;

			if (Map.class.isAssignableFrom(clazz)) {
				Map<?, ?> value = accessible.get(object);
				serializer.write(json, value.entrySet(), Map.Entry::getKey, Map.Entry::getValue);
			} else if (ObjectMap.class.isAssignableFrom(clazz)) {
				ObjectMap<?, ?> value = accessible.get(object);
				serializer.write(json, value.entries(), e -> e.key, e -> e.value);
			}

		});
	}

	@Override
	@SuppressWarnings("unchecked")
	public T read(Json json, JsonValue jsonData, Class type) {

		try {

			T object;
			Class<T> clazz = this.clazz;

			if (annotation.dynamic()) {

				String typeName = json.readValue("type", String.class, jsonData);
				Class<?> typeClazz = Class.forName(typeName);

				if (!clazz.isAssignableFrom(typeClazz)) {
					throw new ReflectionException(clazz.getName() + " is not assignable from " + typeName);
				}

				clazz = (Class<T>) typeClazz;
			}

			object = ClassReflection.newInstance(clazz);

			fieldAdapters.forEach(adapter -> {

				if (isKnownContainerType(adapter)) {

					if (isArray(adapter)) {
						readArray(json, jsonData, object, adapter);
					} else if (isMap(adapter)) {
						readMap(json, jsonData, object, adapter);
					}

				} else {

					readObject(json, jsonData, object, adapter);

				}
			});

			return object;

		} catch (ReflectionException | ClassNotFoundException e) {
			throw new GdxRuntimeException(e);
		}
	}

	private void readObject(Json json, JsonValue jsonData, T object, FieldAdapter adapter) {

		adapter.ensureAccess(accessible -> {

			Class<?> fieldType = accessible.field.getType();
			Class<?> componentType = getFieldComponentType(adapter);

			Object value;

			if (annotation.encodeFP() && fieldType.equals(float.class)) {
				value = JsonFloatSerializer.decodeFloatBits(
						json.readValue(accessible.getName(), String.class, jsonData), accessible.get(object));
			} else if (annotation.encodeFP() && fieldType.equals(double.class)) {
				value = JsonFloatSerializer.decodeDoubleBits(
						json.readValue(accessible.getName(), String.class, jsonData), accessible.get(object));
			} else {
				value = json.readValue(accessible.getName(), fieldType, componentType, jsonData);
			}

			if (value == null) {

				// if the parent annotation allows writing of null, do nothing here
				if (annotation.writeNull()) {
					return;
				}

				// if the field is @JsonSerializable too, create a default instance
				if (ClassReflection.isAnnotationPresent(fieldType, JsonSerializable.class)) {
					try {
						value = ClassReflection.newInstance(fieldType);
					} catch (ReflectionException e) {
						throw new GdxRuntimeException(e);
					}
				} else {
					return;
				}

			}

			accessible.set(object, value);

		});
	}

	private void readArray(Json json, JsonValue jsonData, T object, FieldAdapter adapter) {

		adapter.ensureAccess(accessible -> {

			JsonArraySerializer<?> serializer = (JsonArraySerializer<?>) accessible.serializer;
			Array<?> array = serializer.read(json, jsonData, Array.class);

			if (array == null) {
				// todo: warning
				return;
			}

			accessible.set(object, array);

		});
	}

	private void readMap(Json json, JsonValue jsonData, T object, FieldAdapter adapter) {

		adapter.ensureAccess(accessible -> {

			JsonMap map = accessible.annotation.map()[0];

			Class<?> clazz = map.map();
			JsonMapSerializer<?, ?> serializer = (JsonMapSerializer<?, ?>) accessible.serializer;

			if (Map.class.isAssignableFrom(clazz)) {
				Map<?, ?> value = serializer.read(json, jsonData);
				accessible.set(object, value);
			} else if (ObjectMap.class.isAssignableFrom(clazz)) {
				ObjectMap<?, ?> value = serializer.read(json, jsonData, ObjectMap::new, ObjectMap::put);
				accessible.set(object, value);
			}

		});
	}

	private void createSerializer(Json json) {

		if (!ClassReflection.isAnnotationPresent(clazz, JsonSerializable.class)) {
			throw new GdxRuntimeException("Missing @JsonSerializable annotation for '" +
					ClassReflection.getSimpleName(clazz) + "'.");
		}

		annotation = ClassReflection.getAnnotation(clazz, JsonSerializable.class).getAnnotation(JsonSerializable.class);

		createSerializerFields(json, clazz);

		// register self to Json instance
		json.setSerializer(clazz, this);

		// register class TAG for 'dynamic' types
		if (annotation.dynamic() && !annotation.fullyQualifiedClassTag()) {
			json.addClassTag(clazz.getSimpleName(), clazz);
		}
	}

	private void createSerializerFields(Json json, Class<?> clazz) {

		if (clazz == null) {
			return;
		}

		if (!ClassReflection.isAnnotationPresent(clazz, JsonSerializable.class)) {
			return;
		}

		// declared fields of this class
		Field[] fields = ClassReflection.getDeclaredFields(clazz);
		createSerializerFields(json, fields);

		// continue with super class
		createSerializerFields(json, clazz.getSuperclass());
	}

	private void createSerializerFields(Json json, Field[] fields) {

		ArrayUtils.forEach(fields, field -> {

			// skip anything not annotated
			if (!field.isAnnotationPresent(JsonSerialize.class)) {
				return;
			}

			// add adapter
			FieldAdapter adapter = new FieldAdapter(field);
			fieldAdapters.add(adapter);

			// check for known built-in types
			if (isKnownContainerType(adapter)) {

				// this serializer is not registered to the Json instance
				adapter.serializer = addContainerSerializer(adapter);

				// get types of components stored in container
				Class<?>[] componentTypes = getContainerComponentTypes(adapter);

				// if any of those types is annotated, recursively create a serializer for it
				for (Class<?> componentType : componentTypes) {

					if (ClassReflection.isAnnotationPresent(componentType, JsonSerializable.class)) {

						// no reference stored, this is linked to the Json instance
						new AnnotatedJsonSerializer<>(json, componentType);
					}
				}

			} else {

				Class<?> componentType = getFieldComponentType(adapter);
				Json.Serializer<?> existingSerializer = json.getSerializer(componentType);

				if (existingSerializer != null) {

					// there's already a serializer registered
					adapter.serializer = existingSerializer;

				} else {

					// if type of the field is annotated, recursively create a serializer for it
					if (ClassReflection.isAnnotationPresent(componentType, JsonSerializable.class)) {
						adapter.serializer = new AnnotatedJsonSerializer<>(json, componentType);
					}
				}
			}
		});

	}

	private Class<?> getFieldComponentType(FieldAdapter adapter) {
		Class<?> clazz = adapter.field.getType();
		return clazz.isArray() ? clazz.getComponentType() : clazz;
	}

	private boolean isKnownContainerType(FieldAdapter adapter) {
		return isArray(adapter) || isMap(adapter);
	}

	private Class<?>[] getContainerComponentTypes(FieldAdapter adapter) {

		if (isArray(adapter)) {
			return new Class[] { adapter.annotation.array()[0].value() };
		} else if (isMap(adapter)) {
			JsonMap map = adapter.annotation.map()[0];
			return new Class[] { map.key(), map.value() };
		}

		throw new GdxRuntimeException("Unknown container type!");
	}

	private Json.Serializer<?> addContainerSerializer(FieldAdapter adapter) {

		if (isArray(adapter)) {

			JsonArray[] arrays = adapter.annotation.array();

			if (arrays.length != 1) {
				throw new GdxRuntimeException(
						"@JsonSerialize annotation on Array<> requires array() property.");
			}

			JsonArray array = arrays[0];

			return new JsonArraySerializer<>(adapter.getName(), array);

		} else if (isMap(adapter)) {

			JsonMap map = adapter.annotation.map()[0];

			return new JsonMapSerializer<>(adapter.getName(), map);
		}

		throw new GdxRuntimeException("Unknown container type!");
	}

	private boolean isArray(FieldAdapter adapter) {
		return adapter.field.getType().equals(Array.class);
	}

	private boolean isMap(FieldAdapter adapter) {
		return adapter.annotation.map().length > 0;
	}
}
