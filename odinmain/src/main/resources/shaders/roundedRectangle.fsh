#version 130

uniform vec4 u_Radius;
uniform vec2 u_Size;
uniform vec2 u_Location;
varying vec2 f_Position;

float roundedBoxSDF(vec2 CenterPosition, vec2 Size, vec4 Radius)
{
    Radius.xy = (CenterPosition.x > 0.0) ? Radius.xy : Radius.zw;
    Radius.x  = (CenterPosition.y > 0.0) ? Radius.x  : Radius.y;

    vec2 q = abs(CenterPosition) - Size + Radius.x;
    return min(max(q.x, q.y), 0.0) + length(max(q, 0.0)) - Radius.x;
}

void main() {
    float   u_edgeSoftness  = 5.0; // How soft the edges should be (in pixels). Higher values could be used to simulate a drop shadow.
    vec2    halfSize        = u_Size / 2f;
    vec2    middleOfRect    = u_Location + halfSize;
    float   distance        = roundedBoxSDF(f_Position.xy - middleOfRect, halfSize, u_Radius);
    float   smoothedAlpha   = 1.0 - smoothstep(0.0, u_edgeSoftness, distance);

    gl_FragColor = gl_Color * vec4(1.0, 1.0, 1.0, smoothedAlpha);
}