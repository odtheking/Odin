#version 130

uniform sampler2D texture;
uniform vec2 texelSize;

uniform vec4 color;
uniform float radius;

void main(void) {
    vec4 centerCol = texture2D(texture, gl_TexCoord[0].xy);

    if (centerCol.a != 0) {
        gl_FragColor = vec4(0);
    } else {
        ivec2 size = textureSize(texture, 0);

        float uv_x = gl_TexCoord[0].x * size.x;
        float uv_y = gl_TexCoord[0].y * size.y;
        vec4 foundColor = color;

        for (int n = -4; n < 5; ++n) {
            uv_y = (gl_TexCoord[0].y * size.y) + (radius * float(n - 0.5));
            for (int m = -4; m < 5; ++m) {
                vec4 fetchedColor = texelFetch(texture, ivec2(uv_x - (m * radius), uv_y), 0);
                if (fetchedColor.a > 0) {
                    gl_FragColor = fetchedColor;
                }
            }
        }
    }
}