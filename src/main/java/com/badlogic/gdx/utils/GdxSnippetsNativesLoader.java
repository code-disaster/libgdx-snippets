package com.badlogic.gdx.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL33Ext;

public class GdxSnippetsNativesLoader {

	private static boolean nativesLoaded = false;

	/**
	 * Loads the native library, and (optionally) initializes the flextGL OpenGL bindings.
	 */
	public static synchronized void load(boolean loadNativeLibraries, boolean setupGLBindings) {

		try {

			if (loadNativeLibraries && !nativesLoaded) {

				// customized library name: 32/64 bit shared library on OS X

				SharedLibraryLoader loader = new SharedLibraryLoader() {
					@Override
					public String mapLibraryName(String libraryName) {
						if (isMac) {
							return "lib" + libraryName + ".dylib";
						}
						return super.mapLibraryName(libraryName);
					}
				};

				// load native library

				loader.load("gdx-snippets");

				nativesLoaded = true;
			}

			// initialize flextGL bindings

			if (setupGLBindings && !Gdx.graphics.isGL30Available()) {
				throw new RuntimeException("Custom OpenGL bindings are available for Gdx.gl30 only!");
			}

			GL33Ext.setupGL(setupGLBindings);

		} catch (Exception e) {
			GdxSnippets.log.error(e.getMessage());
		}
	}

}
