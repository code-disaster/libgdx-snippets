package com.badlogic.gdx.scenes.scene2d.ui;

import com.badlogic.gdx.json.annotations.JsonSerialize;
import com.badlogic.gdx.scenes.scene2d.Actor;

import java.util.function.Consumer;
import java.util.function.Function;

@SuppressWarnings("unused")
public class LabelLayout extends ActorLayout<Label> {

	@JsonSerialize
	public String label;

	@Override
	protected Label createActor(Skin skin,
								String styleName,
								Function<String, ActorLayout<?>> layouts,
								Consumer<Actor> registry,
								StageLayoutListener listener) {

		return new Label(label, skin, styleName);
	}
	
}
