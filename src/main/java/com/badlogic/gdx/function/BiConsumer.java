package com.badlogic.gdx.function;

@FunctionalInterface
public interface BiConsumer<T, U> {
	void accept(T t, U u);
}
