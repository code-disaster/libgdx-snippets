package com.badlogic.gdx.scenes.scene2d.ui;

import com.badlogic.gdx.json.annotations.JsonArray;
import com.badlogic.gdx.json.annotations.JsonSerialize;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.utils.Array;

public class GroupLayout extends ActorLayout<LayoutGroup> {

	@JsonSerialize(array = @JsonArray(value = ActorLayout.class))
	public Array<ActorLayout<?>> actors;

	@JsonSerialize
	public String sizeToActor = null;

	public GroupLayout() {
		super(LayoutGroup.class);
	}

	@Override
	protected Actor create(Skin skin, StageLayoutListener listener) {
		Group group = (Group)super.create(skin, listener);

		for(int i=0; i<actors.size; i++){
			ActorLayout layout = actors.get(i);

			group.addActor(layout.create(skin, listener));
		}

		if(sizeToActor != null){
			Actor actor = group.findActor(sizeToActor);
			if(actor != null){
				group.setWidth(actor.getWidth());
				group.setHeight(actor.getHeight());
			}
		}
		else {
			if(layout.width > 0)
				group.setWidth(layout.width);
			if(layout.height > 0)
				group.setHeight(layout.height);
		}

		return group;
	}

	@Override
	protected LayoutGroup createActor(Skin skin, StageLayoutListener listener) {
		return new LayoutGroup();
	}

}
