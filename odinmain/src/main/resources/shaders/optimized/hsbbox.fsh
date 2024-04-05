#version 120
uniform vec2 u_rectCenter;
uniform vec2 u_rectSize;
uniform vec4 u_colorRect;
varying vec2 f_Position;
void main ()
{
  vec2 tmpvar_1;
  tmpvar_1 = ((f_Position - u_rectCenter) / u_rectSize);
  gl_FragColor = mix (mix (vec4(1.0, 1.0, 1.0, 1.0), u_colorRect, (tmpvar_1.x + 0.5)), vec4(0.0, 0.0, 0.0, 1.0), (tmpvar_1.y + 0.5));
}

