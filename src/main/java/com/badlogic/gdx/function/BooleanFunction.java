package com.badlogic.gdx.function;

/**
 * Represents a {@link java.util.function.Function} which produces a boolean result.
 */
@FunctionalInterface
public interface BooleanFunction<T> {

	boolean apply(T t);

}
