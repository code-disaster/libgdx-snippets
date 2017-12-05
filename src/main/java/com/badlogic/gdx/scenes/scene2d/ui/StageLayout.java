package com.badlogic.gdx.scenes.scene2d.ui;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.json.AnnotatedJson;
import com.badlogic.gdx.json.annotations.*;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.*;

import java.io.IOException;

public class StageLayout {

	private final Stage stage;
	private final StageLayout.JsonData json;
	private final StageLayoutListener listener;

	private Array<Actor> rootActors = new Array<>();

	public StageLayout(Stage stage, FileHandle layoutPath, StageLayoutListener listener) throws IOException {
		this.stage = stage;
		this.json = readJson(layoutPath);
		this.listener = listener;
	}

	public void create(Skin skin) {
		rootActors.clear();
		for (ActorLayout<?> layout : json.actors) {
			Actor actor = create(skin, layout, layout.actorClass);
			stage.addActor(actor);
		}
	}

	private <T extends Actor> T create(Skin skin, ActorLayout layout, Class<T> clazz) {
		Actor actor = layout.create(skin, listener);

		if (!clazz.isAssignableFrom(actor.getClass())) {
			throw new GdxRuntimeException("Type mismatch for actor layout: " + layout.name);
		}

		rootActors.add(actor);

		return (T) actor;
	}

	public <T extends Actor> T get(String name, Class<T> clazz) {
		return get(rootActors, name, clazz);
	}

	private <T extends Actor> T get(Array<Actor> actors, String name, Class<T> clazz) {
		for (Actor actor : actors) {
			if(actor.getName() == null)
				continue;

			if (actor.getName().equals(name)) {
				if (!clazz.isAssignableFrom(actor.getClass())) {
					throw new GdxRuntimeException("Type mismatch for actor layout: " + name);
				}

				return (T) actor;
			}

			if (actor instanceof LayoutGroup) {
				LayoutGroup group = (LayoutGroup) actor;

				T foundActor = get(group.getChildren(), name, clazz);
				if (foundActor != null)
					return foundActor;
			}
		}

		return null;
	}

	public Array<Actor> getRootActors() {
		return rootActors;
	}

	public void resizeAll() {
		for (Actor actor : rootActors) {
			resize(actor, true);
		}
	}

	public void resize(Actor actor, boolean root) {
		if(actor.getName() == null)
			return;

		// Resize this actor layout
		ActorLayout<?> actorLayout = getLayoutByName(json.actors, actor.getName());

		if (actorLayout == null) {
			throw new GdxRuntimeException("No actor layout found for: " + actor.getName());
		}

		actorLayout.layout.resize(stage, actor, root);

		// If it happens to be a group, also resize any children
		if (actor instanceof LayoutGroup) {
			LayoutGroup groupActor = (LayoutGroup) actor;

			SnapshotArray<Actor> children = groupActor.getChildren();

			for (int i = 0; i < children.size; i++) {
				resize(children.get(i), false);
			}
		}
	}

	public ActorLayout<?> getLayoutByName(String name) {
		return getLayoutByName(json.actors, name);
	}

	private ActorLayout<?> getLayoutByName(Array<ActorLayout<?>> layouts, String name) {
		if (name == null)
			return null;

		for (ActorLayout layout : layouts) {
			if (name.equals(layout.name)) {
				return layout;
			}

			if (layout instanceof GroupLayout) {
				GroupLayout groupLayout = (GroupLayout) layout;

				ActorLayout actorLayout = getLayoutByName(groupLayout.actors, name);
				if (actorLayout != null)
					return actorLayout;
			}
		}

		return null;
	}

	@JsonSerializable
	@SuppressWarnings("WeakerAccess")
	public static class JsonData {

		@JsonSerialize(array = @JsonArray(value = ActorLayout.class))
		public Array<ActorLayout<?>> actors;
	}

	private static final Json reader = AnnotatedJson.newReader(JsonData.class, StageLayout::setupJson);

	private static void setupJson(Json json) {
		AnnotatedJson.registerSubclasses(json, ActorLayout.class,
				className -> className.startsWith("com.badlogic.gdx.scenes.scene2d.ui"));
	}

	private static JsonData readJson(FileHandle path) throws IOException {
		return AnnotatedJson.read(path, JsonData.class, reader);
	}
}
