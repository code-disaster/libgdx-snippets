# Remotery: build with OpenGL support enabled
# FIXME Windows only for now, as OS X has build errors
if (FIPS_WINDOWS)
    add_definitions(-DRMT_USE_OPENGL=1)
endif()
