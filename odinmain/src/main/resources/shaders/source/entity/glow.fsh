#version 130

uniform sampler2D texture;
uniform vec2 texelSize;

uniform vec3 color;
uniform float radius;
uniform float glow_intensity = 2;

void main() {
    vec4 centerCol = texture2D(texture, gl_TexCoord[0].xy);

    if (centerCol.a != 0) {
        // If the center pixel is not transparent, it's part of the entity; Apply no color to it
        gl_FragColor = vec4(0);
    } else {
        ivec2 size = textureSize(texture, 0);

        float uv_x = gl_TexCoord[0].x * size.x;
        float uv_y = gl_TexCoord[0].y * size.y;
        vec3 foundColor = color;
        float sum = 0.0;
        for (int n = 0; n < 9; ++n) {
            uv_y = (gl_TexCoord[0].y * size.y) + (radius * float(n - 4.5));
            float h_sum = 0.0;
            for (int m = -4; m < 5; ++m) {
                vec4 fetchedColor = texelFetch(texture, ivec2(uv_x - (m * radius), uv_y), 0);
                if (fetchedColor.a > 0) {
                    foundColor = fetchedColor.rgb;
                    h_sum += fetchedColor.a;
                }
            }
            sum += h_sum / 9.0;
        }

        gl_FragColor = vec4(foundColor, (sum / 9.0) * glow_intensity);
    }
}