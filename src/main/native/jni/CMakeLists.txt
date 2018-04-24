#
# project: gdx-snippets-native
#
cmake_minimum_required(VERSION 2.8)

# include the fips main cmake file
get_filename_component(FIPS_ROOT_DIR "../fips" ABSOLUTE)
include("${FIPS_ROOT_DIR}/cmake/fips.cmake")

set(CMAKE_OSX_DEPLOYMENT_TARGET "10.7")

fips_setup()
fips_project(gdx-snippets-native)

if (FIPS_OSX)
    fips_begin_lib(gdx-snippets-osx)

        set(CMAKE_XCODE_ATTRIBUTE_CLANG_ENABLE_OBJC_ARC "YES")
        set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -fobjc-arc")

        fips_dir(glfwext)
        fips_files(
            glfwext.h
            glfwext.m
        )
        
    fips_end_lib()
endif()

fips_begin_sharedlib(gdx-snippets-native)

    fips_setup_jni()

    fips_deps(glsl_optimizer)

    fips_include_directories(
        flextgl
        generated
        glfwext
        glsl-optimizer
    )

    fips_dir(flextgl)
    fips_files(
        flextGL.h flextGL.c
    )

    fips_dir(generated)
    fips_generate(FROM gdx-snippets-jni.yml TYPE JNICodeGenerator SOURCE gdx-snippets-jni.cpp)

    fips_dir(glfwext)
    fips_files(
        glfwext.h
    )

    fips_dir(glsl-optimizer)
    fips_files(
        glslopt.h glslopt.cpp
    )

    fips_libs(Remotery)

    if (FIPS_WINDOWS)
        fips_libs(OpenGL32)
    endif()

    if (FIPS_OSX)
        fips_deps(gdx-snippets-osx)
        fips_frameworks_osx(OpenGL)
    endif()

fips_end_sharedlib()

fips_finish()
