package com.badlogic.gdx.function;

/**
 * Represents a function that produces an boolean-valued result. This is a
 * {@code boolean}-producing primitive specialization for {@link Function}.
 *
 * @param <T> the type of the input to the function
 * @see Function
 */
@FunctionalInterface
public interface ToBooleanFunction<T> {

	/**
	 * Applies this function to the given argument.
	 *
	 * @param value the function argument
	 * @return the function result
	 */
	boolean applyAsBoolean(T value);
}
