package com.badlogic.gdx.graphics;

public class GLFWExt {

	/**
	 * Utility function to use [CocoaWindow toggleFullScreen] on MacOS.
	 */
	public static boolean toggleFullscreenMacOS(long window) {
		return nToggleFullscreenMacOS(window);
	}

	// @off

	/*JNI
		#include "glfwext.h"
	*/

	private static native boolean nToggleFullscreenMacOS(long window); /*
		#ifdef __APPLE__
		id _window = (id) window;
		return glfwext_set_fullscreen_macos(_window);
		#else
		return false;
		#endif
	*/

}
