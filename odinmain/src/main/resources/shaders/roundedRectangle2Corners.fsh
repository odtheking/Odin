#version 130

uniform float u_Radius;
uniform vec4 u_InnerRect;
uniform int u_CornerID;

varying vec2 f_Position;

void main() {
    vec2 tl = u_InnerRect.xy - f_Position;
    vec2 br = f_Position - u_InnerRect.zw;
    vec2 dis = max(br, tl);

    vec2 centerPos = vec2(u_InnerRect.x + (u_InnerRect.z - u_InnerRect.x) / 2f, u_InnerRect.y + (u_InnerRect.w - u_InnerRect.y) / 2f);
    float a = 1.0;

    if (
        (f_Position.x < centerPos.x && u_CornerID == 1) ||
        (f_Position.y < centerPos.y && u_CornerID == 2) ||
        (f_Position.x > centerPos.x && u_CornerID == 3) ||
        (f_Position.y > centerPos.y && u_CornerID == 4)
    ) {
        float v = length(max(vec2(0.0), dis)) - u_Radius;
        a -= smoothstep(0.0, 3.0, v);
    }

    gl_FragColor = gl_Color * vec4(1.0, 1.0, 1.0, a);
}
