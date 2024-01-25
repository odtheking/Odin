#version 130

uniform vec2 u_circleCenter;
uniform float u_circleRadius;
uniform vec4 u_colorCircle;

varying vec2 f_Position;

/**
 * Draw a circle at vec2 `pos` with radius `rad` and
 * color `color`.
 */
vec4 circle(vec2 frag, vec2 pos, float rad, vec4 color) {
    float d = length(pos - frag) - rad;
    float t = clamp(d, 0.0, 1.0);
    return vec4(color.rgb, min(color.a, 1.0 - t));
}

void main() {
    vec4 color = circle(f_Position, u_circleCenter, u_circleRadius, u_colorCircle);

    gl_FragColor = mix(vec4(0.0), color, color.a);
}