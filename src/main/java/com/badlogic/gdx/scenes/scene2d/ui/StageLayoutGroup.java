package com.badlogic.gdx.scenes.scene2d.ui;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.Array;

import java.util.function.Consumer;

public class StageLayoutGroup {

	private final Array<Actor> actors = new Array<>();

	public StageLayoutGroup(Actor... actors) {
		this.actors.addAll(actors);
	}

	public void forEach(Consumer<Actor> consumer) {
		this.actors.forEach(consumer);
	}

}
