package com.badlogic.gdx.function;

@FunctionalInterface
public interface Predicate<T> {
	boolean test(T t);
}
