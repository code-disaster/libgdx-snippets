# libGDX snippets

A collection of code snippets for the lazy [libGDX](https://github.com/libgdx/libgdx) user.

## About

#### Aim

This library is not a framework, nor an engine. It is just a collection of Java classes which might prove worth (re-)using in a **libGDX desktop project**.

#### Update policy

You may notice that commits are done at rather strange intervals. This code is extended and improved in conjunction with our current game development project at [Robotality](http://robotality.com). I found Git submodules very cumbersome to work with, esp. when working in a team with artists and designers. That said, the *main* branch of this library is actually embedded in our game project, using this GitHub repository as a backup/mirror.

This means that changes done are pretty much instantly tested in our production environment, but commits to this public repository are often lagging behind, and are done in batches.

> This also means that there can be *API breaking* changes at any time!

## Outline

- Aimed for desktop projects, so the code won't run on HTML5 and mobile.
- Written/compiled with JDK 8, and uses Java 1.8 language features. Be advised you may encounter heavy (ab)use of lambdas.
- Invades the ```com.badlogic.gdx.*``` namespace.
- Embeds a small native library for some utility functions.

## Highlights

- [AnnotatedJson](https://github.com/code-disaster/libgdx-snippets/blob/master/src/main/java/com/badlogic/gdx/json/AnnotatedJson.java): custom JSON serializer, based on libGDX' Json classes, which allows annotation-driven serialization of object hierarchies.
- [AutoDisposer](https://github.com/code-disaster/libgdx-snippets/blob/master/src/main/java/com/badlogic/gdx/utils/AutoDisposer.java): annotation-driven, semi-automatic disposal of disposable object hierarchies.
- [GL33Ext](https://github.com/code-disaster/libgdx-snippets/blob/master/src/main/java/com/badlogic/gdx/graphics/GL33Ext.java): native interface to [flextGL](https://github.com/code-disaster/flextGL.git) to expose desktop OpenGL functions not made available by libGDX/LWJGL.
- [GLSLOptimizer](https://github.com/code-disaster/libgdx-snippets/blob/master/src/main/java/com/badlogic/gdx/graphics/GLSLOptimizer.java): native interface to [glsl-optimizer](https://github.com/aras-p/glsl-optimizer.git).
- [MultiTargetFrameBuffer](https://github.com/code-disaster/libgdx-snippets/blob/master/src/main/java/com/badlogic/gdx/graphics/glutils/MultiTargetFrameBuffer.java): custom GLFrameBuffer implementation for creating multi-render targets, e.g. usable for deferred rendering (G-buffers).
- [Remotery](https://github.com/code-disaster/libgdx-snippets/blob/master/src/main/java/com/badlogic/gdx/profiler/Remotery.java): native interface to [Remotery](https://github.com/Celtoys/Remotery), a realtime profiler with web browser viewer.

## Usage

To use the native interfaces, you just need to add a call to ```GdxSnippetsNativesLoader.load()```, for example in your create() function.

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

This library uses the [fips](http://floooh.github.io/fips/) cmake build wrapper to compile the native source code. Please read the [list of requirements](http://floooh.github.io/fips/getstarted.html) to run fips (in short: Python 2.7.9, cmake 2.8.11+, and an appropriate C++ compiler environment). In addition, Maven and Java are required for the [fips-jni](https://github.com/code-disaster/fips-jni) module.

The steps below should work on every target system. You only need to specify a different build target.

**The root folder for the native code is located in ```[libgdx-snippets]/src/main/native/jni/```.**

```shell
# navigate to the native source folder
> cd [libgdx-snippets]/src/main/native/jni/
```

**To install fips plus dependencies (one-time):**

```shell
# this will install fips
> ./fips

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
