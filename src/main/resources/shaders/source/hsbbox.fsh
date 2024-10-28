#version 120

uniform vec2 u_rectCenter; // center of rectangle, (x, y)
uniform vec2 u_rectSize; // size of rectangle, (width, height)
uniform vec4 u_colorRect; // Color we want in the top right corner, this color will be blended towards white and black to generate a "HSB box".

varying vec2 f_Position;

void main() {
    vec2 uv = (f_Position - u_rectCenter) / u_rectSize; // gets a vector (x, y) where based on distance from middle, x ∈ [−0.5,0.5], y ∈ [−0.5,0.5]
    vec4 topColor = mix(vec4(1.0), u_colorRect, uv.x + 0.5); // Makes a gradient between white and the wanted color, based on distance from center on the x-axis.
    vec4 finalColor = mix(topColor, vec4(0.0, 0.0, 0.0, 1.0), uv.y + 0.5); // Makes gradient between the above gradient and black, based on distance from center on the y-axis.

    gl_FragColor = finalColor;
}