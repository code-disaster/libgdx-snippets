package com.badlogic.gdx.function;

/**
 * Similar to {@link java.util.function.ObjIntConsumer}.
 */
@FunctionalInterface
public interface IntObjConsumer<T> {

	void accept(int value, T t);
}
