package com.badlogic.gdx.json.annotations;

import com.badlogic.gdx.utils.Array;

import java.lang.annotation.*;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface JsonArray {

	/**
	 * Used to construct the correct container class which must be derived from {@link Array}.
	 */
	Class<?> array() default Array.class;

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
