package com.badlogic.gdx.json.annotations;

import java.lang.annotation.*;

/**
 * Field annotation to mark members of a {@link JsonSerializable} class as subject to serialization.
 *
 * If the optional "name" property is not set, the identifier of the field is used to name the JSON object.
 *
 * The {@link JsonArray} property must be set to add type information to serializable fields of type
 * {@link com.badlogic.gdx.utils.Array}.
 *
 * The {@link JsonMap} property must be set to add type information for {@link java.util.Map} containers.
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface JsonSerialize {
	String name() default "";
	JsonArray[] array() default {};
	JsonMap[] map() default {};
}
