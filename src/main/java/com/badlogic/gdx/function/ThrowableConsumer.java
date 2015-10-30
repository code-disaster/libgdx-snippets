package com.badlogic.gdx.function;

/**
 * Version of {@link java.util.function.Consumer} which can throw an exception.
 *
 * @param <T> the type of parameter passed to this consumer
 * @param <E> the type of exception to be handled
 */
@FunctionalInterface
public interface ThrowableConsumer<T, E extends Throwable> {
	void accept(T t) throws E;
}
