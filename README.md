# libGDX lazy snippets

A collection of code snippets for the lazy [libGDX](https://github.com/libgdx/libgdx) user.

- Aimed for desktop projects, so the code won't run on HTML5 and mobile.
- Is written/compiled with JDK 8, and uses Java 1.8 language features.
- Invades the ```com.badlogic.gdx.*``` namespace.
- Embeds a small native library for some utility functions.
- No external dependencies (besides libGDX, of course).

## Usage

You just need to add a call to ```GdxSnippetsNativesLoader.load()```, for example in your create() function.

```java
public class MyGdxGameAdapter extends ApplicationAdapter {
    @Override
    public void create() {
        GdxSnippetsNativesLoader.load();
    }
}
```

## Building from source

### Java package

This is a Maven project. Just use ```mvn package``` or ```mvn install``` to create a snapshot.

### Native libraries

This library uses the [fips](http://floooh.github.io/fips/) cmake build wrapper to compile the native source code. Please read the [list of requirements](http://floooh.github.io/fips/getstarted.html) to run fips. The steps below should work on every target system. You only need to specify a different build target.

**The root folder for the native code is located in ```[libgdx-snippets]/src/main/native/jni/```.**

```shell
# navigate to the native source folder
> cd [libgdx-snippets]/src/main/native/jni/
```

**To install fips plus dependencies (one-time):**

```shell
# this will install fips
> ./fips
run 'fips help' for more info

# fetch dependencies
> ./fips fetch

# setup fips-jni
> ./fips jni setup
```

**To select the build target (one-time):**

```shell
# [optional] list all configs known to fips
> ./fips list configs

# e.g. for Windows 64 bit, using VS2013
> ./fips set config win64-vs2013-release

# or, for OS X, using XCode
> ./fips set config osx-xcode-release
```

**To compile the native library:**

```shell
# [optional] clean output
> ./fips clean all

# generate JNI code and build the library
> ./fips build
```

> Note: *fips-jni* uses *gdx-jnigen* to generate native code from C++ embedded in Java source files. *Gdx-jnigen* parses both ```.java``` and ```.class``` files. This means that, to compile the native library successfully, it is required to compile the Java code first, e.g. via ```mvn compile``` from the root folder, or from inside your favourite IDE.

**To copy the compiled runtime library:**

```shell
# e.g. for Windows 64 bit
> mvn install -Pwin64-vs2013

# or, for OS X
> mvn install -Posx-xcode
```

> Note: this copies the compiled runtime library to ```[libgdx-snippets]/src/main/resources```.