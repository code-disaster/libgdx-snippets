package com.badlogic.gdx.scenes.scene2d.ui;

import com.badlogic.gdx.checksum.CRC32;
import com.badlogic.gdx.json.AnnotatedJsonObject;
import com.badlogic.gdx.json.annotations.JsonSerializable;
import com.badlogic.gdx.json.annotations.JsonSerialize;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Touchable;

import java.util.function.Consumer;
import java.util.function.Function;

@JsonSerializable(dynamic = true)
public abstract class ActorLayout<T extends Actor> implements AnnotatedJsonObject {

	@JsonSerialize
	public String name;

	@JsonSerialize
	public String style = "default";

	@JsonSerialize
	public ContainerLayout layout;

	@JsonSerialize
	public String[] group = {};

	@Override
	public void onJsonWrite() {
		/* not implemented */
	}

	int nameId;

	@Override
	public void onJsonRead() {

		if (layout == null) {
			layout = new ContainerLayout();
		}

		nameId = CRC32.calculateString(name).hashCode();
	}

	protected final Container<T> create(Skin skin,
										Container<?> envelope,
										Function<String, ActorLayout<?>> layouts,
										Consumer<Actor> registry,
										StageLayoutListener listener) {

		// create actor, using layout data

		T actor = createActor(skin, style, layouts, registry, listener);

		// set name and save to registry for later lookups

		actor.setName(name);
		registry.accept(actor);

		// wrap in container

		return createContainer(actor, envelope);
	}

	private Container<T> createContainer(T actor, Container<?> envelope) {

		Container<T> container = new StageLayoutContainer<T>(actor, envelope);

		if (layout.width > 0) {
			container.width(layout.width);
		}

		if (layout.height > 0) {
			container.height(layout.height);
		}

		if (!layout.touchable) {
			container.setTouchable(Touchable.disabled);
		}

		return container;
	}

	protected abstract T createActor(Skin skin,
									 String styleName,
									 Function<String, ActorLayout<?>> layouts,
									 Consumer<Actor> registry,
									 StageLayoutListener listener);

}
