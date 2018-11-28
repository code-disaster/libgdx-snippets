package com.badlogic.gdx.profiler;

import com.badlogic.gdx.function.Consumer;

/**
 * Simple functional interface to scope (wrap begin/end) sample calls.
 */
public interface Profiler {

	Sample sampleCPU(String name, boolean aggregate);

	default <T> void sampleCPU(String name, boolean aggregate, T context, Consumer<T> consumer) {
		consumer.accept(context);
	}

	Sample sampleOpenGL(String name);

	default <T> void sampleOpenGL(String name, T context, Consumer<T> consumer) {
		consumer.accept(context);
	}

	default void setThreadName(CharSequence name) {

	}

}
