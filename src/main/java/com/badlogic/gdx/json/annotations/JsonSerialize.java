package com.badlogic.gdx.json.annotations;

import java.lang.annotation.*;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface JsonSerialize {
	String name() default "";
	Class<?> clazz() default Class.class;
}
