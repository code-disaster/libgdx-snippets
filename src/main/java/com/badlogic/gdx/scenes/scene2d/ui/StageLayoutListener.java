package com.badlogic.gdx.scenes.scene2d.ui;

public interface StageLayoutListener {

	default void onButtonDown(int layoutId) {

	}

	default void onButtonUp(int layoutId) {

	}

	/**
	 * Called on mouseMoved() events when mouse pointer is hovering this button.
	 */
	default void onMouseOver(int layoutId) {

	}

	/**
	 * Used by {@link LabelLayout} to localize the label text.
	 */
	default String getTranslation(String text) {
		return text;
	}

}
