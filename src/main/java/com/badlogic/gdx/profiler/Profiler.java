package com.badlogic.gdx.profiler;

/**
 * Simple functional interface to scope (wrap begin/end) sample calls.
 */
public interface Profiler {

	void sampleCPU(String name, boolean aggregate, Runnable runnable);

	void sampleOpenGL(String name, Runnable runnable);

}
