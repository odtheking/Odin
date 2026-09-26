in vec4 vertexColor;
in vec2 localPos;
flat in vec2 halfSize;
flat in vec4 radii;

flat in float edgeWidth;

float roundedBoxSDF(vec2 p, vec2 extent, float radius) {
    vec2 q = abs(p) - extent + radius;
    return length(max(q, 0.0)) + min(max(q.x, q.y), 0.0) - radius;
}

float shapeDistance() {
    float radius = localPos.x < 0.0
        ? (localPos.y < 0.0 ? radii.x : radii.w)
        : (localPos.y < 0.0 ? radii.y : radii.z);
    radius = min(radius, min(halfSize.x, halfSize.y));

    return roundedBoxSDF(localPos, halfSize, radius);
}

float shapeCoverage() {
    float dist = shapeDistance();
    float aa = max(fwidth(dist), 1e-4);

    float outer = clamp(0.5 - dist / aa, 0.0, 1.0);
    if (edgeWidth == 0.0) return outer;

    float inner = clamp(0.5 - (dist + abs(edgeWidth)) / aa, 0.0, 1.0);
    if (edgeWidth > 0.0) return outer - inner;

    return inner / max(1.0 - outer + inner, 1e-4);
}

float shadowCoverage() {
    float dist = shapeDistance();
    float falloff = max(edgeWidth, max(fwidth(dist), 1e-4));

    float t = clamp(1.0 - dist / falloff, 0.0, 1.0);

    return t * t * t;
}

vec2 shapeUV() {
    return clamp(localPos / halfSize * 0.5 + 0.5, 0.0, 1.0);
}