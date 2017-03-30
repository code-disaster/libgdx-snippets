package com.badlogic.gdx.scenes.scene2d.ui;

import com.badlogic.gdx.json.annotations.JsonSerialize;
import com.badlogic.gdx.scenes.scene2d.Actor;

import java.util.function.Consumer;
import java.util.function.Function;

@SuppressWarnings("unused")
public class TextButtonLayout extends ActorLayout<TextButton> {

	@JsonSerialize
	public String label;

	@Override
	protected TextButton createActor(Skin skin,
									 String styleName,
									 Function<String, ActorLayout<?>> layouts,
									 Consumer<Actor> registry,
									 StageLayoutListener listener) {

		return new TextButton(label, skin, styleName);
	}

}
