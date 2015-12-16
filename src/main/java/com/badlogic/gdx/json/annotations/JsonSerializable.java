package com.badlogic.gdx.json.annotations;

import java.lang.annotation.*;

/**
 * Class annotation used to prepare classes for use with
 * {@link com.badlogic.gdx.json.AnnotatedJsonSerializer}.
 */
@Inherited
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface JsonSerializable {

	/**
	 * If dynamic == true, {@link com.badlogic.gdx.json.AnnotatedJsonSerializer} writes additional
	 * type information for each instance of the annotated class. This type information is then used
	 * during deserialization to reconstruct the correct (derived) instance type.
	 */
	boolean dynamic() default false;

	/**
	 * If encodeFP == true, floats and doubles are written to (and parsed from) JSON as special format strings.
	 * @see com.badlogic.gdx.json.JsonFloatSerializer
	 */
	boolean encodeFP() default false;
}
