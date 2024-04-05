#version 120

/*
Very simple vertex shader to use in various fragment shaders
*/

varying vec2 f_Position;

void main() {
    f_Position = gl_Vertex.xy;

    gl_Position = gl_ModelViewProjectionMatrix * gl_Vertex;
    gl_FrontColor = gl_Color;
}
