#version 330
#extension GL_ARB_separate_shader_objects : require

#include <minecraft:dynamictransforms.glsl>
#include <minecraft:projection.glsl>

layout(location = 0) in vec3 Position;
layout(location = 1) in vec4 Color;
layout(location = 2) in vec2 UV0;
layout(location = 3) in ivec2 UV1;
layout(location = 4) in ivec2 UV2;

layout(location = 5) in float LineWidth;

layout(location = 0) out vec4 vertexColor;
layout(location = 1) out vec2 localPos;

layout(location = 2) flat out vec2 halfSize;
layout(location = 3) flat out vec4 radii;
layout(location = 4) flat out float edgeWidth;

void main() {
    gl_Position = ProjMat * ModelViewMat * vec4(Position, 1.0);

    vertexColor = Color;
    localPos = UV0;
    halfSize = vec2(UV1) / 8.0;
    radii = vec4(
        float(UV2.x & 0xFF), // top left
        float((UV2.x >> 8) & 0xFF), // top right
        float(UV2.y  & 0xFF), // bottom right
        float((UV2.y >> 8) & 0xFF)  // bottom left
    ) / 4.0;
    edgeWidth = LineWidth;
}