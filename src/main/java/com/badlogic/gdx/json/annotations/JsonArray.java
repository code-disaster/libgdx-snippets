package com.badlogic.gdx.json.annotations;

import java.lang.annotation.*;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface JsonArray {

	/**
	 * Specifies class type of array elements.
	 */
	Class<?> value();

	/**
	 * Used as parameter during {@link com.badlogic.gdx.utils.Array#Array(boolean, int)}
	 * construction on deserialization.
	 */
	boolean ordered() default true;

}
