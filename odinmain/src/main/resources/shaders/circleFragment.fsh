#version 120

uniform vec2 u_circleCenter;
uniform float u_circleRadius;
uniform vec4 u_colorCircle;
uniform vec4 u_colorBorder;
uniform float u_borderThickness;

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
    vec4 color = circle(f_Position, u_circleCenter, u_circleRadius, u_colorBorder); // makes an entire circle in border color
    vec4 colorMixedWithBorder = mix(color, u_colorCircle, step(distance(f_Position, u_circleCenter), u_circleRadius - u_borderThickness)); // mixes circle color with border color based on distance from middle

    gl_FragColor = mix(vec4(0.0), colorMixedWithBorder, color.a);
}