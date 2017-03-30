package com.badlogic.gdx.scenes.scene2d.ui;

import com.badlogic.gdx.json.annotations.JsonSerialize;
import com.badlogic.gdx.scenes.scene2d.Actor;

import java.util.function.Consumer;
import java.util.function.Function;

@SuppressWarnings("unused")
public class ImageLayout extends ActorLayout<Image> {

	@JsonSerialize
	public String drawable;

	@Override
	protected Image createActor(Skin skin,
								String styleName,
								Function<String, ActorLayout<?>> layouts,
								Consumer<Actor> registry,
								StageLayoutListener listener) {
		
		return new Image(skin, drawable);
	}

}
