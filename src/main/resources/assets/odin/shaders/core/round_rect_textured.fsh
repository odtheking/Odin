#version 330

#moj_import <minecraft:dynamictransforms.glsl>
#moj_import <odin:round_rect.glsl>

uniform sampler2D Sampler0;

out vec4 fragColor;

void main() {
    float coverage = shapeCoverage();
    if (coverage <= 0.0) discard;

    vec4 color = texture(Sampler0, shapeUV()) * vertexColor * ColorModulator;

    fragColor = vec4(color.rgb, color.a * coverage);
}