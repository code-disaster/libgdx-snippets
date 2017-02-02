package com.badlogic.gdx.json;

/**
 * Optional interface to customize JSON serialization. Classes annotated with
 * {@link com.badlogic.gdx.json.annotations.JsonSerializable} can implement this
 * interface to hook into the serialization process.
 */
public interface AnnotatedJsonObject {

	/**
	 * This function is called <i>before</i> writing the annotated object.
	 */
	void onJsonWrite();

	/**
	 * This function is called <i>after</i> reading the annotated object.
	 */
	void onJsonRead();

}
