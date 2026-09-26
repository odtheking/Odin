#version 330

#moj_import <minecraft:dynamictransforms.glsl>
#moj_import <odin:round_rect.glsl>

out vec4 fragColor;

void main() {
    float coverage = shadowCoverage();
    if (coverage <= 0.0) discard;

    vec4 color = vertexColor * ColorModulator;

    fragColor = vec4(color.rgb, color.a * coverage);
}