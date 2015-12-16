package com.badlogic.gdx.utils;

import java.lang.annotation.*;

/**
 * Class annotation to mark classes as candidates for {@link AutoDisposer}.
 * @see AutoDisposer
 */
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface AutoDisposable {
}
