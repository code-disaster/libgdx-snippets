package com.badlogic.gdx.graphics.glutils;

import com.badlogic.gdx.graphics.GL30;
import com.badlogic.gdx.utils.BufferUtils;

import java.nio.LongBuffer;

import static com.badlogic.gdx.graphics.GL33Ext.*;

public class GLTextureUtils {

	private static LongBuffer tmp = BufferUtils.newLongBuffer(1);

	public static boolean isInternalFormatSupported(int target, int format) {
		tmp.clear();
		glGetInternalFormativ(target, format, GL_INTERNALFORMAT_SUPPORTED, tmp);
		return tmp.get(0) == GL30.GL_TRUE;
	}

	public static long getInternalFormatPreferred(int target, int format) {
		tmp.clear();
		glGetInternalFormativ(target, format, GL_INTERNALFORMAT_PREFERRED, tmp);
		return tmp.get(0);
	}

}
