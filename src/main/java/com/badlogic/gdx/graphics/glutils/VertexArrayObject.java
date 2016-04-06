package com.badlogic.gdx.graphics.glutils;

import com.badlogic.gdx.utils.Disposable;

import static com.badlogic.gdx.Gdx.gl30;

/**
 * Manages VAO bindings.
 */
public class VertexArrayObject implements Disposable {

	private final int[] vao = new int[1];

	private VertexAttributeArray attributes;
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

	public void bindVertexLayout(VertexAttributeArray attributes) {

		int[] locations = new int[attributes.size()];
		for (int l = 0; l < attributes.size(); l++) {
			locations[l] = l;
		}

		bindVertexLayout(locations, attributes);
	}

	public void bindVertexLayout(int[] locations, VertexAttributeArray attributes) {

		unbindLocations();

		this.attributes = attributes;

		int count = Math.min(locations.length, attributes.size());

		for (int i = 0; i < count; i++) {
			bindLocation(locations[i], i);
		}
	}

	private void bindLocation(int location, int attribute) {

		if (location < 0) {
			return;
		}

		gl30.glEnableVertexAttribArray(location);
		attributes.bind(location, attribute);

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

	public VertexAttributeArray getAttributes() {
		return attributes;
	}

}
