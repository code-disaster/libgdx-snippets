package com.badlogic.gdx.scenes.scene2d.ui;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.json.AnnotatedJson;
import com.badlogic.gdx.json.annotations.*;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.utils.Layout;
import com.badlogic.gdx.utils.*;

import java.io.IOException;

public class StageLayout {

	private final Stage stage;
	private final JsonData json;
	private final StageLayoutListener listener;
	private final ObjectMap<String, Actor> created = new ObjectMap<>();
	private final Array<Container<?>> containers = new Array<>();

	public StageLayout(Stage stage, FileHandle layoutPath, StageLayoutListener listener) throws IOException {
		this.stage = stage;
		this.json = readJson(layoutPath);
		this.listener = listener;
	}

	@SuppressWarnings("unchecked")
	public <T extends Actor> Container<T> create(Skin skin, String name, Class<T> clazz) {

		Container<?> container = create(skin, name, (Container<?>) null);

		Actor actor = container.getActor();
		if (!clazz.isAssignableFrom(actor.getClass())) {
			throw new GdxRuntimeException("type mismatch for actor layout: " + name);
		}

		return (Container<T>) container;
	}

	private Container<?> create(Skin skin, String name, Container<?> envelope) {

		ActorLayout<?> layout = getByName(name);

		if (layout == null) {
			throw new GdxRuntimeException("no actor layout found for: " + name);
		}

		Container<?> container = layout.create(skin, envelope, this::getByName,
				created -> this.created.put(created.getName(), created), listener);

		// use bottom-left origin (libGDX default is 'center')

		container.align(Align.bottomLeft);
		container.setOrigin(Align.bottomLeft);

		// keep a list of containers created

		containers.add(container);

		// create embedded actors

		for (String childName : layout.group) {
			create(skin, childName, container);
		}

		return container;
	}

	@SuppressWarnings("unchecked")
	public <T extends Actor> T get(String name, Class<T> clazz) {

		Actor actor = created.get(name);

		if (clazz.isAssignableFrom(actor.getClass())) {
			return (T) actor;
		}

		throw new GdxRuntimeException("type mismatch for actor: " + name);
	}

	@SuppressWarnings("unchecked")
	public <T extends Actor> Container<T> getContainer(T actor) {

		Group container = actor.getParent();

		if (Container.class.isAssignableFrom(container.getClass())) {
			return (Container<T>) container;
		}

		throw new GdxRuntimeException("type mismatch for container of actor: " + actor.getName());
	}

	@SuppressWarnings("unchecked")
	public <T extends Actor> Container<T> getContainer(String name, Class<T> clazz) {
		T actor = get(name, clazz);
		return getContainer(actor);
	}

	/**
	 * If <i>actor</i> is embedded inside a layout container, add the container.
	 * Otherwise, add the actor directly.
	 */
	public <T extends Actor> void addToStage(Stage stage, T actor) {

		Group container = actor.getParent();

		if (container != null && (container instanceof StageLayoutContainer)) {
			stage.addActor(container);
		} else {
			stage.addActor(actor);
		}
	}

	public <T extends Actor> void removeFromStage(T actor) {

		Group container = actor.getParent();

		if (container != null && (container instanceof StageLayoutContainer)) {
			container.remove();
		} else {
			actor.remove();
		}
	}

	public void resizeAll() {
		for (Container<?> container : containers) {
			resize(container, true);
		}
	}

	private <T extends Actor> void resize(Container<T> container, boolean layout) {

		T actor = container.getActor();
		ActorLayout<?> actorLayout = getByName(actor.getName());

		if (actorLayout == null) {
			throw new GdxRuntimeException("no actor layout found for: " + actor.getName());
		}

		if (layout) {

			if (Layout.class.isAssignableFrom(actor.getClass())) {
				Layout l = Layout.class.cast(actor);
				container.size(l.getPrefWidth(), l.getPrefHeight());
			}

			container.layout();
		}

		actorLayout.layout.resize(stage, container);
	}

	private ActorLayout<?> getByName(String name) {

		if (name == null) {
			return null;
		}

		for (ActorLayout layout : json.actors) {
			if (name.equals(layout.name)) {
				return layout;
			}
		}

		return null;
	}
	
	@JsonSerializable
	public static class JsonData {
		@JsonSerialize(array = @JsonArray(value = ActorLayout.class))
		public Array<ActorLayout<?>> actors;
	}

	private static JsonData readJson(FileHandle path) throws IOException {
		return AnnotatedJson.read(path, JsonData.class, json -> {
			AnnotatedJson.registerSubclasses(json, ActorLayout.class,
					className -> className.startsWith("com.badlogic.gdx.scenes.scene2d.ui."));
		});
	}

}
