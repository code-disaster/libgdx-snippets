package com.badlogic.gdx.graphics;

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

	// @off

	/*JNI
		#include "flextGL.h"
	*/

	public static native void glPolygonMode(int face, int mode); /*
		glPolygonMode(face, mode);
	*/

}
