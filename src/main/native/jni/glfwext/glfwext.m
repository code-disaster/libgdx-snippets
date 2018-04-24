#include "glfwext.h"

bool glfwext_set_fullscreen_macos(id window)
{
#if 0
    bool fullscreen = ([window styleMask] & NSWindowStyleMaskFullScreen) != 0;

    if (!fullscreen)
    {
        [window toggleFullScreen:nil];
    }

    return !fullscreen;
#else
    [window toggleFullScreen:nil];
    return true;
#endif
}
