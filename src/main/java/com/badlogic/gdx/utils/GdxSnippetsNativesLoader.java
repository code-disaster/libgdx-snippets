package com.badlogic.gdx.utils;

public class GdxSnippetsNativesLoader {

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
