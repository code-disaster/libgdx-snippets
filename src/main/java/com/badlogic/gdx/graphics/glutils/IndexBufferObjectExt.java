package com.badlogic.gdx.graphics.glutils;

import com.badlogic.gdx.graphics.GL33Ext;
import com.badlogic.gdx.utils.BufferUtils;

import java.nio.ByteBuffer;
import java.nio.ShortBuffer;

import static com.badlogic.gdx.Gdx.gl30;
import static com.badlogic.gdx.graphics.GL30.*;

/**
 * A lean replacement for {@link IndexBufferObject}.
 * Designed to work with {@link VertexArrayObject}.
 */
public class IndexBufferObjectExt extends GLBufferObject<ShortBuffer> {

	public IndexBufferObjectExt(boolean isStatic, int numIndices) {
		super(GL_ELEMENT_ARRAY_BUFFER, isStatic, numIndices, 2);
	}

	public void setIndices(short[] indices, int offset, int count) {
		buffer.position(0);
		BufferUtils.copy(indices, offset, buffer, count);
		buffer.position(buffer.limit());
		buffer.limit(buffer.capacity());
	}

	public void addIndices(int count, short... indices) {
		buffer.put(indices, 0, count);
	}

	public void drawElements(int count, int offset) {
		gl30.glDrawElements(GL_TRIANGLES, count, GL_UNSIGNED_SHORT, offset * wordSize);
	}

	public void drawElementsBaseVertex(int count, int offset, int baseVertex) {
		GL33Ext.glDrawElementsBaseVertex(GL_TRIANGLES, count, GL_UNSIGNED_SHORT, offset * wordSize, baseVertex);
	}

	@Override
	protected void createElementBuffer(ByteBuffer byteBuffer) {
		buffer = byteBuffer.asShortBuffer();
		wordSize = 2;
	}

	public static boolean fitsElements(IndexBufferObjectExt bufferObject, int numElements) {
		return (bufferObject != null) && (numElements <= bufferObject.getMaxElements());
	}
}
