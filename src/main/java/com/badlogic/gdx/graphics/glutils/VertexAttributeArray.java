package com.badlogic.gdx.graphics.glutils;

import com.badlogic.gdx.utils.GdxRuntimeException;

import java.util.Arrays;

import static com.badlogic.gdx.Gdx.gl30;
import static com.badlogic.gdx.graphics.GL30.*;

public class VertexAttributeArray {

	public static class Attribute {

		public final int numComponents;
		public final int type;
		public final boolean normalized;

		private final Alias alias;

		protected int offset;
		private final int stride;

		public Attribute(int numComponents, int type, boolean normalized, Alias alias) {
			this.numComponents = numComponents;
			this.type = type;
			this.normalized = normalized;
			this.alias = alias;
			// todo:
			this.stride = 0;
		}

		public int getOffset() {
			return offset;
		}

		void bind(int location, int stride) {
			if (type == GL_FLOAT || normalized) {
				gl30.glVertexAttribPointer(location, numComponents, type, normalized, stride, offset);
			} else {
				gl30.glVertexAttribIPointer(location, numComponents, type, stride, offset);
			}
		}

	}

	public enum Alias {

		Position("a_position"),
		Normal("a_normal"),
		ColorPacked("a_color"),

		TexCoord0("a_texCoord0"),

		Generic(null);

		Alias(String alias) {
			this.alias = alias;
		}

		private String alias;
	}

	private final Attribute[] attributes;

	private final int vertexSize;
	private final int vertexStride;

	public VertexAttributeArray(Attribute... attributes) {
		this(0, attributes);
	}

	public VertexAttributeArray(VertexAttributeArray other) {
		this(other.vertexStride, other.attributes);
	}

	public VertexAttributeArray(int vertexStride, Attribute... attributes) {

		this.attributes = Arrays.copyOf(attributes, attributes.length);

		this.vertexStride = vertexStride;
		this.vertexSize = calculateSizeAndOffsets();
	}

	public int size() {
		return attributes.length;
	}

	public Attribute get(int index) {
		return attributes[index];
	}

	public void bind(int location, int index) {
		int stride = vertexSize + vertexStride;
		//int stride = (vertexStride != 0) ? (vertexSize + vertexStride) : 0;
		attributes[index].bind(location, stride);
	}

	public Attribute find(Alias alias) {

		for (Attribute attribute : attributes) {
			if (attribute.alias == alias) {
				return attribute;
			}
		}

		return null;
	}

	public int getVertexSize() {
		return vertexSize + vertexStride;
	}

	private int calculateSizeAndOffsets() {

		int offset = 0;

		for (Attribute attribute : attributes) {

			attribute.offset = offset;

			offset += attribute.numComponents * getTypeSize(attribute.type);
			offset += attribute.stride;

		}

		return offset;
	}

	private int getTypeSize(int type) {

		switch (type) {

			case GL_BYTE:
			case GL_UNSIGNED_BYTE:
				return 1;

			case GL_SHORT:
			case GL_UNSIGNED_SHORT:
				return 2;

			case GL_INT:
			case GL_UNSIGNED_INT:
			case GL_FLOAT:
				return 4;

			default:
				throw new GdxRuntimeException("Unsupported vertex attribute type!");
		}
	}

	public static Attribute Float(Alias alias, int numComponents, boolean normalized) {
		return new Attribute(numComponents, GL_FLOAT, normalized, alias);
	}

	public static Attribute UnsignedByte(Alias alias, int numComponents) {
		return new Attribute(numComponents, GL_UNSIGNED_BYTE, true, alias);
	}

	public static Attribute Int(Alias alias, int numComponents) {
		return new Attribute(numComponents, GL_INT, false, alias);
	}

	public static Attribute UnsignedInt(Alias alias, int numComponents) {
		return new Attribute(numComponents, GL_UNSIGNED_INT, false, alias);
	}

}
