package com.badlogic.gdx.graphics.glutils;

import com.badlogic.gdx.utils.BufferUtils;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

import static com.badlogic.gdx.Gdx.gl30;
import static com.badlogic.gdx.graphics.GL20.GL_TRIANGLES;
import static com.badlogic.gdx.graphics.GL30.GL_ARRAY_BUFFER;

/**
 * A lean replacement for {@link VertexBufferObjectWithVAO}.
 * Designed to work with {@link VertexArrayObject}.
 */
public class VertexBufferObjectExt extends GLBufferObject<FloatBuffer> {

	public VertexBufferObjectExt(boolean isStatic, int numVertices, int vertexSize) {
		super(GL_ARRAY_BUFFER, isStatic, numVertices, vertexSize);
	}

	public void setVertices(float[] vertices, int offset, int count) {
		buffer.position(0);
		BufferUtils.copy(vertices, buffer, count, offset);
		buffer.position(buffer.limit());
		buffer.limit(buffer.capacity());
	}

	public void addVertices(int count, float... vertices) {
		buffer.put(vertices, 0, count);
	}

	public void drawArrays(int count, int offset) {
		gl30.glDrawArrays(GL_TRIANGLES, offset, count);
	}

	@Override
	protected void createElementBuffer(ByteBuffer byteBuffer) {
		buffer = byteBuffer.asFloatBuffer();
		wordSize = 4;
	}

	public static boolean fitsElements(VertexBufferObjectExt bufferObject, int numElements, int elementSize) {
		return (bufferObject != null)
				&& (numElements <= bufferObject.getMaxElements())
				&& (elementSize == bufferObject.elementSize);
	}

}
