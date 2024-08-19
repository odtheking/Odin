#version 120
uniform vec2 u_circleCenter;
uniform float u_circleRadius;
uniform vec4 u_colorCircle;
uniform vec4 u_colorBorder;
uniform float u_borderThickness;
varying vec2 f_Position;
void main ()
{
  vec2 x_1;
  x_1 = (u_circleCenter - f_Position);
  vec4 tmpvar_2;
  tmpvar_2.xyz = u_colorBorder.xyz;
  tmpvar_2.w = min (u_colorBorder.w, (1.0 - clamp (
    (sqrt(dot (x_1, x_1)) - u_circleRadius)
  , 0.0, 1.0)));
  vec2 tmpvar_3;
  tmpvar_3 = (f_Position - u_circleCenter);
  gl_FragColor = (mix (tmpvar_2, u_colorCircle, float(
    ((u_circleRadius - u_borderThickness) >= sqrt(dot (tmpvar_3, tmpvar_3)))
  )) * tmpvar_2.w);
}

