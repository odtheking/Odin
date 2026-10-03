#version 330

#moj_import <minecraft:dynamictransforms.glsl>
#moj_import <minecraft:projection.glsl>

in vec3 Position;
in vec4 Color;
in vec2 UV0;
in ivec2 UV1;
in ivec2 UV2;

in float LineWidth;

out vec4 vertexColor;
out vec2 localPos;

flat out vec2 halfSize;
flat out vec4 radii;
flat out float edgeWidth;

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