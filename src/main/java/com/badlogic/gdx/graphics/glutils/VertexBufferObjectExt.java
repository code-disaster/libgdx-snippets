package com.badlogic.gdx.graphics.glutils;

import com.badlogic.gdx.utils.BufferUtils;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

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
		BufferUtils.copy(vertices, buffer, count, offset);
		buffer.position(0);
		buffer.limit(count);
	}

	public void addVertices(float[] vertices, int count) {
		buffer.put(vertices, 0, count);
	}

	@Override
	protected void createElementBuffer(ByteBuffer byteBuffer) {
		buffer = byteBuffer.asFloatBuffer();
		wordSize = 4;
	}

}
