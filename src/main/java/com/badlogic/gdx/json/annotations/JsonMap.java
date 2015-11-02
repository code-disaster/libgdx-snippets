package com.badlogic.gdx.json.annotations;

import java.lang.annotation.*;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface JsonMap {
	Class<?> map();
	Class<?> key();
	Class<?> value();
}
