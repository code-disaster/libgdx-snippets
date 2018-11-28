package com.badlogic.gdx.graphics.glutils;

import com.badlogic.gdx.function.Consumer;
import com.badlogic.gdx.utils.Disposable;

/**
 * Extension to {@link ShaderProgram} for customized shader construction. The "onCreate" consumer function
 * is called during shader creation, between calls to glCreateProgram() and glLinkProgram().
 */
public class ShaderProgramExt implements Disposable {

	private int handle;
	private Program program;
	private Consumer<Integer> onCreate;
	private Runnable onDispose;

	public ShaderProgramExt(String vertexShader, String fragmentShader,
							Consumer<Integer> onCreate, Runnable onDispose) {

		this.onCreate = onCreate;
		this.onDispose = onDispose;
		this.program = new Program(vertexShader, fragmentShader);
	}

	@Override
	public void dispose() {
		program.dispose();
	}

	public ShaderProgram getProgram() {
		return program;
	}

	private class Program extends ShaderProgram {

		public Program(String vertexShader, String fragmentShader) {
			super(vertexShader, fragmentShader);
		}

		@Override
		public void dispose() {
			onDispose.run();
			super.dispose();
		}

		@Override
		protected int createProgram() {
			handle = super.createProgram();
			onCreate.accept(handle);
			return handle;
		}
	}

}
