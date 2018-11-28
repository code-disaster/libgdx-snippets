package com.badlogic.gdx.function;

@FunctionalInterface
public interface IntFunction<R> {
	R apply(int value);
}
