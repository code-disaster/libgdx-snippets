#pragma once

#include <stdint.h>

struct glslopt_ctx;
struct glslopt_shader;

namespace glslopt
{

    glslopt_ctx* initializeContext(int32_t target);
    glslopt_shader* optimizeShader(glslopt_ctx* ctx, int32_t type, const char* source, uint32_t options);
    bool getShaderStatus(glslopt_shader* shader);
    const char* getShaderOutput(glslopt_shader* shader);
    const char* getShaderLog(glslopt_shader* shader);
    void deleteShader(glslopt_shader* shader);
    void cleanupOptimizerContext(glslopt_ctx* ctx);

}
