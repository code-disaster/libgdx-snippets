package com.badlogic.gdx.graphics.glutils;

import com.badlogic.gdx.utils.BufferUtils;
import com.badlogic.gdx.utils.Disposable;

import java.nio.Buffer;
import java.nio.ByteBuffer;

import static com.badlogic.gdx.Gdx.gl20;
import static com.badlogic.gdx.graphics.GL20.GL_DYNAMIC_DRAW;
import static com.badlogic.gdx.graphics.GL20.GL_STATIC_DRAW;

/**
 * Abstract base class to handle OpenGL buffer objects.
 */
abstract class GLBufferObject<T extends Buffer> implements Disposable {

	private final int handle;
	private final int target;
	private final int usage;
	protected final int elementSize;
	private final ByteBuffer byteBuffer;

	protected T buffer;
	protected int wordSize;

	GLBufferObject(int target, boolean isStatic, int numElements, int elementSize) {

		handle = gl20.glGenBuffer();

		this.target = target;
		usage = isStatic ? GL_STATIC_DRAW : GL_DYNAMIC_DRAW;

		this.elementSize = elementSize;
		byteBuffer = BufferUtils.newUnsafeByteBuffer(numElements * elementSize);

		createElementBuffer(byteBuffer);

		byteBuffer.flip();
	}

	@Override
	public void dispose() {
		gl20.glDeleteBuffer(handle);
		BufferUtils.disposeUnsafeByteBuffer(byteBuffer);
	}

	public void bind() {
		gl20.glBindBuffer(target, handle);
	}

	public void unbind() {
		gl20.glBindBuffer(target, 0);
	}

	public void uploadData() {
		byteBuffer.limit(buffer.limit() * wordSize);
		gl20.glBufferData(target, byteBuffer.limit(), byteBuffer, usage);
	}

	/* TODO: proper API to specify buffer slice
	public void uploadSubData(int firstElement, int numElements) {
		gl20.glBufferSubData(target, firstElement * elementSize, numElements * elementSize, byteBuffer);
	}*/

	public int getNumElements() {
		return buffer.limit() * wordSize / elementSize;
	}

	public int getMaxElements() {
		return byteBuffer.capacity() / elementSize;
	}

	public T getBuffer() {
		return buffer;
	}

	protected abstract void createElementBuffer(ByteBuffer byteBuffer);

}
