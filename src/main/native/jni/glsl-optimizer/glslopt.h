#pragma once

struct glslopt_ctx;
struct glslopt_shader;

namespace glslopt
{

    glslopt_ctx* initializeContext();
    glslopt_shader* optimizeShader(glslopt_ctx* ctx, int type, const char* source);
    bool getShaderStatus(glslopt_shader* shader);
    const char* getShaderOutput(glslopt_shader* shader);
    const char* getShaderLog(glslopt_shader* shader);
    void deleteShader(glslopt_shader* shader);
    void cleanupOptimizerContext(glslopt_ctx* ctx);

}
