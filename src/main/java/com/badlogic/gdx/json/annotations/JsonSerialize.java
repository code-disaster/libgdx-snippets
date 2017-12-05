package com.badlogic.gdx.json.annotations;

import java.lang.annotation.*;

/**
 * Field annotation to mark members of a {@link JsonSerializable} class as subject to serialization.
 * <p>
 * If the optional "name" property is not set, the identifier of the field is used to name the JSON object.
 * <p>
 * The {@link JsonArray} property must be set to add type information to serializable fields of type
 * {@link com.badlogic.gdx.utils.Array}.
 * <p>
 * The {@link JsonMap} property must be set to add type information for {@link java.util.Map} containers.
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface JsonSerialize {

	String name() default "";

	JsonArray[] array() default {};

	JsonMap[] map() default {};

	/**
	 * If the JSON value is 'null', or not present at all, an instance is still
	 * created by default if the annotated field is serializable.
	 * <p>
	 * Set this to property to false to prevent that.
	 */
	boolean createIfNull() default true;

	boolean writeIfDefaultValue() default true;

	int defaultIntValue() default 0;

	float defaultFloatValue() default 0.0f;

	double defaultDoubleValue() default 0.0;
	
}
