package com.badlogic.gdx.scenes.scene2d.ui;


import com.badlogic.gdx.checksum.CRC32;
import com.badlogic.gdx.json.AnnotatedJsonObject;
import com.badlogic.gdx.json.annotations.JsonSerializable;
import com.badlogic.gdx.json.annotations.JsonSerialize;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.utils.GdxRuntimeException;

@JsonSerializable(dynamic = true)
public abstract class ActorLayout<T extends Actor> implements AnnotatedJsonObject {

	@JsonSerialize
	public String name;

	@JsonSerialize
	public String style = "default";

	@JsonSerialize
	public BaseLayout layout;

	@JsonSerialize
	public boolean visible = true;

	ActorLayout(Class<T> actorClass) {
		this.actorClass = actorClass;
	}

	@Override
	public void onJsonWrite() {
		/* not implemented */
	}

	public int nameId;
	Class<T> actorClass;

	@Override
	public void onJsonRead() {

		if (layout == null) {
			layout = new BaseLayout();
		}

		if (name == null) {
			throw new GdxRuntimeException("ActorLayout name is null. It needs a name field!");
		}

		nameId = CRC32.calculateString(name).hashCode();
	}

	protected Actor create(Skin skin, StageLayoutListener listener) {

		// create actor, using layout data
		T actor = createActor(skin, listener);

		// set name for later lookups
		actor.setName(name);

		actor.setTouchable(layout.touchable ? Touchable.enabled : Touchable.disabled);
		actor.setVisible(visible);

		return actor;
	}

	protected abstract T createActor(Skin skin, StageLayoutListener listener);

}