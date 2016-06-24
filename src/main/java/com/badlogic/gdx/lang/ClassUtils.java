package com.badlogic.gdx.lang;

import com.badlogic.gdx.utils.GdxRuntimeException;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public class ClassUtils {

	/**
	 * Uses reflection to obtain the class type of a generic type parameter.
	 */
	public static Class<?> getClassOfGenericType(Class<?> genericType, int typeArgument) {

		Type generic = genericType.getGenericSuperclass();
		Type type = ((ParameterizedType) generic).getActualTypeArguments()[typeArgument];

		String[] types = type.toString().split(" ");
		if (types.length < 2) {
			return null;
		}

		String className = types[1];

		try {
			return Class.forName(className);
		} catch (ClassNotFoundException e) {
			throw new GdxRuntimeException(e);
		}

	}

}
