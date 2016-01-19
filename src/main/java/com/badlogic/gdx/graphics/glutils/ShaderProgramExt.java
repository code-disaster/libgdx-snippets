package com.badlogic.gdx.graphics.glutils;

import com.badlogic.gdx.utils.Disposable;

import java.util.function.Consumer;

/**
 * Extension to {@link ShaderProgram} for customized shader construction. The "onCreate" consumer function
 * is called during shader creation, between calls to glCreateProgram() and glLinkProgram().
 */
public class ShaderProgramExt implements Disposable {

	private int handle;
	private Program program;
	private Consumer<Integer> onCreate;

	public ShaderProgramExt(String vertexShader, String fragmentShader, Consumer<Integer> onCreate) {
		this.onCreate = onCreate;
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
		protected int createProgram() {
			handle = super.createProgram();
			onCreate.accept(handle);
			return handle;
		}
	}

}
