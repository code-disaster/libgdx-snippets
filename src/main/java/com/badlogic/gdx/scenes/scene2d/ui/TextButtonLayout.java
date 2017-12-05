package com.badlogic.gdx.scenes.scene2d.ui;

import com.badlogic.gdx.json.annotations.JsonSerialize;

@SuppressWarnings("unused")
public class TextButtonLayout extends ActorLayout<TextButton> {

	@JsonSerialize
	public String label;

	public TextButtonLayout() {
		super(TextButton.class);
	}

	@Override
	protected TextButton createActor(Skin skin, StageLayoutListener listener) {
		return new TextButton(label, skin, style);
	}

}
