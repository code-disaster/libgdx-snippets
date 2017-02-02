package com.badlogic.gdx.graphics.g2d;

import com.badlogic.gdx.graphics.Pixmap;

public class PixmapTransform {

	public enum Mirror {
		None,
		X,
		Y,
		XY
	}

	public enum Rotate {
		None,
		_90,
		_180,
		_270
	}

	/**
	 * Read pixel from {@link Pixmap}, using back-transformed flipped and rotated pixel coordinates.
	 */
	public static int getPixel(Pixmap pixmap, int x, int y, Mirror mirror, Rotate rotate) {

		int widthMinusOne = pixmap.getWidth() - 1;
		int heightMinusOne = pixmap.getHeight() - 1;

		int px, py;

		// rotate

		switch (rotate) {
			case _90:
				px = y;
				py = heightMinusOne - x;
				break;
			case _180:
				px = widthMinusOne - x;
				py = heightMinusOne - y;
				break;
			case _270:
				px = widthMinusOne - y;
				py = x;
				break;
			default:
			case None:
				px = x;
				py = y;
				break;
		}

		// mirror

		if (mirror == Mirror.X || mirror == Mirror.XY) {
			px = widthMinusOne - px;
		}

		if (mirror == Mirror.Y || mirror == Mirror.XY) {
			py = heightMinusOne - py;
		}

		return pixmap.getPixel(px, py);
	}

	public static int getWidth(int width, int height, Rotate rotate) {
		return (rotate.ordinal() & 0x1) == 1 ? height : width;
	}

	public static int getHeight(int width, int height, Rotate rotate) {
		return (rotate.ordinal() & 0x1) == 1 ? width : height;
	}

	public static int getWidth(Pixmap pixmap, Rotate rotate) {
		return getWidth(pixmap.getWidth(), pixmap.getHeight(), rotate);
	}

	public static int getHeight(Pixmap pixmap, Rotate rotate) {
		return getHeight(pixmap.getWidth(), pixmap.getHeight(), rotate);
	}

}
