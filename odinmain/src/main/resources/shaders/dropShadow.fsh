#version 120

uniform vec2 u_rectCenter; // center of rectangle, (x, y)
uniform vec2 u_rectSize; // size of rectangle, (width, height)
uniform vec4 u_Radii; // The radius of each corner, (r1, r2, r3, r4)
uniform vec4 u_colorShadow; // Color of the shadow.
uniform float u_shadowSoftness; // Softness of shadow. At 0 this will make shadow invisible.

varying vec2 f_Position;

/**
  * Signed Distance Function for a rounded rectangle
  * @param CenterPosition Vec2 for the center of the rounded rectangle
  * @param Size Vec2 containing the width and height of a rounded rectangle
  * @param Radius Vec4 of all the radii in the rounded rectangle
*/
float roundedBoxSDF(vec2 CenterPosition, vec2 Size, vec4 Radius) {
    Radius.xy = (CenterPosition.x > 0.0) ? Radius.xy : Radius.zw;
    Radius.x  = (CenterPosition.y > 0.0) ? Radius.x  : Radius.y;

    vec2 q = abs(CenterPosition)-Size+Radius.x;
    return min(max(q.x,q.y),0.0) + length(max(q,0.0)) - Radius.x;
}

void main() {
    vec2  u_shadowOffset = vec2(0.0, 0.0); // The pixel-space shadow offset from rectangle center
    vec4  u_colorBg      = vec4(0.0); // The color of background
    vec2  halfSize       = (u_rectSize / 2.0); // Rectangle extents (half of the size)

    // Apply a drop shadow effect.

    float shadowDistance  = roundedBoxSDF(f_Position.xy - u_rectCenter + u_shadowOffset, halfSize, u_Radii);
    float shadowAlpha 	  = 1.0 - smoothstep(-u_shadowSoftness, u_shadowSoftness, shadowDistance);

    // Blend background with shadow
    vec4 res_shadow_color =
        mix(
            u_colorBg,
            vec4(u_colorShadow.rgb, shadowAlpha),
            min(u_colorShadow.a, shadowAlpha)
        );

    gl_FragColor = res_shadow_color;
}