package com.badlogic.gdx.graphics;

import java.io.IOException;

public class GLSLOptimizer {

	public enum ShaderType {
		Vertex,
		Fragment
	}

	public static String optimize(ShaderType type, String source) throws IOException {

		long ctx = initializeContext();

		try {

			long shader = optimizeShader(ctx, type.ordinal(), source);

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

	private static native long initializeContext(); /*
		return (int64_t) glslopt::initializeContext();
	*/

	private static native long optimizeShader(long ctx, int type, String source); /*
		return (int64_t) glslopt::optimizeShader((glslopt_ctx*) ctx, type, source);
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
