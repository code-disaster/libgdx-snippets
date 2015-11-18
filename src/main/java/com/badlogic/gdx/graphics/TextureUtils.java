package com.badlogic.gdx.graphics;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Array;

import java.util.function.Consumer;

public class TextureUtils {

	/**
	 * Calls consumer on each texture currently registered as managed texture.
	 */
	public static void forEachManagedTexture(Consumer<Texture> consumer) {
		Array<Texture> textures = Texture.managedTextures.get(Gdx.app);
		for (int i = textures.size - 1; i >= 0; i--) {
			consumer.accept(textures.get(i));
		}
	}

	/**
	 * Disposes all textures still registered as managed textures.
	 */
	public static void disposeAllManagedTextures() {
		// works because forEachManagedTexture() iterates in reverse order
		forEachManagedTexture(Texture::dispose);
	}

}
