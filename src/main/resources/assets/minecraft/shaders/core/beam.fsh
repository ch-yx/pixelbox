#version 150

uniform sampler2D Sampler0;

uniform vec4 ColorModulator;

in vec4 vertexColor;
in vec2 texCoord0;

out vec4 fragColor;

void main() {
    vec4 color = vertexColor;
    float al = min(0.2 / (texCoord0.y * texCoord0.y), 1.0);
    color.a*=al;
    fragColor = color * ColorModulator;
}
