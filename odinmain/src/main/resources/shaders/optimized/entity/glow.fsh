#version 130
uniform sampler2D texture;
uniform vec3 color;
uniform float radius;
uniform float glow_intensity = 2.0;
void main ()
{
  vec4 tmpvar_1;
  tmpvar_1 = texture (texture, gl_TexCoord[0].xy);
  if ((tmpvar_1.w != 0.0)) {
    gl_FragColor = vec4(0.0, 0.0, 0.0, 0.0);
  } else {
    float sum_3;
    vec3 foundColor_4;
    float uv_y_5;
    float uv_x_6;
    ivec2 size_7;
    ivec2 tmpvar_8;
    tmpvar_8 = textureSize (texture, 0);
    size_7 = tmpvar_8;
    uv_x_6 = (gl_TexCoord[0].x * float(tmpvar_8.x));
    uv_y_5 = (gl_TexCoord[0].y * float(tmpvar_8.y));
    foundColor_4 = color;
    sum_3 = 0.0;
    for (int n_2 = 0; n_2 < 9; n_2++) {
      float h_sum_10;
      uv_y_5 = ((gl_TexCoord[0].y * float(size_7.y)) + (radius * (
        float(n_2)
       - 4.5)));
      h_sum_10 = 0.0;
      for (int m_9 = -4; m_9 < 5; m_9++) {
        ivec2 tmpvar_11;
        tmpvar_11.x = int((uv_x_6 - (
          float(m_9)
         * radius)));
        tmpvar_11.y = int(uv_y_5);
        vec4 tmpvar_12;
        tmpvar_12 = texelFetch (texture, tmpvar_11, 0);
        if ((tmpvar_12.w > 0.0)) {
          foundColor_4 = tmpvar_12.xyz;
          h_sum_10 = (h_sum_10 + tmpvar_12.w);
        };
      };
      sum_3 = (sum_3 + (h_sum_10 / 9.0));
    };
    vec4 tmpvar_13;
    tmpvar_13.xyz = foundColor_4;
    tmpvar_13.w = ((sum_3 / 9.0) * glow_intensity);
    gl_FragColor = tmpvar_13;
  };
}

