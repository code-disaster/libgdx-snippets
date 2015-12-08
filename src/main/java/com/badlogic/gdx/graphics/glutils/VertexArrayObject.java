package com.badlogic.gdx.graphics.glutils;

import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.utils.Disposable;

import static com.badlogic.gdx.Gdx.gl30;

/**
 * Manages VAO bindings.
 */
public class VertexArrayObject implements Disposable {

	private final int[] vao = new int[1];

	private VertexAttributes attributes;
	private int maxLocation = -1;

	public VertexArrayObject() {
		gl30.glGenVertexArrays(1, vao, 0);
	}

	@Override
	public void dispose() {
		gl30.glDeleteVertexArrays(1, vao, 0);
	}

	public void bind() {
		gl30.glBindVertexArray(vao[0]);
	}

	public void bindVertexLayout(VertexAttributes attributes) {

		int[] locations = new int[attributes.size()];
		for (int l = 0; l < attributes.size(); l++) {
			locations[l] = l;
		}

		bindVertexLayout(locations, attributes);
	}

	public void bindVertexLayout(int[] locations, VertexAttributes attributes) {

		unbindLocations();

		int count = Math.min(locations.length, attributes.size());

		for (int i = 0; i < count; i++) {

			final VertexAttribute attribute = attributes.get(i);
			final int location = locations[i];

			bindLocation(location, attribute, attributes.vertexSize);
		}

		this.attributes = attributes;
	}

	private void bindLocation(int location, VertexAttribute attribute, int vertexSize) {

		if (location < 0) {
			return;
		}

		gl30.glEnableVertexAttribArray(location);

		gl30.glVertexAttribPointer(location, attribute.numComponents, attribute.type,
				attribute.normalized, vertexSize, attribute.offset);

		maxLocation = Math.max(location, maxLocation);
	}

	private void unbindLocations() {

		for (int i = 0; i <= maxLocation; i++) {
			gl30.glDisableVertexAttribArray(i);
		}

		maxLocation = -1;
	}

	public static void unbind() {
		gl30.glBindVertexArray(0);
	}

	public VertexAttributes getAttributes() {
		return attributes;
	}

}
