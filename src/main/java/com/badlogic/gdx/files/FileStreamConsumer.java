package com.badlogic.gdx.files;

import java.io.IOException;

@FunctionalInterface
public interface FileStreamConsumer {

	void read(byte[] bytes, int length) throws IOException;

}
