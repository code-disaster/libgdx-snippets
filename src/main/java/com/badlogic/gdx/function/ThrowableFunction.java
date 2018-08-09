package com.badlogic.gdx.function;

/**
 * Version of {@link java.util.function.Function} which can throw an exception.
 *
 * @param <T> the type of parameter passed to this consumer
 * @param <R> the type of the function result
 * @param <E> the type of exception to be handled
 */
@FunctionalInterface
public interface ThrowableFunction<T, R, E extends Throwable> {

	R apply(T t) throws E;
}
