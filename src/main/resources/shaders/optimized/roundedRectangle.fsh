#version 120
uniform vec2 u_rectCenter;
uniform vec2 u_rectSize;
uniform vec4 u_Radii;
uniform float u_borderThickness;
uniform float u_edgeSoftness;
uniform vec4 u_colorRect;
uniform vec4 u_colorRect2;
uniform vec2 u_gradientDirectionVector;
uniform vec4 u_colorBorder;
uniform vec4 u_colorShadow;
uniform float u_shadowSoftness;
varying vec2 f_Position;
void main ()
{
  vec2 tmpvar_1;
  tmpvar_1 = (((f_Position - u_rectCenter) / u_rectSize) * u_gradientDirectionVector);
  float tmpvar_2;
  if ((tmpvar_1.x == 0.0)) {
    tmpvar_2 = (tmpvar_1.y + 0.5);
  } else {
    tmpvar_2 = (tmpvar_1.x + 0.5);
  };
  vec4 tmpvar_3;
  tmpvar_3 = mix (u_colorRect, u_colorRect2, tmpvar_2);
  vec2 tmpvar_4;
  tmpvar_4 = (u_rectSize / 2.0);
  float tmpvar_5;
  vec2 CenterPosition_6;
  CenterPosition_6 = (f_Position - u_rectCenter);
  vec4 Radius_7;
  Radius_7 = u_Radii;
  vec2 tmpvar_8;
  if ((CenterPosition_6.x > 0.0)) {
    tmpvar_8 = u_Radii.xy;
  } else {
    tmpvar_8 = u_Radii.zw;
  };
  Radius_7.xy = tmpvar_8;
  float tmpvar_9;
  if ((CenterPosition_6.y > 0.0)) {
    tmpvar_9 = Radius_7.x;
  } else {
    tmpvar_9 = Radius_7.y;
  };
  Radius_7.x = tmpvar_9;
  vec2 tmpvar_10;
  tmpvar_10 = ((abs(CenterPosition_6) - tmpvar_4) + tmpvar_9);
  vec2 tmpvar_11;
  tmpvar_11 = max (tmpvar_10, 0.0);
  tmpvar_5 = ((min (
    max (tmpvar_10.x, tmpvar_10.y)
  , 0.0) + sqrt(
    dot (tmpvar_11, tmpvar_11)
  )) - tmpvar_9);
  float tmpvar_12;
  tmpvar_12 = clamp ((tmpvar_5 / u_edgeSoftness), 0.0, 1.0);
  float tmpvar_13;
  tmpvar_13 = (1.0 - (tmpvar_12 * (tmpvar_12 * 
    (3.0 - (2.0 * tmpvar_12))
  )));
  float edge0_14;
  edge0_14 = (u_borderThickness - 2.0);
  float tmpvar_15;
  tmpvar_15 = clamp (((
    abs(tmpvar_5)
   - edge0_14) / (u_borderThickness - edge0_14)), 0.0, 1.0);
  float tmpvar_16;
  tmpvar_16 = (1.0 - (tmpvar_15 * (tmpvar_15 * 
    (3.0 - (2.0 * tmpvar_15))
  )));
  vec2 CenterPosition_17;
  CenterPosition_17 = (f_Position - u_rectCenter);
  vec4 Radius_18;
  Radius_18 = u_Radii;
  vec2 tmpvar_19;
  if ((CenterPosition_17.x > 0.0)) {
    tmpvar_19 = u_Radii.xy;
  } else {
    tmpvar_19 = u_Radii.zw;
  };
  Radius_18.xy = tmpvar_19;
  float tmpvar_20;
  if ((CenterPosition_17.y > 0.0)) {
    tmpvar_20 = Radius_18.x;
  } else {
    tmpvar_20 = Radius_18.y;
  };
  Radius_18.x = tmpvar_20;
  vec2 tmpvar_21;
  tmpvar_21 = ((abs(CenterPosition_17) - tmpvar_4) + tmpvar_20);
  vec2 tmpvar_22;
  tmpvar_22 = max (tmpvar_21, 0.0);
  float edge0_23;
  edge0_23 = -(u_shadowSoftness);
  float tmpvar_24;
  tmpvar_24 = clamp (((
    ((min (max (tmpvar_21.x, tmpvar_21.y), 0.0) + sqrt(dot (tmpvar_22, tmpvar_22))) - tmpvar_20)
   - edge0_23) / (u_shadowSoftness - edge0_23)), 0.0, 1.0);
  float tmpvar_25;
  tmpvar_25 = (1.0 - (tmpvar_24 * (tmpvar_24 * 
    (3.0 - (2.0 * tmpvar_24))
  )));
  vec4 tmpvar_26;
  tmpvar_26.xyz = u_colorShadow.xyz;
  tmpvar_26.w = tmpvar_25;
  gl_FragColor = mix ((tmpvar_26 * tmpvar_25), mix (tmpvar_3, u_colorBorder, tmpvar_16), tmpvar_13);
}

