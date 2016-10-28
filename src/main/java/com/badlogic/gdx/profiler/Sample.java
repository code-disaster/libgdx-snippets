package com.badlogic.gdx.profiler;

public abstract class Sample implements AutoCloseable {

	@Override
	public void close() {
		end();
	}

	public abstract void end();

}
