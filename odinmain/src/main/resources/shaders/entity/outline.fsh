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

        float sum = 0.0;
        for (int n = 0; n < 9; ++n) {
            uv_y = (gl_TexCoord[0].y * size.y) + (radius * float(n - 4.5));
            float h_sum = 0.0;
            h_sum += texelFetch(texture, ivec2(uv_x - (4.0 * radius), uv_y), 0).a;
            h_sum += texelFetch(texture, ivec2(uv_x - (3.0 * radius), uv_y), 0).a;
            h_sum += texelFetch(texture, ivec2(uv_x - (2.0 * radius), uv_y), 0).a;
            h_sum += texelFetch(texture, ivec2(uv_x - radius, uv_y), 0).a;
            h_sum += texelFetch(texture, ivec2(uv_x, uv_y), 0).a;
            h_sum += texelFetch(texture, ivec2(uv_x + radius, uv_y), 0).a;
            h_sum += texelFetch(texture, ivec2(uv_x + (2.0 * radius), uv_y), 0).a;
            h_sum += texelFetch(texture, ivec2(uv_x + (3.0 * radius), uv_y), 0).a;
            h_sum += texelFetch(texture, ivec2(uv_x + (4.0 * radius), uv_y), 0).a;
            sum += h_sum / 9.0;
        }
        if (sum >= 0.0001) {
            gl_FragColor = color;
        }
    }
}