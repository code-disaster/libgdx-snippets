package com.badlogic.gdx.utils;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Field annotation for disposable members of {@link AutoDisposable} classes.
 * @see AutoDisposer
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface AutoDispose {
}
