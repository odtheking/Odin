#version 130

uniform vec2 u_rectCenter;
uniform vec2 u_rectSize;
uniform vec4 u_colorRect;

varying vec2 f_Position;

void main() {
    vec2 uv = (f_Position - u_rectCenter) / u_rectSize;
    vec4 topColor = mix(vec4(1.0), u_colorRect, uv.x + 0.5);
    vec4 finalColor = mix(topColor, vec4(0.0, 0.0, 0.0, 1.0), uv.y + 0.5);

    gl_FragColor = finalColor;
}