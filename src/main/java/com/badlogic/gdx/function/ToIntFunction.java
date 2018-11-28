package com.badlogic.gdx.function;

@FunctionalInterface
public interface ToIntFunction<T> {
	int applyAsInt(T t);
}
