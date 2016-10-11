package com.badlogic.gdx.profiler;

import java.util.function.Consumer;

/**
 * Simple functional interface to scope (wrap begin/end) sample calls.
 */
public interface Profiler {

	default AutoCloseable sampleCPU(String name, boolean aggregate) {
		return () -> {};
	}

	default void sampleCPU(String name, boolean aggregate, Runnable runnable) {
		sampleCPU(name, aggregate, null, nil -> runnable.run());
	}

	default <T> void sampleCPU(String name, boolean aggregate, T context, Consumer<T> consumer) {
		consumer.accept(context);
	}

	default AutoCloseable sampleOpenGL(String name) {
		return () -> {};
	}

	default void sampleOpenGL(String name, Runnable runnable) {
		sampleOpenGL(name, null, nil -> runnable.run());
	}

	default <T> void sampleOpenGL(String name, T context, Consumer<T> consumer) {
		consumer.accept(context);
	}

}
