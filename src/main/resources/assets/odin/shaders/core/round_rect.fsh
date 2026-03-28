#version 150

layout(std140) uniform u {
    vec4 u_Rect;          // cx, cy, width, height
    vec4 u_Radii;         // topLeft, topRight, bottomRight, bottomLeft
    vec4 u_OutlineColor;  // rgba
    float u_OutlineWidth;
};

#define u_rectCenter u_Rect.xy
#define u_rectSize   u_Rect.zw

in vec2 f_Position;
in vec4 f_Color;
out vec4 fragColor;

float cornerRadius(vec2 p, vec4 r) {
    float sx = step(0.0, p.x);
    float sy = step(0.0, p.y);
    float top = mix(r.x, r.y, sx);
    float bottom = mix(r.w, r.z, sx);
    return mix(top, bottom, sy);
}

float roundedRectSDF(vec2 p, vec2 halfSize, float r) {
    vec2 q = abs(p) - halfSize + r;
    return length(max(q, 0.0)) + min(max(q.x, q.y), 0.0) - r;
}

void main() {
    vec2 halfSize = u_rectSize * 0.5;
    vec2 p = f_Position - u_rectCenter;

    float r = clamp(cornerRadius(p, u_Radii), 0.0, min(halfSize.x, halfSize.y));
    float d = roundedRectSDF(p, halfSize, r);

    float aa = max(fwidth(d), 0.75);

    float fillAlpha = 1.0 - smoothstep(0.0, aa, d);

    float width = max(u_OutlineWidth, 0.0);
    float innerFillAlpha = 1.0 - smoothstep(0.0, aa, d + width);
    float outlineAlpha = max(fillAlpha - innerFillAlpha, 0.0);

    vec4 fillColor = f_Color * fillAlpha;
    vec4 outlineColor = u_OutlineColor * outlineAlpha;

    fragColor = outlineColor + fillColor * (1.0 - outlineColor.a);
}