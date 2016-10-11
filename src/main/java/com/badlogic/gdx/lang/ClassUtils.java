package com.badlogic.gdx.lang;

import com.badlogic.gdx.checksum.SHA1;
import com.badlogic.gdx.utils.FileUtils;
import com.badlogic.gdx.utils.GdxRuntimeException;

import java.io.IOException;
import java.io.InputStream;
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

	/**
	 * Calculates the SHA-1 hash on the byte code (the .class file content) of the given {@link Class}.
	 */
	public static SHA1 getClassHash(Class<?> classType) throws IOException {

		String resourcePath = "/" + classType.getName().replace('.', '/') + ".class";
		InputStream resource = classType.getResourceAsStream(resourcePath);

		return FileUtils.hashStream(resource);
	}

	/**
	 * Calculates the SHA-1 hash on the byte code (the .class file content) of the given {@link Class}.
	 *
	 * This version returns {@code defaultHash} if an {@link IOException} is thrown.
	 */
	public static SHA1 getClassHash(Class<?> classType, SHA1 defaultHash) {
		try {
			return getClassHash(classType);
		} catch (IOException e) {
			return defaultHash;
		}
	}

}
