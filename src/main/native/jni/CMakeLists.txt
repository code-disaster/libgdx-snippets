#
# project: gdx-snippets-native
#
cmake_minimum_required(VERSION 2.8)

# include the fips main cmake file
get_filename_component(FIPS_ROOT_DIR "../fips" ABSOLUTE)
include("${FIPS_ROOT_DIR}/cmake/fips.cmake")

fips_setup()
fips_project(gdx-snippets-native)

fips_begin_sharedlib(gdx-snippets-native)

    fips_setup_jni()

    fips_include_directories(
        flextgl
        generated
        glsl-optimizer
        ../../../../deps/glsl-optimizer/src/glsl
    )

    fips_dir(flextgl)
    fips_files(
        flextGL.h flextGL.c
    )

    fips_dir(generated)
    fips_generate(FROM gdx-snippets-jni.yml TYPE JNICodeGenerator SOURCE gdx-snippets-jni.cpp)

    fips_dir(glsl-optimizer)
    fips_files(
        glslopt.h glslopt.cpp
    )

    if (FIPS_WINDOWS)
        if (FIPS_WIN64)
            link_directories(${FIPS_PROJECT_DIR}/glsl-optimizer/lib/windows/x64)
            fips_libs(glsl_optimizer_lib-x64)
        else()
            link_directories(${FIPS_PROJECT_DIR}/glsl-optimizer/lib/windows/win32)
            fips_libs(glsl_optimizer_lib-win32)
        endif()
        fips_libs(OpenGL32)
    endif()

    if (FIPS_OSX)
        link_directories(glsl-optimizer/lib/osx)
        fips_libs(glsl_optimizer)
        fips_frameworks_osx(OpenGL)
    endif()

fips_end_sharedlib()

fips_finish()
