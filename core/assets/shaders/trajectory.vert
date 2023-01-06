// All components are in the range [0…1], including hue.
vec3 hsv2rgb(vec3 c)
{
    vec4 K = vec4(1.0, 2.0 / 3.0, 1.0 / 3.0, 3.0);
    vec3 p = abs(fract(c.xxx + K.xyz) * 6.0 - K.www);
    return c.z * mix(K.xxx, clamp(p - K.xxx, 0.0, 1.0), c.y);
}

attribute vec2 a_position;
attribute float a_speedPercent;

uniform mat4 u_projTrans;
uniform vec2 u_colorhv;

varying vec4 v_color;

void main()
{
    vec3 _hsvColor = vec3(u_colorhv.x, a_speedPercent * 0.9 + 0.1, u_colorhv.y);
    v_color = vec4(hsv2rgb(_hsvColor), 0);
    vec4 pos = vec4(a_position.xy, 0, 1);
    gl_Position =  u_projTrans * pos;
}