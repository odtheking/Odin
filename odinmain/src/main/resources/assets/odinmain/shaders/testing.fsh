// shader gotten from https://www.shadertoy.com/view/fsdyzB
#version 130

uniform vec2 u_rectCenter;
uniform vec2 u_rectSize;
uniform vec4 u_Radii;
uniform float u_borderThickness;
uniform float u_edgeSoftness;
uniform vec4 u_colorRect;
uniform vec4 u_colorRect2; // Second color for gradient
uniform vec2 u_gradientDirectionVector; // Direction of the gradient based on which variable to use, for example a gradient from left to right will use (1.0, 0.0) to use the positive x value of the length from the middle of the recangle
uniform vec4 u_colorBorder;
uniform vec4 u_colorShadow;
uniform float u_shadowSoftness;

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
    vec2 uv = (f_Position - u_rectCenter) / u_rectSize;
    vec2 strength = vec2(uv.x * u_gradientDirectionVector.x, uv.y * u_gradientDirectionVector.y);
    // Interpolate colors based on the distance
    vec4 gradientColor = mix(u_colorRect, u_colorRect2, (strength.x == 0.0) ? strength.y + 0.5 : strength.x + 0.5);

    // Border
    float u_borderSoftness  = 2.0; // How soft the (internal) border should be (in pixels)

    // Shadow
    vec2  u_shadowOffset   = vec2(0.0, 0.0); // The pixel-space shadow offset from rectangle center

    // Colors
    vec4  u_colorBg     = vec4(0.0); // The color of background
    // =========================================================================

    vec2 halfSize = (u_rectSize / 2.0); // Rectangle extents (half of the size)


    // -------------------------------------------------------------------------

    // Calculate distance to edge.
    float distance = roundedBoxSDF(f_Position.xy - u_rectCenter, halfSize, u_Radii);

    // Smooth the result (free antialiasing).
    float smoothedAlpha = 1.0 - smoothstep(0.0, u_edgeSoftness, distance);

    // -------------------------------------------------------------------------
    // Border.

    float borderAlpha   = 1.0 - smoothstep(u_borderThickness - u_borderSoftness, u_borderThickness, abs(distance));

    // -------------------------------------------------------------------------
    // Apply a drop shadow effect.

    float shadowDistance  = roundedBoxSDF(f_Position.xy - u_rectCenter + u_shadowOffset, halfSize, u_Radii);
    float shadowAlpha 	  = 1.0 - smoothstep(-u_shadowSoftness, u_shadowSoftness, shadowDistance);


    // Blend background with shadow
    vec4 res_shadow_color =
        mix(
            u_colorBg,
            vec4(u_colorShadow.rgb, shadowAlpha),
            shadowAlpha
        );

    // Blend (background+shadow) with rect
    //   Note:
    //     - Used 'min(gradientColor.a, smoothedAlpha)' instead of 'smoothedAlpha'
    //       to enable rectangle color transparency
    vec4 res_shadow_with_rect_color =
        mix(
            res_shadow_color,
            gradientColor,
            min(gradientColor.a, smoothedAlpha)
        );

    // Blend (background+shadow+rect) with border
    //   Note:
    //     - Used 'min(borderAlpha, smoothedAlpha)' instead of 'borderAlpha'
    //       to make border 'internal'
    //     - Used 'min(u_colorBorder.a, alpha)' instead of 'alpha' to enable
    //       border color transparency
    vec4 combinedColor = mix(gradientColor, u_colorBorder, borderAlpha);
    vec4 finalColor = mix(res_shadow_color, combinedColor, smoothedAlpha);

    gl_FragColor = finalColor;
}