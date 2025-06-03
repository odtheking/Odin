#version 120

uniform sampler2D texture;
uniform vec2 texelSize;

uniform int radius;
uniform float clarity;
uniform float innerOpacity;

void main() {
    vec2 coord = gl_TexCoord[0].xy;
    vec4 pixelColor = texture2D(texture, coord);

    if (pixelColor.a != 0) {
        gl_FragColor = vec4(pixelColor.rgb, innerOpacity);
    } else {
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                vec4 currentColor = texture2D(texture, coord + vec2(texelSize.x * x, texelSize.y * y));
                if (currentColor.a != 0) {
                    pixelColor.rgb = mix(pixelColor.rgb, currentColor.rgb, currentColor.a);
                    pixelColor.a += max(0.0, (1.0 + float(radius) - distance(vec2(x, y), vec2(0.0))) * (clarity / float(radius)));
                }
            }
        }
        gl_FragColor = pixelColor;
    }
}
