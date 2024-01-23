#version 130

uniform float u_Radius;
uniform vec4 u_InnerRect;
uniform float u_OutlineThickness; // New uniform for outline thickness

varying vec2 f_Position;

float roundedBoxSDF(vec2 CenterPosition, vec2 Size, float Radius) {
    vec2 q = abs(CenterPosition) - Size + Radius;
    return min(max(q.x, q.y), 0.0) + length(max(q, 0.0)) - Radius;
}

void main() {
    vec2 tl = u_InnerRect.xy - f_Position;
    vec2 br = f_Position - u_InnerRect.zw;
    vec2 dis = max(br, tl);
    vec2 halfSize = (u_InnerRect.zw - u_InnerRect.xy) / 2f;
    vec2 rectCenter = u_InnerRect.xy + halfSize;

    float v = length(max(vec2(0.0), dis)) - u_Radius;
    float a = 1.0 - smoothstep(0.0, 1.0, v);

    if (abs(roundedBoxSDF(f_Position - rectCenter, halfSize / 2f, u_Radius)) > u_OutlineThickness) {
        a = 0.0;
    }


    gl_FragColor = gl_Color * vec4(1.0, 1.0, 1.0, a);
}