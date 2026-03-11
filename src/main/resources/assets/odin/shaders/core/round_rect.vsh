#version 150

#moj_import <minecraft:dynamictransforms.glsl>
#moj_import <minecraft:projection.glsl>

in vec3 Position;
in vec4 Color;

out vec4 f_Color;
out vec2 f_Position;

void main() {
    f_Color = Color;
    f_Position = Position.xy;

    gl_Position = ProjMat * ModelViewMat * vec4(Position, 1.0);
}