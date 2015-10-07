package com.badlogic.gdx.function;

import java.io.IOException;

/**
 * Version of {@link java.util.function.Consumer} which can throw {@link IOException}.
 */
@FunctionalInterface
public interface IOConsumer<T> {
	void accept(T t) throws IOException;
}
