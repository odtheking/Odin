#version 120
uniform vec2 u_rectCenter;
uniform vec2 u_rectSize;
uniform vec4 u_Radii;
uniform vec4 u_colorShadow;
uniform float u_shadowSoftness;
varying vec2 f_Position;
void main ()
{
  vec2 tmpvar_1;
  tmpvar_1 = (u_rectSize / 2.0);
  vec2 CenterPosition_2;
  CenterPosition_2 = (f_Position - u_rectCenter);
  vec4 Radius_3;
  Radius_3 = u_Radii;
  vec2 tmpvar_4;
  if ((CenterPosition_2.x > 0.0)) {
    tmpvar_4 = u_Radii.xy;
  } else {
    tmpvar_4 = u_Radii.zw;
  };
  Radius_3.xy = tmpvar_4;
  float tmpvar_5;
  if ((CenterPosition_2.y > 0.0)) {
    tmpvar_5 = Radius_3.x;
  } else {
    tmpvar_5 = Radius_3.y;
  };
  Radius_3.x = tmpvar_5;
  vec2 tmpvar_6;
  tmpvar_6 = ((abs(CenterPosition_2) - tmpvar_1) + tmpvar_5);
  vec2 tmpvar_7;
  tmpvar_7 = max (tmpvar_6, 0.0);
  float edge0_8;
  edge0_8 = -(u_shadowSoftness);
  float tmpvar_9;
  tmpvar_9 = clamp (((
    ((min (max (tmpvar_6.x, tmpvar_6.y), 0.0) + sqrt(dot (tmpvar_7, tmpvar_7))) - tmpvar_5)
   - edge0_8) / (u_shadowSoftness - edge0_8)), 0.0, 1.0);
  float tmpvar_10;
  tmpvar_10 = (1.0 - (tmpvar_9 * (tmpvar_9 * 
    (3.0 - (2.0 * tmpvar_9))
  )));
  vec4 tmpvar_11;
  tmpvar_11.xyz = u_colorShadow.xyz;
  tmpvar_11.w = tmpvar_10;
  gl_FragColor = (tmpvar_11 * min (u_colorShadow.w, tmpvar_10));
}

