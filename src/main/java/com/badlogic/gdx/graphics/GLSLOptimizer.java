package com.badlogic.gdx.graphics;

import java.io.IOException;

public class GLSLOptimizer {

	public enum Target {
		OpenGL,
		OpenGLES20,
		OpenGLES30,
		Metal
	}

	public enum ShaderType {
		Vertex,
		Fragment
	}

	public static final int OptionSkipPreprocessor = 1;
	public static final int OptionNotFullShader = 2;

	/**
	 * Utility wrapper function to optimize GLSL shader code.
	 *
	 * Runs glsl-optimizer on the provided shader code, and returns the optimized version.
	 *
	 * Throws an IOException with the shader log as error message.
	 */
	public static String optimize(Target target, ShaderType type, String source, int options) throws IOException {

		long ctx = initializeContext(target.ordinal());

		try {

			long shader = optimizeShader(ctx, type.ordinal(), source, options);

			try {

				if (getShaderStatus(shader)) {
					return getShaderOutput(shader);
				} else {
					throw new IOException("GLSL Optimizer: " + getShaderLog(shader));
				}

			} finally {
				deleteShader(shader);
			}

		} finally {
			cleanupOptimizerContext(ctx);
		}
	}

	// @off

	/*JNI
		#include "glslopt.h"
	*/

	private static native long initializeContext(int target); /*
		return (int64_t) glslopt::initializeContext(target);
	*/

	private static native long optimizeShader(long ctx, int type, String source, int options); /*
		return (int64_t) glslopt::optimizeShader((glslopt_ctx*) ctx, type, source, (uint32_t) options);
	*/

	private static native boolean getShaderStatus(long shader); /*
		return glslopt::getShaderStatus((glslopt_shader*) shader);
	*/

	static private native String getShaderOutput(long shader); /*
		return env->NewStringUTF(glslopt::getShaderOutput((glslopt_shader*) shader));
	*/

	static private native String getShaderLog(long shader); /*
		return env->NewStringUTF(glslopt::getShaderLog((glslopt_shader*) shader));
	*/

	private static native void deleteShader(long shader); /*
		glslopt::deleteShader((glslopt_shader*) shader);
	*/

	private static native void cleanupOptimizerContext(long ctx); /*
		glslopt::cleanupOptimizerContext((glslopt_ctx*) ctx);
	*/


}
