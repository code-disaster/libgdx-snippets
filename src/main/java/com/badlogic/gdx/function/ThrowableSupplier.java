package com.badlogic.gdx.function;

/**
 * A variant of {@link Supplier} which can throw an exception.
 *
 * @param <T> the type of result supplied by this supplier
 * @param <E> the type of exception to be handled
 */
@FunctionalInterface
public interface ThrowableSupplier<T, E extends Throwable> {
	T get() throws E;
}
