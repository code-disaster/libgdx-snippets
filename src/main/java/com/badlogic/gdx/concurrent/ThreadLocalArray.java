package com.badlogic.gdx.concurrent;

import com.badlogic.gdx.function.Supplier;
import com.badlogic.gdx.function.ThrowableSupplier;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.reflect.*;

/**
 * A convenience wrapper to {@link ThreadLocal} storing an array of objects.
 *
 * <pre>
 * {@code
 * ThreadLocalArray<UserObject> tls = new ThreadLocalArray<>(64, UserObject.class, () -> new UserObject(params...));
 * UserObject[] array = tls.get();
 * }
 * </pre>
 */
public class ThreadLocalArray<T> implements Supplier<T[]> {

	private final ThreadLocal<T[]> tls = new ThreadLocal<>();

	public ThreadLocalArray(int capacity, Class<? extends T> clazz) {
		this(capacity, clazz, () -> ClassReflection.newInstance(clazz));
	}

	@SuppressWarnings("unchecked")
	public ThreadLocalArray(int capacity, Class<? extends T> clazz,
							ThrowableSupplier<T, ReflectionException> initialValueSupplier) {

		try {

			T[] values = (T[]) ArrayReflection.newInstance(clazz, capacity);

			for (int i = 0; i < capacity; i++) {
				values[i] = initialValueSupplier.get();
			}

			tls.set(values);

		} catch (ReflectionException e) {
			throw new GdxRuntimeException(e);
		}
	}

	@Override
	public T[] get() {
		return tls.get();
	}
}
