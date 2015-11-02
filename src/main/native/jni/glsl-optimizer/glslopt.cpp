#include "glslopt.h"

#include <glsl_optimizer.h>

namespace glslopt
{

    glslopt_ctx* initializeContext(int32_t target)
    {
        return glslopt_initialize((glslopt_target) target);
    }

    glslopt_shader* optimizeShader(glslopt_ctx* ctx, int32_t type,
        const char* source, uint32_t options)
    {
        return glslopt_optimize(ctx, (glslopt_shader_type) type, source, options);
    }

    bool getShaderStatus(glslopt_shader* shader)
    {
        return glslopt_get_status(shader);
    }

    const char* getShaderOutput(glslopt_shader* shader)
    {
        return glslopt_get_output(shader);
    }

    const char* getShaderLog(glslopt_shader* shader)
    {
        return glslopt_get_log(shader);
    }

    void deleteShader(glslopt_shader* shader)
    {
        glslopt_shader_delete(shader);
    }

    void cleanupOptimizerContext(glslopt_ctx* ctx)
    {
        glslopt_cleanup(ctx);
    }

}
