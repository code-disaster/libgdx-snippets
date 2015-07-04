package com.badlogic.gdx.utils;

public class GdxSnippetsNativesLoader {

	/**
	 * Loads the native library, and initializes the flextGL OpenGL bindings.
	 */
	public static synchronized void load() {

		try {
			new SharedLibraryLoader().load("gdx-snippets");
			flextInit();
		} catch (Exception e) {
			System.err.println(e.getMessage());
		}

	}

	// @off

	/*JNI
		#include "flextGL.h"
	*/

	private static native void flextInit(); /*
		flextInit();
	*/

}
