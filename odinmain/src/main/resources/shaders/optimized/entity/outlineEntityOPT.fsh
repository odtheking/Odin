#version 130
uniform sampler2D texture;
uniform float radius;
void main ()
{
  vec4 tmpvar_1;
  tmpvar_1 = texture2D(texture, gl_TexCoord[0].xy);
  if ((tmpvar_1.w != 0.0)) {
    gl_FragColor = vec4(0.0, 0.0, 0.0, 0.0);
  } else {
    float uv_y_3;
    float uv_x_4;
    ivec2 size_5;
    ivec2 tmpvar_6;
    tmpvar_6 = textureSize (texture, 0);
    size_5 = tmpvar_6;
    uv_x_4 = (gl_TexCoord[0].x * float(tmpvar_6.x));
    uv_y_3 = (gl_TexCoord[0].y * float(tmpvar_6.y));
    for (int n_2 = -4; n_2 < 5; n_2++) {
      uv_y_3 = ((gl_TexCoord[0].y * float(size_5.y)) + (radius * (
float(n_2)
 - 0.5)));
      for (int m_7 = -4; m_7 < 5; m_7++) {
        ivec2 tmpvar_8;
        tmpvar_8.x = int((uv_x_4 - (
          float(m_7)
         * radius)));
        tmpvar_8.y = int(uv_y_3);
        vec4 tmpvar_9;
        tmpvar_9 = texelFetch(texture, tmpvar_8, 0);
        if ((tmpvar_9.w > 0.0)) {
          gl_FragColor = tmpvar_9;
        };
      };
    };
  };
}

