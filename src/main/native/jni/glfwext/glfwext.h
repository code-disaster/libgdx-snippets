#pragma once

#ifdef __cplusplus
extern "C" {
#endif

#include <stdbool.h>

#if defined(__APPLE__)

  #include <ApplicationServices/ApplicationServices.h>
  #if defined(__OBJC__)
    #import <Cocoa/Cocoa.h>
  #else
    typedef void* id;
  #endif

  extern bool glfwext_set_fullscreen_macos(id window);

#endif

#ifdef __cplusplus
}
#endif
