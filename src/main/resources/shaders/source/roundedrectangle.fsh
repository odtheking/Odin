// shader gotten from https://www.shadertoy.com/view/fsdyzB
#version 120

uniform vec2 u_rectCenter; // center of rectangle, (x, y)
uniform vec2 u_rectSize; // size of rectangle, (width, height)
uniform vec4 u_Radii; // The radius of each corner, (r1, r2, r3, r4)
uniform float u_borderThickness; // Thickness of border color, in pixels
uniform float u_edgeSoftness; // Softness of edges, free antialiasing, but breaks rounded corners over certain values.
uniform vec4 u_colorRect; // First color of gradient
uniform vec4 u_colorRect2; // Second color for gradient
uniform vec2 u_gradientDirectionVector; // Direction of the gradient based on which variable to use, for example a gradient from left to right will use (1.0, 0.0) to use the positive x value of the length from the middle of the recangle
uniform vec4 u_colorBorder; // Color of the rectangle's border.
uniform vec4 u_colorShadow; // Color of the shadow.
uniform float u_shadowSoftness; // Softness of shadow. At 0 this will make shadow invisible.

varying vec2 f_Position;

/**
  * Signed Distance Function for a rounded rectangle
  * @param CenterPosition Vec2 for the center of the rounded rectangle
  * @param Size Vec2 containing the width and height of a rounded rectangle
  * @param Radius Vec4 of all the radii in the rounded rectangle
*/
float roundedBoxSDF(vec2 position, vec2 halfSize, vec4 radii) {
    vec2 q = abs(position) - halfSize + radii.x;
    return min(max(q.x, q.y), 0.0) + length(max(q, 0.0)) - radii.x;
}

void main() {
    vec2 halfSize = u_rectSize * 0.5;
    vec2 position = f_Position - u_rectCenter;

    // Calculate gradient
    vec2 uv = position / u_rectSize;
    float gradientFactor = dot(uv, u_gradientDirectionVector) + 0.5;
    vec4 gradientColor = mix(u_colorRect, u_colorRect2, gradientFactor);

    // Distance calculations
    float distance = roundedBoxSDF(position, halfSize, u_Radii);
    float smoothedAlpha = 1.0 - smoothstep(0.0, u_edgeSoftness, distance);

    // Border calculation
    float borderSoftness = 2.0;
    float borderAlpha = 1.0 - smoothstep(u_borderThickness - borderSoftness, u_borderThickness, abs(distance));

    // Shadow calculation - only compute if shadow is visible
    vec4 backgroundColor = vec4(0.0);
    vec4 shadowColor = backgroundColor;

    if (u_shadowSoftness > 0.0) {
        float shadowDistance = roundedBoxSDF(position + vec2(0.0, 0.0), halfSize, u_Radii);
        float shadowAlpha = 1.0 - smoothstep(-u_shadowSoftness, u_shadowSoftness, shadowDistance);
        shadowColor = mix(backgroundColor, vec4(u_colorShadow.rgb, shadowAlpha), shadowAlpha);
    }

    // Combine colors efficiently
    vec4 rectangleWithBorder = mix(gradientColor, u_colorBorder, borderAlpha);
    gl_FragColor = mix(shadowColor, rectangleWithBorder, min(gradientColor.a, smoothedAlpha));
}