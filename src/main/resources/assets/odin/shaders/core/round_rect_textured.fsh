#version 330
#extension GL_ARB_separate_shader_objects : require

#include <minecraft:dynamictransforms.glsl>
#include <odin:round_rect.glsl>

uniform sampler2D Sampler0;

layout(location = 0) out vec4 fragColor;

void main() {
    float coverage = shapeCoverage();
    if (coverage <= 0.0) discard;

    vec4 color = texture(Sampler0, shapeUV()) * vertexColor * ColorModulator;

    fragColor = vec4(color.rgb, color.a * coverage);
}