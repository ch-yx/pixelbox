#version 150

in vec4 vertexColor;

uniform vec4 ColorModulator;

out vec4 fragColor;

void main() {
    vec4 color = vec4(1.0);
    if (color.a == 0.0) {
        discard;
    }
    fragColor = color * ColorModulator;
}
