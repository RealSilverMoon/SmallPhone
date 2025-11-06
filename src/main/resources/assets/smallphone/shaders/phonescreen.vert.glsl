#version 120

varying vec2 v_TexCoord;

void main() {
    gl_Position = ftransform();
    v_TexCoord = gl_MultiTexCoord0.st;
}
