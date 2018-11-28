package com.badlogic.gdx.graphics;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.GdxSnippets;

import java.nio.LongBuffer;

/**
 * Extension to Gdx.gl30 which adds functions and constants not available to OpenGL ES, and
 * therefore not exposed through the libGDX interface.
 */
public final class GL33Ext {

	public static final int GL_TEXTURE_BORDER_COLOR = 0x1004;

	public static final int GL_POINT = 0x1B00;
	public static final int GL_LINE = 0x1B01;
	public static final int GL_FILL = 0x1B02;

	public static final int GL_CLAMP_TO_BORDER = 0x812D;

	public static final int GL_CLIP_DISTANCE0 = 0x3000;
	public static final int GL_CLIP_DISTANCE1 = 0x3001;
	public static final int GL_CLIP_DISTANCE2 = 0x3002;
	public static final int GL_CLIP_DISTANCE3 = 0x3003;
	public static final int GL_CLIP_DISTANCE4 = 0x3004;
	public static final int GL_CLIP_DISTANCE5 = 0x3005;
	public static final int GL_CLIP_DISTANCE6 = 0x3006;
	public static final int GL_CLIP_DISTANCE7 = 0x3007;

	public static final int GL_INTERNALFORMAT_SUPPORTED = 0x826F;
	public static final int GL_INTERNALFORMAT_PREFERRED = 0x8270;

	public static void glBlendEquationi(int buffer, int mode) {

		if (!Gdx.graphics.supportsExtension("GL_ARB_draw_buffers_blend")) {
			GdxSnippets.log.warn("Extension ARB_draw_buffers_blend not supported!");
		}

		nglBlendEquationi(buffer, mode);
	}

	public static void glGetInternalFormativ(int target, int internalformat, int pname, LongBuffer params) {

		if (!Gdx.graphics.supportsExtension("GL_ARB_internalformat_query2")) {
			GdxSnippets.log.warn("Extension ARB_internalformat_query2 not supported!");
		}

		nglGetInternalFormati64v(target, internalformat, pname, params.capacity(), params);
	}

	// @off

	/*JNI
		#include "flextGL.h"
	*/

	public static native void setupGL(boolean setupGLBindings); /*
		if (setupGLBindings) {
			flextInit();
		}
	*/

	public static native void glBindFragDataLocation(int program, int colorNumber, String name); /*
		glBindFragDataLocation(program, colorNumber, name);
	*/

	private static native void nglBlendEquationi(int buffer, int mode); /*
		if (FLEXT_ARB_draw_buffers_blend) {
			glpfBlendEquationiARB(buffer, mode);
		}
	*/

	public static native void glColorMaski(int buffer, boolean r, boolean g, boolean b, boolean a); /*
		glColorMaski(buffer, r, g, b, a);
	*/

	public static native void glDrawElementsBaseVertex(int mode, int count, int type, int indices, int baseVertex); /*
		glDrawElementsBaseVertex(mode, count, type, (const void*) ((size_t) indices), baseVertex);
	*/

	private static native void nglGetInternalFormati64v(int target, int internalformat,
														int pname, int bufSize, LongBuffer params); /*
		if (FLEXT_ARB_internalformat_query2) {
			glGetInternalformati64v(target, internalformat, pname, bufSize, (GLint64*) params);
		}
	*/

	public static native void glPolygonMode(int face, int mode); /*
		glPolygonMode(face, mode);
	*/

}
