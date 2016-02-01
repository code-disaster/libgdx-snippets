package com.badlogic.gdx.utils;

import com.badlogic.gdx.utils.reflect.ClassReflection;
import com.badlogic.gdx.utils.reflect.Field;
import com.badlogic.gdx.utils.reflect.ReflectionException;

import java.lang.reflect.Array;

/**
 * A helper class to ease management of disposable resources. Designed to work complementary to, and to be mixed
 * with, {@link Disposable}.
 * <p>
 * {@link AutoDisposer#dispose(Object)} should be called on the root object of an auto-disposable hierarchy. This
 * will walk all non-static members of that object which are annotated as {@link AutoDispose}. This works with
 * plain arrays, too.
 * <p>
 * On these members, the {@link Disposable#dispose()} method is called, if implemented.
 * <p>
 * If the type of a member is annotated as {@link AutoDisposable}, the scan is continued recursively on this member
 * object. This way hierarchies of disposable objects can be created without the need (but the option) to implement
 * any {@link Disposable} interfaces.
 */
public class AutoDisposer {

	public static void dispose(Object object) {

		// allows to be called on null objects

		if(object == null) {
			return;
		}

		Class<?> classType = object.getClass();

		Field[] fields = ClassReflection.getDeclaredFields(classType);

		for (Field field : fields) {

			try {

				// fields as candidates:
				// - non-static
				// - with @AutoDispose if class has AutoDisposable.Members.ANNOTATED flag (default)

				if (field.isStatic() || field.isSynthetic()) {
					continue;
				}

				if (!field.isAnnotationPresent(AutoDispose.class)) {
					continue;
				}

				// instance and class type of field

				boolean accessible = field.isAccessible();

				if (!accessible) {
					field.setAccessible(true);
				}

				Object fieldObject = field.get(object);
				Class<?> fieldType = field.getType();

				// set the field to 0
				field.set(object, null);

				if (!accessible) {
					field.setAccessible(false);
				}

				if (fieldType.isArray()) {
					disposeArray(fieldObject);
				} else {
					dispose(fieldObject, fieldType);
				}

			} catch (ReflectionException e) {
				throw new GdxRuntimeException(e);
			}
		}

	}

	private static void dispose(Object object, Class<?> type) {

		// null check

		if(object == null) {
			return;
		}

		// if the field implements Disposable, call dispose() on it

		if (ClassReflection.isAssignableFrom(Disposable.class, type)) {
			((Disposable) object).dispose();
		}

		// call recursively if this field is @AutoDisposable itself

		if (ClassReflection.isAnnotationPresent(type, AutoDisposable.class)) {
			dispose(object);
		}

	}

	private static void disposeArray(Object object) {

		int length = Array.getLength(object);

		for (int i = 0; i < length; i++) {

			Object element = Array.get(object, i);
			Class<?> elementType = element.getClass();

			if (elementType.isArray()) {
				disposeArray(element);
			} else {
				dispose(element, elementType);
			}

		}

	}

}
