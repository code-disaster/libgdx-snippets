package com.badlogic.gdx.json;

import com.badlogic.gdx.json.annotations.JsonSerializable;
import com.badlogic.gdx.json.annotations.JsonSerialize;
import com.badlogic.gdx.utils.*;
import com.badlogic.gdx.utils.reflect.*;

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
	private JsonSerializable clazzAnnotation;
	private Array<FieldAdapter> fieldAdapters = new Array<>();

	public AnnotatedJsonSerializer(Json json, Class<T> clazz) {
		this.clazz = clazz;
		createSerializer(json);
	}

	@Override
	public void write(Json json, T object, Class knownType) {

		json.writeObjectStart();

		fieldAdapters.forEach(field -> {

			Object value = field.get(object);
			Class<?> fieldType = field.field.getType();

			json.writeValue(field.getName(), value, fieldType);
		});

		json.writeObjectEnd();
	}

	@Override
	public T read(Json json, JsonValue jsonData, Class type) {

		try {

			T object = ClassReflection.newInstance(clazz);

			fieldAdapters.forEach(adapter -> {

				Class<?> fieldType = adapter.field.getType();
				Class<?> componentType = getComponentType(adapter);

				Object value = json.readValue(adapter.getName(), fieldType, componentType, jsonData);

				if (value == null) {
					// todo: warning
					return;
				}

				adapter.set(object, value);

			});

			return object;

		} catch (ReflectionException e) {
			throw new GdxRuntimeException(e);
		}
	}

	private void createSerializer(Json json) {

		if (!ClassReflection.isAnnotationPresent(clazz, JsonSerializable.class)) {
			throw new GdxRuntimeException("Missing @JsonSerializable annotation for '" +
					ClassReflection.getSimpleName(clazz) + "'.");
		}

		clazzAnnotation = ClassReflection.getDeclaredAnnotation(
				clazz, JsonSerializable.class).getAnnotation(JsonSerializable.class);

		Field[] fields = ClassReflection.getFields(clazz);
		createSerializerFields(json, fields, clazzAnnotation);

		// register self to Json instance
		json.setSerializer(clazz, this);
	}

	private void createSerializerFields(Json json, Field[] fields, JsonSerializable clazzAnnotation) {

		ArrayUtils.forEach(fields, field -> {

			// skip anything not annotated
			if (!field.isAnnotationPresent(JsonSerialize.class)) {
				return;
			}

			FieldAdapter adapter = new FieldAdapter(field);

			Class<?> fieldType = field.getType();
			Class<?> componentType = getComponentType(adapter);

			Json.Serializer<?> existingSerializer = json.getSerializer(fieldType);

			if (existingSerializer != null) {

				// there's already a serializer registered
				adapter.serializer = existingSerializer;

			} else {

				if (isKnownContainerType(adapter)) {

					// a known container type
					addContainerSerializer(json, adapter);

				}

				if (ClassReflection.isAnnotationPresent(componentType, JsonSerializable.class)) {

					// type of the field is annotated, recursively create a serializer for it
					adapter.serializer = new AnnotatedJsonSerializer<>(json, componentType);

				}
			}

			fieldAdapters.add(adapter);
		});

	}

	private Class<?> getComponentType(FieldAdapter adapter) {

		Class<?> fieldType = adapter.field.getType();

		if (fieldType.isArray()) {
			return fieldType.getComponentType();
		}

		if (isKnownContainerType(adapter)) {
			return getContainerComponentType(adapter);
		}

		return fieldType;
	}

	private boolean isKnownContainerType(FieldAdapter adapter) {

		Class<?> clazz = adapter.field.getType();

		if (clazz.equals(Array.class)) {
			return true;
		}

		return false;
	}

	private Class<?> getContainerComponentType(FieldAdapter adapter) {

		Class<?> clazz = adapter.annotation.clazz();

		if (clazz.equals(Class.class)) {
			throw new GdxRuntimeException("@JsonSerialize container annotations require a clazz() property.");
		}

		return clazz;
	}

	private void addContainerSerializer(Json json, FieldAdapter adapter) {

		if (adapter.field.getType().equals(Array.class)) {
			ArraySerializer<?> serializer = new ArraySerializer<>(adapter.annotation.clazz());
			json.setSerializer(adapter.field.getType(), serializer);
		}
	}

}
