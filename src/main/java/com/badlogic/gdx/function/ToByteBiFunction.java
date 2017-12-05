package com.badlogic.gdx.function;

@FunctionalInterface
public interface ToByteBiFunction<T, U> {

	/**
	 * Applies this function to the given arguments.
	 */
	byte applyAsByte(T t, U u);
}
