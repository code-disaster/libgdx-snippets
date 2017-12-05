package com.badlogic.gdx.utils;

import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

/**
 * A version of {@link com.badlogic.gdx.utils.viewport.ScreenViewport} which does not
 * depend on {@link Graphics#getWidth()} and {@link Graphics#getHeight()}.
 * <p>
 * This is accompanied by a version of {@link OrthographicCamera} which also ignores
 * the application window size.
 * <p>
 * Both viewport and camera assume that their width and height properties match.
 */
public class VirtualScreenViewport extends ScreenViewport {

	private final Vector3 tmp = new Vector3();

	public VirtualScreenViewport() {
		setCamera(new VirtualScreenCamera());
	}

	/**
	 * Does not call {@link com.badlogic.gdx.graphics.glutils.HdpiUtils#glViewport(int, int, int, int)}.
	 */
	@Override
	public void apply(boolean centerCamera) {
		Camera camera = getCamera();
		camera.viewportWidth = getWorldWidth();
		camera.viewportHeight = getWorldHeight();
		if (centerCamera) {
			camera.position.set(getWorldWidth() / 2, getWorldHeight() / 2, 0);
		}
		camera.update();
	}

	@Override
	public Vector2 toScreenCoordinates(Vector2 worldCoords, Matrix4 transformMatrix) {
		Camera camera = getCamera();
		tmp.set(worldCoords.x, worldCoords.y, 0);
		tmp.mul(transformMatrix);
		camera.project(tmp);
		tmp.y = camera.viewportHeight - tmp.y;
		worldCoords.x = tmp.x;
		worldCoords.y = tmp.y;
		return worldCoords;
	}

	@Override
	public int getRightGutterWidth() {
		return (int) getCamera().viewportWidth - (getScreenX() + getScreenWidth());
	}

	@Override
	public int getTopGutterHeight() {
		return (int) getCamera().viewportHeight - (getScreenY() + getScreenHeight());
	}

	private class VirtualScreenCamera extends OrthographicCamera {

		@Override
		public Vector3 unproject(Vector3 screenCoords, float viewportX, float viewportY, float viewportWidth, float viewportHeight) {
			float x = screenCoords.x, y = screenCoords.y;
			x = x - viewportX;
			y = VirtualScreenViewport.this.getScreenHeight() - y - 1;
			y = y - viewportY;
			screenCoords.x = (2 * x) / viewportWidth - 1;
			screenCoords.y = (2 * y) / viewportHeight - 1;
			screenCoords.z = 2 * screenCoords.z - 1;
			screenCoords.prj(invProjectionView);
			return screenCoords;
		}

		@Override
		public Vector3 unproject(Vector3 screenCoords) {
			unproject(screenCoords, 0, 0, getScreenWidth(), getScreenHeight());
			return screenCoords;
		}

		@Override
		public Vector3 project(Vector3 worldCoords) {
			project(worldCoords, 0, 0, getScreenWidth(), getScreenHeight());
			return worldCoords;
		}

		@Override
		public Ray getPickRay(float screenX, float screenY) {
			return getPickRay(screenX, screenY, 0, 0, getScreenWidth(), getScreenHeight());
		}

	}

}
