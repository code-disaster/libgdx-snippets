package com.badlogic.gdx.function;

@FunctionalInterface
public interface ThrowableBiFunction<T, U, R, E extends Throwable> {

	R apply(T t, U u) throws E;
}
